package lt.gama.api.impl;

import jakarta.persistence.criteria.*;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.*;
import lt.gama.api.service.InventoryApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.PageRequestUtils;
import lt.gama.helpers.Validators;
import lt.gama.integrations.vmi.ITaxRefundService;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.PartPurchaseDto;
import lt.gama.model.dto.entities.InventoryHistoryDto;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.documents.items.*;
import lt.gama.model.sql.entities.PartSql_;
import lt.gama.model.sql.system.CountryVatCodeSql;
import lt.gama.model.sql.system.CountryVatNoteSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.EstimateType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.inventory.InvoiceNote;
import lt.gama.model.type.inventory.TaxFree;
import lt.gama.model.type.part.PartSN_;
import lt.gama.report.RepInventoryBalance;
import lt.gama.report.RepInventoryDetail;
import lt.gama.report.RepInvoice;
import lt.gama.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gama
 * Created by valdas on 15-04-20.
 */
@RestController
public class InventoryApiImpl implements InventoryApi {

    private final TradeService tradeService;
    private final InventoryService inventoryService;
    private final DocumentService documentService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final ITaxRefundService taxRefundService;
    private final InventorySqlMapper inventorySqlMapper;
    private final InventoryOpeningBalanceSqlMapper inventoryOpeningBalanceSqlMapper;
    private final OrderSqlMapper orderSqlMapper;
    private final TransProdSqlMapper transportationSqlMapper;
    private final PurchaseSqlMapper purchaseSqlMapper;
    private final InvoiceSqlMapper invoiceSqlMapper;
    private final EstimateSqlMapper estimateSqlMapper;
    private final APIResultService apiResultService;

    public InventoryApiImpl(TradeService tradeService, InventoryService inventoryService, DocumentService documentService, Auth auth, DBServiceSQL dbServiceSQL, ITaxRefundService taxRefundService, InventorySqlMapper inventorySqlMapper, InventoryOpeningBalanceSqlMapper inventoryOpeningBalanceSqlMapper, OrderSqlMapper orderSqlMapper, TransProdSqlMapper transportationSqlMapper, PurchaseSqlMapper purchaseSqlMapper, InvoiceSqlMapper invoiceSqlMapper, EstimateSqlMapper estimateSqlMapper, APIResultService apiResultService) {
        this.tradeService = tradeService;
        this.inventoryService = inventoryService;
        this.documentService = documentService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.taxRefundService = taxRefundService;
        this.inventorySqlMapper = inventorySqlMapper;
        this.inventoryOpeningBalanceSqlMapper = inventoryOpeningBalanceSqlMapper;
        this.orderSqlMapper = orderSqlMapper;
        this.transportationSqlMapper = transportationSqlMapper;
        this.purchaseSqlMapper = purchaseSqlMapper;
        this.invoiceSqlMapper = invoiceSqlMapper;
        this.estimateSqlMapper = estimateSqlMapper;
        this.apiResultService = apiResultService;
    }

    /*
     * Opening Balance
     */

    @Override
    public APIResult<PageResponse<InventoryOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.list(request, InventoryOpeningBalanceSql.class,
                InventoryOpeningBalanceSql.GRAPH_ALL, inventoryOpeningBalanceSqlMapper,
                (cb, root) -> EntityUtils.whereDoc(request, cb, root, null, null),
                (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<InventoryOpeningBalanceDto> saveOpeningBalance(InventoryOpeningBalanceDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.saveOpeningBalance(request));
    }

    @Override
    public APIResult<InventoryOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
            return documentService.getDocument(InventoryOpeningBalanceSql.class, request.getId(), db);
        });
    }

    @Override
    public APIResult<String> finishOpeningBalanceTask(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                tradeService.finishOpeningBalanceTask(request.getId()));
    }

    @Override
    public APIResult<InventoryOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                tradeService.importOpeningBalance(request.getId(), request.getFileName()));
    }

    @Override
    public APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    /*
     * Invoice
     */

    @Override
    public ResponseEntity<StreamingResponseBody> listInvoice(PageRequest request) throws GamaApiException {
        return ResponseEntity.ok(outputStream -> apiResultService.execute(() -> dbServiceSQL.list(request, InvoiceSql.class,
                request.isNoDetail()
                        ? InvoiceSql.GRAPH_NO_PARTS
                        : InvoiceSql.GRAPH_ALL,
                invoiceSqlMapper, outputStream,
                root -> Map.of(
                        InvoiceSql_.COUNTERPARTY, root.join(InvoiceSql_.COUNTERPARTY, JoinType.LEFT),
                        InvoiceSql_.WAREHOUSE, root.join(InvoiceSql_.WAREHOUSE, JoinType.LEFT),
                        InvoiceSql_.ACCOUNT, root.join(InvoiceSql_.ACCOUNT, JoinType.LEFT),
                        InvoiceSql_.EMPLOYEE, root.join(InvoiceSql_.EMPLOYEE, JoinType.LEFT)),
                (cb, root, joins) -> {
                    Join<InvoiceSql, InvoiceBasePartSql> parts = root.join(InvoiceSql_.PARTS, JoinType.LEFT);
                    var wherePredicate = EntityUtils.whereDoc(request, cb, root, patterns -> {
                        Predicate[] predicates = new Predicate[patterns.length];
                        for (int i = 0; i < patterns.length; i++) {
                            String pattern = patterns[i];
                            predicates[i] = cb.or(
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(InvoiceBasePartSql_.PART).get(PartSql_.NAME))), '%' + pattern + '%'),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(InvoiceBasePartSql_.PART).get(PartSql_.SKU))), pattern + '%'),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(InvoiceBasePartSql_.SN).get(PartSN_.SN))), pattern),
                                    cb.equal(
                                            cb.lower(cb.function("regexp_replace", String.class,
                                                    cb.function("unaccent", String.class, parts.get(InvoiceBasePartSql_.SN).get(PartSN_.SN)),
                                                    cb.literal("\\s"), cb.literal(""),
                                                    cb.literal("g"))),
                                            pattern));
                        }
                        return patterns.length == 1 ? predicates[0] : cb.and(predicates);
                    }, joins);
                    Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.COUNTERPARTY);
                    if (value != null) {
                        var counterpartyPredicate = cb.equal(root.get(InvoiceSql_.COUNTERPARTY).get("id"), value);
                        wherePredicate = wherePredicate == null ? counterpartyPredicate : cb.and(wherePredicate, counterpartyPredicate);
                    }
                    return wherePredicate;
                },
                (cb, root, joins) -> EntityUtils.orderList(cb, request.getOrder(),
                        invoiceExpresionsList(request.getOrder(), cb, root, false).toArray(Expression[]::new)),
                (cb, root, joins) -> invoiceExpresionsList(request.getOrder(), cb, root, true))));
    }

    private List<Selection<?>> invoiceExpresionsList(String orderBy, CriteriaBuilder cb, Root<?> root, boolean id) {
        var expStd = EntityUtils.expresionsList(orderBy, cb, root, id);
        if ("total".equalsIgnoreCase(orderBy) || "-total".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(InvoiceSql_.TOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("subtotal".equalsIgnoreCase(orderBy) || "-subtotal".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(InvoiceSql_.SUBTOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("taxTotal".equalsIgnoreCase(orderBy) || "-taxTotal".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(InvoiceSql_.TAX_TOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("baseTotal".equalsIgnoreCase(orderBy) || "-baseTotal".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(InvoiceSql_.BASE_TOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("baseSubtotal".equalsIgnoreCase(orderBy) || "-baseSubtotal".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(InvoiceSql_.BASE_SUBTOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("baseTaxTotal".equalsIgnoreCase(orderBy) || "-baseTaxTotal".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(InvoiceSql_.BASE_TAX_TOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("dueDate".equalsIgnoreCase(orderBy) || "-dueDate".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(InvoiceSql_.DUE_DATE));
            exp.addAll(expStd);
            return exp;
        }
        return expStd;
    }

    @Override
    public APIResult<InvoiceDto> saveInvoice(InvoiceDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.saveInvoice(request));
    }

    @Override
    public APIResult<InvoiceDto> getInvoice(IdRequest request) throws GamaApiException {
        DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
        return apiResultService.result(() -> documentService.getDocument(InvoiceSql.class, request.getId(), db));
    }

    @Override
    public APIResult<InvoiceDto> finishInvoice(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.finishInvoice(request.id, request.finishGL));
    }

    @Override
    public APIResult<PageResponse<RepInvoice, Void>> reportInvoiceSQL(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.reportInvoiceSQL(request));
    }

    @Override
    public APIResult<Void> deleteInvoice(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<InvoiceDto> recallInvoice(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.recallInvoice(request.getId()));
    }

    @Override
    public APIResult<InvoiceDto> updateInvoiceISAF(InvoiceDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.updateInvoiceISAFSQL(request));
    }

    @Override
    public APIResult<LastInvoicePriceResponse> getLastInvoicePrice(GetLastInvoicePriceRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.getLastInvoicePriceSQL(request.counterpartyId, request.counterpartyDb, request.partId));
    }

    @Override
    public APIResult<String> syncInvoiceTask() throws GamaApiException {
        return apiResultService.result(() -> tradeService.syncTask());
    }

    @Override
    public APIResult<InvoiceDto> saveInvoiceTaxFree(TaxFreeRequest request) throws GamaApiException {
        return apiResultService.result(() -> taxRefundService.saveInvoiceTaxFreeSQL(request.id, request.taxFree, request.db));
    }

    @Override
    public APIResult<TaxFree> generateInvoiceTaxFree(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> taxRefundService.generateInvoiceTaxFreeSQL(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<InvoiceDto> submitInvoiceTaxFree(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> taxRefundService.submitInvoiceTaxFreeSQL(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<InvoiceDto> cancelInvoiceTaxFree(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> taxRefundService.cancelInvoiceTaxFreeSQL(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<InvoiceDto> submitInvoiceTaxFreePaymentInfo(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> taxRefundService.submitInvoiceTaxFreePaymentInfoSQL(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<Void> emailInvoiceTaxFree(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.emailInvoiceTaxFree(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<List<InvoiceNote>> getInvoiceNotes() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getInvoiceNotes();
        });
    }

    @Override
    public APIResult<String> syncWarehouseInvoiceTask(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.syncWarehouseInvoiceTask(request.getId()));
    }

    /*
     * Purchase
     */

    @Override
    public ResponseEntity<StreamingResponseBody> listPurchase(PageRequest request) throws GamaApiException {
        return ResponseEntity.ok(outputStream -> apiResultService.execute(() -> dbServiceSQL.list(request, PurchaseSql.class,
                PurchaseSql.GRAPH_ALL, purchaseSqlMapper, outputStream,
                root -> Map.of(
                        PurchaseSql_.COUNTERPARTY, root.join(PurchaseSql_.COUNTERPARTY, JoinType.LEFT),
                        PurchaseSql_.WAREHOUSE, root.join(PurchaseSql_.WAREHOUSE, JoinType.LEFT),
                        PurchaseSql_.EMPLOYEE, root.join(PurchaseSql_.EMPLOYEE, JoinType.LEFT)),
                (cb, root, joins) -> {
                    Join<PurchaseSql, PurchasePartSql> parts = root.join(PurchaseSql_.PARTS, JoinType.LEFT);
                    var wherePredicate = EntityUtils.whereDoc(request, cb, root, patterns -> {
                        Predicate[] predicates = new Predicate[patterns.length];
                        for (int i = 0; i < patterns.length; i++) {
                            String pattern = patterns[i];
                            predicates[i] = cb.or(
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(PurchasePartSql_.PART).get(PartSql_.NAME))), '%' + pattern + '%'),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(PurchasePartSql_.PART).get(PartSql_.SKU))), pattern + '%'),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(PurchasePartSql_.SN).get(PartSN_.SN))), pattern),
                                    cb.equal(
                                            cb.lower(cb.function("regexp_replace", String.class,
                                                    cb.function("unaccent", String.class, parts.get(PurchasePartSql_.SN).get(PartSN_.SN)),
                                                    cb.literal("\\s"), cb.literal(""),
                                                    cb.literal("g"))),
                                            pattern));
                        }
                        return patterns.length == 1 ? predicates[0] : cb.and(predicates);
                    }, joins);
                    Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.COUNTERPARTY);
                    if (value != null) {
                        var counterpartyPredicate = cb.equal(root.get(InvoiceSql_.COUNTERPARTY).get("id"), value);
                        wherePredicate = wherePredicate == null ? counterpartyPredicate : cb.and(wherePredicate, counterpartyPredicate);
                    }
                    return wherePredicate;
                },
                (cb, root, joins) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                (cb, root, joins) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root))));
    }

    @Override
    public APIResult<PurchaseDto> savePurchase(PurchaseDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.savePurchase(request));
    }

    @Override
    public APIResult<PurchaseDto> getPurchase(IdRequest request) throws GamaApiException {
        DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
        return apiResultService.result(() -> documentService.getDocument(PurchaseSql.class, request.getId(), db));
    }

    @Override
    public APIResult<PurchaseDto> finishPurchase(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.finishPurchase(request.id, request.finishGL));
    }

    @Override
    public APIResult<Void> deletePurchase(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<PurchaseDto> recallPurchase(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.recallPurchase(request.getId()));
    }

    @Override
    public APIResult<PurchaseDto> updatePurchaseISAF(PurchaseDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.updatePurchaseISAFSQL(request));
    }

    @Override
    public APIResult<String> syncWarehousePurchaseTask(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.syncWarehousePurchaseTask(request.getId()));
    }

    @Override
    public APIResult<List<PartPurchaseDto>> importPurchase(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.importPurchase(request.getFileName(), request.getFormat(), request.getCurrency()));
    }

    /*
     * Transportation - Production
     */

    @Override
    public APIResult<PageResponse<TransProdDto, Void>> listTransProd(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.list(request, TransProdSql.class, TransProdSql.GRAPH_ALL, transportationSqlMapper,
                root -> Map.of(
                        TransProdSql_.COUNTERPARTY, root.join(TransProdSql_.COUNTERPARTY, JoinType.LEFT),
                        TransProdSql_.EMPLOYEE, root.join(TransProdSql_.EMPLOYEE, JoinType.LEFT),
                        TransProdSql_.WAREHOUSE_FROM, root.join(TransProdSql_.WAREHOUSE_FROM, JoinType.LEFT),
                        TransProdSql_.WAREHOUSE_TO, root.join(TransProdSql_.WAREHOUSE_TO, JoinType.LEFT),
                        TransProdSql_.WAREHOUSE_RESERVED, root.join(TransProdSql_.WAREHOUSE_RESERVED, JoinType.LEFT)),
                (cb, root, joins) -> {
                    Join<TransProdSql, TransProdPartSql> parts = root.join(TransProdSql_.PARTS, JoinType.LEFT);
                    return EntityUtils.whereDoc(request, cb, root, patterns -> {
                        Predicate[] predicates = new Predicate[patterns.length];
                        for (int i = 0; i < patterns.length; i++) {
                            String pattern = patterns[i];
                            predicates[i] = cb.or(
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(TransProdPartSql_.PART).get(PartSql_.NAME))), '%' + pattern + '%'),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(TransProdPartSql_.PART).get(PartSql_.SKU))), pattern + '%'),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(TransProdPartSql_.SN).get(PartSN_.SN))), pattern),
                                    cb.equal(
                                            cb.lower(cb.function("regexp_replace", String.class,
                                                    cb.function("unaccent", String.class, parts.get(TransProdPartSql_.SN).get(PartSN_.SN)),
                                                    cb.literal("\\s"), cb.literal(""),
                                                    cb.literal("g"))),
                                            pattern));
                        }
                        return patterns.length == 1 ? predicates[0] : cb.and(predicates);
                    }, joins);
                },
                (cb, root, joins) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                (cb, root, joins) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<TransProdDto> saveTransProd(TransProdDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.saveTransProd(request));
    }

    @Override
    public APIResult<TransProdDto> getTransProd(IdRequest request) throws GamaApiException {
        DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
        return apiResultService.result(() -> documentService.getDocument(TransProdSql.class, request.getId(), db));
    }

    @Override
    public APIResult<TransProdDto> finishTransProd(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.finishTransProd(request.id, request.finishGL));
    }

    @Override
    public APIResult<Void> deleteTransProd(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<TransProdDto> recallTransProd(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.recallTransProd(request.getId()));
    }

    @Override
    public APIResult<TransProdDto> importTransProd(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.importTransProd(request.getId(), request.getFileName()));
    }

    @Override
    public APIResult<TransProdDto> reserveTransProd(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.reserveTransProd(request.getId()));
    }

    @Override
    public APIResult<TransProdDto> recallReserveTransProd(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.recallReserveTransProd(request.getId()));
    }

    /*
     * Inventory
     */

    @Override
    public APIResult<PageResponse<InventoryDto, Void>> listInventory(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.list(request, InventorySql.class, InventorySql.GRAPH_ALL, inventorySqlMapper,
                (cb, root) -> EntityUtils.whereDoc(request, cb, root, null, null),
                (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<InventoryDto> saveInventory(InventoryDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.saveInventory(request));
    }

    @Override
    public APIResult<InventoryDto> getInventory(IdRequest request) throws GamaApiException {
        DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
        return apiResultService.result(() -> documentService.getDocument(InventorySql.class, request.getId(), db));
    }

    @Override
    public APIResult<String> finishInventoryTask(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.finishInventoryTask(request.id, request.finishGL));
    }

    @Override
    public APIResult<Void> deleteInventory(IdRequest request) throws GamaApiException {
        DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), db));
    }

    @Override
    public APIResult<String> recallInventoryTask(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.recallInventoryTask(request.getId(), false));
    }

    @Override
    public APIResult<InventoryDto> importInventory(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.importInventory(request.getId(), request.getFileName()));
    }

    /*
     * Estimate
     */

    @Override
    public APIResult<PageResponse<EstimateDto, Void>> listEstimate(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.list(request, EstimateSql.class, EstimateSql.GRAPH_ALL, estimateSqlMapper,
                    root -> Map.of(
                            EstimateSql_.COUNTERPARTY, root.join(EstimateSql_.COUNTERPARTY, JoinType.LEFT),
                            EstimateSql_.WAREHOUSE, root.join(EstimateSql_.WAREHOUSE, JoinType.LEFT),
                            EstimateSql_.ACCOUNT, root.join(EstimateSql_.ACCOUNT, JoinType.LEFT),
                            EstimateSql_.EMPLOYEE, root.join(EstimateSql_.EMPLOYEE, JoinType.LEFT)),
                    (cb, root, joins) -> {
                        Join<EstimateSql, EstimateBasePartSql> parts = root.join(EstimateSql_.PARTS, JoinType.LEFT);
                        var wherePredicate = EntityUtils.whereDoc(request, cb, root, patterns -> {
                            Predicate[] predicates = new Predicate[patterns.length];
                            for (int i = 0; i < patterns.length; i++) {
                                String pattern = patterns[i];
                                predicates[i] = cb.or(
                                        cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(EstimateBasePartSql_.PART).get(PartSql_.NAME))), '%' + pattern + '%'),
                                        cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(EstimateBasePartSql_.PART).get(PartSql_.SKU))), pattern + '%'),
                                        cb.like(cb.lower(cb.function("unaccent", String.class, parts.get(EstimateBasePartSql_.SN).get(PartSN_.SN))), pattern),
                                        cb.equal(
                                                cb.lower(cb.function("regexp_replace", String.class,
                                                        cb.function("unaccent", String.class, parts.get(EstimateBasePartSql_.SN).get(PartSN_.SN)),
                                                        cb.literal("\\s"), cb.literal(""),
                                                        cb.literal("g"))),
                                                pattern));
                            }
                            return patterns.length == 1 ? predicates[0] : cb.and(predicates);
                        }, joins);
                        Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.ESTIMATE_TYPE);
                        if (value != null) {
                            EstimateType estimateType = value instanceof EstimateType ? (EstimateType) value : EstimateType.from(String.valueOf(value));
                            if (estimateType != null) {
                                var estimateTypePredicate = cb.equal(root.get(EstimateSql_.TYPE), estimateType);
                                wherePredicate = wherePredicate == null ? estimateTypePredicate : cb.and(wherePredicate, estimateTypePredicate);
                            }
                        }
                        return wherePredicate;
                    },
                    (cb, root, joins) -> EntityUtils.orderList(cb, request.getOrder(),
                            estimateExpresionsList(request.getOrder(), cb, root, false).toArray(Expression[]::new)),
                    (cb, root, joins) -> estimateExpresionsList(request.getOrder(), cb, root, true)));
    }

    private List<Selection<?>> estimateExpresionsList(String orderBy, CriteriaBuilder cb, Root<?> root, boolean id) {
        var expStd = EntityUtils.expresionsList(orderBy, cb, root, id);
        if ("total".equalsIgnoreCase(orderBy) || "-total".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(EstimateSql_.TOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("subtotal".equalsIgnoreCase(orderBy) || "-subtotal".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(EstimateSql_.SUBTOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("taxTotal".equalsIgnoreCase(orderBy) || "-taxTotal".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(EstimateSql_.TAX_TOTAL).get("amount"));
            exp.addAll(expStd);
            return exp;
        }
        if ("days".equalsIgnoreCase(orderBy) || "-days".equalsIgnoreCase(orderBy)) {
            List<Selection<?>> exp = new ArrayList<>();
            exp.add(root.get(EstimateSql_.NEXT_DATE));
            exp.addAll(expStd);
            return exp;
        }
        return expStd;
    }

    @Override
    public APIResult<EstimateDto> saveEstimate(EstimateDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.saveEstimate(request));
    }

    @Override
    public APIResult<EstimateDto> getEstimate(IdRequest request) throws GamaApiException {
        DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
        return apiResultService.result(() -> documentService.getDocument(EstimateSql.class, request.getId(), db));
    }

    @Override
    public APIResult<EstimateDto> finishEstimate(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.finishEstimate(request.getId()));
    }

    @Override
    public APIResult<Void> deleteEstimate(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    /*
     * Order
     */

    @Override
    public APIResult<PageResponse<OrderDto, Void>> listOrder(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.list(request, OrderSql.class, OrderSql.GRAPH_ALL, orderSqlMapper,
                root -> Map.of(
                        OrderSql_.COUNTERPARTY, root.join(OrderSql_.COUNTERPARTY, JoinType.LEFT),
                        OrderSql_.WAREHOUSE, root.join(OrderSql_.WAREHOUSE, JoinType.LEFT),
                        OrderSql_.EMPLOYEE, root.join(OrderSql_.EMPLOYEE, JoinType.LEFT)),
                (cb, root, joins) -> EntityUtils.whereDoc(request, cb, root, null, joins),
                (cb, root, joins) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                (cb, root, joins) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<OrderDto> saveOrder(OrderDto request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.saveOrder(request));
    }

    @Override
    public APIResult<OrderDto> getOrder(IdRequest request) throws GamaApiException {
        DBType db = request.getDb() != null ? request.getDb() : DBType.POSTGRESQL;
        return apiResultService.result(() -> documentService.getDocument(OrderSql.class, request.getId(), db));
    }

    @Override
    public APIResult<OrderDto> finishOrder(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.finishOrder(request.getId()));
    }

    /*
     * Reports
     */

    @Override
    public APIResult<PageResponse<RepInventoryBalance, Void>> reportBalance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.reportBalance(request));
    }

    @Override
    public APIResult<InventoryBalanceResponse> getBalance(InventoryBalanceRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                inventoryService.getBalance(request.getPartId(), request.getWarehouseId(), request.getSn()));
    }

    @Override
    public APIResult<PageResponse<InventoryHistoryDto, RepInventoryDetail>> reportDetail(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.reportDetail(request));
    }

    @Override
    public APIResult<PageResponse<InventoryGpais, Void>> reportGpais(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.reportGpais(request));
    }

    /*
     * Mail
     */

    @Override
    public APIResult<Void> sendInvoice(MailRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.emailInvoice(request.getDocId(), request.getRecipients(),
                request.getLanguage(), request.getCountry()));
    }

    /*
     * VAT codes (for iSAF)
     */

    @Override
    public APIResult<CountryVatCodeSql> getVatCode() throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.getById(CountryVatCodeSql.class, auth.getSettings().getCountry()));
    }

    @Override
    public APIResult<Map<String, GLDC>> getVatCodeGLSettings() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getGl() != null ? companySettings.getGl().getVatCode() : null;
        });
    }

    @Override
    public APIResult<CountryVatNoteSql> getVatNote() throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.getById(CountryVatNoteSql.class, auth.getSettings().getCountry()));
    }

    @Override
    public APIResult<CheckVatResponse> checkVat(CheckVatRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.checkVat(request));
    }
}
