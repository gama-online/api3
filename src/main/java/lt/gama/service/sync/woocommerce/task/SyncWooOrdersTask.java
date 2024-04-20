package lt.gama.service.sync.woocommerce.task;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.*;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.InvoiceSql_;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.type.*;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.enums.TaxpayerType;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.CounterService;
import lt.gama.service.TradeService;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaSimilarDocumentAlreadyExistsException;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.woocommerce.model.*;
import lt.gama.tasks.BigDataDeferredTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lt.gama.service.sync.i.ISyncWoocommerceService.API_3_SUFFIX;
import static lt.gama.service.sync.i.ISyncWoocommerceService.STD_TAX_CLASS;

public class SyncWooOrdersTask extends BigDataDeferredTask<SyncWooData> {

    @Serial
    private static final long serialVersionUID = -1L;

    int TAX_PAGE_SIZE = 100;
    int ORDERS_PAGE_SIZE = 50;


    @Autowired
    transient protected SyncHttpService syncHttpService;

    @Autowired
    transient protected TradeService tradeService;

    @Autowired
    transient protected CounterService counterService;


    transient private String api;
    transient private String authorizationHeader;
    transient private SyncWooData taskData;

    public enum Step {
        TAXES,
        ORDERS
    }

    private final LocalDateTime date;
    private final int page;
    private final Step step;


    public SyncWooOrdersTask(String token, long companyId, LocalDateTime date) {
        this(token, companyId, null, date, 1, Step.TAXES);
    }

    public SyncWooOrdersTask(String token, long companyId, String fileName, LocalDateTime date, int page, Step step) {
        super(token, companyId, fileName);
        this.date = date != null ? date : DateUtils.now().minusYears(100);
        this.page = page;
        this.step = step;
    }

    @Override
    public void execute() {
        var companySettings = auth.getSettings();
        Validators.checkNotNull(companySettings.getSync(), "No sync settings");
        if (BooleanUtils.isNotTrue(companySettings.getSync().getSyncActive())) {
            log.info(className + ": Sync is not active");
            finish(TaskResponse.success());
            return;
        }
        api = companySettings.getSync().getUrl().endsWith("/")
                ? companySettings.getSync().getUrl()
                : (companySettings.getSync().getUrl() + '/');
        String key = companySettings.getSync().getId();
        String secret = companySettings.getSync().getKey();
        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((key + ":" + secret).getBytes());

        taskData = loadData(new TypeReference<>() {});
        if (taskData == null) taskData = new SyncWooData();
        if (taskData.errors == null) taskData.errors = new ArrayList<>();
        if (taskData.warnings == null) taskData.warnings = new ArrayList<>();

        if (step == Step.TAXES && page == 1) taskData.wooTaxes = new HashMap<>();

        try {
            switch (step) {
                case TAXES -> readTaxes();
                case ORDERS -> readOrders();
            }
        } catch (GamaException | NullPointerException | IllegalArgumentException e) {
            taskData.errors.add(e.getMessage());
            finish(TaskResponse.errors(taskData.errors).withWarnings(taskData.warnings));
        }
    }

    private void readTaxes() {
        List<WooTax> wooTaxes = syncHttpService.getRequestData(
                SyncHttpService.HttpMethod.GET,
                api + API_3_SUFFIX + "taxes",
                Map.of("per_page", String.valueOf(TAX_PAGE_SIZE),
                        "page", String.valueOf(this.page)),
                new TypeReference<>() {},
                authorizationHeader
        );
        if (CollectionsHelper.hasValue(wooTaxes)) {
            wooTaxes.forEach(wooTax -> taskData.wooTaxes
                    .computeIfAbsent(wooTax.country(), k -> new HashMap<>())
                    .put(wooTax.tax_class(), wooTax.rate()));
            saveData(taskData);
            taskQueueService.queueTask(new SyncWooOrdersTask(getToken(), getCompanyId(), getFileName(), date, page + 1, Step.TAXES));
        } else {
            log.info(className + ": Taxes=" + taskData.wooTaxes.values().stream().mapToInt(x -> x.values().size()).sum());

            // Prepare Vat rate codes
            var companySettings = auth.getSettings();

            CountryVatRateSql countryVatRate = Validators.checkNotNull(dbServiceSQL.getById(CountryVatRateSql.class, companySettings.getCountry()),
                    "No country Vat Rates data for companyId=" + getCompanyId() +
                            ", country=" + companySettings.getCountry());

            VATRatesDate vatRatesDate = Validators.checkNotNull(countryVatRate.getRatesMap(DateUtils.now().toLocalDate()),
                    "No Vat Rates data for companyId=" + getCompanyId() +
                            " for " + DateUtils.date(companySettings.getTimeZone()));

            taskData.rates = vatRatesDate.getRates();
            saveData(taskData);
            taskQueueService.queueTask(new SyncWooOrdersTask(getToken(), getCompanyId(), getFileName(), date, 1, Step.ORDERS));
        }
    }

    private void readOrders() {
        var status = auth.getSettings().getSync().getStatusIds();
        Validators.checkArgument(StringHelper.hasValue(status), TranslationService.getInstance().translate(TranslationService.INVENTORY.NoSyncOrderStatus, auth.getLanguage()));

        var transportation = auth.getSettings().getSync().getTransportation();
        Validators.checkValid(transportation, TranslationService.getInstance().translate(TranslationService.INVENTORY.NoSyncTransportation, auth.getLanguage()));

        var warehouse = Validators.isValid(auth.getSettings().getSync().getWarehouse())
                ? auth.getSettings().getSync().getWarehouse()
                : auth.getSettings().getWarehouse();
        Validators.checkValid(warehouse, TranslationService.getInstance().translate(TranslationService.INVENTORY.NoSyncWarehouse, auth.getLanguage()));

        var counterparty = Validators.isValid(auth.getSettings().getSync().getCounterparty())
                ? auth.getSettings().getSync().getCounterparty()
                : null;

        var label = auth.getSettings().getSync().getLabel();
        var timeZone = auth.getSettings().getTimeZone();

        Map<String, Long> skuMap = new HashMap<>();

        List<WooOrder> wooOrders = syncHttpService.getRequestData(
                SyncHttpService.HttpMethod.GET,
                api + API_3_SUFFIX + "orders",
                Map.of("modified_after", date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "status", status,
                        "per_page", String.valueOf(ORDERS_PAGE_SIZE),
                        "page", String.valueOf(this.page)),
                new TypeReference<>() {},
                authorizationHeader
        );

        if (CollectionsHelper.hasValue(wooOrders)) {

            log.info(className + ": Syncing " + wooOrders.size() + " orders");
            taskData.ordersTotal += wooOrders.size();

            var country = auth.getSettings().getCountry();
            var taxesByClass = taskData.wooTaxes.get(country);

            dbServiceSQL.executeInTransaction(entityManager -> wooOrders.forEach(wooOrder -> {
                // check if invoice with such number already exists
                String number = getMetaData(wooOrder.meta_data(), "_wcpdf_invoice_number");
                if (StringHelper.hasValue(number) && checkInvoiceExists(entityManager, number)) {
                    String warning = "Invoice with number " + number + " already exists";
                    log.warn(className + ": " + warning);
                    taskData.warnings.add(warning);
                    return;
                }

                long counterpartyId = counterparty != null && StringHelper.isEmpty(wooOrder.billing().company())
                        ? counterparty.getId()
                        : loadOrCreateCounterparty(wooOrder.billing());

                skuMap.putAll(CollectionsHelper.streamOf(wooOrder.line_items())
                        .map(WooOrderItem::sku)
                        .distinct()
                        .filter(sku -> !skuMap.containsKey(sku))
                        .collect(Collectors.toMap(sku -> sku, this::loadProduct)));

                InvoiceSql invoice = new InvoiceSql();
                invoice.setCompanyId(getCompanyId());
                invoice.setWarehouse(entityManager.getReference(WarehouseSql.class, warehouse.getId()));
                String timeInString = getMetaData(wooOrder.meta_data(), "_wcpdf_invoice_date");
                invoice.setDate(StringHelper.hasValue(timeInString)
                        ? DateUtils.adjust(LocalDateTime.ofEpochSecond(Long.parseLong(timeInString), 0, ZoneOffset.UTC), timeZone).toLocalDate()
                        : wooOrder.date_completed() != null
                        ? wooOrder.date_completed().toLocalDate()
                        : wooOrder.date_created().toLocalDate());
                invoice.setDueDate(invoice.getDate());
                invoice.setCounterparty(entityManager.getReference(CounterpartySql.class, counterpartyId));

                invoice.setPaymentId(String.valueOf(wooOrder.id()));
                invoice.setExportId(String.valueOf(wooOrder.id()));

                invoice.setInvoiceNote(wooOrder.customer_note());
                invoice.setNote("Order_id = " + wooOrder.id() + ",\n" +
                        "order_key = " + wooOrder.order_key() + ",\n" +
                        "payment_method = " + wooOrder.payment_method_title());

                String contactName = StringHelper.trim(wooOrder.billing().first_name() + " " + wooOrder.billing().last_name());
                if (StringHelper.hasValue(wooOrder.billing().company())) {
                    contactName = StringHelper.hasValue(contactName) ? contactName + ", " + wooOrder.billing().company() : wooOrder.billing().company();
                }
                invoice.setContact(new NameContact(StringHelper.trim(contactName),
                        List.of(new Contact(Contact.ContactType.email, Contact.ContactSubtype.work, wooOrder.billing().email()),
                                new Contact(Contact.ContactType.phone, Contact.ContactSubtype.work, wooOrder.billing().phone()))));

                invoice.setLocation(new Location());
                invoice.getLocation().setName(TranslationService.getInstance().translate(TranslationService.INVENTORY.BillingAddress, auth.getLanguage()));
                invoice.getLocation().setAddress1(wooOrder.billing().address_1());
                invoice.getLocation().setAddress2(wooOrder.billing().address_2());
                invoice.getLocation().setCity(wooOrder.billing().city());
                invoice.getLocation().setCountry(wooOrder.billing().country());
                invoice.getLocation().setZip(wooOrder.billing().postcode());

                if (wooOrder.shipping() != null) {
                    invoice.setUnloadAddress(new Location());
                    invoice.getUnloadAddress().setAddress1(wooOrder.shipping().address_1());
                    invoice.getUnloadAddress().setAddress2(wooOrder.shipping().address_2());
                    invoice.getUnloadAddress().setCity(wooOrder.shipping().city());
                    invoice.getUnloadAddress().setCountry(wooOrder.shipping().country());
                    invoice.getUnloadAddress().setZip(wooOrder.shipping().postcode());
                    String shippingName = StringHelper.trim(wooOrder.shipping().first_name() + " " + wooOrder.shipping().last_name());
                    if (StringHelper.hasValue(wooOrder.shipping().company())) {
                        shippingName = StringHelper.hasValue(shippingName) ? shippingName + ", " + wooOrder.shipping().company() : wooOrder.shipping().company();
                    }
                    invoice.getUnloadAddress().setName(shippingName);
                }

                String currency = wooOrder.currency();
                invoice.setExchange(new Exchange(currency));
                invoice.setTotal(GamaMoney.of(currency, wooOrder.total()));
                invoice.setTaxTotal(GamaMoney.of(currency, wooOrder.total_tax()));
                invoice.setSubtotal(GamaMoneyUtils.subtract(invoice.getTotal(), invoice.getTaxTotal()));

                if (CollectionsHelper.hasValue(wooOrder.line_items())) {
                    invoice.setParts(new ArrayList<>(wooOrder.line_items().size()));
                    wooOrder.line_items().forEach(wooOrderItem -> {
                        var invoicePart = new InvoicePartSql();
                        invoicePart.setParent(invoice);
                        invoicePart.setCompanyId(getCompanyId());
                        invoicePart.setPart(entityManager.getReference(PartSql.class, skuMap.get(wooOrderItem.sku())));
                        invoicePart.setDocPart(new DocPart());
                        invoicePart.getDocPart().setType(PartType.PRODUCT);
                        invoicePart.getDocPart().setName(wooOrderItem.name());
                        invoicePart.getDocPart().setSku(wooOrderItem.sku());
                        invoicePart.setQuantity(wooOrderItem.quantity());
                        invoicePart.setTotal(GamaMoney.of(currency, wooOrderItem.total()));
                        invoicePart.setPrice(GamaMoneyUtils.dividedBy(invoicePart.getTotal().toBigMoney(), invoicePart.getQuantity()).withScale(2));
                        invoicePart.setFixTotal(true);
                        invoicePart.setDiscountedTotal(invoicePart.getTotal());

                        var vatRate = getVatRate(taskData.rates, taxesByClass.get(
                                StringHelper.hasValue(wooOrderItem.tax_class()) ? wooOrderItem.tax_class() : STD_TAX_CLASS));
                        if (vatRate != null) {
                            invoicePart.setVat(vatRate);
                            invoicePart.setVatRateCode(vatRate.getCode());
                        }

                        invoice.getParts().add(invoicePart);
                    });
                }
                if (CollectionsHelper.hasValue(wooOrder.shipping_lines())) {
                    if (invoice.getParts() == null) invoice.setParts(new ArrayList<>());
                    wooOrder.shipping_lines().forEach(wooShippingLine -> {
                        var invoicePart = new InvoicePartSql();
                        invoicePart.setParent(invoice);
                        invoicePart.setCompanyId(getCompanyId());
                        invoicePart.setPart(entityManager.getReference(PartSql.class, transportation.getId()));
                        invoicePart.setDocPart(new DocPart());
                        invoicePart.getDocPart().setType(PartType.SERVICE);
                        invoicePart.getDocPart().setName(wooShippingLine.method_title());
                        invoicePart.setQuantity(BigDecimal.ONE);
                        invoicePart.setTotal(GamaMoney.of(currency, wooShippingLine.total()));
                        invoicePart.setPrice(GamaBigMoney.of(currency, wooShippingLine.total()));

                        var vatRate = getVatRate(taskData.rates, taxesByClass.get(STD_TAX_CLASS));
                        if (vatRate != null) {
                            invoicePart.setVat(vatRate);
                            invoicePart.setVatRateCode(vatRate.getCode());
                        }

                        invoice.getParts().add(invoicePart);
                    });
                }

                if (StringHelper.hasValue(label)) invoice.setLabels(Set.of(label));

                if (StringHelper.hasValue(number)) {
                    invoice.setNumber(number);
                    counterService.decodeDocNumber(invoice);
                }

                try {
                    tradeService.prepareSaveInvoiceSQL(false, false, false, invoice);

                    if (StringHelper.hasValue(invoice.getNumber())) {
                        dbServiceSQL.saveEntityInCompany(invoice);
                    } else {
                        invoice.setAutoNumber(true);
                        dbServiceSQL.saveWithCounter(invoice);
                    }
                    taskData.ordersCreated++;

                } catch (GamaSimilarDocumentAlreadyExistsException e) {
                    taskData.warnings.add(e.getMessage());
                }

            }));
        }

        if (wooOrders.size() == ORDERS_PAGE_SIZE) {
            saveData(taskData);
            taskQueueService.queueTask(new SyncWooOrdersTask(getToken(), getCompanyId(), getFileName(), date, page + 1, Step.ORDERS));
            return;
        }

        var message = this.page == 1
                ? TranslationService.getInstance().translate(TranslationService.INVENTORY.NoOrdersToSync, auth.getLanguage())
                : MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.OrdersSynced, auth.getLanguage()),
                taskData.ordersCreated, taskData.ordersCreated);
        log.info(className + ": " + message);
        var response = Map.of(
                "message", message,
                "productsTotal", taskData.productsTotal,
                "productsCreated", taskData.productsCreated,
                "ordersTotal", taskData.ordersTotal,
                "ordersCreated", taskData.ordersCreated);
        if (taskData.errors.isEmpty()) {
            finish(TaskResponse.success(response).withWarnings(taskData.warnings));
        } else {
            finish(TaskResponse.errors(taskData.errors).withWarnings(taskData.warnings));
        }
    }

    private boolean checkInvoiceExists(EntityManager entityManager, String number) {
        return entityManager.createQuery(
                        "SELECT COUNT(*) FROM " + InvoiceSql.class.getName() + " a" +
                                " WHERE " + InvoiceSql_.COMPANY_ID + " = :companyId" +
                                " AND " + InvoiceSql_.NUMBER + " = :number" +
                                " AND " + InvoiceSql_.DATE + " > :date" +
                                " AND (archive IS null OR archive = false)" +
                                " AND (hidden IS null OR hidden = false)",
                        Long.class)
                .setParameter("companyId", getCompanyId())
                .setParameter("number", number)
                .setParameter("date", date.toLocalDate().minusDays(30))
                .getSingleResult() > 0;
    }

    private String getMetaData(List<WooMetaData> metaData, String key) {
        return CollectionsHelper.streamOf(metaData)
                .filter(x -> x.key().equals(key))
                .findFirst()
                .map(WooMetaData::valueAsString)
                .orElse("");
    }

    private long loadOrCreateCounterparty(WooCustomer wooCustomer) {
        @SuppressWarnings("unchecked")
        long counterpartyId = ((Stream<Number>) entityManager.createNativeQuery("""
                        SELECT id
                        FROM counterparties,
                            jsonb_array_elements(contacts) AS name_contact,
                            jsonb_array_elements(name_contact->'contacts') AS contact
                        WHERE company_id = :companyId
                        	AND archive IS NOT true
                        	AND hidden IS NOT true
                        	AND contact->>'type' = 'email' AND contact->>'contact' = :email
                        ORDER BY updated_on DESC, id DESC
                        LIMIT 1
                        """)
                .setParameter("companyId", getCompanyId())
                .setParameter("email", wooCustomer.email())
                .getResultStream())
                .findFirst()
                .map(Number::longValue)
                .orElse(0L);
        if (counterpartyId == 0) {
            CounterpartySql counterparty = new CounterpartySql();
            counterparty.setCompanyId(getCompanyId());
            boolean isCompany = StringHelper.hasValue(wooCustomer.company());
            String personName = StringHelper.trim(wooCustomer.first_name() + " " + wooCustomer.last_name());
            counterparty.setName(isCompany ? wooCustomer.company() : personName);
            counterparty.setTaxpayerType(isCompany ? TaxpayerType.LEGAL : TaxpayerType.PHYSICAL);
            counterparty.setBusinessAddress(new Location());
            counterparty.getBusinessAddress().setAddress1(wooCustomer.address_1());
            counterparty.getBusinessAddress().setAddress2(wooCustomer.address_2());
            counterparty.getBusinessAddress().setCity(wooCustomer.city());
            counterparty.getBusinessAddress().setCountry(wooCustomer.country());
            counterparty.getBusinessAddress().setZip(wooCustomer.postcode());
            counterparty.setAccount(DebtType.CUSTOMER, auth.getSettings().getGl().getCounterpartyCustomer());
            counterparty.setContacts(List.of(new NameContact(personName, List.of(
                    new Contact(Contact.ContactType.email, Contact.ContactSubtype.work, wooCustomer.email()),
                    new Contact(Contact.ContactType.phone, Contact.ContactSubtype.work, wooCustomer.phone())))));
            entityManager.persist(counterparty);
            counterpartyId = counterparty.getId();
        }
        return counterpartyId;
    }

    private long loadProduct(String sku) {
        //noinspection unchecked
        return ((Stream<Number>) entityManager.createNativeQuery("""
                        SELECT id
                        FROM parts
                        WHERE company_id = :companyId
                        	AND archive IS NOT true
                        	AND hidden IS NOT true
                        	AND sku = :sku
                        ORDER BY updated_on DESC, id DESC
                        LIMIT 1
                        """)
                .setParameter("companyId", getCompanyId())
                .setParameter("sku", sku)
                .getResultStream())
                .findFirst()
                .map(Number::longValue)
                .orElseThrow(() -> new GamaException("Product not found SKU=" + sku));
    }

    private VATRate getVatRate(List<VATRate> rates, double rate) {
        return rates.stream()
                .filter(vat -> Math.abs(vat.getRate() - rate) < 0.01)
                .findFirst()
                .orElse(rates.stream()
                        .max(Comparator.comparing(VATRate::getRate))
                        .orElse(null));
    }

    @Override
    public String toString() {
        return "SyncWooOrdersTask{" +
                "date=" + date +
                ", page=" + page +
                ", step=" + step +
                "} " + super.toString();
    }
}
