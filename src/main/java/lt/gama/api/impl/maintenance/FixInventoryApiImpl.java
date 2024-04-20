package lt.gama.api.impl.maintenance;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.ex.GamaApiNotFoundException;
import lt.gama.api.service.maintenance.FixInventoryApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.Doc_;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * gama-online
 * Created by valdas on 2016-03-21.
 */
@RestController
public class FixInventoryApiImpl implements FixInventoryApi {

    @PersistenceContext
    private EntityManager entityManager;

    private final InventoryService inventoryService;
    private final Auth auth;
    private final TradeService tradeService;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final APIResultService apiResultService;
    private final DBServiceSQL dbServiceSQL;

    public FixInventoryApiImpl(InventoryService inventoryService, Auth auth, TradeService tradeService, AuthSettingsCacheService authSettingsCacheService, APIResultService apiResultService, DBServiceSQL dbServiceSQL) {
        this.inventoryService = inventoryService;
        this.auth = auth;
        this.tradeService = tradeService;
        this.authSettingsCacheService = authSettingsCacheService;
        this.apiResultService = apiResultService;
        this.dbServiceSQL = dbServiceSQL;
    }


    @Override
    public APIResult<Map<String, Integer>> deleteInventory(DeleteInventoryRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return inventoryService.deleteInventory(request.partId);
        });
    }

    @Override
    public APIResult<String> createInventoryNow(CreateInventoryNowRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            BaseDocumentSql document = dbServiceSQL.getAndCheck(BaseDocumentSql.class, request.docId);

            StringJoiner sj = new StringJoiner(" ");
            sj.add("SELECT n FROM " + InventoryNowSql.class.getName() + " n");
            sj.add("WHERE i." + InventoryNowSql_.COMPANY_ID + " = :companyId");
            sj.add("AND i." + InventoryNowSql_.PART + "." + PartSql_.ID + " = :partId");
            sj.add("AND " + InventoryNowSql_.WAREHOUSE + "." + WarehouseSql_.ID + " = :warehouseId");
            sj.add("AND " + InventoryNowSql_.DOC + "." + Doc_.ID + " = :docId");
            if (request.sn != null) sj.add("AND sn.sn = :sn");
            sj.add(" ORDER BY doc.date, id");

            TypedQuery<InventoryNowSql> q = entityManager.createQuery(sj.toString(), InventoryNowSql.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("partId", request.partId);
            q.setParameter("warehouseId", request.warehouseId);
            if (request.sn != null) q.setParameter("sn", request.sn.getSn());
            List<InventoryNowSql> inventories = q.getResultList();

            if (CollectionsHelper.hasValue(inventories)) throw new GamaException("InventoryNow exists");

            InventoryNowSql inventoryNow = new InventoryNowSql(request.companyId,
                    entityManager.getReference(PartSql.class, request.partId), request.sn,
                    entityManager.getReference(WarehouseSql.class, request.warehouseId),
                    Doc.of(document),
                    Validators.isValid(document.getCounterparty())
                            ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null);
            inventoryNow.setQuantity(BigDecimalUtils.add(inventoryNow.getQuantity(), new BigDecimal(request.quantity)));
            inventoryNow.setCostTotal(GamaMoneyUtils.add(inventoryNow.getCostTotal(), GamaMoney.parse(request.costTotal)));
            dbServiceSQL.saveEntityInCompany(inventoryNow);

            return "OK";
        });
    }

    @Override
    public APIResult<List<InventoryHistorySql>> retrieveInventoryHistory(RetrieveInventoryHistoryRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return entityManager.createQuery(
                            "SELECT id FROM " + InventoryHistorySql.class.getName() + " h" +
                                    " WHERE h." + InventoryHistorySql_.COMPANY_ID + " = :companyId" +
                                    " AND h." + InventoryHistorySql_.PART + "." + PartSql_.ID + " = :partId" +
                                    " AND h." + InventoryHistorySql_.WAREHOUSE + "." + WarehouseSql_.ID + " = :warehouseId" +
                                    " AND h." + InventoryHistorySql_.DOC + "." + Doc_.DATE + " BETWEEN :dateFrom AND :dateTo" +
                                    " AND (h." + InventoryHistorySql_.archive + " IS null OR h." + InventoryHistorySql_.archive + " = false)" +
                                    " AND (h." + InventoryHistorySql_.hidden + " IS null OR h." + InventoryHistorySql_.hidden + " = false)" +
                                    " ORDER BY h." + InventoryHistorySql_.DOC + "." + Doc_.DATE +
                                    ", h." + InventoryHistorySql_.DOC + "." + Doc_.ORDINAL +
                                    ", h." + InventoryHistorySql_.DOC + "." + Doc_.NUMBER +
                                    ", h." + InventoryHistorySql_.DOC + "." + Doc_.ID,
                            InventoryHistorySql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("partId", request.partId)
                    .setParameter("warehouseId", request.warehouseId)
                    .setParameter("dateFrom", request.dateFrom.format(EntityUtils.DATE_FMT))
                    .setParameter("dateTo", request.dateTo.format(EntityUtils.DATE_FMT))
                    .getResultList();
        });
    }

    @Override
    public APIResult<InventoryHistorySql> createInventoryHistoryFromCopy(CreateInventoryHistoryFromCopyRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.executeAndReturnInTransaction(em -> {

            InventoryHistorySql entity = dbServiceSQL.getById(InventoryHistorySql.class, request.id);
            if (entity == null)
                throw new GamaApiNotFoundException("Not found InventoryHistory with id=" + request.id);

            auth.setCompanyId(entity.getCompanyId());
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            entityManager.detach(entity);
            entity.setId(null);
            entity.setSellBaseTotal(null);
            entity.setSellTotal(null);
            entity.setQuantity(request.quantity);
            entity.setCostTotal(request.cost);
            entity = dbServiceSQL.saveEntityInCompany(entity);

            return entity;
        }));
    }

    @Override
    public APIResult<List<InventoryHistorySql>> createInventoryHistoryFromDoc(CreateInventoryHistoryFromDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return inventoryService.createInventoryHistoryFromDoc(request.partId, request.warehouseId, request.docId, request.uuid);
        });
    }

    @Override
    public APIResult<String> fixTransProdDocumentStatus(FixTransProdDocumentStatusRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.fixTransProdDocumentStatus(request.documentId));
    }

    @Override
    public APIResult<String> recallInventory(RecallInventoryRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return tradeService.recallInventoryTask(request.inventoryId, true);
        });
    }

    @Override
    public APIResult<PurchaseSql> fixClearPurchaseInventoryFinishedMark(FixClearPurchaseInventoryFinishedMarkRequest request) throws GamaApiException {
        return apiResultService.result(() -> tradeService.fixClearPurchaseInventoryFinishedMark(request.docId));
    }

    @Override
    public APIResult<String> createInvoices(CreateInvoicesRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return tradeService.createInvoicesTask(request.count, request.dateFrom, request.dateTo);
        });
    }
}
