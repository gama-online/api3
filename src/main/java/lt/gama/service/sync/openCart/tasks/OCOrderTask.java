package lt.gama.service.sync.openCart.tasks;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.*;
import lt.gama.model.mappers.CounterpartySqlMapper;
import lt.gama.model.mappers.InvoiceSqlMapper;
import lt.gama.model.mappers.PartSqlMapper;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.ImportSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.doc.DocPartSync;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.InventoryCheckService;
import lt.gama.service.TradeService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.openCart.SyncOpenCartUtilsService;
import lt.gama.service.sync.openCart.model.*;
import lt.gama.tasks.BaseDeferredTask;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.*;

import static lt.gama.ConstWorkers.IMPORT_QUEUE;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCOrderTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected TradeService tradeService;

    @Autowired
    transient protected SyncOpenCartUtilsService syncOpenCartUtilsService;

    @Autowired
    transient protected CounterpartySqlMapper counterpartySqlMapper;

    @Autowired
    transient protected SyncHttpService syncHttpService;

    @Autowired
    transient protected PartSqlMapper partSqlMapper;

    @Autowired
    transient protected InvoiceSqlMapper invoiceSqlMapper;

    @Autowired
    transient protected InventoryCheckService inventoryCheckService;


    private final List<Long> ids;
    private final VATRatesDate vatRatesDate;
    private final String api;
    private final String key;
    private final String username;
    private final Map<ImportId, ImpRecord> importMap = new HashMap<>();


    public OCOrderTask(String api, String key, String username, long companyId, List<Long> ids, VATRatesDate vatRatesDate) {
        super(companyId, IMPORT_QUEUE);
        this.ids = ids;
        this.vatRatesDate = vatRatesDate;
        this.api = api;
        this.key = key;
        this.username = username;
    }

    @Override
    public void execute() {
        if (CollectionsHelper.isEmpty(ids)) {
            log.info(className + ": companyId=" + getCompanyId() + " ids is empty");
            finish(TaskResponse.success());
            return;
        }
        OCLogin login = syncOpenCartUtilsService.login(api, key, username);
        if (isNotValid(login)) {
            finish(TaskResponse.error(": Can't login with api='" + api + '\''));
            return;
        }
        var step = dbServiceSQL.executeAndReturnInTransaction(entityManager -> this.executeInTransaction(entityManager, login));
        log.info(className + ": companyId=" + getCompanyId() +
                " synced invoices total=" + ids.size() +
                ", imported=" + step.getImported() +
                ", created=" + step.getCreated() +
                ", updated=" + step.getUpdated() +
                ", skipped=" + step.getSkipped() +
                ", errors=" + step.getErrors());
        finish(TaskResponse.success());
    }

    private OCImportStep executeInTransaction(EntityManager entityManager, OCLogin login) {
        OCImportStep step = new OCImportStep();
        for (Long id : ids) {

            int retry = 0;

            while (true) {

                if (isNotValid(login)) break;

                try {
                    OCOrderResponse ocInvoiceResponse = getInvoice(id, login);
                    if (ocInvoiceResponse == null || ocInvoiceResponse.getOrder() == null || StringHelper.hasValue(ocInvoiceResponse.getError())) {
                        if (ocInvoiceResponse != null && ++retry <= 3) {
                            try {
                                Thread.sleep(5000L * retry);
                            } catch (InterruptedException e) {
                                log.error(className + ": " + e.getMessage(), e);
                            }
                            log.info(className + ": Retrying orderId=" + id + ", companyId=" + getCompanyId());

                            if (retry == 3) {
                                // one last time try login again
                                login = syncOpenCartUtilsService.login(api, key, username);
                                if (isNotValid(login)) break;
                            }
                            continue;   // retry the same operation again
                        }

                        log.error(className + ": Error orderId=" + id + ", companyId=" + getCompanyId());
                        step.errors();
                    }
                    importOrder(entityManager, ocInvoiceResponse, step);
                    step.imported();

                } catch (Exception e) {
                    log.error(className + ": " + e.getMessage(), e);
                }
                break;
            }
        }
        return step;
    }

    private OCOrderResponse getInvoice(long id, OCLogin login) {
        try {
            return syncHttpService.getRequestData(
                    SyncHttpService.HttpMethod.POST,
                    auth.getSettings().getSync().getUrl(),
                    Map.of(
                            "route", "api/order/gamaOrder",
                            "token", login.getToken(),
                            "order_id", String.valueOf(id)),
                    null, null, OCOrderResponse.class, login.getSession());
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            return null;
        }
    }

    protected VATRate getVatRate(double rate) {
        if (vatRatesDate == null || vatRatesDate.getRates() == null) return null;
        for (VATRate vatRate : vatRatesDate.getRates()) {
            if (Double.compare(Math.abs(vatRate.getRate() - rate), 0.1) < 0) {
                return vatRate;
            }
        }
        return null;
    }

    protected void importOrder(EntityManager entityManager, OCOrderResponse ocInvoiceResponse, OCImportStep step) {
        if (BooleanUtils.isNotTrue(auth.getSettings().getSync().getAbilities().order().toGama())) {
            throw new GamaException("No ability to create new order in Gama");
        }
        if (step == null) step = new OCImportStep();

        final OCOrder ocOrder = Validators.checkNotNull(ocInvoiceResponse.getOrder(), "OCOrderResponse order null: " +  ocInvoiceResponse);
        final long orderId = ocOrder.getOrder_id();

        ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(getCompanyId(), InvoiceSql.class, String.valueOf(orderId)));
        InvoiceSql document;
        if (imp == null) {
            step.created();
            document = new InvoiceSql();
        } else {
            final long id = imp.getEntityId();
            document = dbServiceSQL.getById(InvoiceSql.class, id);
            if (document != null) {
                if (BooleanUtils.isTrue(document.getFinished())) {
                    step.skipped();
                    return; // do nothing if invoice finished
                }
                log.info(className + ": Document already exists" +
                        ", id=" + id +
                        ", orderId=" + orderId);
                step.updated();
            } else {
                step.created();
                document = new InvoiceSql();
                int deleted = dbServiceSQL.removeById(ImportSql.class, imp.getId());
                if (deleted != 0 ) {
                    imp = null;
                } else {
                    throw new GamaException("Import record was not deleted");
                }
            }
        }

        fillInvoice(entityManager, document, ocInvoiceResponse.getOrder());

        try {
            tradeService.prepareSaveInvoiceSQL(false, false, true, document);
            document = dbServiceSQL.saveEntityInCompany(document);

            if (imp == null) {
                imp = new ImportSql(getCompanyId(), InvoiceSql.class, String.valueOf(orderId), document.getId(), DBType.POSTGRESQL);
                dbServiceSQL.saveEntity(imp);
                log.info(className + "::importOrder: import saved" +
                        ", externalId=" + imp.getId().getExternalId() +
                        ", entityId=" + imp.getEntityId());
            }

        } catch (Exception e) {
            log.error(className + ": companyId=" + getCompanyId() + ", orderId=" + orderId + ", error in importOrder prepareSaveInvoice", e);
            step.errors();
        }
    }

    private void fillInvoice(EntityManager entityManager, InvoiceSql document, OCOrder ocOrder) {
        var companySettings = auth.getSettings();

        if (StringHelper.hasValue(companySettings.getSync().getLabel())) {
            document.setLabels(Collections.singleton(companySettings.getSync().getLabel()));
        }
        document.setPaymentId(String.valueOf(ocOrder.getOrder_id()));
        document.setExportId(String.valueOf(ocOrder.getOrder_id()));
        document.setArchive(false);

        document.setNumber(ocOrder.getInvoice_prefix() + ocOrder.getInvoice_no());

        document.setDate(ocOrder.getDate_modified().toLocalDate());
        document.setDueDate(document.getDate());

        document.setNote("Order id=" + ocOrder.getOrder_id() + ", " + ocOrder.getPayment_method() + ", " + ocOrder.getShipping_method());

        DocWarehouse warehouse = Validators.isValid(companySettings.getSync().getWarehouse())
                ? companySettings.getSync().getWarehouse()
                : companySettings.getWarehouse();
        if (Validators.isValid(warehouse)) document.setWarehouse(entityManager.getReference(WarehouseSql.class, warehouse.getId()));

        final String currency = ocOrder.getCurrency_code() != null ? ocOrder.getCurrency_code() : companySettings.getCurrency().getCode();
        document.setExchange(new Exchange(currency));

        final var currencyRate = ocOrder.getCurrency_value();

        GamaBigMoney transportationCost = null;
        //GamaMoney subtotal = null;
        GamaBigMoney tax = null;

        if (ocOrder.getTotals() != null) {
            for (OCOrderTotal total : ocOrder.getTotals()) {

                if ("shipping".equalsIgnoreCase(total.getCode().trim())) {
                    transportationCost = GamaMoneyUtils.add(transportationCost, GamaBigMoney.of(currency, total.getValue()));

                } else if ("tax".equalsIgnoreCase(total.getCode().trim())) {
                    tax = GamaMoneyUtils.add(tax, GamaBigMoney.of(currency, total.getValue()));
                }
            }
        }

        document.setTotal(adjust(currencyRate, GamaMoney.of(currency, ocOrder.getTotal())));
        if (GamaMoneyUtils.isNonZero(tax)) document.setTaxTotal(GamaMoneyUtils.toMoney(adjust(currencyRate, tax)));
        document.setSubtotal(GamaMoneyUtils.subtract(document.getTotal(), document.getTaxTotal()));

        GamaMoney subtotal = document.getSubtotal();

        boolean isZeroVat = GamaMoneyUtils.isZero(tax);
        document.setZeroVAT(isZeroVat);

        transportationCost = adjust(currencyRate, transportationCost);
        if (GamaMoneyUtils.isNonZero(transportationCost)) {
            Validators.checkValid(companySettings.getSync().getTransportation(), "Transportation is not configured in sync settings");
        }

        // link customer
        long counterpartyId = ocOrder.getCustomer_id();
        if (counterpartyId > 0) {
            var counterparty = importCounterparty(entityManager, counterpartyId, ocOrder);
            if (Validators.isValid(counterparty)) {
                document.setCounterparty(counterparty);
                if (LocationUtils.isValid(counterparty.getBusinessAddress())) {
                    document.setLocation(counterparty.getBusinessAddress());
                } else if (LocationUtils.isValid(counterparty.getPostAddress())) {
                    document.setLocation(counterparty.getPostAddress());
                } else if (LocationUtils.isValid(counterparty.getRegistrationAddress())) {
                    document.setLocation(counterparty.getRegistrationAddress());
                } else if (CollectionsHelper.hasValue(counterparty.getLocations()) && LocationUtils.isValid(counterparty.getLocations().get(0))) {
                    document.setLocation(counterparty.getLocations().get(0));
                }
            }
        }

        // link parts
        if (ocOrder.getProducts() != null) {
            VATRate defaultRate = vatRatesDate.getRates().stream().max(Comparator.comparingDouble(VATRate::getRate)).orElse(null);

            if (document.getParts() != null) {
                document.getParts().clear();
            }
            else {
                document.setParts(new ArrayList<>());
            }

            InvoicePartSql lastPart = null;
            for (OCOrderLine line : ocOrder.getProducts()) {
                var part = importPart(entityManager, line.getProduct_id(), defaultRate, line);

                InvoicePartSql partInvoice;
                if (part != null) {
                    partInvoice = new InvoicePartSql();
                    partInvoice.setDocPart(new DocPart(part));
                    partInvoice.getDocPart().setName(line.getName());
                    partInvoice.getDocPart().setSku(line.getModel());
                    if (StringHelper.hasValue(part.getVatRateCode()) && StringHelper.isEmpty(partInvoice.getVatRateCode())) {
                        partInvoice.setVatRateCode(part.getVatRateCode());
                        partInvoice.setVat(vatRatesDate.getRatesMap().get(part.getVatRateCode()));
                    }
                } else {
                    part = new PartSql();
                    part.setName("*** OpenCart (" + line.getProduct_id() + ") " + line.getName());
                    part.setSku(line.getModel());
                    part.setType(PartType.PRODUCT);
                    part = dbServiceSQL.saveEntityInCompany(part);
                    partInvoice = new InvoicePartSql();
                    partInvoice.setDocPart(new DocPart(part));
                    partInvoice.getDocPart().setName(part.getName());
                    partInvoice.getDocPart().setSku(line.getModel());
                    if (defaultRate != null) {
                        partInvoice.setTaxable(true);
                        partInvoice.setVat(defaultRate);
                        partInvoice.setVatRateCode(defaultRate.getCode());
                    }
                }
                partInvoice.setPart(part);
                partInvoice.setFixTotal(true);
                partInvoice.setTotal(GamaMoneyUtils.toMoney(adjust(currencyRate, GamaBigMoney.of(currency, line.getTotal()))));
                partInvoice.setQuantity(BigDecimal.valueOf(line.getQuantity()));

                double price = partInvoice.getTotal().getAmount().doubleValue() / partInvoice.getQuantity().doubleValue();
                partInvoice.setPrice(GamaBigMoney.of(currency, price).withScale(partInvoice.getTotal().getScale() * 2));

                partInvoice.setDiscountedTotal(partInvoice.getTotal());
                partInvoice.setDiscountedPrice(partInvoice.getPrice());

                if (partInvoice.getVat() == null) partInvoice.setVat(new VATRate());
                if (!BigDecimalUtils.isZero(line.getTax()) && !BigDecimalUtils.isZero(line.getTotal())) {
                    double rate = line.getTax().doubleValue() / line.getPrice().doubleValue();
                    VATRate vatRate = getVatRate(rate);
                    if (vatRate != null) {
                        partInvoice.setVat(vatRate);
                    }
                    partInvoice.setTaxable(true);
                }
                partInvoice.setParent(document);
                document.getParts().add(partInvoice);
                subtotal = GamaMoneyUtils.subtract(subtotal, partInvoice.getTotal());

                lastPart = partInvoice;
            }

            if (GamaMoneyUtils.isNonZero(transportationCost)) {
                DocPartSync trans = companySettings.getSync().getTransportation();

                var partInvoice = new InvoicePartSql();
                partInvoice.setPart(entityManager.getReference(PartSql.class, trans.getId()));
                partInvoice.setDocPart(new DocPart());
                partInvoice.setType(trans.getType());
                partInvoice.setName(trans.getName());
                partInvoice.setSku(trans.getSku());
                partInvoice.setBarcode(trans.getBarcode());
                partInvoice.setUnit(trans.getUnit());
                partInvoice.setVat(trans.getVat());
                partInvoice.setVatRateCode(trans.getVatRateCode());
                partInvoice.setTaxable(trans.isTaxable());
                partInvoice.setQuantity(BigDecimal.ONE);
                partInvoice.setPrice(transportationCost);
                partInvoice.setTotal(GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(partInvoice.getPrice(), partInvoice.getQuantity())));

                partInvoice.setDiscountedTotal(partInvoice.getTotal());
                partInvoice.setDiscountedPrice(partInvoice.getPrice());

                partInvoice.setParent(document);
                document.getParts().add(partInvoice);
                subtotal = GamaMoneyUtils.subtract(subtotal, partInvoice.getTotal());
            }

            if (GamaMoneyUtils.isNonZero(subtotal) && lastPart != null) {
                // need to fix parts totals - fixing the last position
                // if subtotal is positive - need to increase price and vice versa
                lastPart.setTotal(GamaMoneyUtils.add(lastPart.getTotal(), subtotal));

                double price = lastPart.getTotal().getAmount().doubleValue() / lastPart.getQuantity().doubleValue();
                lastPart.setPrice(GamaBigMoney.of(currency, price).withScale(lastPart.getTotal().getScale() * 2));

                lastPart.setDiscountedTotal(lastPart.getTotal());
                lastPart.setDiscountedPrice(lastPart.getPrice());
            }
        }
    }

    private GamaMoney adjust(BigDecimal currencyRate, GamaMoney value) {
        return GamaMoneyUtils.isZero(value) ? value : GamaMoneyUtils.multipliedBy(value, currencyRate);
    }

    private GamaBigMoney adjust(BigDecimal currencyRate, GamaBigMoney value) {
        return GamaMoneyUtils.isZero(value) ? value : GamaMoneyUtils.multipliedBy(value, currencyRate);
    }

    private CounterpartySql importCounterparty(EntityManager entityManager, long externalId, OCOrder ocInvoice) {
        var impRecord = getImport(CounterpartySql.class, String.valueOf(externalId));
        if (impRecord != null) {
            log.info(className + "::importCounterparty: externalId=" + externalId + ", " + impRecord);
            CounterpartySql counterparty = dbServiceSQL.getByIdOrForeignId(CounterpartySql.class, impRecord.entityId, impRecord.entityDb);
            if (counterparty != null && !BooleanUtils.isTrue(counterparty.getArchive())) {
                log.info(className + "::importCounterparty: find SQL counterpartyId=" + counterparty.getId() +
                        ", name=" + counterparty.getName());
                return counterparty;
            } else {
                log.info(className + "::importCounterparty: wrong or no import entity SQL class - no Counterparty, deleteImport " + impRecord);
                deleteImport(entityManager, impRecord);
            }
        }

        log.info(className + "::importCounterparty: createCounterparty(), externalId=" + externalId);
        var counterparty = createCounterparty(entityManager, externalId, ocInvoice);
        if (counterparty == null) return null;

        if (counterparty.getId() == null) {
            counterparty = dbServiceSQL.saveEntityInCompany(counterparty);
            log.info(className + "::importCounterparty: new counterparty saved" +
                    ", name=" + counterparty.getName() +
                    ", comCode=" + counterparty.getComCode() +
                    ", vatCode=" + counterparty.getVatCode() +
                    ", exportId=" + counterparty.getExportId() +
                    ", id=" + counterparty.getId());
        }
        var imp = new ImportSql(getCompanyId(), CounterpartySql.class, String.valueOf(externalId), counterparty.getId(), DBType.POSTGRESQL);
        imp = dbServiceSQL.saveEntity(imp);
        log.info(className + "::importCounterparty: import saved" +
                ", externalId=" + imp.getId().getExternalId() +
                ", entityId=" + imp.getEntityId());

        // cache import record
        importMap.put(imp.getId(), new ImpRecord(imp.getId(), imp.getEntityId(), imp.getEntityDb()));

        return counterparty;
    }

    protected CounterpartySql createCounterparty(EntityManager entityManager, long externalId, OCOrder ocInvoice) {
        if (BooleanUtils.isNotTrue(auth.getSettings().getSync().getAbilities().customer().toGama())) {
            throw new GamaException("No ability to create new customer in Gama");
        }
        var companySettings = auth.getSettings();
        // try search by company/person id
        String compId = StringHelper.trim(ocInvoice.getPayment_taxid1());
        if (StringHelper.hasValue(compId)) {
            // try search by VAT code
            List<CounterpartySql> counterparties = entityManager.createQuery(
                            "SELECT a FROM " + CounterpartySql.class.getName() + " a" +
                                    " WHERE companyId = :companyId" +
                                    " AND (a.archive IS null OR a.archive = false)" +
                                    " AND vatCode = :vatCode",
                            CounterpartySql.class)
                    .setParameter("companyId", getCompanyId())
                    .setParameter("vatCode", compId)
                    .setMaxResults(1)
                    .getResultList();
            if (CollectionsHelper.hasValue(counterparties)) {
                log.info(className + "::createCounterparty:" +
                        " counterparty found with vatCode=" + compId + ", id=" + counterparties.get(0).getId());
                return counterparties.get(0);
            }
            // try search by com. code
            counterparties = entityManager.createQuery(
                            "SELECT a FROM " + CounterpartySql.class.getName() + " a" +
                                    " WHERE companyId = :companyId" +
                                    " AND (a.archive IS null OR a.archive = false)" +
                                    " AND a.comCode = :comCode",
                            CounterpartySql.class)
                    .setParameter("companyId", getCompanyId())
                    .setParameter("comCode", compId)
                    .setMaxResults(1)
                    .getResultList();
            if (CollectionsHelper.hasValue(counterparties)) {
                log.info(className + "::createCounterparty:" +
                        " counterparty found with comCode=" + compId + ", id=" + counterparties.get(0).getId());
                return counterparties.get(0);
            }
        }

        // try search by company name
        String companyName = StringHelper.trim(ocInvoice.getPayment_company());
        if (StringHelper.hasValue(companyName)) {
            List<CounterpartySql> counterparties = entityManager.unwrap(Session.class).createNativeQuery("""
                            SELECT a.* FROM counterparties a
                            WHERE company_id = :companyId
                            AND (a.archive IS null OR a.archive = false)
                            AND (lower(trim(unaccent(name))) = :name OR lower(trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g'))) = :name)
                            """, CounterpartySql.class)
                    .addSynchronizedEntityClass(CounterpartySql.class)
                    .setParameter("companyId", getCompanyId())
                    .setParameter("name", EntityUtils.prepareName(companyName))
                    .setMaxResults(1)
                    .getResultList();
            if (CollectionsHelper.hasValue(counterparties)) {
                log.info(className + "::createCounterparty:" +
                        " counterparty found with name=" + EntityUtils.prepareName(companyName) + ", id=" + counterparties.get(0).getId());
                return counterparties.get(0);
            }
        }

        // try to search by person name and also look at person's country and city
        String fullName = StringHelper.trim(ocInvoice.getPayment_firstname()) + " " + StringHelper.trim(ocInvoice.getPayment_lastname());
        if (StringHelper.isEmpty(companyName) && StringHelper.hasValue(fullName)) {
            List<CounterpartySql> counterparties = entityManager.unwrap(Session.class).createNativeQuery("""
                            SELECT a.* FROM counterparties a
                            WHERE company_id = :companyId
                            AND (a.archive IS null OR a.archive = false)
                            AND (lower(trim(unaccent(name))) = :name OR lower(trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g'))) = :name)
                            """, CounterpartySql.class)
                    .addSynchronizedEntityClass(CounterpartySql.class)
                    .setParameter("companyId", getCompanyId())
                    .setParameter("name", EntityUtils.prepareName(fullName))
                    .getResultList();
            if (counterparties != null) {
                for (CounterpartySql counterparty : counterparties) {
                    Location address = counterparty.getBusinessAddress() != null ? counterparty.getBusinessAddress() :
                            counterparty.getRegistrationAddress() != null ? counterparty.getRegistrationAddress() : counterparty.getPostAddress();
                    if (address != null && Objects.equals(address.getCountry(), CountryCode.getByCodeIgnoreCase(ocInvoice.getPayment_iso_code_2()).getAlpha2()) &&
                            Objects.equals(address.getCity(), ocInvoice.getPayment_city())) {
                        log.info(className + "::createCounterparty:" +
                                " counterparty found with person name=" + EntityUtils.prepareName(companyName) +
                                " and city=" + address.getCity() +
                                " and country=" + address.getCountry() +
                                ", id=" + counterparties.get(0).getId());
                        return counterparty;
                    }
                }
            }
        }

        CounterpartySql counterparty = new CounterpartySql();
        counterparty.setCompanyId(getCompanyId());
        counterparty.setExportId(String.valueOf(externalId));
        counterparty.setName(syncHttpService.decode(StringHelper.hasValue(companyName) ? companyName : fullName));
        counterparty.setVatCode(ocInvoice.getPayment_taxid1());

        if (StringHelper.hasValue(ocInvoice.getPayment_address_1()) ||
                StringHelper.hasValue(ocInvoice.getPayment_postcode()) ||
                StringHelper.hasValue(ocInvoice.getPayment_city())) {
            Location location = new Location();
            location.setAddress1(ocInvoice.getPayment_address_1());
            location.setAddress2(ocInvoice.getPayment_address_2());
            location.setZip(ocInvoice.getPayment_postcode());
            location.setCity(ocInvoice.getPayment_city());
            if (StringHelper.isEmpty(ocInvoice.getPayment_iso_code_2())) {
                location.setCountry(companySettings.getCountry());
            } else {
                location.setCountry(CountryCode.getByCodeIgnoreCase(ocInvoice.getPayment_iso_code_2()).getAlpha2());
            }
            counterparty.setBusinessAddress(location);
        }

        if (StringHelper.hasValue(ocInvoice.getShipping_address_1()) ||
                StringHelper.hasValue(ocInvoice.getShipping_postcode()) ||
                StringHelper.hasValue(ocInvoice.getShipping_city())) {
            Location location = new Location();
            location.setAddress1(ocInvoice.getShipping_address_1());
            location.setAddress2(ocInvoice.getShipping_address_2());
            location.setZip(ocInvoice.getShipping_postcode());
            location.setCity(ocInvoice.getShipping_city());
            if (StringHelper.isEmpty(ocInvoice.getShipping_iso_code_2())) {
                location.setCountry(companySettings.getCountry());
            } else {
                location.setCountry(CountryCode.getByCodeIgnoreCase(ocInvoice.getShipping_iso_code_2()).getAlpha2());
            }
            counterparty.setLocations(new ArrayList<>());
            counterparty.getLocations().add(location);
        }

        if (!companySettings.isDisableGL()) {
            counterparty.setAccount(DebtType.CUSTOMER, companySettings.getGl().getCounterpartyCustomer());
            counterparty.setAccount(DebtType.VENDOR, companySettings.getGl().getCounterpartyVendor());
        }

        log.info(className + "::createCounterparty: new counterparty created" +
                ", name=" + counterparty.getName() +
                ", comCode=" + counterparty.getComCode() +
                ", vatCode=" + counterparty.getVatCode() +
                ", address=" + counterparty.getAddress() +
                ", exportId=" + counterparty.getExportId());
        return counterparty;
    }

    private PartSql importPart(EntityManager entityManager, long externalId, VATRate defaultRate, OCOrderLine line) {
        ImpRecord impRec = getImport(PartSql.class, String.valueOf(externalId));
        if (impRec != null) {
            PartSql part = dbServiceSQL.getByIdOrForeignId(PartSql.class, impRec.entityId, impRec.entityDb);
            if (part != null && BooleanUtils.isNotTrue(part.getArchive())) {
                log.info(className + "::importPart: find SQL partId=" + part.getId() + ", name=" + part.getName());
                return part;
            }
            log.info(className + "::importPart: wrong or no import entity SQL class - no Part, deleteImport " + impRec);
            deleteImport(entityManager, impRec);
        }

        log.info(className + "::importPart: createPart(), externalId=" + externalId);
        var part = createPart(entityManager, externalId, defaultRate, line);
        if (part == null) return null;

        var companySettings = auth.getSettings();
        var syncPart = companySettings.getSync().getPart();

        if (part.getId() == null) {
            part = dbServiceSQL.saveEntityInCompany(part);
            log.info(className + "::importPart: new part saved" +
                    ", name=" + part.getName() +
                    ", sku=" + part.getSku() +
                    ", exportId=" + part.getExportId() +
                    ", id=" + part.getId());
        } else if (Validators.isValid(syncPart) && Objects.equals(part.getId(), syncPart.getId())) {
            return part;
        }

        var imp = new ImportSql(getCompanyId(), PartSql.class, String.valueOf(externalId), part.getId(), DBType.POSTGRESQL);
        imp = dbServiceSQL.saveEntity(imp);
        log.info(className + "::importPart: import saved" +
                ", externalId=" + imp.getId().getExternalId() +
                ", entityId=" + imp.getEntityId());
        // cache import record
        importMap.put(imp.getId(), new ImpRecord(imp.getId(), imp.getEntityId(), imp.getEntityDb()));

        return part;
    }

    protected PartSql createPart(EntityManager entityManager, long externalId, VATRate defaultRate, OCOrderLine product) {
        // try search by SKU/Model
        String model = StringHelper.trim(product.getModel());
        if (StringHelper.hasValue(model)) {
            List<PartSql> parts = entityManager.createQuery(
                            "SELECT p FROM " + PartSql.class.getName() + " p" +
                                    " WHERE sku = :sku" +
                                    " AND companyId = :companyId" +
                                    " AND (p.archive IS null OR p.archive = false)" +
                                    " AND (p.hidden IS null OR p.hidden = false)", PartSql.class)
                    .setParameter("companyId", getCompanyId())
                    .setParameter("sku", model)
                    .setMaxResults(2)
                    .getResultList();
            log.info(className + "::createPart: " +
                    (CollectionsHelper.isEmpty(parts) ? "no part found with sku=\"" + model + '"'
                    : parts.size() > 1 ? "too many parts found with sku=\"" + model + '"'
                    : "part found with sku=\"" + model + "\", id=" + parts.get(0).getId()));
            if (CollectionsHelper.isEmpty(parts)) {
                // try search by compressed SKU
                parts = entityManager.unwrap(Session.class).createNativeQuery("""
                                SELECT * FROM parts p
                                WHERE lower(regexp_replace(sku, '\\s', '', 'g')) = lower(regexp_replace(:sku, '\\s', '', 'g'))
                                    AND company_id = :companyId
                                    AND (p.archive IS null OR p.archive = false)
                                    AND (p.hidden IS null OR p.hidden = false)
                                """, PartSql.class)
                        .addSynchronizedEntityClass(PartSql.class)
                        .setParameter("companyId", getCompanyId())
                        .setParameter("sku", model)
                        .setMaxResults(2)
                        .getResultList();
                log.info(className + "::createPart: " +
                        (CollectionsHelper.isEmpty(parts) ? "no part found with compressed sku=\"" + model.replaceAll("\\s", "") + '"'
                        : parts.size() > 1 ? "too many parts found with compressed sku=\"" + model.replaceAll("\\s", "") + '"'
                        : "part found with compressed sku=\"" + model.replaceAll("\\s", "") + "\", id=" + parts.get(0).getId()));
            }
            PartSql part = parts != null && parts.size() == 1 ? parts.get(0) : null;
            if (part != null) return part;
        }
        var companySettings = auth.getSettings();
        var syncPart = companySettings.getSync().getPart();
        if (!Validators.isValid(syncPart) &&
                (companySettings.getSync().getAbilities() == null ||
                        BooleanUtils.isNotTrue(companySettings.getSync().getAbilities().product().toGama()))) {
            throw new GamaException("No ability to create new part in Gama and no sync part in sync settings");
        }

        if (Validators.isValid(syncPart)) {
            return dbServiceSQL.getAndCheck(PartSql.class, syncPart.getId());
        } else {
            var part = new PartSql();
            part.setCompanyId(getCompanyId());
            part.setExportId(String.valueOf(externalId));

            part.setName(syncHttpService.decode(product.getName()));
            part.setSku(product.getModel());
            if (defaultRate != null) part.setVatRateCode(defaultRate.getCode());
            if (StringHelper.hasValue(part.getVatRateCode())) part.setTaxable(true);
            part.setType(PartType.PRODUCT);
            if (!companySettings.isDisableGL()) {
                part.setAccountAsset(companySettings.getGl().getProductAsset());
                part.setGlExpense(new GLDC(companySettings.getGl().getProductExpense(), companySettings.getGl().getProductExpense()));
                part.setGlIncome(new GLDC(companySettings.getGl().getProductIncome(), companySettings.getGl().getProductIncome()));
            }
            log.info(className + "::createPart: new part created" +
                    ", name=" + part.getName() +
                    ", sku=" + part.getSku() +
                    ", exportId=" + part.getExportId());
            return part;
        }
    }

    private <E> ImpRecord getImport(Class<E> type, String externalId) {
        ImportId key = new ImportId(getCompanyId(), type, externalId);
        if (importMap.containsKey(key)) {
            return importMap.get(key);
        }

        ImportSql imp = dbServiceSQL.getById(ImportSql.class, key);
        if (imp != null) {
            var id = new ImpRecord(imp.getId(), imp.getEntityId(), imp.getEntityDb());
            importMap.put(key, id);
            return id;
        }
        return null;
    }

    private void deleteImport(EntityManager entityManager, ImpRecord imp) {
        importMap.remove(imp.id);
        ImportSql entity = entityManager.getReference(ImportSql.class, imp.id);
        entityManager.remove(entity);
    }

    private boolean isNotValid(OCLogin login) {
        return login == null || StringHelper.isEmpty(login.getToken());
    }

    record ImpRecord(ImportId id, long entityId, DBType entityDb) implements Serializable {}
}
