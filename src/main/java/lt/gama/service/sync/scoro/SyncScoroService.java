package lt.gama.service.sync.scoro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.sql.documents.items.PurchasePartSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.ImportSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.sql.system.SyncSql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CounterDesc;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.CounterService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TradeService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.sync.i.ISyncScoroService;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.SyncResult;
import lt.gama.service.sync.scoro.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;


/**
 * gama-online
 * Created by valdas on 2017-10-03.
 */
public class SyncScoroService implements ISyncScoroService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @PersistenceContext
    private EntityManager entityManager;

    private final TradeService tradeService;
    private final CounterService counterService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final SyncHttpService syncHttpService;
    private final ObjectMapper objectMapper;
    

    private static final String ENTITY_DEPOT = "depot";
    private static final String ENTITY_EXPENSES = "expenses";
    private static final String ENTITY_CONTACTS = "contacts";
    private static final String ENTITY_PRODUCTS = "products";
    private static final String ENTITY_VATCODES = "vatCodes";
    private static final String ENTITY_INVOICES = "invoices";

    SyncScoroService(TradeService tradeService,
                     CounterService counterService,
                     Auth auth,
                     DBServiceSQL dbServiceSQL,
                     AuthSettingsCacheService authSettingsCacheService,
                     SyncHttpService syncHttpService, 
                     ObjectMapper objectMapper) {
        this.tradeService = tradeService;
        this.counterService = counterService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.authSettingsCacheService = authSettingsCacheService;
        this.syncHttpService = syncHttpService;
        this.objectMapper = objectMapper;
    }

    /**
     * Sync documents from date until yesterday
     * @param companyId company Id
     */
    @Override
    public SyncResult sync(long companyId) {
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));
        try {
            dbServiceSQL.executeInTransaction(entityManager -> {
                SyncSql sync = dbServiceSQL.getById(SyncSql.class, companyId);
                if (sync == null) return;

                CompanySql company = dbServiceSQL.getById(CompanySql.class, companyId);
                if (company == null) return;

                if (company.getStatus() == null || company.getStatus() == CompanyStatusType.INACTIVE) {
                    log.info(this.getClass().getSimpleName() + ": companyId=" + companyId + " status: null or INACTIVE");
                    return;
                }

                CompanySettings companySettings = company.getSettings();
                if (companySettings == null) {
                    log.error(this.getClass().getSimpleName() + ": companyId=" + companyId + " has no settings");
                    return;
                }

                LocalDateTime dateTo = DateUtils.now(companySettings.getTimeZone());

                syncWarehouseSQL(companySettings, sync.getDate(), dateTo);

                // check settings before Purchases and Invoices sync
                Validators.checkValid(companySettings.getWarehouse(), "No Default Warehouse");

                if (!companySettings.isDisableGL()) {
                    Validators.checkNotNull(companySettings.getGl(), "No G.L. Settings");

                    Validators.checkValid(companySettings.getGl().getProductAsset(), "No Product Assets G.L. Account");
                    Validators.checkValid(companySettings.getGl().getProductExpense(), "No Product Expense G.L. Account");
                    Validators.checkValid(companySettings.getGl().getProductIncome(), "No Product Income G.L. Account");

                    Validators.checkValid(companySettings.getGl().getServiceExpense(), "No Service Expense G.L. Account");
                    Validators.checkValid(companySettings.getGl().getServiceIncome(), "No Service Income G.L. Account");

                    Validators.checkValid(companySettings.getGl().getCounterpartyVendor(), "No Vendor G.L. Account");
                    Validators.checkValid(companySettings.getGl().getCounterpartyCustomer(), "No Customer G.L. Account");
                }

                // Prepare Vat rate codes
                Map<Long, VATRate> vatRateMap = getMapVatRate(companySettings);
                Map<String, ImportSql> importMap = new HashMap<>();

                syncPurchasesSQL(companyId, companySettings, sync.getDate(), dateTo, vatRateMap, importMap);
                syncInvoicesSQL(companyId, companySettings, sync.getDate(), dateTo, vatRateMap, importMap);

                sync.setDate(dateTo);
                dbServiceSQL.saveEntity(sync);
            });

        } catch (NullPointerException | IllegalArgumentException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    private Map<Long, VATRate> getMapVatRate(CompanySettings companySettings) {
        List<ScoroVatCodes> vatCodes = getVatCodes(companySettings);
        List<VATRate> vatRates = getVatRates(companySettings);

        Map<Long, VATRate> map = new HashMap<>();
        if (vatCodes == null || vatCodes.size() == 0 || vatRates == null || vatRates.size() == 0) return map;

        for (ScoroVatCodes vatCode : vatCodes) {
            for (VATRate vatRate : vatRates) {
                if (Math.abs((vatRate.getRate() == null ? 0.0 : vatRate.getRate()) - (vatCode.getPercent() == null ? 0.0 : vatCode.getPercent().doubleValue())) < 0.001) {
                    map.put(vatCode.getVatCodeId(), vatRate);
                    break;
                }
            }
        }

        return map;
    }

    private List<VATRate> getVatRates(CompanySettings companySettings) {
        CountryVatRateSql countryVatRate = dbServiceSQL.getById(CountryVatRateSql.class, companySettings.getCountry());
        if (countryVatRate != null) {
            VATRatesDate vatRatesDate = countryVatRate.getRatesMap(DateUtils.date());
            if (vatRatesDate != null && vatRatesDate.getRates() != null && vatRatesDate.getRates().size() > 0) {
                return vatRatesDate.getRates();
            }
        }
        return null;
    }

    private List<ScoroVatCodes> getVatCodes(CompanySettings companySettings) {
        ScoroResponseVatCodesList responseList = getList(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                companySettings.getSync().getId(), companySettings.getLanguage(),
                ENTITY_VATCODES, ScoroResponseVatCodesList.class, 100, 1, null, null);

        return responseList == null ? null : responseList.getData();
    }

    private void syncWarehouseSQL(CompanySettings companySettings, LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (companySettings == null || companySettings.getSync() == null || companySettings.getSync().getType() == null) return;

        final int pageSize = 100;
        int page = 0;
        int imported = 0;
        int created = 0;
        int updated = 0;
        ScoroResponseDepotList responseList;
        do {
            page++;

            responseList = getList(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                    companySettings.getSync().getId(), companySettings.getLanguage(),
                    ENTITY_DEPOT, ScoroResponseDepotList.class, pageSize, page, dateFrom, dateTo);

            if (responseList == null || responseList.getData() == null) break;

            for (ScoroDepot item : responseList.getData()) {
                imported++;
                ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(auth.getCompanyId(), WarehouseDto.class, String.valueOf(item.getId())));

                WarehouseSql warehouseSql;
                if (imp == null) {
                    created++;
                    warehouseSql = new WarehouseSql();
                } else {
                    warehouseSql = dbServiceSQL.getById(WarehouseSql.class, imp.getEntityId());
                    if (warehouseSql != null) {
                        updated++;
                    } else {
                        created++;
                        warehouseSql = new WarehouseSql();
                        int deleted = dbServiceSQL.removeById(ImportSql.class, imp.getId());
                        if (deleted != 0) {
                            imp = null;
                        } else {
                            throw new GamaException("Import record was not deleted");
                        }
                    }
                }
                warehouseSql.setExportId(String.valueOf(item.getId()));
                warehouseSql.setName(item.getName());
                warehouseSql = dbServiceSQL.saveEntityInCompany(warehouseSql);

                if (imp == null) {
                    imp = new ImportSql(auth.getCompanyId(), WarehouseSql.class, String.valueOf(item.getId()), warehouseSql.getId(), DBType.POSTGRESQL);
                    dbServiceSQL.saveEntity(imp);
                }

            }

        } while (responseList.getData().size() == pageSize);

        log.info("syncWarehouse total pages: " + page +
                ", imported: " + imported +
                ", created: " + created +
                ", updated: " + updated);
    }

    private void syncInvoicesSQL(long companyId, CompanySettings companySettings, LocalDateTime dateFrom, LocalDateTime dateTo,
                                Map<Long, VATRate> vatRateMap, Map<String, ImportSql> importMap) {
        if (companySettings == null || companySettings.getSync() == null || companySettings.getSync().getType() == null) return;

        final int pageSize = 100;
        int page = 0;
        int imported = 0;
        int created = 0;
        int updated = 0;
        int skipped = 0;

        ScoroResponseInvoiceList responseList;
        do {
            page++;

            responseList = getList(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                    companySettings.getSync().getId(), companySettings.getLanguage(),
                    ENTITY_INVOICES, ScoroResponseInvoiceList.class, pageSize, page, dateFrom, dateTo);

            if (responseList == null || responseList.getData() == null) break;

            for (ScoroInvoice item : responseList.getData()) {
                imported++;
                ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, InvoiceSql.class, String.valueOf(item.getId())));
                InvoiceSql invoice;
                if (imp == null) {
                    created++;
                    invoice = new InvoiceSql();
                } else {
                    long id = imp.getEntityId();
                    invoice = dbServiceSQL.getById(InvoiceSql.class, id);
                    if (invoice != null) {
                        if (BooleanUtils.isTrue(invoice.getFinished())) {
                            skipped++;
                            continue;
                        }
                        updated++;
                    }

                    if (invoice == null) {
                        created++;
                        invoice = new InvoiceSql();
                        int deleted = dbServiceSQL.removeById(ImportSql.class, imp.getId());
                        if (deleted != 0 ) {
                            imp = null;
                        } else {
                            throw new GamaException("Import record was not deleted");
                        }
                    }
                }

                ScoroResponseInvoiceView responseView = getItem(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                        companySettings.getSync().getId(), companySettings.getLanguage(),
                        ENTITY_INVOICES, ScoroResponseInvoiceView.class, item.getId());

                if (responseView == null || responseView.getData() == null) continue;

                ScoroInvoice itemView = responseView.getData();

                invoice.setExportId(String.valueOf(itemView.getId()));

                long no = itemView.getNo();
                CounterDesc desc = companySettings.getCounterByClass(InvoiceSql.class);
                invoice.setNumber(counterService.format(no, desc));

                invoice.setDate(itemView.getDate());
                invoice.setDueDate(itemView.getDeadline());
                invoice.setWarehouse(entityManager.getReference(WarehouseSql.class, companySettings.getWarehouse().getId()));

                final String currency = itemView.getCurrency() != null ? itemView.getCurrency() : companySettings.getCurrency().getCode();
                invoice.setExchange(new Exchange(currency));

                invoice.setSubtotal(GamaMoney.of(currency, itemView.getSum()));
                invoice.setTaxTotal(GamaMoney.of(currency, itemView.getVatSum()));
                invoice.setTotal(GamaMoneyUtils.add(invoice.getSubtotal(), invoice.getTaxTotal()));

                long counterpartyId = itemView.getCompanyId();
                if (counterpartyId > 0) {
                    CounterpartySql counterparty = importCounterparty(companySettings, companyId, counterpartyId, importMap);
                    if (counterparty != null) invoice.setCounterparty(counterparty);
                }

                if (itemView.getLines() != null) {
                    VATRate defaultRate = vatRateMap.values().stream().max(Comparator.comparingDouble(VATRate::getRate)).orElse(null);
                    invoice.setParts(new ArrayList<>());
                    for (ScoroInvoiceLine line : itemView.getLines()) {
                        PartSql part = importPartSQL(companySettings, companyId, line.getProductId(), line.getUnit(), vatRateMap, defaultRate, importMap);

                        InvoicePartSql partInvoice = new InvoicePartSql();
                        if (part != null) {
                            partInvoice.setPart(part);
                            partInvoice.setDocPart(new DocPart(part));
                        } else {
                            part = new PartSql();
                            part.setName("Imported from Scoro with id=" + line.getProductId());
                            part.setPrice(GamaBigMoney.of(currency, line.getPrice()));
                            part = dbServiceSQL.saveEntityInCompany(part);

                            partInvoice.setPart(part);
                            partInvoice.setDocPart(new DocPart(part));
                            partInvoice.setName("Imported from Scoro with id=" + line.getProductId());
                        }

                        partInvoice.setPrice(GamaBigMoney.of(currency, line.getPrice()));
                        partInvoice.setQuantity(line.getAmount());
                        partInvoice.setTotal(GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(partInvoice.getPrice(), line.getAmount())));

                        if (partInvoice.getVat() == null) partInvoice.setVat(new VATRate());
                        if (!BigDecimalUtils.isZero(line.getVat())) {
                            partInvoice.getVat().setRate(line.getVat().doubleValue());
                            partInvoice.setTaxable(line.getVat().doubleValue() > 0);
                        }

                        invoice.getParts().add(partInvoice);
                    }
                }

                try {

                    tradeService.prepareSaveInvoiceSQL(false, false, true, invoice);
                    invoice = dbServiceSQL.saveEntityInCompany(invoice);

                    if (imp == null) {
                        imp = new ImportSql(companyId, InvoiceSql.class, String.valueOf(item.getId()), invoice.getId(), DBType.POSTGRESQL);
                        dbServiceSQL.saveEntity(imp);
                    }

                } catch (Exception e) {
                    log.error("syncInvoices prepareSaveInvoice: " + e);
                }
            }

        } while (responseList.getData().size() == pageSize);

        log.info("syncInvoices total pages: " + page +
                ", imported: " + imported +
                ", created: " + created +
                ", updated: " + updated +
                ", skipped: " + skipped);
    }

    private void syncPurchasesSQL(long companyId, CompanySettings companySettings, LocalDateTime dateFrom, LocalDateTime dateTo,
                                 Map<Long, VATRate> vatRateMap, Map<String, ImportSql> importMap) {
        if (companySettings == null || companySettings.getSync() == null || companySettings.getSync().getType() == null) return;

        final int pageSize = 100;
        int page = 0;
        int imported = 0;
        int created = 0;
        int updated = 0;
        int skipped = 0;

        ScoroResponseExpenseList responseList;
        do {
            page++;

            responseList = getList(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                    companySettings.getSync().getId(), companySettings.getLanguage(),
                    ENTITY_EXPENSES, ScoroResponseExpenseList.class, pageSize, page, dateFrom, dateTo);

            if (responseList == null || responseList.getData() == null) break;

            for (ScoroExpense item : responseList.getData()) {
                imported++;
                ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, PurchaseSql.class, String.valueOf(item.getId())));
                PurchaseSql purchase;
                if (imp == null) {
                    created++;
                    purchase = new PurchaseSql();
                } else {
                    long id = imp.getEntityId();
                    purchase = dbServiceSQL.getById(PurchaseSql.class, id);
                    if (purchase != null) {
                        if (BooleanUtils.isTrue(purchase.getFinished())) {
                            skipped++;
                            continue;
                        }
                        updated++;
                    } else {
                        created++;
                        purchase = new PurchaseSql();
                        int deleted = dbServiceSQL.removeById(ImportSql.class, imp.getId());
                        if (deleted != 0 ) {
                            imp = null;
                        } else {
                            throw new GamaException("Import record was not deleted");
                        }
                    }
                }

                ScoroResponseExpenseView responseView = getItem(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                        companySettings.getSync().getId(), companySettings.getLanguage(),
                        ENTITY_EXPENSES, ScoroResponseExpenseView.class, item.getId());

                if (responseView == null || responseView.getData() == null) continue;

                ScoroExpense itemView = responseView.getData();

                purchase.setCompanyId(companyId);

                purchase.setExportId(String.valueOf(itemView.getId()));

                purchase.setNumber(itemView.getNo());
                purchase.setDate(itemView.getDate());
                purchase.setDueDate(itemView.getDeadline());

                purchase.setWarehouse(entityManager.getReference(WarehouseSql.class, companySettings.getWarehouse().getId()));

                final String currency = itemView.getCurrency() != null ? itemView.getCurrency() : companySettings.getCurrency().getCode();
                purchase.setExchange(new Exchange(currency));

                purchase.setSubtotal(GamaMoney.of(currency, itemView.getSum()));
                purchase.setTaxTotal(GamaMoney.of(currency, itemView.getVatSum()));
                purchase.setTotal(GamaMoneyUtils.add(purchase.getSubtotal(), purchase.getTaxTotal()));

                long counterpartyId = itemView.getCompanyId();
                if (counterpartyId > 0) {
                    CounterpartySql counterparty = importCounterparty(companySettings, companyId, counterpartyId, importMap);
                    if (counterparty != null) purchase.setCounterparty(counterparty);
                }

                if (itemView.getLines() != null) {
                    purchase.setParts(new ArrayList<>());
                    for (ScoroExpenseLine line : itemView.getLines()) {
                        VATRate defaultRate = vatRateMap.values().stream().max(Comparator.comparingDouble(VATRate::getRate)).orElse(null);
                        PartSql part = importPartSQL(companySettings, companyId, line.getProductId(), line.getUnit(), vatRateMap, defaultRate, importMap);

                        PurchasePartSql partPurchase = new PurchasePartSql();
                        if (part != null) {
                            partPurchase.setPart(part);
                            partPurchase.setDocPart(new DocPart(part));
                        } else {
                            part = new PartSql();
                            part.setName("Imported from Scoro with id=" + line.getProductId());
                            part.setPrice(GamaBigMoney.of(currency, line.getPrice()));
                            part = dbServiceSQL.saveEntityInCompany(part);

                            partPurchase.setPart(part);
                            partPurchase.setDocPart(new DocPart(part));
                            partPurchase.setName("Imported from Scoro with id=" + line.getProductId());
                        }

                        partPurchase.setPrice(GamaBigMoney.of(currency, line.getPrice()));
                        partPurchase.setQuantity(line.getAmount());
                        partPurchase.setTotal(GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(partPurchase.getPrice(), line.getAmount())));

                        purchase.getParts().add(partPurchase);
                    }
                }

                try {
                    tradeService.prepareSavePurchaseSQL(purchase);
                    purchase = dbServiceSQL.saveEntityInCompany(purchase);

                    if (imp == null) {
                        imp = new ImportSql(companyId, PurchaseSql.class, String.valueOf(item.getId()), purchase.getId(), DBType.POSTGRESQL);
                        dbServiceSQL.saveEntity(imp);
                    }

                } catch (Exception e) {
                    log.error("syncPurchases prepareSavePurchase: " + e);
                }
            }

        } while (responseList.getData().size() == pageSize);

        log.info("syncPurchases total pages: " + page +
                ", imported: " + imported +
                ", created: " + created +
                ", updated: " + updated +
                ", skipped: " + skipped);
    }

    private PartSql importPartSQL(CompanySettings companySettings, long companyId, long externalId, String units,
                                 Map<Long, VATRate> vatRateMap, VATRate defaultRate, Map<String, ImportSql> importMap) {

        ImportSql imp = getImport(companyId, PartSql.class, String.valueOf(externalId), importMap);
        PartSql part;

        if (imp != null) {
           if (imp.getEntityDb() == DBType.POSTGRESQL) {
                part = dbServiceSQL.getById(PartSql.class, imp.getEntityId());
                if (part != null && !BooleanUtils.isTrue(part.getArchive())) {
                    return part;
                }
            } else {
                dbServiceSQL.removeById(ImportSql.class, imp.getId());
            }
        }

        part = createPartSQL(companySettings, companyId, externalId, units, vatRateMap, defaultRate);
        if (part == null) return null;

        if (part.getId() == null) {
            part = dbServiceSQL.saveEntityInCompany(part);
        }
        imp = new ImportSql(companyId, PartSql.class, String.valueOf(externalId), part.getId(), DBType.POSTGRESQL);
        dbServiceSQL.saveEntity(imp);

        // cache import record
        importMap.put(imp.getId().toString(), imp);

        return part;
    }

    private PartSql createPartSQL(CompanySettings companySettings, long companyId, long externalId, String units, Map<Long, VATRate> mapVatRate, VATRate defaultRate)
    {
        ScoroResponseProductView responseView = getItem(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                companySettings.getSync().getId(), companySettings.getLanguage(),
                ENTITY_PRODUCTS, ScoroResponseProductView.class, externalId);

        if (responseView == null || responseView.getData() == null) return null;

        ScoroProduct product = responseView.getData();

        PartSql part = new PartSql();
        part.setCompanyId(companyId);
        part.setExportId(String.valueOf(externalId));

        part.setName(syncHttpService.decode(product.getName()));
        part.setSku(product.getCode());
        part.setDescription(syncHttpService.decode(product.getDescription()));
        part.setUnit(units);

        if (mapVatRate != null && mapVatRate.size() > 0) {
            VATRate rate = mapVatRate.get(product.getDefaultVatCodeId());
            if (rate == null) rate = defaultRate;
            part.setVatRateCode(rate.getCode());
        }

        if (part.getVatRate() != null && StringHelper.hasValue(part.getVatRateCode())) part.setTaxable(true);

        if (!BigDecimalUtils.isZero(product.getPrice())) {
            part.setPrice(GamaBigMoney.of(companySettings.getCurrency().getCode(), product.getPrice()));
        }

        part.setType(product.isService() ? PartType.SERVICE : PartType.PRODUCT);

        if (!companySettings.isDisableGL()) {
            if (part.getType() == PartType.PRODUCT) {
                part.setAccountAsset(companySettings.getGl().getProductAsset());
                part.setGlExpense(new GLDC(companySettings.getGl().getProductExpense(), companySettings.getGl().getProductExpense()));
                part.setGlIncome(new GLDC(companySettings.getGl().getProductIncome(), companySettings.getGl().getProductIncome()));
            } else {
                part.setGlExpense(new GLDC(companySettings.getGl().getProductExpense(), companySettings.getGl().getServiceExpense()));
                part.setGlIncome(new GLDC(companySettings.getGl().getProductIncome(), companySettings.getGl().getServiceIncome()));
            }
        }

        return part;
    }

    private CounterpartySql importCounterparty(CompanySettings companySettings, long companyId, long externalId, Map<String, ImportSql> importMap) {
        ImportSql imp = getImport(companyId, CounterpartySql.class, String.valueOf(externalId), importMap);
        CounterpartySql counterparty;

        if (imp != null) {
            return dbServiceSQL.getByIdOrForeignId(CounterpartySql.class, imp.getEntityId(), imp.getEntityDb());
        }

        counterparty = createCounterparty(companySettings, companyId, externalId);
        if (counterparty == null) return null;

        if (counterparty.getId() == null) {
            dbServiceSQL.saveEntityInCompany(counterparty);
        }
        imp = new ImportSql(companyId, CounterpartySql.class, String.valueOf(externalId), counterparty.getId(), DBType.POSTGRESQL);
        dbServiceSQL.saveEntity(imp);

        // cache import record
        importMap.put(imp.getId().toString(), imp);

        return counterparty;
    }

    private CounterpartySql createCounterparty(CompanySettings companySettings, long companyId, long externalId) {
        ScoroResponseContactView responseView = getItem(companySettings.getSync().getUrl(), companySettings.getSync().getKey(),
                companySettings.getSync().getId(), companySettings.getLanguage(),
                ENTITY_CONTACTS, ScoroResponseContactView.class, externalId);

        if (responseView == null || responseView.getData() == null) return null;

        ScoroContact contact = responseView.getData();

        // try search by company/person Id
        if (StringHelper.hasValue(contact.getIdCode())) {
            List<CounterpartySql> counterparties = entityManager.createQuery(
                    "SELECT a FROM " + CounterpartySql.class.getName() + " a" +
                            " WHERE companyId = :companyId" +
                            " AND (a.archive IS null OR a.archive = false)" +
                            " AND comCode = :comCode",
                            CounterpartySql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("comCode", contact.getIdCode())
                    .setMaxResults(1)
                    .getResultList();
            if (counterparties != null && counterparties.size() > 0) {
                return counterparties.get(0);
            }
        }

        // try search by name
        String fullName = "person".equals(contact.getContactType()) ?
                StringHelper.concatenate(contact.getName(), contact.getLastname()) :
                contact.getName();

        if (StringHelper.hasValue(fullName)) {
            @SuppressWarnings("unchecked") List<CounterpartySql> counterparties = entityManager.createNativeQuery(
                    "SELECT a.* FROM counterparties a" +
                            " WHERE company_id = :companyId" +
                            " AND (a.archive IS null OR a.archive = false)" +
                            " AND (lower(trim(unaccent(name))) = :name OR lower(trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g'))) = :name)",
                            CounterpartySql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("name", EntityUtils.prepareName(fullName))
                    .setMaxResults(1)
                    .getResultList();
            if (counterparties != null && counterparties.size() > 0) {
                return counterparties.get(0);
            }
        }

        CounterpartySql counterparty = new CounterpartySql();
        counterparty.setCompanyId(companyId);
        counterparty.setExportId(String.valueOf(externalId));
        counterparty.setName(syncHttpService.decode(fullName));
        counterparty.setComCode(contact.getIdCode());
        counterparty.setVatCode(contact.getVatno());
        if (contact.getAddresses() != null && contact.getAddresses().size() > 0) {
            counterparty.setLocations(new ArrayList<>());
            for (ScoroAddress address : contact.getAddresses()) {
                Location location = new Location();
                location.setAddress1(address.getStreet());
                location.setZip(address.getZipcode());
                location.setCity(address.getCity());
                location.setMunicipality(address.getMunicipality());
                if (StringHelper.isEmpty(address.getCountry())) {
                    location.setCountry(companySettings.getCountry());
                } else {
                    location.setCountry(CountryCode.getByCodeIgnoreCase(address.getCountry()).getAlpha2());
                }
                counterparty.getLocations().add(location);
            }
        }

        if (!companySettings.isDisableGL()) {
            counterparty.setAccount(DebtType.CUSTOMER, companySettings.getGl().getCounterpartyCustomer());
            counterparty.setAccount(DebtType.VENDOR, companySettings.getGl().getCounterpartyVendor());
        }

        return counterparty;
    }

    private <E> ImportSql getImport(long companyId, Class<E> type, String externalId, Map<String, ImportSql> importMap) {
        ImportId key = new ImportId(companyId, type, externalId);
        if (importMap.containsKey(key.toString())) {
            return importMap.get(key.toString());
        }

        ImportSql imp = dbServiceSQL.getById(ImportSql.class, key);
        if (imp != null) {
            importMap.put(key.toString(), imp);
        }
        return imp;
    }

    private <T extends ScoroResponseList<?>> T getList(String apiURl, String apiKey, String apiCompanyName,
                                                       String language, String entity,
                                                       Class<T> clazz, int pageSize, int page,
                                                       LocalDateTime dateFrom, LocalDateTime dateTo) {
        try {
            ScoroRequestList requestData = new ScoroRequestList(apiKey, apiCompanyName, language, pageSize, page,
                    new ScoroRequestFilter(new ScoroRequestFilterDate(dateFrom, dateTo)));
            String requestJson = objectMapper.writeValueAsString(requestData);

            URI uri = new URI(apiURl + '/' + entity).normalize();
            return getResponse(entity, uri, requestJson, clazz);

        } catch (IOException | URISyntaxException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return null;
        }
    }

    private <T extends ScoroResponseView<?>> T getItem(String apiURl, String apiKey, String apiCompanyName,
                                                       String language, String entity, Class<T> clazz, long id) {
        try {
            ScoroRequest requestData = new ScoroRequest(apiKey, apiCompanyName, language);
            String requestJson = objectMapper.writeValueAsString(requestData);

            URI uri = new URI(apiURl + '/' + entity + "/view/" + id).normalize();
            return getResponse(entity, uri, requestJson, clazz);

        } catch (IOException | URISyntaxException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return null;
        }
    }

    private <T extends ScoroResponseView<?>> T getResponse(String entity, URI uri, String data, Class<T> clazz) {
        try {
            for (int count = 1; count <= 3; ++count) {

                if (count > 1) {
                    try {
                        Thread.sleep(count * 1000);
                    } catch (InterruptedException e) {
                        log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
                    }
                }

                T response = syncHttpService.getRequestData(SyncHttpService.HttpMethod.POST, uri, data, clazz);

                if (response == null) {
                    log.error(this.getClass().getSimpleName() + ": #" + count + " " + entity + ", url='" + uri +  "', data='" + data + "', response: null");

                } else if (!"200".equals(response.getStatusCode())) {
                    log.error(this.getClass().getSimpleName() + ": #" + count + " " + entity + ", url='" + uri +  "', data='" + data + "', response: " + response.getStatusCode() + " " + response.getStatus());

                } else if (response.getData() == null) {
                    log.error(this.getClass().getSimpleName() + ": #" + count + " " + entity + ", url='" + uri +  "', data='" + data + "', response: no data");

                } else {
                    return response;
                }
            }

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return null;
    }
}
