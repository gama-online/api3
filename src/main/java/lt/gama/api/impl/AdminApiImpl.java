package lt.gama.api.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.CalendarAdminRequest;
import lt.gama.api.request.CreateCompanyRequest;
import lt.gama.api.request.GLAccountRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.LoginResponse;
import lt.gama.api.response.PageResponse;
import lt.gama.api.response.UploadResponse;
import lt.gama.api.service.AdminApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.dto.entities.AccountDto;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.GLAccountDto;
import lt.gama.model.dto.system.CalendarSettingsDto;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseCompanySql_;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.system.*;
import lt.gama.model.sql.system.id.CalendarId;
import lt.gama.model.type.CalendarMonth;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CurrencySettings;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AdminApiImpl implements AdminApi {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${gama.version}") private String appVersion;

    private final DBServiceSQL dbServiceSQL;
    private final AccountService accountService;
    private final StorageService storageService;
    private final CalendarService calendarService;
    private final AccountingService accountingService;
    private final GLAccountSqlMapper glAccountSqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final Auth auth;
    private final GLService glService;
    private final AccountSqlMapper accountSqlMapper;
    private final CompanySqlMapper companySqlMapper;
    private final CalendarSettingsSqlMapper calendarSettingsSqlMapper;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final APIResultService apiResultService;


    public AdminApiImpl(DBServiceSQL dbServiceSQL, AccountService accountService, StorageService storageService, CalendarService calendarService, AccountingService accountingService, GLAccountSqlMapper glAccountSqlMapper, EmployeeSqlMapper employeeSqlMapper, Auth auth, GLService glService, AccountSqlMapper accountSqlMapper, CompanySqlMapper companySqlMapper, CalendarSettingsSqlMapper calendarSettingsSqlMapper, AuthSettingsCacheService authSettingsCacheService, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.accountService = accountService;
        this.storageService = storageService;
        this.calendarService = calendarService;
        this.accountingService = accountingService;
        this.glAccountSqlMapper = glAccountSqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.auth = auth;
        this.glService = glService;
        this.accountSqlMapper = accountSqlMapper;
        this.companySqlMapper = companySqlMapper;
        this.calendarSettingsSqlMapper = calendarSettingsSqlMapper;
        this.authSettingsCacheService = authSettingsCacheService;
        this.apiResultService = apiResultService;
    }


    @Override
    public APIResult<String> getVersion() throws GamaApiException {
        return apiResultService.result(() -> appVersion);
    }

    @Override
    public APIResult<SystemSettingsSql> getSystemSettings() throws GamaApiException {
        return apiResultService.result(() ->
            dbServiceSQL.getById(SystemSettingsSql.class, SystemSettingsSql.ID));
    }

    @Override
    public APIResult<SystemSettingsSql> saveSystemSettings(SystemSettingsSql systemSettings) throws GamaApiException {
        return apiResultService.result(() ->
            dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                SystemSettingsSql entity = dbServiceSQL.getById(SystemSettingsSql.class, SystemSettingsSql.ID);
                if (entity == null) entity = new SystemSettingsSql();

                entity.setAccountPrice(systemSettings.getAccountPrice());
                entity.setOwnerCompanyId(systemSettings.getOwnerCompanyId());
                entity.setSubscriptionServiceId(systemSettings.getSubscriptionServiceId());
                entity.setSubscriptionWarehouseId(systemSettings.getSubscriptionWarehouseId());

                return dbServiceSQL.saveEntity(entity);
            }));
    }

    @Override
    public APIResult<PageResponse<CompanyDto, Void>> companyList(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            dbServiceSQL.queryPage(request, CompanySql.class, null,
                    companySqlMapper,
                    () -> allQueryCompany(request),
                    () -> countQueryCompany(request),
                    resp -> dataQueryCompany(request, resp)));
    }

    private String toCompanyField(String field, boolean isNativeField) {
        return CompanySql_.NAME.equalsIgnoreCase(field) ? "name"
                : CompanySql_.ID.equalsIgnoreCase(field) ? "id"
                : CompanySql_.LAST_LOGIN.equalsIgnoreCase(field) ? (isNativeField ? "last_login" : CompanySql_.LAST_LOGIN)
                : CompanySql_.ACTIVE_ACCOUNTS.equalsIgnoreCase(field) ? (isNativeField ? "active_accounts" : CompanySql_.ACTIVE_ACCOUNTS)
                : CompanySql_.PAYER_ACCOUNTS.equalsIgnoreCase(field) ? (isNativeField ? "payer_accounts" : CompanySql_.PAYER_ACCOUNTS)
                : null;
    }

    private String companyNativeOrder(String field) {
        return companyOrder(field, true);
    }

    private String companyJPAOrder(String field) {
        return companyOrder(field, false);
    }

    private String companyOrder(String field, boolean isNativeField) {
        if (StringHelper.isEmpty(field)) return "ORDER BY a.name, a.id";
        String order = "";
        if (field.charAt(0) == '-') {
            order = "DESC";
            field = field.substring(1);
        }
        if (CompanySql_.NAME.equalsIgnoreCase(field) ||
                CompanySql_.ID.equalsIgnoreCase(field) ||
                CompanySql_.LAST_LOGIN.equalsIgnoreCase(field)) {
            return "ORDER BY a." + toCompanyField(field, isNativeField) + " " + order + ", a.id";
        } else if (CompanySql_.ACTIVE_ACCOUNTS.equalsIgnoreCase(field) ||
                CompanySql_.PAYER_ACCOUNTS.equalsIgnoreCase(field)) {
            return "ORDER BY COALESCE(a." + toCompanyField(field, isNativeField) + ", 0) " + order + ", a.id";
        } else if ("totalAccounts".equalsIgnoreCase(field)) {
            return "ORDER BY (COALESCE(a." + toCompanyField(CompanySql_.ACTIVE_ACCOUNTS, isNativeField) +
                    ",0) + COALESCE(a." + toCompanyField(CompanySql_.PAYER_ACCOUNTS, isNativeField) + ",0)) " + order + ", a.id";
        }
        return "ORDER BY a.name, a.id";
    }

    private Query allQueryCompany(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT * FROM companies a");
        sj.add("LEFT JOIN companies p on a.payer_id=p.id");
        makeQueryCompany(request, sj, params);
        sj.add(companyNativeOrder(request.getOrder()));

        Query query = entityManager.createNativeQuery(sj.toString(), CompanySql.class);
        params.forEach(query::setParameter);

        return query;
    }

    private Integer countQueryCompany(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT a.id) FROM companies a");
        sj.add("LEFT JOIN companies p on a.payer_id=p.id");
        makeQueryCompany(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void makeQueryCompany(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        sj.add("WHERE (a.hidden IS null OR a.hidden = false)");
        if (PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.ARCHIVE) instanceof Boolean value && value) {
            sj.add("AND (a.archive = true)");
        } else {
            sj.add("AND (a.archive IS null OR a.archive = false)");
        }
        Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.COMPANY_STATUS);
        if (value != null) {
            CompanyStatusType status = value instanceof CompanyStatusType ? (CompanyStatusType) value : CompanyStatusType.from(value.toString());
            if (status != null) {
                sj.add("AND a.status = :status");
                params.put("status", status.toString());
            }
        }
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("CAST(a.id AS TEXT) ILIKE :filter");
            sj.add("OR a.subscriber_email ILIKE :filter");
            sj.add("OR trim(unaccent(a.name)) ILIKE :filter");
            sj.add("OR trim(unaccent(p.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(a.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(p.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter().trim()).toLowerCase() + "%");
        }
    }

    private Query idsPageQueryCompany(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT a.id AS id, a.name, a.last_login");
        sj.add(", COALESCE(a.active_accounts, 0) AS active_accounts");
        sj.add(", COALESCE(a.payer_accounts, 0) AS payer_accounts");
        sj.add(", COALESCE(a.active_accounts, 0) + COALESCE(a.payer_accounts, 0) AS total_accounts");
        sj.add("FROM companies a");
        sj.add("LEFT JOIN companies p on a.payer_id=p.id");
        makeQueryCompany(request, sj, params);
        sj.add(companyNativeOrder(request.getOrder()));

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryCompany(PageRequest request, PageResponse<CompanyDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryCompany(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                                "SELECT a FROM " + CompanySql.class.getName() + " a" +
                                        " LEFT JOIN FETCH a." + CompanySql_.PAYER +
                                        " WHERE a.id IN :ids" +
                                        " " + companyJPAOrder(request.getOrder()),
                                CompanySql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", BigInteger.class).longValue())
                                        .collect(Collectors.toList()));
    }

    @Override
    public APIResult<CompanyDto> companySave(CompanyDto request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (request.getSettings() == null) request.setSettings(new CompanySettings());
            if (request.getSettings().getRegion() == null) request.getSettings().setRegion("LT");
            if (request.getSettings().getCurrency() == null) request.getSettings().setCurrency(new CurrencySettings());
            if (request.getSettings().getCurrency().getCode() == null) request.getSettings().getCurrency().setCode("EUR");

            long companyId = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {

                final Long oldPayer;

                // update
                CompanySql company;
                if (request.getId() != null) {
                    company = dbServiceSQL.getById(CompanySql.class, request.getId());

                    oldPayer = Validators.isValid(company.getPayer()) ? company.getPayer().getId() : null;
                    boolean isBecameInactive = request.getStatus() == CompanyStatusType.INACTIVE && company.getStatus() != CompanyStatusType.INACTIVE;

                    company.setName(request.getName());
                    company.setContacts(request.getContacts());
                    company.setBusinessName(request.getBusinessName());
                    company.setRegistrationAddress(request.getRegistrationAddress());
                    company.setBusinessAddress(request.getBusinessAddress());
                    company.setLocations(request.getLocations());
                    company.setBanks(request.getBanks());
                    company.setCode(request.getCode());
                    company.setVatCode(request.getVatCode());
                    company.setSsCode(request.getSsCode());
                    company.setLogo(request.getLogo());
                    company.setEmail(request.getEmail());
                    company.setCcEmail(request.getCcEmail());
                    company.setSettings(request.getSettings());
                    company.setStatus(request.getStatus());
                    company.setTotalPrice(GamaMoneyUtils.isPositive(request.getTotalPrice()) ? request.getTotalPrice() : null);
                    company.setAccountPrice(GamaMoneyUtils.isPositive(request.getAccountPrice()) ? request.getAccountPrice() : null);
                    company.setSubscriptionDate(request.getSubscriptionDate());
                    company.setSubscriberName(request.getSubscriberName());
                    company.setSubscriberEmail(request.getSubscriberEmail());
                    company.setPayer(Validators.isValid(request.getPayer())
                            ? entityManager.getReference(CompanySql.class, request.getPayer().getId())
                            : null);
                    company.setExCompanies(request.getExCompanies());
                    company = dbServiceSQL.saveEntity(company);

                    // remove or update company info in accounts
                    if (isBecameInactive) {
                        accountService.removeCompany(company.getId());
                    } else {
                        accountService.updateCompanyInfo(company.getId());
                    }

                } else {
                    oldPayer = null;
                    company = dbServiceSQL.saveEntity(companySqlMapper.toEntity(request));
                }

                // refresh always!!!
                Long newPayer = Validators.isValid(company.getPayer()) ? company.getPayer().getId() : null;
                final LocalDate dateNow = DateUtils.now(request.getSettings().getTimeZone()).toLocalDate();
                accountingService.refreshCompaniesConnections(company.getId(), dateNow, oldPayer, newPayer);

                return company.getId();
            });

            return companySqlMapper.toDto(dbServiceSQL.getById(CompanySql.class, companyId, CompanySql.GRAPH_ALL));
        });
    }

    @Override
    public APIResult<CompanyDto> companyGet(CompanyIdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            companySqlMapper.toDto(Validators.checkNotNull(
                    dbServiceSQL.getById(CompanySql.class, request.companyId, CompanySql.GRAPH_ALL), "No company")));
    }

    @Override
    public APIResult<Void> companyDelete(CompanyIdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, request.companyId), "No company");

            company.setArchive(true);
            company.setStatus(CompanyStatusType.INACTIVE);
            company.setActiveAccounts(null);
            company.setOtherAccounts(null);
            dbServiceSQL.saveEntity(company);

            accountService.removeCompany(request.companyId);
        });
    }

    @Override
    public APIResult<GamaMoney> companyMonthlyPayment(CompanyIdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, request.companyId, CompanySql.GRAPH_ALL), "No company");
            return accountingService.companyAmountToPay(company);
        });
    }

    @Override
    public APIResult<CompanyDto> companyCreate(CreateCompanyRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            companySqlMapper.toDto(accountService.companyCreate(request)));
    }

    @Override
    public APIResult<PageResponse<EmployeeDto, Void>> employeeList(EmployeeListRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId == null ? 0 : request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return dbServiceSQL.queryPage(request, EmployeeSql.class,
                    EmployeeSql.GRAPH_ALL, employeeSqlMapper,
                    (cb, root) -> whereEmployee(request, cb, root),
                    (cb, root) -> "mainIndex".equalsIgnoreCase(request.getOrder()) || "name".equalsIgnoreCase(request.getOrder())
                            ? Arrays.asList(
                                    cb.asc(root.get(EmployeeSql_.NAME)),
                                    cb.asc(root.get(EmployeeSql_.EMAIL)),
                                    cb.asc(root.get(EmployeeSql_.ID)))
                            : Arrays.asList(
                                    cb.desc(root.get(EmployeeSql_.NAME)),
                                    cb.asc(root.get(EmployeeSql_.EMAIL)),
                                    cb.asc(root.get(EmployeeSql_.ID))),
                    (cb, root) -> Arrays.asList(
                            root.get(EmployeeSql_.NAME),
                            root.get(EmployeeSql_.EMAIL),
                            root.get(EmployeeSql_.ID).alias("id")));
        });
    }

    private Predicate whereEmployee(PageRequest request, CriteriaBuilder cb, Root<?> root) {
        if (StringUtils.isNotBlank(request.getFilter())) {
            String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
            return cb.or(
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeSql_.NAME))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeSql_.EMAIL))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.join(EmployeeSql_.ROLES, JoinType.LEFT).get(RoleSql_.NAME))), "%" + filter + "%")
            );
        }
        return null;
    }

    @Override
    public APIResult<PageResponse<AccountDto, Void>> accountList(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            dbServiceSQL.queryPage(request, AccountSql.class, null,
                    accountSqlMapper,
                    () -> allQueryAccount(request),
                    () -> countQueryAccount(request),
                    resp -> dataQueryAccount(request, resp)));
    }

    private String accountOrder(String field) {
        if (StringHelper.isEmpty(field)) return "ORDER BY a.id";
        String order = "";
        if (field.charAt(0) == '-') {
            order = "DESC";
            field = field.substring(1);
        }
        if ("id".equalsIgnoreCase(field)) {
            return "ORDER BY a.id " + order;
        }
        return "ORDER BY a.id";
    }

    private Query allQueryAccount(PageRequest request) {
        return entityManager.createQuery(
                "SELECT a FROM " + AccountSql.class.getName() + " a" +
                        " LEFT JOIN FETCH a." + AccountSql_.PAYER +
                        " " + accountOrder(request.getOrder()));
    }

    private Integer countQueryAccount(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT a.id) FROM accounts a");
        sj.add("LEFT JOIN companies p on a.payer_id=p.id");
        makeQueryAccount(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void makeQueryAccount(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("LEFT JOIN jsonb_array_elements_text(jsonb_path_query_array(companies, '$.companyName')) com ON true");
        }
        sj.add("WHERE (a.archive IS null OR a.archive = false)");
        sj.add("AND (a.hidden IS null OR a.hidden = false)");
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("a.id ILIKE :filter");
            sj.add("OR trim(unaccent(com.value)) ILIKE :filter");
            sj.add("OR trim(unaccent(p.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(com.value), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(p.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter().trim()).toLowerCase() + "%");
        }
    }

    private Query idsPageQueryAccount(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT a.id FROM accounts a");
        sj.add("LEFT JOIN companies p on a.payer_id=p.id");
        makeQueryAccount(request, sj, params);
        sj.add(accountOrder(request.getOrder()));

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryAccount(PageRequest request, PageResponse<AccountDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryAccount(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                                "SELECT a FROM " + AccountSql.class.getName() + " a" +
                                        " LEFT JOIN FETCH a." + AccountSql_.PAYER + " p" +
                                        " WHERE a.id IN :ids" +
                                        " " + accountOrder(request.getOrder()) ,
                                AccountSql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", String.class))
                                        .collect(Collectors.toList()));
    }

    @Override
    public APIResult<AccountDto> accountGet(AccountGetRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            AccountSql account = Validators.checkNotNull(dbServiceSQL.getById(AccountSql.class, request.accountId, AccountSql.GRAPH_ALL), "No account");
            return Front.Account(account);
        });
    }

    @Override
    public APIResult<AccountDto> accountAssignPayer(AccountAssignPayerRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                Front.Account(Validators.checkNotNull(accountService.assignPayer(request.accountId, request.payerId, DateUtils.date()), "No account")));
    }

    @Override
    public APIResult<LoginResponse> employeeImpersonate(EmployeeImpersonateRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return accountService.impersonate(request.companyId, request.employeeId);
        });
    }

    @Override
    public APIResult<LoginResponse> accountImpersonate(AccountImpersonateRequest request) throws GamaApiException {
        return apiResultService.result(() -> accountService.impersonate(request.accountId));
    }

    @Override
    public APIResult<UploadResponse> storageUrl(StorageUrlRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId == null ? 0 : request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return storageService.getUploadUrlv4(request.contentType, request.folder, request.fileName, request.isPublic, request.sourceFileName);
        });
    }

    @Override
    public APIResult<String> storageImport(StorageImportRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId == null ? 0 : request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return storageService.startImport(request.fileName, request.entity, request.delete, request.format);
        });
    }

    @Override
    public APIResult<String> storageUpload(UploadDataRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(request.companyId));

            return storageService.upload(request.data, request.folder, request.fileName, request.contentType);
        });
    }

    @Override
    public APIResult<PageResponse<GLAccountDto, Void>> gLAccountList(GLAccountListRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId == null ? 0 : request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return dbServiceSQL.list(request, GLAccountSql.class, null, glAccountSqlMapper,
                    (cb, root) ->
                            request.companyId == null || request.companyId <= 0
                                    ? cb.equal(root.get(BaseCompanySql_.COMPANY_ID), 0)
                                    : null,
                    (cb, root) -> Collections.singletonList(cb.asc(root.get(GLAccountSql_.NUMBER))),
                    (cb, root) -> Arrays.asList(root.get(GLAccountSql_.NUMBER), root.get(GLAccountSql_.ID).alias("id")));
        });
    }

    @Override
    public APIResult<GLAccountDto> gLAccountSave(GLAccountRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(0L);
            auth.setSettings(null);

            if (request.getParent() != null) {
                dbServiceSQL.saveEntityInCompany(glAccountSqlMapper.toEntity(request.getParent()));
            }
            GLAccountSql entity = glAccountSqlMapper.toEntity(request.getModel());
            dbServiceSQL.saveEntityInCompany(entity);
            return glAccountSqlMapper.toDto(entity);
        });
    }

    @Override
    public APIResult<Void> gLAccountDelete(GLAccountRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(0L);
            auth.setSettings(null);

            glService.deleteAccount(request);
        });
    }

    /*
     * Calendar API
     */

    @Override
    public APIResult<CalendarSql> calendarGetYear(CalendarAdminRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            calendarService.getYearInCountry(request.getCountry(), request.getYear(),
                    BooleanUtils.isTrue(request.getRefresh())));
    }

    @Override
    public APIResult<CalendarMonth> calendarGetMonth(CalendarAdminRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            calendarService.getMonthInCountry(request.getCountry(), request.getYear(), request.getMonth(),
                    BooleanUtils.isTrue(request.getRefresh())));
    }

    @Override
    public APIResult<Void> calendarSaveMonth(CalendarAdminRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            calendarService.saveMonthInCountry(request.getCountry(), request.getYear(), request.getMonth(), request.getCalendarMonth()));
    }

    @Override
    public APIResult<List<CalendarSettingsDto>> calendarSettingsList(CalendarSettingsListRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            entityManager.createQuery(
                    "SELECT a FROM " + CalendarSettingsSql.class.getName() + " a" +
                            " WHERE id.country = :country" +
                            " ORDER BY id.year", CalendarSettingsSql.class)
                    .setParameter("country", request.country)
                    .getResultStream()
                    .map(calendarSettingsSqlMapper::toDto)
                    .collect(Collectors.toList()));
    }

    @Override
    public APIResult<CalendarSettingsDto> calendarSettingsGet(CalendarSettingsGetRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            calendarSettingsSqlMapper.toDto(
                    dbServiceSQL.getById(CalendarSettingsSql.class, new CalendarId(request.country, request.year))));
    }

    @Override
    public APIResult<CalendarSettingsDto> calendarSettingsSave(CalendarSettingsDto request) throws GamaApiException {
        return apiResultService.result(() ->
                calendarSettingsSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                    CalendarSettingsSql entity = dbServiceSQL.getById(CalendarSettingsSql.class,
                            new CalendarId(request.getCountry(), request.getYear()));
                    entity.setHolidays(request.getHolidays());
                    return dbServiceSQL.saveEntity(entity);
                })));
    }

    @Override
    public APIResult<Void> calendarSettingsDelete(CalendarSettingsDto request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.executeInTransaction(entityManager ->
                        entityManager.createQuery("DELETE FROM " + CalendarSettingsSql.class.getName() + " c" +
                                        " WHERE id.country = :country AND id.year = :year")
                                .setParameter("country", request.getCountry())
                                .setParameter("year", request.getYear())
                                .executeUpdate()));
    }

    @Override
    public APIResult<PageResponse<CountryWorkTimeCodeSql, Void>> workTimeCodesList() throws GamaApiException {
        return apiResultService.result(() -> {
            PageResponse<CountryWorkTimeCodeSql, Void> response = new PageResponse<>();
            response.setItems(dbServiceSQL.makeQuery(CountryWorkTimeCodeSql.class).getResultList());
            response.setTotal(CollectionsHelper.hasValue(response.getItems()) ? response.getItems().size() : 0);
            return response;
        });
    }

    @Override
    public APIResult<CountryWorkTimeCodeSql> workTimeCodesGet(WorkTimeCodesGetRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.getById(CountryWorkTimeCodeSql.class, request.id));
    }

    @Override
    public APIResult<CountryWorkTimeCodeSql> workTimeCodesSave(CountryWorkTimeCodeSql request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (request.getId() == null || request.getId().isEmpty()) throw new GamaException("No country");
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                CountryWorkTimeCodeSql code = dbServiceSQL.getById(CountryWorkTimeCodeSql.class, request.getId());
                if (code == null) code = new CountryWorkTimeCodeSql(request.getId());
                code.setCodes(request.getCodes());
                dbServiceSQL.saveEntity(code);
                return code;
            });
        });
    }

    @Override
    public APIResult<InvoiceDto> generateSubscriptionInvoice(GenerateSubscriptionInvoiceRequest request) throws GamaApiException {
        return apiResultService.result(() -> accountingService.invoicingCompany(request.date, request.companyId, BooleanUtils.isTrue(request.debug)));
    }

    @Override
    public APIResult<PageResponse<CountryVatCodeSql, Void>> vatCodeList() throws GamaApiException {
        return apiResultService.result(() -> {
            PageResponse<CountryVatCodeSql, Void> response = new PageResponse<>();
            response.setItems(dbServiceSQL.makeQuery(CountryVatCodeSql.class).getResultList());
            response.setTotal(CollectionsHelper.hasValue(response.getItems()) ? response.getItems().size() : 0);
            return response;
        });
    }

    @Override
    public APIResult<CountryVatCodeSql> vatCodeGet(VatCodeGetGetRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.getById(CountryVatCodeSql.class, request.id));
    }

    @Override
    public APIResult<CountryVatCodeSql> vatCodeSave(CountryVatCodeSql request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (request.getId() == null || request.getId().isEmpty()) throw new GamaException("No country");
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                CountryVatCodeSql entity = dbServiceSQL.getById(CountryVatCodeSql.class, request.getId());
                if (entity == null) entity = new CountryVatCodeSql(request.getId());
                entity.setCodes(request.getCodes());
                return dbServiceSQL.saveEntity(entity);
            });
        });
    }

    @Override
    public APIResult<List<CountryVatNoteSql>> vatNoteList() throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.makeQuery(CountryVatNoteSql.class).getResultList());
    }

    @Override
    public APIResult<CountryVatNoteSql> vatNoteGet(VatNoteGetRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.getById(CountryVatNoteSql.class, request.id));
    }

    @Override
    public APIResult<CountryVatNoteSql> vatNoteSave(CountryVatNoteSql request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (request.getId() == null || request.getId().isEmpty()) throw new GamaException("No country");
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                CountryVatNoteSql entity = dbServiceSQL.getById(CountryVatNoteSql.class, request.getId());
                if (entity == null) entity = new CountryVatNoteSql(request.getId());
                entity.setNotes(request.getNotes());
                return dbServiceSQL.saveEntity(entity);
            });
        });
    }



    @Override
    public APIResult<List<CountryVatRateSql>> vatRateList() throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.makeQuery(CountryVatRateSql.class).getResultList());
    }

    @Override
    public APIResult<CountryVatRateSql> vatRateGet(VatRateGetRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.getById(CountryVatRateSql.class, request.id));
    }

    @Override
    public APIResult<CountryVatRateSql> vatRateSave(CountryVatRateSql request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (request.getId() == null || request.getId().isEmpty()) throw new GamaException("No country");
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                CountryVatRateSql entity = dbServiceSQL.getById(CountryVatRateSql.class, request.getId());
                if (entity == null) entity = new CountryVatRateSql(request.getId());
                List<VATRatesDate> vats = request.getVats();
                if (CollectionsHelper.hasValue(vats)) {
                    for (VATRatesDate vatRateDate : vats) {
                        if (CollectionsHelper.hasValue(vatRateDate.getRates())) {
                            for (VATRate vatRate : vatRateDate.getRates()) {
                                if (StringHelper.isEmpty(vatRate.getCode())) {
                                    vatRate.setCode(UUID.randomUUID().toString());
                                }
                            }
                        }
                    }
                }
                entity.setVats(vats);
                dbServiceSQL.saveEntity(entity);
                return entity;
            });
        });
    }

    @Override
    public APIResult<List<String>> getAvailableTimeZoneIDs() throws GamaApiException {
        return apiResultService.result(() -> Arrays.asList(TimeZone.getAvailableIDs()));
    }
}
