package lt.gama.service.sync.openCart;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.ImportSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.sql.system.SyncSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.SyncType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TaskQueueService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.sync.i.ISyncOpenCartAService;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.SyncResult;
import lt.gama.service.sync.openCart.model.*;
import lt.gama.service.sync.openCart.tasks.OCOrderTask;
import lt.gama.service.sync.openCart.tasks.OCPartSpecialPriceTask;
import lt.gama.service.sync.openCart.tasks.OCPartTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * gama-online
 * Created by valdas on 2019-03-02.
 */
public class SyncOpenCartAService implements ISyncOpenCartAService {

    private static final Logger log = LoggerFactory.getLogger(SyncOpenCartService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final SyncOpenCartUtilsService syncOpenCartUtilsService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final SyncHttpService syncHttpService;
    private final TaskQueueService taskQueueService;


    public SyncOpenCartAService(SyncOpenCartUtilsService syncOpenCartUtilsService,
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

    private Set<String> getCustomersIds(String api, OCLogin login) {
        OCCompanyIdsResponse response = syncHttpService.getRequestData(
                SyncHttpService.HttpMethod.POST,
                api,
                Map.of("route", "api/customerproduct/sendCustomerCompanyId", "token", login.getToken()),
                null, null,
                OCCompanyIdsResponse.class,
                login.getSession());
        if (response == null || CollectionsHelper.isEmpty(response.getCompanyIds())) return null;
        return response.getCompanyIds().stream().filter(StringHelper::hasValue).collect(Collectors.toSet());
    }

    private void uploadSpecPrices(SyncResult syncResult, String api, String key, String username, List<Tuple> results) {
        // last inserted will overwrite previously added if the same by key (counterpartyId + partId)
        Map<CounterpartyPart, SpecPrice> specPrices = new LinkedHashMap<>();

        for (Tuple o : results) {
            var partDiscPriceAmount = o.get("partDiscPriceAmount", BigDecimal.class);
            if (BigDecimalUtils.isPositive(partDiscPriceAmount)) {
                SpecPrice specPrice = new SpecPrice(
                        o.get("counterpartyId", BigInteger.class).longValue(),
                        o.get("partId", BigInteger.class).longValue(),
                        partDiscPriceAmount);
                specPrices.put(specPrice.key(), specPrice);
            }
        }

        specPrices.values().forEach(specPrice ->
            taskQueueService.queueTask(new OCPartSpecialPriceTask(api, key, username, auth.getCompanyId(), specPrice.counterpartyId(),
                    specPrice.partId(), specPrice.partDiscPrice())));

        syncResult.setSpecialPrices(specPrices.size());
        log.info(this.getClass().getSimpleName() + ": Sync Special Prices Tasks count = " + specPrices.size());
    }

    @Override
    public void syncSpecialPrices(SyncResult syncResult, String api, String key, String username, LocalDateTime dateFrom) {
        Validators.checkNotNull(syncResult, "syncResult is null");

        OCLogin login = syncOpenCartUtilsService.login(api, key, username);
        if (login == null) return;

        List<Long> ids = CollectionsHelper.streamOf(getCustomersIds(api, login)).map(Long::valueOf).toList();
        if (CollectionsHelper.isEmpty(ids)) return;

        @SuppressWarnings("unchecked")
        List<Tuple> results = entityManager.createNativeQuery("""
                        SELECT DISTINCT ON (c.id, p.id) d.date, d.number, c.id AS counterpartyId, p.id AS partId,
                            ip.discounted_price_amount AS partDiscPriceAmount,
                            ip.discounted_price_currency AS partDiscPriceCurrency
                        FROM invoice i
                        JOIN documents d ON i.id = d.id
                        JOIN counterparties c ON d.counterparty_id = c.id
                        JOIN invoice_parts ip ON i.id = ip.parent_id
                        JOIN parts p ON ip.part_id = p.id
                        WHERE d.company_id = :companyId
                            AND (d.archive IS null OR d.archive = false)
                            AND d.updated_on >= :dateFrom
                            AND c.id IN :counterpartyIds
                        ORDER BY c.id, p.id, date DESC, ordinal DESC, number DESC, d.id DESC
                        """, Tuple.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("dateFrom", dateFrom)
                .setParameter("counterpartyIds", ids)
                .getResultList();

        uploadSpecPrices(syncResult, api, key, username, results);
    }

    @Override
    public void syncSpecialPricesForCustomer(String api, String key, String username, long counterpartyId) {
        OCLogin login = syncOpenCartUtilsService.login(api, key, username);
        if (login == null) return;

        final LocalDateTime dateFrom = DateUtils.now().minusYears(1).withDayOfMonth(1);
        final LocalDateTime dateTo = DateUtils.now().withDayOfMonth(1);

        //TODO *** fix after migration. *** Make test
        @SuppressWarnings("unchecked")
        List<Tuple> results = entityManager.createNativeQuery("""
                        SELECT DISTINCT ON (p.id) d.date, d.number, c.id AS counterpartyId, p.id AS partId,
                            ip.discounted_price_amount AS partDiscPriceAmount,
                            ip.discounted_price_currency AS partDiscPriceCurrency
                        FROM invoice i
                        JOIN documents d ON i.id = d.id
                        JOIN counterparties c ON d.counterparty_id = c.id
                        JOIN invoice_parts ip ON i.id = ip.parent_id
                        JOIN parts p ON ip.part_id = p.id
                        WHERE d.company_id = :companyId
                            AND (d.archive IS null OR d.archive = false)
                            AND d.updated_on >= :dateFrom
                            AND d.updated_on < :dateTo
                            AND c.id = :counterpartyId
                        ORDER BY p.id, date DESC, ordinal DESC, number DESC, d.id DESC
                        """, Tuple.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo)
                .setParameter("counterpartyId", counterpartyId)
                .getResultList();

        uploadSpecPrices(new SyncResult(), api, key, username, results);
    }

    private void syncProducts(SyncResult syncResult, String api, String key, String username, LocalDateTime date) {
        Validators.checkNotNull(syncResult, "syncResult is null");

        final int BLOCK_SIZE = 100;

        List<Long> ids = new ArrayList<>(BLOCK_SIZE);
        int count = 0;

        List<Long> partsIds = entityManager.createQuery(
                        "SELECT id" +
                                " FROM " + PartSql.class.getName() + " p" +
                                " WHERE companyId = :companyId" +
                                " AND updatedOn >= :updatedOn", Long.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("updatedOn", date != null ? date : DateUtils.now().minusYears(100))
                .getResultList();

        for (long id : partsIds) {
            ids.add(id);
            if (ids.size() == BLOCK_SIZE) {
                syncResult.getTasksIds().add(taskQueueService.queueTask(new OCPartTask(api, key, username, auth.getCompanyId(), ids)));
                ids = new ArrayList<>(BLOCK_SIZE);
            }

            ++count;
        }

        if (CollectionsHelper.hasValue(ids)) {
            syncResult.getTasksIds().add(taskQueueService.queueTask(new OCPartTask(api, key, username, auth.getCompanyId(), ids)));
        }

        syncResult.setParts(count);
        log.info(this.getClass().getSimpleName() + ": companyId=" + auth.getCompanyId() + " count=" + count);
    }

    /**
     * Upload customer by id to OpenCart from Datastore
     */
    @Override
    public boolean uploadCustomer(String api, String key, String username, long counterpartyId, String email) {
        CounterpartySql counterparty = dbServiceSQL.getAndCheck(CounterpartySql.class, counterpartyId);
        CompanySettings companySettings = dbServiceSQL.getCompanySettings(auth.getCompanyId());
        SyncSettings syncSettings = Validators.checkNotNull(companySettings.getSync(), "No Sync Settings");
        Validators.checkNotNull(syncSettings.getUrl(), "No URL in Sync Settings");

        OCLogin login = syncOpenCartUtilsService.login(api, key, username);
        if (login == null) {
            throw new GamaException("Can't login");
        }

        // check if customer is not uploaded
        Set<String> ids = getCustomersIds(api, login);
        boolean isNewCustomer = CollectionsHelper.isEmpty(ids) || !ids.contains(String.valueOf(counterpartyId));

        OCIdResponse response = syncHttpService.getRequestData(
                SyncHttpService.HttpMethod.POST,
                syncSettings.getUrl(),
                Map.of("route", "api/customerproduct/addCustomerAndGroup", "token", login.getToken()),
                SyncHttpService.ContentType.FORM,
                Map.of(
                        "company_name", counterparty.getName(),
                        "email", email,
                        "password", "-none-",
                        "status", "1",
                        "name", counterparty.getId().toString()),
                OCIdResponse.class,
                login.getSession());

        if (response == null || StringHelper.isEmpty(response.getId())) {
            throw new GamaException("Error: counterpartyId = " + counterpartyId + ", companyId = " +  auth.getCompanyId());

        } else {
            ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(auth.getCompanyId(), CounterpartySql.class, response.getId()));
            if (imp != null) {
                if (imp.getEntityId() != counterpartyId) {
                    log.warn(this.getClass().getSimpleName() +
                            ": Not the same counterpartyId" +
                            ", old=" + imp.getEntityId() +
                            ", new=" + counterpartyId);
                    imp.setEntityDb(DBType.POSTGRESQL);
                    imp.setEntityId(counterpartyId);
                    dbServiceSQL.saveEntity(imp);
                }
            } else {
                imp = new ImportSql(auth.getCompanyId(), CounterpartySql.class, response.getId(), counterpartyId, DBType.POSTGRESQL);
                dbServiceSQL.saveEntity(imp);
            }
        }

        return isNewCustomer;
    }


    /**
     * SYNCHRONIZATION
     * add OpenCart synchronization date to Sync entity
     */
    @Override
    public SyncResult sync(long companyId) {
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));
        try {
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                SyncSql sync = dbServiceSQL.getById(SyncSql.class, companyId);
                if (sync == null) return null;

                CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company with id=" + companyId);

                if (company.getStatus() == null || company.getStatus() == CompanyStatusType.INACTIVE) {
                    log.info(this.getClass().getSimpleName() + ": company=" + companyId + " status: null or INACTIVE");
                    return null;
                }

                CompanySettings companySettings = Validators.checkNotNull(company.getSettings(), "Company " + companyId + " has no settings");

                SyncSettings syncSettings = Validators.checkNotNull(companySettings.getSync(), "Company " + companyId + " has no Sync settings");
                Validators.checkArgument(syncSettings.getType() == SyncType.OPENCART_A, "Company " + companyId + " has wrong Sync type");

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

                final String api = Validators.checkNotNull(syncSettings.getUrl(), "No api url");
                final String key = Validators.checkNotNull(syncSettings.getKey(), "No api key");
                final String username = Validators.checkNotNull(syncSettings.getId(), "No api username");

                SyncResult syncResult = new SyncResult();

                if (syncSettings.getAbilities() != null && BooleanUtils.isTrue(syncSettings.getAbilities().product().fromGama())) {
                    //sync parts
                    syncProducts(syncResult, api, key, username, dateFrom);

                    if (BooleanUtils.isTrue(syncSettings.getAbilities().price().fromGama())) {
                        //sync special prices
                        syncSpecialPrices(syncResult, api, key, username, dateFrom);
                    }
                }

                if (syncSettings.getAbilities() != null && BooleanUtils.isTrue(syncSettings.getAbilities().order().toGama())) {
                    // sync Orders (OC) into Invoices (gama) - using local (adjusted) dates
                    syncOrders(syncResult, api, key, username, companyId, companySettings, vatRatesDate, localDateFrom, localDateTo);
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

    private OCOrdersListResponse getOrdersList(String token, SyncHttpService.Cookie session, SyncSettings syncSettings, LocalDateTime dateFrom, LocalDateTime dateTo) {
        try {
            Map<String, String> query = new HashMap<>();
            query.put("route", "api/order/gamaList");
            query.put("token", token);
            query.put("filter_order_status", syncSettings.getStatusIds());
            if (dateFrom != null) query.put("filter_date_from", dateFrom.toString());
            if (dateTo != null) query.put("filter_date_to", dateTo.toString());

            return syncHttpService.getRequestData(SyncHttpService.HttpMethod.POST, syncSettings.getUrl(), query, null, null, OCOrdersListResponse.class, session);

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return null;
        }
    }

    private void syncOrders(SyncResult syncResult, String api, String key, String username, long companyId, CompanySettings companySettings, VATRatesDate vatRatesDate, LocalDateTime dateFrom, LocalDateTime dateTo) {
        Validators.checkNotNull(syncResult, "syncResult is null");

        if (companySettings == null || companySettings.getSync() == null || companySettings.getSync().getType() == null)
            return;

        OCLogin login = syncOpenCartUtilsService.login(api, key, username);
        if (login == null) return;

        OCOrdersListResponse ordersList = getOrdersList(login.getToken(), login.getSession(), companySettings.getSync(), dateFrom, dateTo);
        if (ordersList == null || CollectionsHelper.isEmpty(ordersList.getOrders())) return;

        // put all imports into queue using blocks of 10 documents
        final int BLOCK_SIZE = 10;
        List<Long> ids = new ArrayList<>(BLOCK_SIZE);

        int count = 0;
        for (OCOrder invoice : ordersList.getOrders()) {
            ids.add(invoice.getOrder_id());
            if (ids.size() == BLOCK_SIZE) {
                syncResult.getTasksIds().add(taskQueueService.queueTask(new OCOrderTask(api, key, username, companyId, ids, vatRatesDate)));
                ids = new ArrayList<>(BLOCK_SIZE);
            }
            ++count;
        }
        if (CollectionsHelper.hasValue(ids)) {
            syncResult.getTasksIds().add(taskQueueService.queueTask(new OCOrderTask(api, key, username, companyId, ids, vatRatesDate)));
        }

        syncResult.setOrders(count);
        log.info(this.getClass().getSimpleName() + ": Sync Orders count=" + count);
    }
}

record SpecPrice(long counterpartyId, long partId, BigDecimal partDiscPrice) {
    CounterpartyPart key() {
        return new CounterpartyPart(counterpartyId, partId);
    }
}

record CounterpartyPart(long counterpartyId, long partId) {}
