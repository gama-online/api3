package lt.gama.service.sync.openCart;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.sql.system.SyncSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.SyncType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TaskQueueService;
import lt.gama.service.sync.i.ISyncOpenCartService;
import lt.gama.service.sync.i.ISyncOpenCartUtilsService;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.SyncResult;
import lt.gama.service.sync.openCart.model.OCLogin;
import lt.gama.service.sync.openCart.model.OCOrder;
import lt.gama.service.sync.openCart.model.OCOrdersListResponse;
import lt.gama.service.sync.openCart.tasks.OCOrderTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * gama-online
 * Created by valdas on 10/11/2018.
 */
public class SyncOpenCartService implements ISyncOpenCartService {

    private static final Logger log = LoggerFactory.getLogger(SyncOpenCartService.class);


    private final ISyncOpenCartUtilsService syncOpenCartUtilsService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final SyncHttpService syncHttpService;
    private final TaskQueueService taskQueueService;


    public SyncOpenCartService(ISyncOpenCartUtilsService syncOpenCartUtilsService,
                               Auth auth, 
                               DBServiceSQL dbServiceSQL,
                               AuthSettingsCacheService authSettingsCacheService,
                               SyncHttpService syncHttpService, 
                               TaskQueueService taskQueueService) {
        this.syncOpenCartUtilsService = syncOpenCartUtilsService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.authSettingsCacheService = authSettingsCacheService;
        this.syncHttpService = syncHttpService;
        this.taskQueueService = taskQueueService;
    }

    @Override
    public SyncResult sync(long companyId) {
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

        try {
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                SyncSql sync = dbServiceSQL.getById(SyncSql.class, companyId);
                if (sync == null) return null;

                CompanySql company = dbServiceSQL.getById(CompanySql.class, companyId);
                if (company == null) return null;

                if (company.getStatus() == null || company.getStatus() == CompanyStatusType.INACTIVE) {
                    log.info(this.getClass().getSimpleName() + ": companyId=" + companyId + " status: null or INACTIVE");
                    return null;
                }

                CompanySettings companySettings = company.getSettings();
                if (companySettings == null) {
                    log.error(this.getClass().getSimpleName() + ": companyId=" + companyId + " has no settings");
                    return null;
                }

                // check settings sync
                SyncSettings syncSettings = companySettings.getSync();
                if (syncSettings == null || syncSettings.getType() != SyncType.OPENCART) {
                    log.error(this.getClass().getSimpleName() + ": companyId=" + companyId + " has no Sync settings");
                    return null;
                }

                final String api = Validators.checkNotNull(syncSettings.getUrl(), "No api url");
                final String key = Validators.checkNotNull(syncSettings.getKey(), "No api key");
                final String username = Validators.checkNotNull(syncSettings.getId(), "No api username");

                Validators.checkValid(companySettings.getSync().getWarehouse(), "No Sync Default Warehouse");
                if (!Validators.isValid(companySettings.getSync().getWarehouse())) {
                    Validators.checkValid(companySettings.getWarehouse(), "No Default Warehouse");
                }

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

                final LocalDateTime dateFrom = sync.getDate();
                final LocalDateTime dateTo = DateUtils.now();

                final LocalDateTime localDateFrom = DateUtils.adjust(dateFrom, companySettings.getTimeZone());
                final LocalDateTime localDateTo = DateUtils.adjust(dateTo, companySettings.getTimeZone());

                log.info(this.getClass().getSimpleName() + ": Syncing " + dateFrom + " - " + dateTo + " (" + localDateFrom + " - " + localDateTo + ")");

                // Prepare Vat rate codes
                CountryVatRateSql countryVatRate = Validators.checkNotNull(dbServiceSQL.getById(CountryVatRateSql.class, companySettings.getCountry()),
                        "No country Vat Rates data for companyId=" + company.getId() +
                                ", country=" + companySettings.getCountry());

                VATRatesDate vatRatesDate = Validators.checkNotNull(countryVatRate.getRatesMap(dateTo.toLocalDate()),
                        "No Vat Rates data for companyId=" + company.getId() +
                                " for " + DateUtils.date(companySettings.getTimeZone()));

                OCLogin login = syncOpenCartUtilsService.login(api, key, username);
                if (login == null) return null;

                SyncResult syncResult = new SyncResult();

                if (syncSettings.getAbilities() != null && BooleanUtils.isTrue(syncSettings.getAbilities().order().toGama())) {
                    // sync Orders (OC) into Invoices (gama)
                    syncOrders(syncResult, login, syncSettings.getUrl(), syncSettings.getKey(), syncSettings.getId(), companyId, companySettings, vatRatesDate, localDateFrom, localDateTo);
                }

                // Update sync date
                sync.setDate(dateTo);

                log.info(this.getClass().getSimpleName() + ": " + syncResult);
                return syncResult;
            });

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    private OCOrdersListResponse getOrdersList(SyncSettings syncSettings, String token, LocalDateTime dateFrom, LocalDateTime dateTo) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("route", "api/order/gamaList");
            data.put("token", token);
            data.put("filter_order_status", syncSettings.getStatusIds());
            if (dateFrom != null) data.put("filter_date_from", dateFrom.toString());
            if (dateTo != null) data.put("filter_date_to", dateTo.toString());

            return syncHttpService.getRequestData(SyncHttpService.HttpMethod.POST, syncSettings.getUrl(), data, OCOrdersListResponse.class);

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return null;
        }
    }

    private void syncOrders(SyncResult syncResult, OCLogin login, String api, String key, String username, long companyId,
                            CompanySettings companySettings, VATRatesDate vatRatesDate, LocalDateTime dateFrom, LocalDateTime dateTo) {
        Validators.checkNotNull(syncResult, "syncResult is null");

        if (companySettings == null || companySettings.getSync() == null || companySettings.getSync().getType() == null)
            return;

        OCOrdersListResponse ordersList = getOrdersList(companySettings.getSync(), login.getToken(), dateFrom, dateTo);
        if (ordersList == null || CollectionsHelper.isEmpty(ordersList.getOrders())) return;

        // put all imports into queue using blocks of 10 documents
        final int BLOCK_SIZE = 10;
        List<Long> ids = new ArrayList<>(BLOCK_SIZE);

        int count = 0;
        for (OCOrder invoice : ordersList.getOrders()) {
            ids.add(invoice.getOrder_id());
            if (ids.size() == BLOCK_SIZE) {
                String taskId = taskQueueService.queueTask(new OCOrderTask(api, key, username, companyId, ids, vatRatesDate));
                syncResult.getTasksIds().add(taskId);

                ids = new ArrayList<>(BLOCK_SIZE);
            }
            ++count;
        }
        if (CollectionsHelper.hasValue(ids)) {
            String taskId = taskQueueService.queueTask(new OCOrderTask(api, key, username, companyId, ids, vatRatesDate));
            syncResult.getTasksIds().add(taskId);
        }

        log.info(this.getClass().getSimpleName() + ": Synced Orders count=" + count);
    }
}
