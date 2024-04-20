package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import lt.gama.api.request.CheckVatRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.CheckVatResponse;
import lt.gama.api.response.LastInvoicePriceResponse;
import lt.gama.api.response.PageResponse;
import lt.gama.api.response.TaskResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.integrations.eu.EUCheckVatService;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.*;
import lt.gama.model.i.IFinished;
import lt.gama.model.i.IId;
import lt.gama.model.i.IVatRate;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.documents.items.EstimatePartSql;
import lt.gama.model.sql.documents.items.InvoiceBasePartSql;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.sql.documents.items.PurchasePartSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.type.DocumentDoubleEntry;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.doc.DocExpense;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DataFormatType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.ibase.IBaseDocPartSql;
import lt.gama.model.type.inventory.VATCodeTotal;
import lt.gama.model.type.part.DocPart;
import lt.gama.report.RepInvoice;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaSimilarDocumentAlreadyExistsException;
import lt.gama.service.sync.i.ISyncWarehouseService;
import lt.gama.service.sync.i.base.ISyncService;
import lt.gama.service.sync.SyncResult;
import lt.gama.service.sync.SyncTypeService;
import lt.gama.tasks.FinishInventoryOpeningBalanceTask;
import lt.gama.tasks.FinishInventoryTask;
import lt.gama.tasks.RecallInventoryTask;
import lt.gama.tasks.maintenance.CreateInvoicesTask;
import lt.gama.tasks.sync.SyncTask;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gama
 * Created by valdas on 15-03-27.
 */
@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    private final StorageService storageService;
    private final DocumentService documentService;
    private final CurrencyService currencyService;
    private final GLOperationsService glOperationsService;
    private final SyncTypeService syncTypeService;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final InventorySqlMapper inventorySqlMapper;
    private final InventoryOpeningBalanceSqlMapper inventoryOpeningBalanceSqlMapper;
    private final OrderSqlMapper orderSqlMapper;
    private final TransProdSqlMapper transportationSqlMapper;
    private final PurchaseSqlMapper purchaseSqlMapper;
    private final InvoiceSqlMapper invoiceSqlMapper;
    private final EUCheckVatService euCheckVatService;
    private final InventoryCheckService inventoryCheckService;
    private final EstimateSqlMapper estimateSqlMapper;
    private final TaskQueueService taskQueueService;


    TradeService(StorageService storageService,
                 DocumentService documentService,
                 CurrencyService currencyService,
                 GLOperationsService glOperationsService,
                 SyncTypeService syncTypeService,
                 DoubleEntrySqlMapper doubleEntrySqlMapper,
                 Auth auth,
                 DBServiceSQL dbServiceSQL,
                 InventorySqlMapper inventorySqlMapper,
                 InventoryOpeningBalanceSqlMapper inventoryOpeningBalanceSqlMapper,
                 OrderSqlMapper orderSqlMapper,
                 TransProdSqlMapper transportationSqlMapper,
                 PurchaseSqlMapper purchaseSqlMapper,
                 InvoiceSqlMapper invoiceSqlMapper,
                 EUCheckVatService euCheckVatService,
                 InventoryCheckService inventoryCheckService,
                 EstimateSqlMapper estimateSqlMapper,
                 TaskQueueService taskQueueService) {
        this.storageService = storageService;
        this.documentService = documentService;
        this.currencyService = currencyService;
        this.glOperationsService = glOperationsService;
        this.syncTypeService = syncTypeService;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.inventorySqlMapper = inventorySqlMapper;
        this.inventoryOpeningBalanceSqlMapper = inventoryOpeningBalanceSqlMapper;
        this.orderSqlMapper = orderSqlMapper;
        this.transportationSqlMapper = transportationSqlMapper;
        this.purchaseSqlMapper = purchaseSqlMapper;
        this.invoiceSqlMapper = invoiceSqlMapper;
        this.euCheckVatService = euCheckVatService;
        this.inventoryCheckService = inventoryCheckService;
        this.estimateSqlMapper = estimateSqlMapper;
        this.taskQueueService = taskQueueService;
    }

    /**
     * Check if document number is not duplicated. Checked only a year (12 months) before
     * @param document document to check
     */
    private void checkDocNumberSQL(BaseDocumentSql document) {
        Validators.checkNotNull(document.getDate(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentDate));

        if (StringHelper.hasValue(document.getNumber())) {

            boolean purchaseHasCounterparty = document instanceof PurchaseSql && Validators.isValid(document.getCounterparty());

            StringJoiner sj = new StringJoiner(" ");
            sj.add("SELECT d.id AS id, d.date AS date, d.number AS number");
            sj.add("FROM " + document.getClass().getName() + " d");
            sj.add("WHERE d.date >= :dateFrom");
            sj.add("AND d.number = :number");
            sj.add("AND d.companyId = :companyId");
            sj.add("AND (d.archive IS null OR d.archive = false)");
            sj.add("AND (d.hidden IS null OR d.hidden = false)");
            if (purchaseHasCounterparty) sj.add("AND d.counterparty.id = :counterpartyId");

            TypedQuery<Tuple> q = entityManager.createQuery(sj.toString(), Tuple.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("dateFrom", document.getDate().minusMonths(12));
            q.setParameter("number", document.getNumber());
            if (purchaseHasCounterparty) q.setParameter("counterpartyId", document.getCounterparty().getId());

            List<Tuple> result = q.getResultList();

            if (CollectionsHelper.isEmpty(result)) return; //OK
            if (result.size() == 1 && result.get(0).get("id", Long.class).equals(document.getId())) return; // OK

            // Can be error
            StringBuilder error = new StringBuilder();
            for (Tuple o : result) {
                // need to check if not the current document
                if (!o.get("id", Long.class).equals(document.getId())) {
                    error.append(o.get("date", LocalDate.class).toString()).append(' ').append(o.get("number", String.class)).append('\n');
                }
            }
            if (!error.isEmpty()) {
                if (document instanceof PurchaseSql) {
                    throw new GamaSimilarDocumentAlreadyExistsException(
                            TranslationService.getInstance().translate(TranslationService.INVENTORY.SameDocumentNumberVendor) + ": " + error);
                } else {
                    throw new GamaSimilarDocumentAlreadyExistsException(
                            TranslationService.getInstance().translate(TranslationService.INVENTORY.SameDocumentNumber) + ": " + error);
                }
            }
        }
    }

    private void calculateOrder(OrderDto document) {
        if (document == null) return;

        document.setSubtotal(null);
        document.setTaxTotal(null);
        document.setTotal(null);

        document.setBaseSubtotal(null);
        document.setBaseTaxTotal(null);
        document.setBaseTotal(null);

        document.setCurrentSubtotal(null);
        document.setCurrentTaxTotal(null);
        document.setCurrentTotal(null);

        if (CollectionsHelper.isEmpty(document.getParts())) return;

        Map<Double, GamaMoney> vats = new HashMap<>();
        Map<Double, GamaMoney> currentVats = new HashMap<>();

        for (PartOrderDto part : document.getParts()) {

            part.setTotal(GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(part.getPrice(), part.getEstimate())));
            part.setCurrentTotal(GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(part.getPrice(), part.getQuantity())));

            if (document.getExchange() != null) part.setBaseTotal(document.getExchange().exchange(part.getTotal()));

            document.setSubtotal(GamaMoneyUtils.add(document.getSubtotal(), part.getTotal()));
            document.setCurrentSubtotal(GamaMoneyUtils.add(document.getCurrentSubtotal(), part.getCurrentTotal()));

            if (part.isTaxable() && !document.isZeroVAT()) {
                double vatRate = part.getVat() != null ? NumberUtils.doubleValue(part.getVat().getRate()) :
                        part.getVatRate() != null ? NumberUtils.doubleValue(part.getVatRate()) : 0;
                if (vatRate > 0) {
                    GamaMoney total = vats.get(vatRate);
                    if (total == null) {
                        vats.put(vatRate, part.getTotal());
                    } else {
                        vats.put(vatRate, GamaMoneyUtils.add(total, part.getTotal()));
                    }
                    total = currentVats.get(vatRate);
                    if (total == null) {
                        currentVats.put(vatRate, part.getCurrentTotal());
                    } else {
                        currentVats.put(vatRate, GamaMoneyUtils.add(total, part.getCurrentTotal()));
                    }
                }
            }
        }

        for (Map.Entry<Double, GamaMoney> entry : vats.entrySet()) {
            document.setTaxTotal(GamaMoneyUtils.add(document.getTaxTotal(),
                    GamaMoneyUtils.multipliedBy(entry.getValue(), entry.getKey() / 100.0)));
        }
        for (Map.Entry<Double, GamaMoney> entry : currentVats.entrySet()) {
            document.setCurrentTaxTotal(GamaMoneyUtils.add(document.getCurrentTaxTotal(),
                    GamaMoneyUtils.multipliedBy(entry.getValue(), entry.getKey() / 100.0)));
        }

        document.setTotal(GamaMoneyUtils.add(document.getSubtotal(), document.getTaxTotal()));
        document.setBaseTotal(GamaMoneyUtils.add(document.getBaseSubtotal(), document.getBaseTaxTotal()));

        document.setCurrentTotal(GamaMoneyUtils.add(document.getCurrentSubtotal(), document.getCurrentTaxTotal()));
    }

    private void calculateTaxesSQL(InvoiceSql document, List<InvoicePartSql> parts, GamaMoney taxTotal) {

        document.setTaxTotal(null);
        document.setTotal(null);

        document.setBaseTaxTotal(null);
        document.setBaseTotal(null);

        if (document.getVatCodeTotals() == null) {
            document.setVatCodeTotals(new ArrayList<>());
        } else {
            for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
                vatCodeTotal.setAmount(null);
                vatCodeTotal.setTax(null);
            }
        }

        for (InvoicePartSql part : parts) {
            if (part.isTaxable()) {
                double vatRate = part.getVat() != null ? NumberUtils.doubleValue(part.getVat().getRate()) : 0;
                boolean found = false;
                for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
                    if (vatCodeTotal.getRate() != null && NumberUtils.isEq(vatCodeTotal.getRate(), vatRate, 2)) {
                        vatCodeTotal.setAmount(GamaMoneyUtils.add(vatCodeTotal.getAmount(), part.getBaseTotal()));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    VATCodeTotal vatCodeTotal = new VATCodeTotal();
                    vatCodeTotal.setRate(vatRate);
                    vatCodeTotal.setAmount(part.getBaseTotal());
                    document.getVatCodeTotals().add(vatCodeTotal);
                }
            } else {
                boolean found = false;
                for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
                    if (vatCodeTotal.getRate() == null) {
                        vatCodeTotal.setAmount(GamaMoneyUtils.add(vatCodeTotal.getAmount(), part.getBaseTotal()));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    VATCodeTotal vatCodeTotal = new VATCodeTotal();
                    vatCodeTotal.setAmount(part.getBaseTotal());
                    document.getVatCodeTotals().add(vatCodeTotal);
                }
            }
        }

        for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
            if (!NumberUtils.isZero(vatCodeTotal.getRate(), 2)) {
                vatCodeTotal.setTax(GamaMoneyUtils.multipliedBy(vatCodeTotal.getAmount(), vatCodeTotal.getRate() / 100.0));
                if (!document.isZeroVAT()) {
                    document.setBaseTaxTotal(GamaMoneyUtils.add(document.getBaseTaxTotal(), vatCodeTotal.getTax()));
                }
            }
        }

        document.setBaseTotal(GamaMoneyUtils.add(document.getBaseSubtotal(), document.getBaseTaxTotal()));

        if (taxTotal != null) {
            document.setTaxTotal(taxTotal);
            if (document.getVatCodeTotals().size() == 1 && document.getVatCodeTotals().get(0).getTax() != null &&
                    Objects.equals(taxTotal.getCurrency(), document.getVatCodeTotals().get(0).getTax().getCurrency()) &&
                    !GamaMoneyUtils.isEqual(taxTotal, document.getVatCodeTotals().get(0).getTax())) {
                document.getVatCodeTotals().get(0).setTax(taxTotal);
                document.setBaseTaxTotal(taxTotal);
                document.setBaseTotal(GamaMoneyUtils.add(document.getBaseSubtotal(), document.getBaseTaxTotal()));
            }
        } else {
            document.setTaxTotal(document.getExchange().exchangeReverse(document.getBaseTaxTotal()));
        }

        document.setTotal(GamaMoneyUtils.add(document.getSubtotal(), document.getTaxTotal()));
    }

    public void calculateInvoiceSQL(InvoiceSql document, List<InvoicePartSql> parts) {
        if (document == null) return;

        GamaMoney taxTotalSaved = document.getTaxTotal();

        document.setPartsTotal(null);
        document.setDiscountTotal(null);
        document.setSubtotal(null);
        document.setTaxTotal(null);
        document.setTotal(null);

        document.setBaseSubtotal(null);
        document.setBaseTaxTotal(null);
        document.setBaseTotal(null);

        if (CollectionsHelper.isEmpty(document.getParts())) return;

        GamaMoney discountDocTotal = null;
        int priceScale = auth.getSettings().getDecimalPrice();

        for (InvoicePartSql part : parts) {
            part.setDiscountDoc(document.getDiscount());
            if (GamaMoneyUtils.isZero(part.getPrice()) || BigDecimalUtils.isZero(part.getQuantity())) {
                part.setDiscountedPrice(null);
                part.setDiscountedTotal(null);
                part.setDiscountDocTotal(null);
                part.setTotal(null);

                part.setBaseTotal(null);

            } else {
                // fix price scale
                if (part.getPrice() != null && part.getPrice().getScale() < priceScale) {
                    part.setPrice(part.getPrice().withScale(priceScale));
                }
                if (!part.isFixTotal()) {
                    part.setDiscountedPrice(GamaMoneyUtils.multipliedBy(part.getPrice(), 1.0 - NumberUtils.doubleValue(part.getDiscount()) / 100.0));
                    part.setDiscountedTotal(GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(part.getDiscountedPrice(), part.getQuantity())));
                    part.setDiscountDocTotal(GamaMoneyUtils.multipliedBy(part.getDiscountedTotal(), NumberUtils.doubleValue(document.getDiscount()) / 100.0));

                } else {
                    // part 'discountedTotal' is fixed - need to recalculate everything else
                    double discountedTotal = GamaMoneyUtils.isZero(part.getDiscountedTotal()) ? 0 : part.getDiscountedTotal().getAmount().doubleValue();
                    double discountedPrice = discountedTotal / part.getQuantity().doubleValue();
                    part.setDiscountedPrice(part.getPrice().withAmount(discountedPrice).withScale(priceScale > 2 ? priceScale : 4));

                    double price = discountedTotal / (1.0 - NumberUtils.doubleValue(part.getDiscount()) / 100.0) / part.getQuantity().doubleValue();
                    part.setPrice(part.getPrice().withAmount(price).withScale(priceScale > 2 ? priceScale : 4));

                    part.setDiscountDocTotal(GamaMoneyUtils.multipliedBy(part.getDiscountedTotal(), NumberUtils.doubleValue(part.getDiscountDoc()) / 100.0));
                }

                part.setTotal(GamaMoneyUtils.subtract(part.getDiscountedTotal(), part.getDiscountDocTotal()));
                if (document.getExchange() != null) part.setBaseTotal(document.getExchange().exchange(part.getTotal()));
            }

            discountDocTotal = GamaMoneyUtils.add(discountDocTotal, part.getDiscountDocTotal());

            document.setPartsTotal(GamaMoneyUtils.add(document.getPartsTotal(), part.getDiscountedTotal()));
        }

        if (!NumberUtils.isZero(document.getDiscount(), 4)) {
            document.setDiscountTotal(GamaMoneyUtils.multipliedBy(document.getPartsTotal(), document.getDiscount() / 100));
            if (!GamaMoneyUtils.isEqual(document.getDiscountTotal(), discountDocTotal) && CollectionsHelper.hasValue(document.getParts())) {
                // add diff to the last parts item
                GamaMoney diff = GamaMoneyUtils.subtract(discountDocTotal, document.getDiscountTotal());
                InvoicePartSql last = parts.get(parts.size() - 1);
                last.setDiscountDocTotal(GamaMoneyUtils.subtract(last.getDiscountDocTotal(), diff));
                last.setTotal(GamaMoneyUtils.subtract(last.getDiscountedTotal(), last.getDiscountDocTotal()));
                if (document.getExchange() != null) last.setBaseTotal(document.getExchange().exchange(last.getTotal()));
            }
        } else {
            document.setDiscountTotal(null);
        }
        document.setSubtotal(GamaMoneyUtils.subtract(document.getPartsTotal(), document.getDiscountTotal()));
        document.setBaseSubtotal(document.getExchange().exchange(document.getSubtotal()));

        Validators.checkArgument(GamaMoneyUtils.isEqual(document.getSubtotal(),
                        GamaMoneyUtils.subtract(document.getPartsTotal(), document.getDiscountTotal())),
                "Wrong totals: " + document.getSubtotal() + " != " + document.getPartsTotal() + " - " + document.getDiscountTotal());

        currencyCorrectionSQL(parts, document.getBaseSubtotal(), document.getExchange());
        calculateTaxesSQL(document, parts, taxTotalSaved);
    }

    public void prepareSaveInvoiceSQL(boolean sameTaxTotal, boolean doNotTouchParts, boolean doNotCheckDocNumber,
                                      InvoiceSql document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        // check if number not in use?
        if (!doNotCheckDocNumber) {
            checkDocNumberSQL(document);
        }

        if (!doNotTouchParts) {

            if ((document.getExchange() == null && document.getTotal() != null) ||
                    (document.getExchange() != null && document.getTotal() != null &&
                            !Objects.equals(document.getExchange().getCurrency(),
                                    document.getTotal().getCurrency()))) {
                document.setExchange(new Exchange(document.getTotal().getCurrency()));
            }
            Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings,
                    document.getExchange(), document.getDate()), "No exchange");
            document.setExchange(exchange);


            // check return doc id
            if (document.getParts() != null) {
                for (InvoiceBasePartSql basePart : document.getParts()) {
                    if (basePart instanceof InvoicePartSql part && BigDecimalUtils.isNegative(part.getQuantity()) && part.getType() != PartType.SERVICE &&
                            part.getDocReturn() != null && part.getDocReturn().getId() == null &&
                            part.getDocReturn().getDate() != null && part.getDocReturn().getNumber() != null) {

                        List<Long> invoiceIds = entityManager.createQuery(
                                        "SELECT id" +
                                                " FROM " + InvoiceSql.class.getName() + " i" +
                                                " WHERE i." + InvoiceSql_.COMPANY_ID + " = :companyId" +
                                                " AND i." + InvoiceSql_.DATE + " = :docReturnDate" +
                                                " AND i." + InvoiceSql_.NUMBER + " = :docReturnNumber", Long.class)
                                .setParameter("companyId", auth.getCompanyId())
                                .setParameter("docReturnDate", part.getDocReturn().getDate())
                                .setParameter("docReturnNumber", part.getDocReturn().getDate())
                                .getResultList();

                        if (CollectionsHelper.isEmpty(invoiceIds))
                            throw new GamaException("Part " + part + " - Invoice not found: "
                                    + part.getDocReturn().getDate() + " " + part.getDocReturn().getNumber());
                        if (invoiceIds.size() > 1)
                            throw new GamaException("Part " + part + " - Too many invoices with the same date/number: " +
                                    part.getDocReturn().getDate() + " " + part.getDocReturn().getNumber());
                        part.getDocReturn().setId(invoiceIds.get(0));
                    }
                    //  Needed when saving in frontend, do not affect tests if commented.
                    if (basePart instanceof InvoicePartSql part && part.getDocReturn() != null && part.getDocReturn().getDb() != DBType.POSTGRESQL) {
                        part.getDocReturn().setDb(DBType.POSTGRESQL);
                    }
                }
            }

            List<InvoicePartSql> parts = CollectionsHelper.streamOf(document.getParts())
                    .filter(p -> p instanceof InvoicePartSql)
                    .map(InvoicePartSql.class::cast)
                    .toList();

            setPartsVAT(companySettings.getCountry(), document.getDate(), parts);
            calculateInvoiceSQL(document, parts);

        } else if (!sameTaxTotal) {

            if (document.isZeroVAT()) {

                document.setTaxTotal(GamaMoney.zero(document.getExchange().getCurrency()));
                document.setTotal(document.getSubtotal());

                document.setBaseTaxTotal(GamaMoney.zero(document.getExchange().getBase()));
                document.setBaseTotal(document.getBaseSubtotal());

            } else {

                document.setTotal(GamaMoneyUtils.add(document.getSubtotal(), document.getTaxTotal()));

                document.setBaseTaxTotal(document.getExchange().exchange(document.getTaxTotal()));
                document.setBaseTotal(GamaMoneyUtils.add(document.getBaseSubtotal(), document.getBaseTaxTotal()));

            }
        }
        inventoryCheckService.checkPartUuids(document.getParts(), true);
        inventoryCheckService.checkPartLinkUuidsEntity(document.getParts(), InvoicePartSql::new);

        if (document.getId() == null) {
            InventoryUtils.clearFinished(document.getParts());
            InventoryUtils.clearPartId(document.getParts());
        }

        // check debt type
        if (Validators.isValid(document.getCounterparty()) && document.getDebtType() == null) {
            document.setDebtType(DebtType.CUSTOMER);
        }
    }

    public InvoiceDto saveInvoice(InvoiceDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        if (document.getId() == null) {
            CollectionsHelper.streamOf(document.getParts()).forEach(part -> {
                part.setRecordId(null);
                part.setLinkUuid(null);
                boolean isReturnedPart = BigDecimalUtils.isNegative(part.getQuantity());
                if (!isReturnedPart) part.setUuid(null);
                CollectionsHelper.streamOf(part.getParts()).forEach(partPart -> {
                    partPart.setRecordId(null);
                    partPart.setParentLinkUuid(null);
                    if (!isReturnedPart) partPart.setUuid(null);
                });
            });
        }
        DocumentDoubleEntry<InvoiceSql> pair = dbServiceSQL.executeAndReturnInTransaction(em -> {
            boolean samePartsAndExchange = false;
            boolean sameTaxTotal = false;

            if (document.getId() != null) {
                InvoiceSql entity = dbServiceSQL.getAndCheck(InvoiceSql.class, document.getId(), InvoiceSql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, document, auth.getLanguage());

                if (!BooleanUtils.isSame(entity.getFinished(), document.getFinished()) ||
                        !BooleanUtils.isSame(entity.getFinishedParts(), document.getFinishedParts()) ||
                        !BooleanUtils.isSame(entity.getFinishedDebt(), document.getFinishedDebt())) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentChangeStatus));
                }

                if (entity.isFullyFinished()) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready));
                }

                samePartsAndExchange =
                        InventoryUtils.isInvoicePartsCurrencyAndSumAreSame(
                                entity.getParts().stream().filter(p -> p instanceof InvoicePartSql).map(InvoicePartSql.class::cast).toList(), document.getParts()) &&
                                Objects.equals(entity.getExchange(), document.getExchange());
                sameTaxTotal = samePartsAndExchange && Objects.equals(entity.getTaxTotal(), document.getTaxTotal());

                log.info(document.getDate() + " " + document.getNumber() + " samePartsAndExchange = " + samePartsAndExchange);

                if (BooleanUtils.isNotTrue(document.getFinished()) && document.getParts() != null) {
                    for (PartInvoiceDto part : document.getParts()) {
                        if (BooleanUtils.isTrue(part.getFinished())) {
                            document.setFinished(true);
                            break;
                        }
                        if (part.getParts() != null) {
                            for (PartInvoiceSubpartDto partPart : part.getParts()) {
                                if (BooleanUtils.isTrue(partPart.getFinished())) {
                                    document.setFinished(true);
                                    break;
                                }
                            }
                            if (BooleanUtils.isTrue(document.getFinished())) break;
                        }
                    }
                }

                // prevent TaxFree change - it can be done in other method
                document.setTaxFree(entity.getTaxFree());
            }

            // fix returning invoice parts in bundle quantities:
            if (document.getParts() != null) {
                for (PartInvoiceDto part : document.getParts()) {
                    if (BigDecimalUtils.isNegative(part.getQuantity()) && part.getParts() != null) {
                        for (PartInvoiceSubpartDto partPart : part.getParts()) {
                            if (BigDecimalUtils.isPositive(partPart.getQuantity())) {
                                partPart.setQuantity(BigDecimalUtils.negated(partPart.getQuantity()));
                            }
                        }
                    }
                }
            }

            inventoryCheckService.checkPartLinkUuids(document.getParts());
            InvoiceSql entity = invoiceSqlMapper.toEntity(document);

            InventoryUtils.assignSortOrder(entity.getParts());

            //refreshDocumentCounterpartySQL(entity);
            prepareSaveInvoiceSQL(sameTaxTotal, samePartsAndExchange, false, entity);

            DoubleEntryDto doubleEntryDto = entity.getDoubleEntry();

            // uuid need for generating printing form
            if (entity.getUuid() == null) entity.setUuid(UUID.randomUUID());

            entity = dbServiceSQL.saveWithCounter(entity);
            Hibernate.initialize(entity.getCounterparty());
            Hibernate.initialize(entity.getEmployee());

            DoubleEntrySql doubleEntry = null;
            try {
                doubleEntry = glOperationsService.finishInvoice(invoiceSqlMapper.toDto(entity), doubleEntryDto, false);
            } catch (Exception ignored) {
            }

            //  save used vatCode G.L. accounts if needed
            glOperationsService.updateVatCodeGLAccounts(entity.getVatCodeTotals());

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        InvoiceDto result = invoiceSqlMapper.toDto(pair.getDocument());

        // generate main printing form
        documentService.generatePrintForm(result);

        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public LastInvoicePriceResponse getLastInvoicePriceSQL(long counterpartyId, DBType counterpartyDb, long partId) {
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = entityManager.createNativeQuery("""
                                SELECT d.id AS invoiceId, d.date AS date, d.number AS number, 
                                    CAST(d.uuid AS text) AS uuid,
                                    p.doc_part ->> 'id' AS partId,
                                    CAST(p.doc_part -> 'price' -> 'amount' AS DECIMAL) AS amount,
                                    p.doc_part -> 'price' ->> 'currency' AS currency, p.discount AS discount
                                FROM documents d
                                LEFT JOIN invoice_parts p ON d.id = p.parent_id
                                WHERE d.company_id = :companyId
                                    AND (d.archive IS null OR d.archive = false)
                                    AND d.counterparty_id = :counterpartyId
                                    AND jsonb_path_exists(p.doc_part, '$.id ? (@ == $partId)',
                                        jsonb_build_object('partId', :partId))
                                ORDER BY d.date DESC, d.ordinal DESC, d.number DESC, d.id DESC
                                """,
                        Tuple.class)
                .setMaxResults(1)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("counterpartyId", counterpartyId)
                .setParameter("partId", partId)
                .getResultList();

        return CollectionsHelper.hasValue(tuples)
                ? new LastInvoicePriceResponse(
                tuples.get(0).get("invoiceId", BigInteger.class).longValue(),
                tuples.get(0).get("number", String.class),
                tuples.get(0).get("date", java.sql.Date.class).toLocalDate(),
                UUID.fromString(tuples.get(0).get("uuid", String.class)),
                Long.parseLong(tuples.get(0).get("partId", String.class)),
                GamaBigMoney.of(tuples.get(0).get("currency", String.class), tuples.get(0).get("amount", BigDecimal.class)),
                tuples.get(0).get("discount", Double.class))
                : null;
    }

    public String syncTask() {
        return taskQueueService.queueTask(new SyncTask(auth.getCompanyId()));
    }

    public TaskResponse<SyncResult> runSyncTask() {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkNotNull(companySettings.getSync(), "No Sync settings");
        SyncResult syncResult = null;
        if (BooleanUtils.isTrue(companySettings.getSync().getSyncActive())) {
            ISyncService syncService = syncTypeService.getSyncTypeService(companySettings.getSync().getType());
            if (syncService != null) {
                syncResult = syncService.sync(auth.getCompanyId());
            }
        }
        return TaskResponse.success(syncResult);
    }

    public String syncWarehousePartsTask() {
        CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        var syncSettings = Validators.checkNotNull(companySettings.getSync(), "No Sync settings");
        var syncWarehouseSettings = Validators.checkNotNull(syncSettings.getSyncWarehouse(), "No Sync Warehouse settings");
        Validators.checkNotNull(syncSettings.getWarehouseAbilities(), "No Sync Warehouse abilities settings");
        Validators.checkArgument(BooleanUtils.isTrue(syncSettings.getSyncActive()), "Sync is not active");

        ISyncWarehouseService syncWarehouseService = syncTypeService.getSyncWarehouseTypeService(syncWarehouseSettings.type());
        if (syncWarehouseService != null) {
            return syncWarehouseService.uploadProducts(auth.getCompanyId());
        }
        return "";
    }

    public String syncWarehouseInvoiceTask(long invoiceId) {
        CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        var syncSettings = Validators.checkNotNull(companySettings.getSync(), "No Sync settings");
        var syncWarehouseSettings = Validators.checkNotNull(syncSettings.getSyncWarehouse(), "No Sync Warehouse settings");
        Validators.checkNotNull(syncSettings.getWarehouseAbilities(), "No Sync Warehouse abilities settings");
        Validators.checkArgument(BooleanUtils.isTrue(syncSettings.getSyncActive()), "Sync is not active");

        ISyncWarehouseService syncWarehouseService = syncTypeService.getSyncWarehouseTypeService(syncWarehouseSettings.type());
        if (syncWarehouseService != null) {
            return syncWarehouseService.uploadOrder(auth.getCompanyId(), invoiceId);
        }
        return "";
    }

    private <E extends IBaseDocPartSql & IFinished>
    void currencyCorrectionSQL(List<E> parts, final GamaMoney baseSubtotal, final Exchange exchange) {
        if (CollectionsHelper.isEmpty(parts) || GamaMoneyUtils.isZero(baseSubtotal)) return;

        GamaMoney subtotal = null;
        for (E part : parts) {
            if (BooleanUtils.isNotTrue(part.getFinished())) {
                // clear warehouse - should use documents
                part.setWarehouse(null);
                part.setBaseTotal(exchange.exchange(part.getTotal()));
            }
            subtotal = GamaMoneyUtils.add(part.getBaseTotal(), subtotal);
        }
        if (GamaMoneyUtils.isEqual(subtotal, baseSubtotal)) return;

        // Very rare special case - if zero - assign all to first unfinished part
        if (GamaMoneyUtils.isZero(subtotal)) {
            for (E part : parts) {
                if (BooleanUtils.isNotTrue(part.getFinished())) {
                    part.setBaseTotal(GamaMoneyUtils.add(part.getBaseTotal(), baseSubtotal));
                    break;
                }
            }
            return;
        }

        GamaMoney delta = GamaMoneyUtils.subtract(baseSubtotal, subtotal);
        boolean minus = GamaMoneyUtils.isNegative(delta);
        double coefficient = 1.0 + delta.getAmount().doubleValue() / subtotal.getAmount().doubleValue();

        for (E part : parts) {
            if (BooleanUtils.isTrue(part.getFinished())) continue;

            if (GamaMoneyUtils.isZero(delta)) break;

            GamaMoney baseTotal = part.getBaseTotal();
            GamaMoney baseTotalNew = GamaMoneyUtils.isZero(baseTotal) ? baseTotal : baseTotal.multipliedBy(coefficient);
            if (GamaMoneyUtils.isEqual(baseTotal, baseTotalNew)) continue;

            GamaMoney partDelta = GamaMoneyUtils.subtract(baseTotalNew, baseTotal);
            if (minus && GamaMoneyUtils.isLessThanOrEqual(partDelta, delta) || !minus && GamaMoneyUtils.isGreaterThanOrEqual(partDelta, delta)) {
                part.setBaseTotal(GamaMoneyUtils.add(part.getBaseTotal(), delta));
                delta = null;
                break;

            } else {
                part.setBaseTotal(baseTotalNew);
                delta = GamaMoneyUtils.subtract(delta, partDelta);
            }
        }
        if (GamaMoneyUtils.isNonZero(delta)) {
            // the rest add to first unfinished
            for (E part : parts) {
                if (BooleanUtils.isNotTrue(part.getFinished())) {
                    parts.get(0).setBaseTotal(GamaMoneyUtils.add(parts.get(0).getBaseTotal(), delta));
                    break;
                }
            }
        }
    }

    /**
     * Recalculate and save expenses
     * @param document purchase document
     * @param settings company settings
     */
    private void expensesSQL(PurchaseSql document, CompanySettings settings) {
        //2015.12.16 !!! part cost will have a new value if no expenses or cost is zero
        //2015.12.16 part cost will have a recalculated value every time on save

        GamaMoney expensesSelfTotal = null;
        GamaMoney costTotal = null;
        if (document.getParts() != null) {
            for (PurchasePartSql part : document.getParts()) {
                if (part.getType() == PartType.SERVICE && part.isInCost()) {
                    part.setCostTotal(null);
                    expensesSelfTotal = GamaMoneyUtils.add(expensesSelfTotal, part.getBaseTotal());
                } else {
                    part.setCostTotal(part.getBaseTotal());
                }
                part.setExpense(null);
                costTotal = GamaMoneyUtils.add(costTotal, part.getCostTotal());
            }
        }

        // calculate 3'rd parties expenses
        GamaMoney expensesExtTotal = null;
        if (CollectionsHelper.hasValue(document.getExpenses())) {
            for (DocExpense expense : document.getExpenses()) {
                if ((expense.getExchange() == null && expense.getTotal() != null) ||
                        (expense.getExchange() != null && expense.getTotal() != null &&
                                !Objects.equals(expense.getExchange().getCurrency(),
                                        expense.getTotal().getCurrency()))) {
                    expense.setExchange(new Exchange(expense.getTotal().getCurrency()));
                }
                Exchange exchangeExp = Validators.checkNotNull(currencyService.currencyExchange(settings,
                        expense.getExchange(), document.getDate()), "No exchange");
                expense.setExchange(exchangeExp);
                // convert
                expense.setBaseTotal(exchangeExp.exchange(expense.getTotal()));
                expensesExtTotal = GamaMoneyUtils.add(expensesExtTotal, expense.getBaseTotal());
            }
        }

        // allocate expenses
        GamaMoney expensesTotal = GamaMoneyUtils.add(expensesSelfTotal, expensesExtTotal);
        if (CollectionsHelper.hasValue(document.getParts()) &&
                GamaMoneyUtils.isNonZero(expensesTotal) &&
                GamaMoneyUtils.isNonZero(document.getBaseSubtotal()) &&
                (!GamaMoneyUtils.isEqual(costTotal, GamaMoneyUtils.add(document.getBaseSubtotal(), expensesTotal)))) {

            GamaMoney baseTotal = null;
            for (PurchasePartSql part : document.getParts()) {
                if (part.getType() != PartType.SERVICE || !part.isInCost() && part.isAddExp()) {
                    baseTotal = GamaMoneyUtils.add(baseTotal, part.getBaseTotal());
                }
            }
            if (GamaMoneyUtils.isNonZero(baseTotal)) {

                double coefficientSelf = expensesSelfTotal == null ? 0 : expensesSelfTotal.getAmount().doubleValue() / baseTotal.getAmount().doubleValue();
                double coefficient3rd = expensesExtTotal == null ? 0 : expensesExtTotal.getAmount().doubleValue() / baseTotal.getAmount().doubleValue();

                GamaMoney calcCostTotal = null;
                GamaMoney calcCost3rdTotal = null;

                for (PurchasePartSql part : document.getParts()) {
                    //noinspection StatementWithEmptyBody
                    if (part.getType() == PartType.SERVICE && part.isInCost()) {
                        // do nothing

                    } else if (part.getType() == PartType.SERVICE && !part.isInCost() && !part.isAddExp()) {
                        calcCostTotal = GamaMoneyUtils.add(calcCostTotal, part.getCostTotal());

                    } else {
                        if (GamaMoneyUtils.isZero(part.getBaseTotal())) {
                            part.setCostTotal(null);
                            part.setExpense(null);

                        } else {
                            if (coefficient3rd != 0) {
                                part.setExpense(part.getBaseTotal().multipliedBy(coefficient3rd));
                                calcCost3rdTotal = GamaMoneyUtils.add(calcCost3rdTotal, part.getExpense());
                            }
                            part.setCostTotal(GamaMoneyUtils.total(
                                    part.getBaseTotal(),
                                    part.getBaseTotal().multipliedBy(coefficientSelf),
                                    part.getExpense()));
                            calcCostTotal = GamaMoneyUtils.add(calcCostTotal, part.getCostTotal());
                        }
                    }
                }

                calcCostTotal = GamaMoneyUtils.subtract(GamaMoneyUtils.subtract(calcCostTotal, expensesExtTotal), document.getBaseSubtotal());
                calcCost3rdTotal = GamaMoneyUtils.subtract(calcCost3rdTotal, expensesExtTotal);
                if (GamaMoneyUtils.isNonZero(calcCostTotal) || GamaMoneyUtils.isNonZero(calcCost3rdTotal)) {
                    for (int i = document.getParts().size() - 1; i >= 0; --i) {
                        PurchasePartSql part = document.getParts().get(i);
                        if (part.getType() != PartType.SERVICE || !part.isInCost() && part.isAddExp()) {
                            part.setCostTotal(GamaMoneyUtils.subtract(part.getCostTotal(), calcCostTotal));
                            part.setExpense(GamaMoneyUtils.subtract(part.getExpense(), calcCost3rdTotal));
                            calcCostTotal = null;
                            calcCost3rdTotal = null;
                            break;
                        }
                    }
                }

                if (GamaMoneyUtils.isNonZero(calcCostTotal)) {
                    throw new GamaException("Can't allocate cost " + calcCostTotal);
                }
                if (GamaMoneyUtils.isNonZero(calcCost3rdTotal)) {
                    throw new GamaException("Can't allocate 3rd parties expenses " + calcCost3rdTotal);
                }
            }
        }
    }

    public void prepareSavePurchaseSQL(PurchaseSql document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        // check if number not in use?
        checkDocNumberSQL(document);

        if ((document.getExchange() == null && document.getTotal() != null) ||
                (document.getExchange() != null && document.getTotal() != null &&
                        !Objects.equals(document.getExchange().getCurrency(),
                                document.getTotal().getCurrency()))) {
            document.setExchange(new Exchange(document.getTotal().getCurrency()));
            // new exchange data so need to reset parts cost and expense
            if (document.getParts() != null) {
                for (PurchasePartSql part : document.getParts()) {
                    part.setCostTotal(null);
                    part.setExpense(null);
                }
            }
        }
        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings,
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);

        if (document.getParts() != null) {
            for (PurchasePartSql part : document.getParts()) {
                // clear warehouse - will use from document
                part.setWarehouse(null);
            }
        }

        // BaseTotal <- total
        // BaseTaxTotal <- taxTotal
        // BaseSubtotal <- BaseTotal - BaseTaxTotal
        document.setBaseTotal(exchange.exchange(document.getTotal()));
        document.setBaseTaxTotal(exchange.exchange(document.getTaxTotal()));
        document.setBaseSubtotal(GamaMoneyUtils.subtract(document.getBaseTotal(), document.getBaseTaxTotal()));

        currencyCorrectionSQL(document.getParts(), document.getBaseSubtotal(), document.getExchange());
        expensesSQL(document, companySettings);

        inventoryCheckService.checkPartUuids(document.getParts(), false);

        if (document.getId() == null) {
            InventoryUtils.clearFinished(document.getParts());
            InventoryUtils.clearPartId(document.getParts());
        }

        // check debt type
        if (Validators.isValid(document.getCounterparty()) && document.getDebtType() == null) {
            document.setDebtType(DebtType.VENDOR);
        }
    }

    public PurchaseDto savePurchase(PurchaseDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        if (document.getId() == null) {
            CollectionsHelper.streamOf(document.getParts()).forEach(part -> {
                part.setRecordId(null);
                if (BigDecimalUtils.isPositiveOrZero(part.getQuantity())) part.setUuid(null);
            });
        }
        DocumentDoubleEntry<PurchaseSql> pair = dbServiceSQL.executeAndReturnInTransaction(em -> {
            if (document.getId() != null) {
                PurchaseSql entity = dbServiceSQL.getAndCheck(PurchaseSql.class, document.getId(), PurchaseSql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, document, auth.getLanguage());

                if (!BooleanUtils.isSame(entity.getFinished(), document.getFinished()) ||
                        !BooleanUtils.isSame(entity.getFinishedParts(), document.getFinishedParts()) ||
                        !BooleanUtils.isSame(entity.getFinishedDebt(), document.getFinishedDebt())) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentChangeStatus));
                }

                if (entity.isFullyFinished()) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready));
                }
                if (BooleanUtils.isNotTrue(document.getFinished()) && document.getParts() != null) {
                    // if document not marked as finished - check if there is at least one finished part
                    for (PartPurchaseDto part : document.getParts()) {
                        if (BooleanUtils.isTrue(part.getFinished())) {
                            document.setFinished(true);
                            break;
                        }
                    }
                }
            }

            PurchaseSql entity = purchaseSqlMapper.toEntity(document);

            InventoryUtils.assignSortOrder(entity.getParts());

            //refreshDocumentCounterpartySQL(entity);
            prepareSavePurchaseSQL(entity);

            deleteExtDocsArchivedSQL(entity);

            DoubleEntryDto doubleEntryDto = entity.getDoubleEntry();
            entity = dbServiceSQL.saveWithCounter(entity);
            Hibernate.initialize(entity.getCounterparty());

            DoubleEntrySql doubleEntry = null;
            try {
                doubleEntry = glOperationsService.finishPurchase(purchaseSqlMapper.toDto(entity), doubleEntryDto, false);
            } catch (Exception ignored) {
            }

            // save used vatCode G.L. accounts if needed
            glOperationsService.updateVatCodeGLAccounts(entity.getVatCodeTotals());

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        PurchaseDto result = purchaseSqlMapper.toDto(dbServiceSQL.getById(PurchaseSql.class, pair.getDocument().getId(), PurchaseSql.GRAPH_ALL));
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public String syncWarehousePurchaseTask(long purchaseId) {
        CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        var syncSettings = Validators.checkNotNull(companySettings.getSync(), "No Sync settings");
        var syncWarehouseSettings = Validators.checkNotNull(syncSettings.getSyncWarehouse(), "No Sync Warehouse settings");
        Validators.checkNotNull(syncSettings.getWarehouseAbilities(), "No Sync Warehouse abilities settings");
        Validators.checkArgument(BooleanUtils.isTrue(syncSettings.getSyncActive()), "Sync is not active");

        ISyncWarehouseService syncWarehouseService = syncTypeService.getSyncWarehouseTypeService(syncWarehouseSettings.type());
        if (syncWarehouseService != null) {
            return syncWarehouseService.uploadArrival(auth.getCompanyId(), purchaseId);
        }
        return "";
    }

    private void deleteExtDocsArchivedSQL(BaseDocumentSql doc) {
        if (doc == null || doc.getUrls() == null) return;
        doc.getUrls()
                .stream()
                .filter(x -> BooleanUtils.isTrue(x.getArchive()) && StringHelper.hasValue(x.getStorageFilename()))
                .forEach(x -> storageService.deleteFile(x.getStorageFilename()));
        doc.getUrls().removeIf(x -> BooleanUtils.isTrue(x.getArchive()));
    }

    public InvoiceDto updateInvoiceISAFSQL(InvoiceDto document) {
        Validators.checkNotNull(document.getId(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId));

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        dbServiceSQL.executeInTransaction(em -> {
            InvoiceSql entity = dbServiceSQL.getAndCheck(InvoiceSql.class, document.getId(), InvoiceSql.GRAPH_ALL);
            Validators.checkDocumentVersion(entity, document, auth.getLanguage());

            entity.setIsafInvoiceType(document.getIsafInvoiceType());
            entity.setIsafSpecialTaxation(document.isIsafSpecialTaxation());
            entity.setVatCodeTotals(document.getVatCodeTotals());
        });
        return documentService.getDocument(InvoiceSql.class, document.getId(), DBType.POSTGRESQL);
    }

    public PurchaseDto updatePurchaseISAFSQL(PurchaseDto document) {
        Validators.checkNotNull(document.getId(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId));

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        dbServiceSQL.executeInTransaction(em -> {
            PurchaseSql entity = dbServiceSQL.getAndCheck(PurchaseSql.class, document.getId(), PurchaseSql.GRAPH_ALL);
            Validators.checkDocumentVersion(entity, document, auth.getLanguage());

            entity.setIsafInvoiceType(document.getIsafInvoiceType());
            entity.setIsafSpecialTaxation(document.isIsafSpecialTaxation());
            entity.setRegDate(document.getRegDate());
            entity.setVatCodeTotals(document.getVatCodeTotals());
        });
        return documentService.getDocument(PurchaseSql.class, document.getId(), DBType.POSTGRESQL);
    }

    public InventoryOpeningBalanceDto saveOpeningBalance(InventoryOpeningBalanceDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());

        if (document.getId() == null) {
            CollectionsHelper.streamOf(document.getParts()).forEach(part -> {
                part.setRecordId(null);
                part.setUuid(null);
            });
        }
        return inventoryOpeningBalanceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            if (document.getId() != null) {
                InventoryOpeningBalanceSql entity = dbServiceSQL.getAndCheck(InventoryOpeningBalanceSql.class, document.getId(), InventoryOpeningBalanceSql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, document, auth.getLanguage());

                if (!BooleanUtils.isSame(entity.getFinished(), document.getFinished()) ||
                        !BooleanUtils.isSame(entity.getFinishedParts(), document.getFinishedParts())) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentChangeStatus));
                }

                if (entity.isFullyFinished()) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready));
                }
                if (BooleanUtils.isNotTrue(document.getFinished()) && document.getParts() != null) {
                    // if document not marked as finished - check if there is at least one finished part
                    for (PartOpeningBalanceDto part : document.getParts()) {
                        if (BooleanUtils.isTrue(part.getFinished())) {
                            document.setFinished(true);
                            break;
                        }
                    }
                }
            }

            if (document.getParts() != null) {
                for (PartOpeningBalanceDto part : document.getParts()) {
                    // clear warehouse - should use documents
                    part.setWarehouse(null);

                    if ((part.getExchange() == null && part.getTotal() != null) ||
                            (part.getExchange() != null && part.getTotal() != null &&
                                    !Objects.equals(part.getExchange().getCurrency(),
                                            part.getTotal().getCurrency()))) {
                        part.setExchange(new Exchange(part.getTotal().getCurrency()));
                    }
                    Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings,
                            part.getExchange(), document.getDate()), "No exchange");
                    part.setExchange(exchange);

                    if (exchange.getBase().equals(exchange.getCurrency())) {
                        part.setBaseTotal(part.getTotal());
                    } else {
                        if (GamaMoneyUtils.isNonZero(part.getTotal())) {
                            part.setBaseTotal(exchange.exchange(part.getTotal()));
                        } else {
                            part.setBaseTotal(null);
                        }
                    }

                    part.setCostTotal(part.getBaseTotal());
                }
            }

            InventoryOpeningBalanceSql entity = inventoryOpeningBalanceSqlMapper.toEntity(document);

            inventoryCheckService.checkPartUuids(entity.getParts(), false);

            InventoryUtils.assignSortOrder(entity.getParts());
            if (entity.getId() == null) {
                InventoryUtils.clearPartId(entity.getParts());
            }

            return dbServiceSQL.saveWithCounter(entity);
        }));
    }

    public TransProdDto saveTransProd(TransProdDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseTo(), "warehouse to/production", document.toString(), auth.getLanguage());

        if (document.getId() == null) {
            CollectionsHelper.streamOf(document.getPartsFrom()).forEach(part -> {
                part.setRecordId(null);
                part.setUuid(null);
            });
            CollectionsHelper.streamOf(document.getPartsTo()).forEach(part -> {
                part.setRecordId(null);
                part.setUuid(null);
            });
        }
        DocumentDoubleEntry<TransProdSql> pair = dbServiceSQL.executeAndReturnInTransaction(em -> {
            if (document.getId() != null) {
                TransProdSql entity = dbServiceSQL.getAndCheck(TransProdSql.class, document.getId(), TransProdSql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, document, auth.getLanguage());

                if (!BooleanUtils.isSame(entity.getFinished(), document.getFinished()) ||
                        !BooleanUtils.isSame(entity.getFinishedPartsFrom(), document.getFinishedPartsFrom()) ||
                        !BooleanUtils.isSame(entity.getFinishedPartsTo(), document.getFinishedPartsTo())) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentChangeStatus));
                }

                if (entity.isFullyFinished()) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready));
                }
                if (BooleanUtils.isNotTrue(document.getFinished()) && document.getPartsFrom() != null) {
                    // if document not marked as finished - check if there is at least one finished part
                    for (PartFromDto part : document.getPartsFrom()) {
                        if (BooleanUtils.isTrue(part.getFinished())) {
                            document.setFinished(true);
                            break;
                        }
                    }
                }
                if (BooleanUtils.isNotTrue(document.getFinished()) && document.getPartsTo() != null) {
                    // if document not marked as finished - check if there is at least one finished part
                    for (PartToDto part : document.getPartsTo()) {
                        if (BooleanUtils.isTrue(part.getFinished())) {
                            document.setFinished(true);
                            break;
                        }
                    }
                }
            }

            // check if sum of cost percent is 100
            if (CollectionsHelper.hasValue(document.getPartsTo())) {
                if (document.getPartsTo().size() == 1)
                    document.getPartsTo().get(0).setCostPercent(BigDecimal.valueOf(100));
                else {
                    BigDecimal percent = null;
                    for (PartToDto part : document.getPartsTo()) {
                        percent = BigDecimalUtils.add(percent, part.getCostPercent());
                    }
                    BigDecimal delta = BigDecimalUtils.subtract(BigDecimal.valueOf(100), percent);
                    if (!BigDecimalUtils.isZero(delta)) {
                        BigDecimal deltaPercent = delta.divide(BigDecimal.valueOf(document.getPartsTo().size()), 2, RoundingMode.HALF_UP);
                        percent = null;
                        for (PartToDto part : document.getPartsTo()) {
                            part.setCostPercent(BigDecimalUtils.add(part.getCostPercent(), deltaPercent));
                            percent = BigDecimalUtils.add(percent, part.getCostPercent());
                        }
                        delta = BigDecimalUtils.subtract(BigDecimal.valueOf(100), percent);
                        if (!BigDecimalUtils.isZero(delta)) {
                            PartToDto part = document.getPartsTo().get(document.getPartsTo().size() - 1);
                            part.setCostPercent(BigDecimalUtils.add(part.getCostPercent(), delta));
                        }
                    }
                }
            }

            TransProdSql entity = transportationSqlMapper.toEntity(document);

            inventoryCheckService.checkPartUuids(entity.getPartsFrom(), false);
            inventoryCheckService.checkPartUuids(entity.getPartsTo(), false);

            InventoryUtils.assignSortOrder(entity.getPartsFrom());
            InventoryUtils.assignSortOrder(entity.getPartsTo());
            if (entity.getId() == null) {
                InventoryUtils.clearPartId(entity.getPartsFrom());
                InventoryUtils.clearPartId(entity.getPartsTo());
            }

            DoubleEntryDto doubleEntryDto = entity.getDoubleEntry();

            // uuid need for generating printing form
            if (entity.getUuid() == null) entity.setUuid(UUID.randomUUID());

            entity = dbServiceSQL.saveWithCounter(entity);

            DoubleEntrySql doubleEntry = glOperationsService.finishTransProd(transportationSqlMapper.toDto(entity), doubleEntryDto, false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        TransProdDto result = transportationSqlMapper.toDto(pair.getDocument());

        documentService.generatePrintForm(result);

        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }


    public InventoryDto saveInventory(InventoryDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        if (document.getId() == null) {
            CollectionsHelper.streamOf(document.getParts()).forEach(part -> {
                part.setRecordId(null);
                part.setUuid(null);
            });
        }
        DocumentDoubleEntry<InventorySql> pair = dbServiceSQL.executeAndReturnInTransaction(em -> {
            if (document.getId() != null) {
                InventorySql entity = dbServiceSQL.getAndCheck(InventorySql.class, document.getId(), InventorySql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, document, auth.getLanguage());

                if (!BooleanUtils.isSame(entity.getFinished(), document.getFinished()) ||
                        !BooleanUtils.isSame(entity.getFinishedParts(), document.getFinishedParts())) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentChangeStatus));
                }

                if (entity.isFullyFinished()) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready));
                }
                if (BooleanUtils.isNotTrue(document.getFinished()) && document.getParts() != null) {
                    // if document not marked as finished - check if there is at least one finished part
                    for (PartInventoryDto part : document.getParts()) {
                        if (BooleanUtils.isTrue(part.getFinished())) {
                            document.setFinished(true);
                            break;
                        }
                    }
                }
            }

            InventorySql entity = inventorySqlMapper.toEntity(document);

            inventoryCheckService.checkPartUuids(entity.getParts(), false);

            InventoryUtils.setPartsWarehouseSQL(entity.getWarehouse(), entity.getTag(), entity.getParts());
            InventoryUtils.assignSortOrder(entity.getParts());
            if (entity.getId() == null) {
                InventoryUtils.clearPartId(entity.getParts());
            }

            DoubleEntryDto doubleEntryDto = entity.getDoubleEntry();

            entity = dbServiceSQL.saveWithCounter(entity);

            DoubleEntrySql doubleEntry = glOperationsService.finishInventory(inventorySqlMapper.toDto(entity), doubleEntryDto, false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        InventoryDto result = inventorySqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));

        return result;
    }

    public EstimateDto saveEstimate(EstimateDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        inventoryCheckService.checkPartUuids(document.getParts(), false);
        setPartsVAT(companySettings.getCountry(), document.getDate(), document.getParts());

        if (document.isZeroVAT()) {
            document.setTotal(document.getSubtotal());
            document.setTaxTotal(document.getTotal().withAmount(0));
        }

        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings,
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);

        if (document.getId() == null) {
            CollectionsHelper.streamOf(document.getParts()).forEach(part -> {
                part.setRecordId(null);
                part.setUuid(null);
                part.setLinkUuid(null);
                CollectionsHelper.streamOf(part.getParts()).forEach(partPart -> {
                    partPart.setRecordId(null);
                    partPart.setUuid(null);
                    partPart.setParentLinkUuid(null);
                });
            });
        }
        var saved = dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            inventoryCheckService.checkPartLinkUuids(document.getParts());
            EstimateSql entity = estimateSqlMapper.toEntity(document);

            InventoryUtils.assignSortOrder(entity.getParts());
            if (entity.getId() == null) {
                InventoryUtils.clearPartId(entity.getParts());
            }

            deleteExtDocsArchivedSQL(entity);

            entity = dbServiceSQL.saveWithCounter(entity);
            Hibernate.initialize(entity.getCounterparty());
            Hibernate.initialize(entity.getEmployee());

            return entity;
        });

        var result = estimateSqlMapper.toDto(dbServiceSQL.getById(EstimateSql.class, saved.getId(), EstimateSql.GRAPH_ALL));

        // generate printing form
        documentService.generatePrintForm(result);

        return result;
    }

    public OrderDto saveOrder(OrderDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        setPartsVAT(companySettings.getCountry(), document.getDate(), document.getParts());

        if (document.isZeroVAT()) {
            document.setTotal(document.getSubtotal());
            document.setTaxTotal(document.getTotal().withAmount(0));
            document.setCurrentTotal(document.getCurrentSubtotal());
            document.setCurrentTaxTotal(document.getTotal().withAmount(0));
        }

        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings,
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);

        calculateOrder(document);

        if (document.getId() == null) {
            CollectionsHelper.streamOf(document.getParts()).forEach(part -> {
                part.setRecordId(null);
                part.setUuid(null);
            });
        }
        var saved = dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            OrderSql entity = orderSqlMapper.toEntity(document);
            inventoryCheckService.checkPartUuids(entity.getParts(), false);

            InventoryUtils.assignSortOrder(entity.getParts());
            if (entity.getId() == null) {
                InventoryUtils.clearPartId(entity.getParts());
            }

            return dbServiceSQL.saveWithCounter(entity);
        });
        return orderSqlMapper.toDto(dbServiceSQL.getById(OrderSql.class, saved.getId(), OrderSql.GRAPH_ALL));
    }

    public InvoiceDto finishInvoice(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<InvoiceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            InvoiceSql document = dbServiceSQL.getAndCheck(InvoiceSql.class, id, InvoiceSql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            Validators.checkValid(document.getCounterparty(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCustomer, auth.getLanguage()));
            return documentService.finishSQL(document, finishGL);
        });
        InvoiceDto result = invoiceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public PurchaseDto finishPurchase(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<PurchaseSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            PurchaseSql document = dbServiceSQL.getAndCheck(PurchaseSql.class, id, PurchaseSql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            Validators.checkValid(document.getCounterparty(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoVendor, auth.getLanguage()));
            return documentService.finishSQL(document, finishGL);
        });
        PurchaseDto result = purchaseSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public String finishOpeningBalanceTask(long id) {
        return taskQueueService.queueTask(new FinishInventoryOpeningBalanceTask(auth.getCompanyId(), id));
    }

    public TaskResponse<Void> runOpeningBalanceTask(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        InventoryOpeningBalanceSql result = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            InventoryOpeningBalanceSql document = dbServiceSQL.getAndCheck(InventoryOpeningBalanceSql.class, id, InventoryOpeningBalanceSql.GRAPH_ALL);
            Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());
            return documentService.finishSQL(document);
        });
        return result != null && result.isFullyFinished() ? TaskResponse.success() : TaskResponse.error("OpeningBalanceSql not fully finished");
    }

    public TransProdDto reserveTransProd(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            TransProdSql document = dbServiceSQL.getAndCheck(TransProdSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return transportationSqlMapper.toDto(documentService.reserveSQL(document));
        });
    }

    public TransProdDto finishTransProd(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<TransProdSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            TransProdSql document = dbServiceSQL.getAndCheck(TransProdSql.class, id, TransProdSql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finishSQL(document, finishGL);
        });
        TransProdDto result = transportationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public String finishInventoryTask(long id, Boolean finishGL) {
        return taskQueueService.queueTask(new FinishInventoryTask(auth.getCompanyId(), id, finishGL));
    }

    public TaskResponse<Void> runFinishInventoryTask(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<InventorySql> result = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            InventorySql document = dbServiceSQL.getAndCheck(InventorySql.class, id, InventorySql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finishSQL(document, finishGL);
        });
        return result != null && result.getDocument() != null && result.getDocument().isFullyFinished() ? TaskResponse.success() : TaskResponse.error("InventorySql not fully finished");
    }

    public EstimateDto finishEstimate(long id) {
        return estimateSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            EstimateSql document = dbServiceSQL.getAndCheck(EstimateSql.class, id, EstimateSql.GRAPH_ALL);
            document.setFinished(BooleanUtils.isNotTrue(document.getFinished()));
            inventoryCheckService.checkPartUuids(document.getParts(), false);
            inventoryCheckService.checkPartLinkUuidsEntity(document.getParts(), EstimatePartSql::new);
            return dbServiceSQL.saveEntityInCompany(document);
        }));
    }

    public OrderDto finishOrder(long id) {
        return orderSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            OrderSql document = dbServiceSQL.getAndCheck(OrderSql.class, id, OrderSql.GRAPH_ALL);
            document.setFinished(BooleanUtils.isNotTrue(document.getFinished()));
            inventoryCheckService.checkPartUuids(document.getParts(), false);
            return dbServiceSQL.saveEntityInCompany(document);
        }));
    }

    public InvoiceDto recallInvoice(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<InvoiceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            InvoiceSql document = dbServiceSQL.getAndCheck(InvoiceSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recallSQL(document);
        });
        InvoiceDto result = invoiceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public PurchaseDto recallPurchase(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<PurchaseSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            PurchaseSql document = dbServiceSQL.getAndCheck(PurchaseSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recallSQL(document);
        });
        PurchaseDto result = purchaseSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public TransProdDto recallTransProd(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<TransProdSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            TransProdSql document = dbServiceSQL.getAndCheck(TransProdSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recallSQL(document);
        });
        TransProdDto result = transportationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public TransProdDto recallReserveTransProd(long id) {
        return dbServiceSQL.executeAndReturnInTransaction(em -> {
            TransProdSql document = dbServiceSQL.getAndCheck(TransProdSql.class, id);
            return transportationSqlMapper.toDto(documentService.recallReserveSQL(document));
        });
    }

    public String recallInventoryTask(long id, boolean noTransaction) {
        return taskQueueService.queueTask(new RecallInventoryTask(auth.getCompanyId(), id, noTransaction));
    }

    public TaskResponse<Void> runRecallInventoryTask(long id, boolean noTransaction) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<InventorySql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            InventorySql document = dbServiceSQL.getAndCheck(InventorySql.class, id, InventorySql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recallSQL(document);
        });
        return pair != null && pair.getDocument() != null && BooleanUtils.isNotTrue(pair.getDocument().getFinished()) ? TaskResponse.success() : TaskResponse.error("Inventory is not finished yet");
    }

    public InventoryOpeningBalanceDto importOpeningBalance(long id, String fileName) {
        InventoryOpeningBalanceDto entity = inventoryOpeningBalanceSqlMapper.toDto(dbServiceSQL.getAndCheck(InventoryOpeningBalanceSql.class, id));
        if (BooleanUtils.isTrue(entity.getFinished())) return entity;

        return storageService.importInventoryOpeningBalanceSQL(entity, fileName);
    }

    public TransProdDto importTransProd(long id, String fileName) {
        TransProdSql entity = dbServiceSQL.getAndCheck(TransProdSql.class, id);
        if (BooleanUtils.isTrue(entity.getFinished())) return transportationSqlMapper.toDto(entity);

        return storageService.importTransProdSQL(transportationSqlMapper.toDto(entity), fileName);
    }

    public InventoryDto importInventory(long id, String fileName) {
        InventorySql entity = dbServiceSQL.getAndCheck(InventorySql.class, id);
        if (BooleanUtils.isTrue(entity.getFinished())) return inventorySqlMapper.toDto(entity);

        return storageService.importInventorySQL(inventorySqlMapper.toDto(entity), fileName);
    }

    public List<PartPurchaseDto> importPurchase(String fileName, DataFormatType format, String currency) {
        return storageService.importPurchase(fileName, format, currency);
    }

    public PageResponse<RepInvoice, Void> reportInvoiceSQL(PageRequest request) {
        LocalDate dateFrom = Validators.checkNotNull(request.getDateFrom(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateFrom));
        LocalDate dateTo = Validators.checkNotNull(request.getDateTo(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateTo));

        List<String> months = new ArrayList<>();
        StringBuilder sums = new StringBuilder();
        for (LocalDate date = dateFrom.withDayOfMonth(1); !date.isAfter(dateTo); date = date.plusMonths(1)) {
            String alias = String.format("m_%1$d_%2$d", date.getYear(), date.getMonthValue());
            months.add(alias);
            sums.append(String.format(", SUM (CASE WHEN (y = %1$d AND m = %2$d) THEN S.amount ELSE 0 END) AS ",
                    date.getYear(), date.getMonthValue())).append(alias);
        }

        String sql = "WITH S(id, name, y, m, currency, amount) AS (" +
                    " SELECT c.id AS id, c.name AS name," +
                        " EXTRACT('Year' FROM d.date) AS y," +
                        " EXTRACT('Month' FROM d.date) AS m," +
                        " i.base_total_currency AS currency," +
                        " SUM(i.base_total_amount) AS amount" +
                    " FROM invoice i" +
                    " JOIN documents d ON i.id = d.id" +
                    " JOIN counterparties c ON c.id = d.counterparty_id" +
                    " WHERE d.company_id = :companyId" +
                        " AND (d.archive IS null OR d.archive = false)" +
                        " AND (d.finished = true)" +
                        " AND d.date >= :dateFrom" +
                        " AND d.date < :dateTo" +
                    " GROUP BY name, c.id, y, m, currency" +
                " )" +
                " SELECT S.id AS id, S.name AS name, S.currency AS currency, SUM(S.amount) AS total" + sums +
                " FROM S" +
                " GROUP BY S.name, S.id, S.currency" +
                " ORDER BY lower(unaccent(S.name)), S.id, S.currency";
        log.info(this.getClass().getSimpleName() + ": sql=" + sql);

        @SuppressWarnings("unchecked")
        List<Tuple> results = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo.plusDays(1))
                .getResultList();

        PageResponse<RepInvoice, Void> response = new PageResponse<>();
        List<RepInvoice> report = new ArrayList<>();

        for (Tuple o : results) {
            RepInvoice record = new RepInvoice();
            record.setId(o.get("id", BigInteger.class).longValue());
            record.setName(o.get("name", String.class));
            record.setCurrency(o.get("currency", String.class));
            record.setTotal(o.get("total", BigDecimal.class));

            Map<String, BigDecimal> totalsByMonth = months.stream()
                    .collect(Collectors.toMap(Function.identity(), m -> {
                        var amount = o.get(m, BigDecimal.class);
                        return amount == null ? BigDecimal.ZERO : amount;
                    }));
            record.setTotalsByMonth(totalsByMonth);
            report.add(record);
        }
        response.setItems(report);

        return response;
    }

    private <T extends IVatRate & IId<Long>> void setPartsVAT(String country, LocalDate date, List<T> parts) {
        if (CollectionsHelper.isEmpty(parts)) return;

        CountryVatRateSql countryVatRate = dbServiceSQL.getById(CountryVatRateSql.class, country);
        if (countryVatRate == null) return;
        VATRatesDate vatRatesDate = countryVatRate.getRatesMap(date);
        if (vatRatesDate == null || CollectionsHelper.isEmpty(vatRatesDate.getRates())) return;

        Set<Long> ids = parts.stream()
                .map(p -> p instanceof InvoiceBasePartSql e ? e.getPartId() : null)
                .collect(Collectors.toSet());

        List<Tuple> result = entityManager.createQuery(
                        "SELECT id AS id, vatRateCode AS vatRateCode" +
                                " FROM " + PartSql.class.getName() + " p" +
                                " WHERE id IN :ids" +
                                " AND companyId = :companyId" +
                                " AND (p.archive IS null OR p.archive = false)" +
                                " AND (p.hidden IS null OR p.hidden = false)", Tuple.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("ids", ids)
                .getResultList();

        Map<Long, String> map = new HashMap<>();
        result.forEach(t -> map.put(t.get("id", Long.class), t.get("vatRateCode", String.class)));

        for (T docPart : parts) {
            Long id = docPart instanceof InvoiceBasePartSql e ? e.getPartId() : docPart.getId();
            if (docPart.getVat() == null && id != null) {
                String vatRateCode = map.get(id);
                if (StringHelper.hasValue(vatRateCode)) {
                    docPart.setVat(vatRatesDate.getRatesMap().get(vatRateCode));
                }
            }
        }
    }

    public PurchaseSql fixClearPurchaseInventoryFinishedMark(long docId) {
        return dbServiceSQL.executeAndReturnInTransaction(em -> {
            PurchaseSql purchase = dbServiceSQL.getById(PurchaseSql.class, docId);
            if (purchase != null) {
                if (purchase.getParts() != null) {
                    purchase.getParts().forEach(p -> p.setFinished(null));
                }
                purchase.setFinished(null);
                purchase.setFinishedParts(null);
            }
            return purchase;
        });
    }

    public CheckVatResponse checkVat(CheckVatRequest request) {
        Validators.checkArgument(StringHelper.hasValue(request.getCountryCode()), "No Country Code");
        Validators.checkArgument(StringHelper.hasValue(request.getVatNumber()), "No VAT Number");

        return euCheckVatService.checkVat(request);
    }

    public String createInvoicesTask(int count, LocalDate dateFrom, LocalDate dateTo) {
        return taskQueueService.queueTask(new CreateInvoicesTask(auth.getCompanyId(), count, dateFrom, dateTo));
    }

    public TaskResponse<Void> runCreateInvoicesTask(int count, LocalDate dateFrom, LocalDate dateTo) {
        try {
            CompanySettings companySettings = Validators.checkNotNull(dbServiceSQL.getCompanySettings(auth.getCompanyId()),
                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.DB.NoCompanySettings), auth.getCompanyId()));
            CountryVatRateSql countryVatRate = dbServiceSQL.getById(CountryVatRateSql.class, companySettings.getCountry());

            // take first 10 customers
            final int COUNTERPARTIES = 10;
            List<CounterpartySql> counterparties = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                    entityManager.createQuery(
                                    "SELECT a FROM " + CounterpartySql.class.getName() + " a" +
                                            " WHERE companyId = :companyId" +
                                            " AND archive IS NOT true",
                                    CounterpartySql.class)
                            .setParameter("companyId", auth.getCompanyId())
                            .setMaxResults(COUNTERPARTIES)
                            .getResultList());

            // take first 10 parts
            final int PARTS = 10;
            List<PartSql> parts = dbServiceSQL.makeQueryInCompany(PartSql.class, null).setMaxResults(PARTS).getResultList();

            final int WAREHOUSES = 2;
            List<WarehouseSql> warehouses = dbServiceSQL.makeQueryInCompany(WarehouseSql.class, null).setMaxResults(WAREHOUSES).getResultList();

            Random random = new Random();

            final int INVOICE_MAX_PARTS = 5;
            final int INVOICE_PART_MAX_QUANTITY = 10;
            final int INVOICE_PART_MAX_PRICE = 1000_00; // max price in cents
            final int INVOICE_MAX_DUE_DAYS = 15;
            final String INVOICE_SERIES = "TEST" + (random.nextInt(900) + 100);

            AtomicInteger number = new AtomicInteger(1);
            int days = Period.between(dateFrom, dateTo).getDays() + 1;
            Stream.generate(() -> dateFrom.plusDays(random.nextInt(days)))
                    .limit(count)
                    .sorted(LocalDate::compareTo)
                    .forEach(date -> {
                        InvoiceSql invoice = new InvoiceSql();
                        invoice.setCompanyId(auth.getCompanyId());
                        invoice.setDate(date);
                        invoice.setDueDate(date.plusDays(random.nextInt(INVOICE_MAX_DUE_DAYS)));
                        invoice.setSeries(INVOICE_SERIES);
                        invoice.setOrdinal(number.longValue());
                        invoice.setNumber(INVOICE_SERIES + " " + number.getAndIncrement());
                        invoice.setWarehouse(warehouses.get(random.nextInt(WAREHOUSES)));
                        invoice.setCounterparty(counterparties.get(random.nextInt(COUNTERPARTIES)));
                        invoice.setParts(Stream.generate(() -> {
                                    PartSql part = parts.get(random.nextInt(PARTS));
                                    DocPart docPart = new DocPart(part);
                                    InvoicePartSql invoicePart = new InvoicePartSql();
                                    invoicePart.setDocPart(docPart);
                                    invoicePart.setPart(part);
                                    invoicePart.setQuantity(BigDecimal.valueOf(random.nextLong(INVOICE_PART_MAX_QUANTITY) + 1));
                                    invoicePart.setPrice(GamaBigMoney.of("EUR",
                                            BigDecimal.valueOf(random.nextLong(INVOICE_PART_MAX_PRICE - 1) + 1, 2)));
                                    invoicePart.setTaxable(part.isTaxable());
                                    invoicePart.setUuid(UUID.randomUUID());
                                    invoicePart.setParent(invoice);
                                    VATRatesDate vatRatesDate = countryVatRate.getRatesMap(date);
                                    if (StringHelper.hasValue(part.getVatRateCode())) {
                                        invoicePart.setVat(vatRatesDate.getRatesMap().get(part.getVatRateCode()));
                                        invoicePart.setVatRateCode(part.getVatRateCode());
                                    }
                                    return invoicePart;
                                })
                                .limit(1 + random.nextInt(INVOICE_MAX_PARTS - 1))
                                .collect(Collectors.toList()));
                        invoice.setExchange(new Exchange("EUR", date));

                        calculateInvoiceSQL(invoice, CollectionsHelper.streamOf(invoice.getParts())
                                .filter(b -> b instanceof InvoicePartSql)
                                .map(InvoicePartSql.class::cast)
                                .toList());
                        dbServiceSQL.saveEntityInCompany(invoice);
                    });

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return TaskResponse.error(e.getMessage());
        }
        return TaskResponse.success();
    }
}
