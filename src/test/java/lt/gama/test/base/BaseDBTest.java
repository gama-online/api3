package lt.gama.test.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lt.gama.ConstWorkers;
import lt.gama.api.*;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.IntermediateBalanceRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.PageRequestCondition;
import lt.gama.api.response.LoginResponse;
import lt.gama.api.response.TaskResponse;
import lt.gama.api.service.*;
import lt.gama.api.service.maintenance.FixApi;
import lt.gama.api.service.maintenance.FixInventoryApi;
import lt.gama.api.service.maintenance.MigrationApi;
import lt.gama.auth.i.IAuth;
import lt.gama.auth.impl.Auth;
import lt.gama.auth.service.AuthService;
import lt.gama.auth.service.TokenService;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.impexp.EntityType;
import lt.gama.integrations.eu.EUCheckVatService;
import lt.gama.integrations.vmi.TaxRefundService;
import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.*;
import lt.gama.model.dto.entities.*;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IPart;
import lt.gama.model.i.IPartSN;
import lt.gama.model.i.IWorkSchedule;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.base.EntitySql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.sql.system.SystemSettingsSql;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.auth.*;
import lt.gama.model.type.doc.*;
import lt.gama.model.type.enums.*;
import lt.gama.model.type.gl.*;
import lt.gama.model.type.ibase.IBaseDocPart;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartDto;
import lt.gama.model.type.part.*;
import lt.gama.model.type.salary.WorkScheduleDay;
import lt.gama.service.*;
import lt.gama.tasks.ImportTask;
import lt.gama.test.base.service.TaskInfoAndBody;
import lt.gama.test.base.service.TasksQueue;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.stat.EntityStatistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static lt.gama.ConstWorkers.DEFAULT_QUEUE_NAME;
import static lt.gama.Constants.*;
import static lt.gama.Constants.LOG_LABEL_USER_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public abstract class BaseDBTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final static LocalDateTime NOW_LOCAL = LocalDateTime.of(2016, 4, 15, 9, 30);
    protected final static Instant NOW_INSTANT = NOW_LOCAL.toInstant(ZoneOffset.UTC);

    protected final static String GL_VAT_RECEIVABLE = "2441";
    protected final static String GL_VAT_PAYABLE = "4492";
    protected final static String GL_TEMP = "390";

    private final static String DEFERRED_TASK_DEFAULT_QUEUE = "default";
    
    @Value("${gama.init.login}") String gamaLogin;
    @Value("${gama.init.password}") String gamaPassword;

    @Autowired protected EntityManagerFactory entityManagerFactory;
    @Autowired protected User user;
    @Autowired protected GamaConnectionFactory gamaConnectionFactory;

    /*
     * Auth services
     */
    @Autowired protected AuthService authService;
    @Autowired protected TokenService tokenService;
    @Autowired protected Auth auth;

    /*
     *	Services
     */
    @Autowired protected AppPropService appPropService;
    @Autowired protected AccountService accountService;
    @Autowired protected AccountingService accountingService;
    @Autowired protected AdminApi adminApi;
    @Autowired protected AdminService adminService;
    @Autowired protected AuthSettingsCacheService authSettingsCacheService;
    @Autowired protected BankService bankService;
    @Autowired protected CalendarService calendarService;
    @Autowired protected CashService cashService;
    @Autowired protected CheckRequestTimeoutService checkRequestTimeoutService;
    @Autowired protected CounterService counterService;
    @Autowired protected CurrencyService currencyService;
    @Autowired protected DBServiceSQL dbServiceSQL;
    @Autowired protected DebtService debtService;
    @Autowired protected DepreciationService depreciationService;
    @Autowired protected DocumentService documentService;
    @Autowired protected EmployeeService employeeService;
    @Autowired protected GLGenService glGenService;
    @Autowired protected GLOperationsService glOperationsService;
    @Autowired protected GLService glService;
    @Autowired protected InventoryService inventoryService;
    @Autowired protected MoneyAccountService moneyAccountService;
    @Autowired protected SalaryService salaryService;
    @Autowired protected SalarySettingsService salarySettingsService;
    @Autowired protected StorageService storageService;
    @Autowired protected TradeService tradeService;
    @Autowired protected InventoryCheckService inventoryCheckService;
    @Autowired protected DocsMappersService docsMappersService;

    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected TasksQueue tasksQueue;
    @Autowired protected TaskQueueService taskQueueService;

    // ws
    @Autowired protected EUCheckVatService euCheckVatService;
    @Autowired protected TaxRefundService taxRefundService;

    // SQL mappers
    @Autowired protected AccountSqlMapper accountSqlMapper;
    @Autowired protected AssetSqlMapper assetSqlMapper;
    @Autowired protected BankAccountSqlMapper bankAccountSqlMapper;
    @Autowired protected BankOpeningBalanceSqlMapper bankOpeningBalanceSqlMapper;
    @Autowired protected BankOperationSqlMapper bankOperationSqlMapper;
    @Autowired protected BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper;
    @Autowired protected CalendarSettingsSqlMapper calendarSettingsSqlMapper;
    @Autowired protected CashOpeningBalanceSqlMapper cashOpeningBalanceSqlMapper;
    @Autowired protected CashOperationSqlMapper cashOperationSqlMapper;
    @Autowired protected CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper;
    @Autowired protected CashSqlMapper cashSqlMapper;
    @Autowired protected ChargeSqlMapper chargeSqlMapper;
    @Autowired protected CompanySqlMapper companySqlMapper;
    @Autowired protected CounterpartySqlMapper counterpartySqlMapper;
    @Autowired protected DebtCorrectionSqlMapper debtCorrectionSqlMapper;
    @Autowired protected DebtCoverageSqlMapper debtCoverageSqlMapper;
    @Autowired protected DebtHistorySqlMapper debtHistorySqlMapper;
    @Autowired protected DebtNowSqlMapper debtNowSqlMapper;
    @Autowired protected DebtOpeningBalanceSqlMapper debtOpeningBalanceSqlMapper;
    @Autowired protected DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper;
    @Autowired protected DoubleEntrySqlMapper doubleEntrySqlMapper;
    @Autowired protected EmployeeAbsenceSqlMapper employeeAbsenceSqlMapper;
    @Autowired protected EmployeeCardSqlMapper employeeCardSqlMapper;
    @Autowired protected EmployeeChargeSqlMapper employeeChargeSqlMapper;
    @Autowired protected EmployeeOpeningBalanceSqlMapper employeeOpeningBalanceSqlMapper;
    @Autowired protected EmployeeOperationSqlMapper employeeOperationSqlMapper;
    @Autowired protected EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper;
    @Autowired protected EmployeeSqlMapper employeeSqlMapper;
    @Autowired protected EmployeeVacationSqlMapper employeeVacationSqlMapper;
    @Autowired protected EstimateSqlMapper estimateSqlMapper;
    @Autowired protected ExchangeRateSqlMapper exchangeRateSqlMapper;
    @Autowired protected GLAccountSqlMapper glAccountSqlMapper;
    @Autowired protected GLSaftAccountSqlMapper glSaftAccountSqlMapper;
    @Autowired protected GLOpeningBalanceSqlMapper glOpeningBalanceSqlMapper;
    @Autowired protected InventoryApiSqlMapper inventoryApiSqlMapper;
    @Autowired protected InventoryHistorySqlMapper inventoryHistorySqlMapper;
    @Autowired protected InventoryNowSqlMapper inventoryNowSqlMapper;
    @Autowired protected InventoryOpeningBalanceSqlMapper inventoryOpeningBalanceSqlMapper;
    @Autowired protected InventorySqlMapper inventorySqlMapper;
    @Autowired protected InvoiceSqlMapper invoiceSqlMapper;
    @Autowired protected LabelSqlMapper labelSqlMapper;
    @Autowired protected ManufacturerSqlMapper manufacturerSqlMapper;
    @Autowired protected MoneyHistorySqlMapper moneyHistorySqlMapper;
    @Autowired protected OrderSqlMapper orderSqlMapper;
    @Autowired protected PartSqlMapper partSqlMapper;
    @Autowired protected PositionSqlMapper positionSqlMapper;
    @Autowired protected PurchaseApiSqlMapper purchaseApiSqlMapper;
    @Autowired protected PurchaseSqlMapper purchaseSqlMapper;
    @Autowired protected RecipeSqlMapper recipeSqlMapper;
    @Autowired protected ResponsibilityCenterSqlMapper responsibilityCenterSqlMapper;
    @Autowired protected RoleSqlMapper roleSqlMapper;
    @Autowired protected SalarySqlMapper salarySqlMapper;
    @Autowired protected TransProdApiSqlMapper transportationApiSqlMapper;
    @Autowired protected TransProdSqlMapper transportationSqlMapper;
    @Autowired protected WarehouseSqlMapper warehouseSqlMapper;
    @Autowired protected WorkHoursSqlMapper workHoursSqlMapper;
    @Autowired protected WorkScheduleSqlMapper workScheduleSqlMapper;

    /*
     *	APIs
     */
    @Autowired protected AccountApi accountApi;
    @Autowired protected AssetApi assetApi;
    @Autowired protected BankApi bankApi;
    @Autowired protected CalendarApi calendarApi;
    @Autowired protected CashApi cashApi;
    @Autowired protected CurrencyApi currencyApi;
    @Autowired protected DebtApi debtApi;
    @Autowired protected DocumentApi documentApi;
    @Autowired protected EmployeeApi employeeApi;
    @Autowired protected GLApi glApi;
    @Autowired protected InventoryApi inventoryApi;
    @Autowired protected LabelApi labelApi;
    @Autowired protected MigrationApi migrationApi;
    @Autowired protected PartApi partApi;
    @Autowired protected SalaryApi salaryApi;
    @Autowired protected SettingsApi settingsApi;
    @Autowired protected StorageApi storageApi;
    @Autowired protected WarehouseApi warehouseApi;
    @Autowired protected FixInventoryApi fixInventoryApi;
    @Autowired protected FixApi fixApi;
    
    protected EntityManager entityManager;


    private long defaultCompanyId;
    private LoginResponse loginResponse;

    public BaseDBTest() {
        // set system time and time zone
        DateUtils.mockClock = Clock.fixed(NOW_INSTANT, ZoneId.systemDefault());
    }
    
    public long getCompanyId() {
        return auth.getCompanyId();
    }

    public void setCompanyId(long companyId) {
        authSettingsCacheService.remove(companyId);
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

        // prepare for tasks
        MDC.put(LOG_LABEL_COMPANY, String.valueOf(auth.getCompanyId()));
        MDC.put(LOG_LABEL_PERMISSIONS, Permission.ADMIN.toString());
    }

    public void resetCompanyId() {
        setCompanyId(defaultCompanyId);
    }

    public CompanySettings getCompanySettings() {
        return auth.getSettings();
    }

    public CompanyTaxSettings getCompanyTaxSettings(int year, int month) {
        return salarySettingsService.getCompanyTaxSettings(year, month);
    }

    public CompanySalarySettings getCompanySalarySettings(int year, int month) {
        return salarySettingsService.getCompanySalarySettings(year, month);
    }

    public long getDefaultCompanyId() {
        return defaultCompanyId;
    }

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }

    public String getAccessToken() {
        return loginResponse.getAccessToken();
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get();
    }

    protected void timer(String title, CheckedRunnable r) throws Exception {
        long startTimer = System.currentTimeMillis();
        r.run();
        long time = System.currentTimeMillis() - startTimer;
        log.info(this.getClass().getSimpleName() + ": " + title + " in " + time + "ms");
    }

    @BeforeEach
    protected void setUp() throws Exception {
        log.info(this.getClass().getSimpleName() + ": BeforeEach");
        entityManager = entityManagerFactory.createEntityManager();

        Locale.setDefault(Locale.of("en", "US"));
        authSettingsCacheService.removeAll();
        currencyService.setTestMode(true);

        timer("setUp", this::init);
    }

    @AfterEach
    protected void tearDown() {
        log.info(this.getClass().getSimpleName() + ": AfterEach");
        clearCaches();
        entityManager.close();
        SQLHelper.executeSqlScriptFromAsUser(gamaConnectionFactory.get(user), user.username(), "sql/delete-user-data.sql");
    }

    void init() {
        dbServiceSQL.executeInTransaction(entityManager -> {
            // create test data
            timer("create test data", () -> accountService.initData());

            // System settings - subscription price - needed for adminApi.companySave
            SystemSettingsSql systemSettings = new SystemSettingsSql();
            systemSettings.setOwnerCompanyId(getCompanyId());
            systemSettings.setAccountPrice(GamaMoney.parse("EUR 15.00"));
            dbServiceSQL.saveEntity(systemSettings);

            // login
            timer("login", this::login);

            // modify accounting period 2015.01.01 - 2016.01.01
            CompanySql company = dbServiceSQL.getById(CompanySql.class, getCompanyId());
            company.getSettings().setAccYear(2015);
            company.getSettings().setAccMonth(1);

            CompanyTaxSettings taxes = new CompanyTaxSettings();
            taxes.setDate(LocalDate.of(2015, 1, 1));

            taxes.setIncomeTaxRate(BigDecimal.valueOf(15));
            taxes.setIncome(new GLDC(new GLOperationAccount("11"), new GLOperationAccount("12")));

            taxes.setEmployeeSSAddTaxRates(new ArrayList<>());
            taxes.getEmployeeSSAddTaxRates().add(BigDecimal.valueOf(24, 1)); // 2.4
            taxes.getEmployeeSSAddTaxRates().add(BigDecimal.valueOf(3));
            taxes.setEmployeeSS(new GLDC(new GLOperationAccount("21"), new GLOperationAccount("22")));

            taxes.setCompanySSTaxRate(BigDecimal.valueOf(30));
            taxes.setCompanySS(new GLDC(new GLOperationAccount("31"), new GLOperationAccount("32")));

            taxes.setGuarantyFundTaxRate(BigDecimal.valueOf(2));
            taxes.setGuarantyFund(new GLDC(new GLOperationAccount("41"), new GLOperationAccount("42")));

            taxes.setShiTaxRate(BigDecimal.valueOf(0.2));
            taxes.setShi(new GLDC(new GLOperationAccount("51"), new GLOperationAccount("52")));

            company.getSettings().setTaxes(new ArrayList<>());
            company.getSettings().getTaxes().add(taxes);

            CompanySalarySettings salary = new CompanySalarySettings();
            salary.setDate(LocalDate.of(2015, 1, 1));
            salary.setShorterWorkingDay(true);

            company.getSettings().setSalary(new ArrayList<>());
            company.getSettings().getSalary().add(salary);

            dbServiceSQL.saveEntity(company);
            authSettingsCacheService.remove(company.getId());

            // Set taxes for country LT
            CountryVatRateSql countryVatRate = new CountryVatRateSql("LT");
            countryVatRate.setVats(new ArrayList<>());
            VATRatesDate vatRatesDate = new VATRatesDate();
            vatRatesDate.setDate(LocalDate.of(2014, 1, 1));
            vatRatesDate.setRates(new ArrayList<>());
            vatRatesDate.getRates().add(new VATRate(UUID.randomUUID().toString(), "Zero", 0.0));
            vatRatesDate.getRates().add(new VATRate(UUID.randomUUID().toString(), "Std", 21.0));
            vatRatesDate.getRates().add(new VATRate(UUID.randomUUID().toString(), "Spec.9", 9.0));
            vatRatesDate.getRates().add(new VATRate(UUID.randomUUID().toString(), "Spec.5", 5.0));
            vatRatesDate.getRates().add(new VATRate(UUID.randomUUID().toString(), "Spec.10", 10.0));
            countryVatRate.getVats().add(vatRatesDate);
            dbServiceSQL.saveEntity(countryVatRate);
        });

        // login again
        defaultCompanyId = getCompanyId();
        resetCompanyId();
        clearCaches();
    }

    protected void login() {
        loginResponse = accountService.login(gamaLogin, gamaPassword, 1);

        IAuth auth = tokenService.getAuthentication(loginResponse.getAccessToken());
        assertThat(auth).isNotNull();
        assertThat(auth.getCompanyId()).isGreaterThan(0);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));
        assertThat(auth.getSettings().getStartAccounting()).isEqualTo(LocalDate.of(2015, 1, 1));

        auth.cloneFrom(auth);

        // prepare for tasks
        MDC.put(LOG_LABEL_LOGIN, auth.getId());
        MDC.put(LOG_LABEL_COMPANY, String.valueOf(auth.getCompanyId()));
        MDC.put(LOG_LABEL_USER_NAME, auth.getName());
        MDC.put(LOG_LABEL_PERMISSIONS, String.join(",", auth.getPermissions()));
    }
    
    public void clearCaches() {
        if (entityManager.getTransaction().isActive()) entityManager.flush();
        entityManager.clear();
    }

    public <T> TaskResponse<T> runDeferredTask() {
        return runDeferredTask(DEFAULT_QUEUE_NAME);
    }

    public TaskInfoAndBody getDeferredTask() {
        return getDeferredTask(0);
    }

    public TaskInfoAndBody getDeferredTask(String queueName) {
        return getDeferredTask(0, queueName);
    }

    public TaskInfoAndBody getDeferredTask(int index) {
        return getDeferredTask(index, DEFERRED_TASK_DEFAULT_QUEUE);
    }

    public TaskInfoAndBody getDeferredTask(int index, String queueName) {
        var taskInfoAndBody = tasksQueue.getTask(queueName, index);
        log.info(this.getClass().getSimpleName() + ": " + tasksQueue.getCountTasks(queueName) + " tasks in queue='" + queueName + '\'' +
                " getDeferredTask(index=" + index + ')' +
                ' ' + taskInfoAndBody.body());
        return taskInfoAndBody;
    }

    public <T> TaskResponse<T> runDeferredTask(String queueName) {
        var task = getDeferredTask(0, queueName);
        task.body().run();
        String token = task.body().getToken();
        try {
            String resp = storageService.getContent(ConstWorkers.TASKS_FOLDER, token);
            if (StringHelper.isEmpty(resp)) return TaskResponse.success();
            APIResult<TaskResponse<T>> response = objectMapper.readValue(resp, new TypeReference<>() {});
            return response.getData();
        } catch (JsonProcessingException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return TaskResponse.error(e.getMessage());
        } finally {
            clearCaches();
            tasksQueue.deleteTask(queueName, task.info().taskName());
        }
    }

    public void deleteDeferredTask(String taskName) {
        deleteDeferredTask(taskName, DEFERRED_TASK_DEFAULT_QUEUE);
    }

    public void deleteDeferredTask(String taskName, String queueName) {
        int count = tasksQueue.getCountTasks(queueName);
        tasksQueue.deleteTask(queueName, taskName);
        assertThat(tasksQueue.getCountTasks(queueName)).isEqualTo(count - 1);
    }

    public int deferredTasksCount() {
        return deferredTasksCount(DEFERRED_TASK_DEFAULT_QUEUE);
    }

    public int deferredTasksCount(String queueName) {
        return tasksQueue.getCountTasks(queueName);
    }

    public void runAllDeferredTasks() {
        runAllDeferredTasks(DEFERRED_TASK_DEFAULT_QUEUE);
    }

    public TaskResponse<?> runAllDeferredTasksUntilError() {
        return runAllDeferredTasksUntilError(DEFERRED_TASK_DEFAULT_QUEUE);
    }

    public void runAllDeferredTasks(String queueName) {
        while (deferredTasksCount(queueName) > 0) runDeferredTask(queueName);
    }

    public TaskResponse<?> runAllDeferredTasksUntilError(String queueName) {
        while (deferredTasksCount(queueName) > 0) {
            var taskResponse = runDeferredTask(queueName);
            if (taskResponse.getStatus() == ProcessingStatusType.ERROR) return taskResponse;
        }
        return TaskResponse.success();
    }

    public LoginResponse loginResponse() {
        return accountService.login(gamaLogin, gamaPassword, null);
    }

    public void setDisableGL() {
        CompanySql company = dbServiceSQL.getById(CompanySql.class, getCompanyId());
        CompanySettings setting = company.getSettings();
        setting.setDisableGL(true);
        dbServiceSQL.saveEntity(company);
        resetCompanyId();
    }

    public void setExpandInvoice(Boolean expandInvoice) {
        CompanySql company = dbServiceSQL.getById(CompanySql.class, getCompanyId());
        CompanySettings setting = company.getSettings();
        if (setting.getGl() == null) setting.setGl(new CompanySettingsGL());
        setting.getGl().setExpandInvoice(expandInvoice);
        dbServiceSQL.saveEntity(company);
        resetCompanyId();
    }

    /*
     * Settings tools
     */

    //---

    /*
     *	Test objects
     */

    public void createAccount(CompanySql company, String email, LocalDate date) throws GamaApiException {
        setCompanyId(company.getId());
        createAccount(company, email, date, null);
        resetCompanyId();
        clearCaches();
    }

    public void createAccount(CompanySql company, String email, LocalDate date, Long payerId) throws GamaApiException {
        setCompanyId(company.getId());
        EmployeeDto employee = new EmployeeDto();
        employee.setName("Jonas " + (int)(Math.random() * 100));
        employee.setCompanyId(company.getId());
        employee.setEmail(email);
        APIResult<EmployeeDto> apiResult = employeeApi.saveEmployee(employee);
        assertThat(apiResult.getError()).isNull();
        employee = apiResult.getData();
        clearCaches();
        assertThat(employee.getId()).isNotNull();

        AccountInfo accountInfo = new AccountInfo(company, employee);

        accountService.activateAccount(email, accountInfo, date);
        clearCaches();

        if (payerId != null) {
            AccountSql account = accountService.findAccount(email);
            account.setPayer(entityManager.getReference(CompanySql.class, payerId));
            dbServiceSQL.saveEntity(account);
            clearCaches();
        }
        resetCompanyId();
    }

    public EmployeeDto createEmployee(String name, String number) throws GamaApiException {
        return createEmployee(name, null, Lists.newArrayList(number));
    }

    public EmployeeDto createEmployee(String name, String currency, String number) throws GamaApiException {
        return createEmployee(name, Lists.newArrayList(currency), Lists.newArrayList(number));
    }

    public EmployeeDto createEmployee(String name, List<String> currency, List<String> number) throws GamaApiException {
        EmployeeDto entity = new EmployeeDto();
        entity.setName(name);
        GLMoneyAccount moneyAccount = new GLMoneyAccount();
        moneyAccount.setAccounts(new ArrayList<>());
        if (currency == null) {
            moneyAccount.getAccounts().add(new GLCurrencyAccount(null, new GLOperationAccount(number.get(0), "Employee " + number.get(0))));
        } else {
            for (int i = 0; i < number.size(); i++) {
                moneyAccount.getAccounts().add(new GLCurrencyAccount(currency.get(i), new GLOperationAccount(number.get(i), "Employee " + number.get(i))));
            }
        }
        entity.setMoneyAccount(moneyAccount);

        APIResult<EmployeeDto> apiResult = employeeApi.saveEmployee(entity);
        assertThat(apiResult.getError()).isNull();
        entity = apiResult.getData();
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public EmployeeCardDto createEmployeeCard(EmployeeDto employee) {
        EmployeeCardDto entity = new EmployeeCardDto();
        entity.setEmployee(employee);
        entity = employeeCardSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(employeeCardSqlMapper.toEntity(entity)));
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public EmployeeCardDto saveEmployeeCard(EmployeeCardDto employeeCard) throws GamaApiException {
        APIResult<EmployeeCardDto> apiResult = salaryApi.saveEmployeeCard(employeeCard);
        assertThat(apiResult.getError()).isNull();
        clearCaches();
        return apiResult.getData();
    }

    public ChargeDto createCharge(String name, GLOperationAccount debit, GLOperationAccount credit) {
        return createCharge(name, debit, credit, AvgSalaryType.ALL);
    }

    public ChargeDto createCharge(String name, GLOperationAccount debit, GLOperationAccount credit, AvgSalaryType avgSalaryType) {
        ChargeDto entity = new ChargeDto(name);
        entity.setDebit(debit);
        entity.setCredit(credit);
        entity.setAvgSalary(avgSalaryType);
        entity.setCompanySSTax(new GLDCActive(true));
        entity.setEmployeeSSTax(new GLDCActive(true));
        entity.setIncomeTax(new GLDCActive(true));
        entity.setShiTax(new GLDCActive(true));
        entity.setGuarantyFund(new GLDCActive(true));

        entity = chargeSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(chargeSqlMapper.toEntity(entity)));
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public DocCharge createDocCharge(ChargeDto charge) {
        return new DocCharge(charge);
    }

    public DocCharge createDocCharge(AvgSalaryType avgSalaryType, int period) {
        DocCharge docCharge = new DocCharge();
        docCharge.setAvgSalary(avgSalaryType);
        docCharge.setPeriod(period);
        return docCharge;
    }

    public WarehouseDto createWarehouse(String name) throws GamaApiException {
        return createWarehouse(name, null);
    }

    public WarehouseDto createWarehouse(String name, Boolean withTag) throws GamaApiException {
        WarehouseDto entity = new WarehouseDto();
        entity.setName(name);
        entity.setWithTag(withTag);

        APIResult<WarehouseDto> apiResult = warehouseApi.saveWarehouse(entity);
        assertThat(apiResult.getError()).isNull();
        entity = apiResult.getData();
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public PositionDto createPosition(String name) throws GamaApiException {
        return createPosition(name, null);
    }

    public PositionDto createPosition(String name, IWorkSchedule workSchedule) throws GamaApiException {
        if (workSchedule == null) {
            WorkScheduleDto workScheduleDto = new WorkScheduleDto();
            workScheduleDto.setType(WorkScheduleType.WEEKLY);
            workScheduleDto.setSchedule(new ArrayList<>());
            workScheduleDto.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(8)));
            workScheduleDto.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(9)));
            workScheduleDto.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(4)));
            workScheduleDto.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(9)));
            workScheduleDto.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(8)));
            APIResult<WorkScheduleDto> apiResult = salaryApi.saveWorkSchedule(workScheduleDto);
            assertThat(apiResult.getError()).isNull();
            workSchedule = apiResult.getData();
            assertThat(workSchedule.getId()).isNotNull();
            clearCaches();
        }

        PositionDto position = new PositionDto();
        position.setName(name);
        position.setWorkSchedule(new DocWorkSchedule(workSchedule));
        APIResult<PositionDto> apiResult = salaryApi.savePosition(position);
        assertThat(apiResult.getError()).isNull();
        position = apiResult.getData();
        assertThat(position.getId()).isNotNull();
        clearCaches();

        return position;
    }

    public WorkScheduleDto createWorkSchedule(String name, WorkScheduleType type) throws GamaApiException {
        WorkScheduleDto workSchedule = new WorkScheduleDto();
        workSchedule.setName(name);
        workSchedule.setType(type);
        workSchedule.setSchedule(new ArrayList<>());
        if (type == WorkScheduleType.WEEKLY) {
            workSchedule.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(1)));
            workSchedule.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(2)));
            workSchedule.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(3)));
            workSchedule.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(4)));
            workSchedule.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(5)));
        } else if (type == WorkScheduleType.PERIODIC) {
            workSchedule.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(10)));
            workSchedule.getSchedule().add(new WorkScheduleDay(BigDecimal.valueOf(10)));
        }
        APIResult<WorkScheduleDto> apiResult = salaryApi.saveWorkSchedule(workSchedule);
        assertThat(apiResult.getError()).isNull();
        clearCaches();
        workSchedule = apiResult.getData();
        assertThat(workSchedule.getId()).isNotNull();

        return workSchedule;
    }

    public CountryVatRateSql getCountryVatRate() {
        return dbServiceSQL.getById(CountryVatRateSql.class, getCompanySettings().getCountry());
    }

    public VATRate getVatRate(Double rate) {
        CountryVatRateSql countryVatRate = getCountryVatRate();
        for (VATRate vatRate : countryVatRate.getRatesMap(DateUtils.date()).getRates()) {
            if (Math.abs(vatRate.getRate() - rate) < 0.01) {
                return vatRate;
            }
        }
        return null;
    }

    public String getVatRateCode(Double rate) {
        VATRate vatRate = getVatRate(rate);
        return vatRate != null ? vatRate.getCode() : null;
    }

    public PartDto getPart(long partId) throws GamaApiException {
        APIResult<PartDto> apiResult = partApi.getPart(new IdRequest(partId));
        assertThat(apiResult.getError()).isNull();
        PartDto part = apiResult.getData();
        clearCaches();
        assertThat(part.getId()).isNotNull();
        return part;
    }

    public PartDto createPart(String name, String sku, PartType type, String asset, String income, String expense) {
        return createPart(name, sku, type, false, 0.0, null, asset, income, expense);
    }

    public PartDto createPart(String name, String sku, PartType type, String asset, String income, String expense,
                              CounterpartyDto vendor, String vendorCode) {
        return createPart(name, sku, type, false, 0.0, null, asset, income, expense, null, null, vendor, vendorCode);
    }

    public PartDto createPart(String name, String sku, PartType type, boolean taxable, double vatRate, String asset, String income, String expense) {
        return createPart(name, sku, type, taxable, vatRate, null, asset, income, expense);
    }

    public PartDto createPart(String name, String sku, PartType type, ManufacturerDto manufacturer, Set<String> labels) {
        return createPart(name, sku, type, false, 10.0, GamaBigMoney.parse("EUR 1.2345"), "10", "50", "60", manufacturer, labels);
    }

    public PartDto createPart(String name, String sku, PartType type, boolean taxable, double vatRate,
                              GamaBigMoney price, String asset, String income, String expense) {
        return createPart(name, sku, type, taxable, vatRate, price, asset, income, expense, null, null, null, null);
    }

    public PartDto createPart(String name, String sku, PartType type, boolean taxable, double vatRate,
                              GamaBigMoney price, String asset, String income, String expense, ManufacturerDto manufacturer, Set<String> labels) {
        return createPart(name, sku, type, taxable, vatRate, price, asset, income, expense, manufacturer, labels, null, null);
    }

    public PartDto createPart(String name, String sku, PartType type, boolean taxable, double vatRate,
                              GamaBigMoney price, String asset, String income, String expense, ManufacturerDto manufacturer, Set<String> labels,
                              CounterpartyDto vendor, String vendorCode) {
        PartDto entity = new PartDto();
        entity.setName(name);
        entity.setSku(sku);
        entity.setType(type);
        entity.setTaxable(taxable);
        if (taxable) entity.setVatRateCode(getVatRateCode(vatRate));
        entity.setPrice(price);

        if (asset != null) entity.setAccountAsset(new GLOperationAccount(asset, "Asset " + asset));
        if (income != null) entity.setGlIncome(new GLDC(new GLOperationAccount(income, "Income " + income), null));
        if (expense != null) entity.setGlExpense(new GLDC(new GLOperationAccount(expense, "Expense " + expense), null));

        entity.setManufacturer(manufacturer);
        entity.setLabels(labels);

        entity.setVendor(vendor);
        entity.setVendorCode(vendorCode);
        entity.setDb(DBType.POSTGRESQL);

        entity = partSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(partSqlMapper.toEntity(entity)));
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public PartDto savePart(PartDto part) {
        part = partSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(partSqlMapper.toEntity(part)));
        clearCaches();
        return part;
    }

    private <E extends IBaseDocPart & IBaseDocPartCost & IPart & IPartSN & IDb>
    E createDocPart(E obj, PartDto src, PartSN sn, BigDecimal quantity,
                    GamaBigMoney price, GamaMoney total, GamaMoney costTotal) {
        obj.setId(src.getId());
        obj.setName(src.getName());
        obj.setUnit(src.getUnit());
        obj.setSku(src.getSku());
        obj.setType(src.getType());
        obj.setSn(sn);
        obj.setQuantity(quantity);
        obj.setPrice(price);
        if (price != null && quantity != null) {
            obj.setTotal(GamaMoneyUtils.toMoney(price.multipliedBy(quantity)));
        } else {
            obj.setTotal(total);
        }
        obj.setCostTotal(costTotal);
        obj.setForwardSell(src.isForwardSell());
        obj.setDb(src.getDb());
        return obj;
    }

    //TODO *** fix after migration. *** rename IBaseDocPartDto meaningfully
    private <E extends BaseDocPartDto & IBaseDocPartDto & IBaseDocPartCost>
    E createDocPart(E obj, PartDto src, PartSN sn, BigDecimal quantity,
                    GamaBigMoney price, GamaMoney total, GamaMoney costTotal) {
        obj = createBaseDocPartDto(obj, src, sn, price);
        obj.setUnit(src.getUnit());
        obj.setQuantity(quantity);
        if (price != null && quantity != null) {
            obj.setTotal(GamaMoneyUtils.toMoney(price.multipliedBy(quantity)));
        } else {
            obj.setTotal(total);
        }
        obj.setCostTotal(costTotal);
        return obj;
    }

    public <E extends BaseDocPartDto>
    E createBaseDocPartDto(E obj, PartDto part, PartSN sn, GamaBigMoney price) {
        obj.setId(part.getId());
        obj.setCompanyId(part.getCompanyId());
        obj.setName(part.getName());
        obj.setSku(part.getSku());
        obj.setType(part.getType());
        obj.setSn(sn);
        obj.setPrice(price);
        obj.setAccountAsset(part.getAccountAsset());
        obj.setGlIncome(part.getGlIncome());
        obj.setGlExpense(part.getGlExpense());
        obj.setForwardSell(part.isForwardSell());
        obj.setDb(part.getDb());
        return obj;
    }

    public <E extends IBaseDocPartDto> E createPartPart(E part, BigDecimal quantity) {
        part.setQuantity(quantity);
        return part;
    }

    public <E extends IBaseDocPartDto & IPartSN> E createPartPart(E part, PartSN sn, BigDecimal quantity) {
        createPartPart(part, quantity);
        part.setSn(sn);
        return part;
    }

    public <E extends IBaseDocPartDto & IPartSN> E createPartPart(E part, PartSN sn, BigDecimal quantity, Boolean forwardSell) {
        createPartPart(part, sn, quantity);
        part.setForwardSell(forwardSell);
        return part;
    }

    public InventoryOpeningBalanceDto getInventoryOpeningBalance(long inventoryId) throws GamaApiException {
        APIResult<InventoryOpeningBalanceDto> apiResult = inventoryApi.getOpeningBalance(new IdRequest(inventoryId));
        assertThat(apiResult.getError()).isNull();
        InventoryOpeningBalanceDto inventory = apiResult.getData();
        clearCaches();
        assertThat(inventory.getId()).isNotNull();
        return inventory;
    }

    public DocPartOB createDocPartOB(PartDto src, PartSN sn, BigDecimal quantity, GamaMoney total) {
        return createDocPart(new DocPartOB(), src, sn, quantity, null, total, null);
    }

    public DocPartOB createDocPartOB(PartDto src, BigDecimal quantity, GamaMoney total) {
        return createDocPart(new DocPartOB(), src, null, quantity, null, total, null);
    }

    public PartOpeningBalanceDto createPartOpeningBalance(PartDto src, PartSN sn, BigDecimal quantity, GamaMoney total) {
        return createDocPart(new PartOpeningBalanceDto(), src, sn, quantity, null, total, null);
    }

    public PartOpeningBalanceDto createPartOpeningBalance(PartDto src, BigDecimal quantity, GamaMoney total) {
        return createDocPart(new PartOpeningBalanceDto(), src, null, quantity, null, total, null);
    }

    public RecipeDto getRecipe(long recipeId) throws GamaApiException {
        APIResult<RecipeDto> apiResult = partApi.getRecipe(new IdRequest(recipeId));
        assertThat(apiResult.getError()).isNull();
        RecipeDto recipe = apiResult.getData();
        clearCaches();
        assertThat(recipe.getId()).isNotNull();
        return recipe;
    }

    public TransProdDto getTransProd(long transProdId) throws GamaApiException {
        APIResult<TransProdDto> apiResult = inventoryApi.getTransProd(new IdRequest(transProdId));
        assertThat(apiResult.getError()).isNull();
        TransProdDto transProd = apiResult.getData();
        clearCaches();
        assertThat(transProd.getId()).isNotNull();
        return transProd;
    }

    public DocPartFrom createDocPartFrom(PartDto src, PartSN sn, BigDecimal quantity) {
        return createDocPart(new DocPartFrom(), src, sn, quantity, null, null, null);
    }

    public DocPartFrom createDocPartFrom(PartDto src, BigDecimal quantity) {
        return createDocPart(new DocPartFrom(), src, null, quantity, null, null, null);
    }

    public PartFromDto createPartFrom(PartDto part, BigDecimal quantity) {
        return createPartFrom(part, null, quantity);
    }

    public PartFromDto createPartFrom(PartDto part, PartSN sn, BigDecimal quantity) {
        return createDocPart(new PartFromDto(), part, sn, quantity, null, null, null);
    }

    public DocPartTo createDocPartTo(PartDto src, BigDecimal quantity, BigDecimal percent) {
        return createDocPartTo(src, null, quantity, percent);
    }

    public DocPartTo createDocPartTo(PartDto src, PartSN sn, BigDecimal quantity, BigDecimal percent) {
        DocPartTo docPartTo = createDocPart(new DocPartTo(), src, sn, quantity, null, null, null);
        docPartTo.setCostPercent(percent);
        return docPartTo;
    }

    public PartToDto createPartTo(PartDto part, BigDecimal quantity, BigDecimal percent) {
        return createPartTo(part, null, quantity, percent);
    }

    public PartToDto createPartTo(PartDto part, PartSN sn, BigDecimal quantity, BigDecimal percent) {
        PartToDto partTo = createDocPart(new PartToDto(), part, sn, quantity, null, null, null);
        partTo.setCostPercent(percent);
        return partTo;
    }

    public PurchaseDto getPurchase(long purchaseId) throws GamaApiException {
        APIResult<PurchaseDto> apiResult = inventoryApi.getPurchase(new IdRequest(purchaseId, DBType.POSTGRESQL));
        assertThat(apiResult.getError()).isNull();
        PurchaseDto purchase = apiResult.getData();
        clearCaches();
        assertThat(purchase.getId()).isNotNull();
        return purchase;
    }

    public DocPartPurchase createDocPartPurchase(PartDto src, BigDecimal quantity, GamaBigMoney price) {
        return createDocPartPurchase(src, quantity, price, false);
    }

    public DocPartPurchase createDocPartPurchase(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price) {
        return createDocPart(new DocPartPurchase(), src, sn, quantity, price, null, null);
    }

    public DocPartPurchase createDocPartPurchase(PartDto src, BigDecimal quantity, GamaBigMoney price, boolean inCost) {
        DocPartPurchase docPartPurchase = createDocPart(new DocPartPurchase(), src, null, quantity, price, null, null);
        docPartPurchase.setInCost(inCost);
        return docPartPurchase;
    }

    public DocPartPurchase createDocPartPurchase(PartDto src, BigDecimal quantity, GamaBigMoney price, boolean inCost, boolean addExp) {
        DocPartPurchase docPartPurchase = createDocPart(new DocPartPurchase(), src, null, quantity, price, null, null);
        docPartPurchase.setInCost(inCost);
        docPartPurchase.setAddExp(addExp);
        return docPartPurchase;
    }

    public PartPurchaseDto createPartPurchase(PartDto src, BigDecimal quantity, GamaBigMoney price) {
        return createPartPurchase(src, quantity, price, false);
    }

    public PartPurchaseDto createPartPurchase(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price) {
        return createDocPart(new PartPurchaseDto(), src, sn, quantity, price, null, null);
    }

    public PartPurchaseDto createPartPurchase(PartDto src, BigDecimal quantity, GamaBigMoney price, boolean inCost) {
        PartPurchaseDto partPurchaseDto = createDocPart(new PartPurchaseDto(), src, null, quantity, price, null, null);
        partPurchaseDto.setInCost(inCost);
        return partPurchaseDto;
    }

    public PartPurchaseDto createPartPurchase(PartDto src, BigDecimal quantity, GamaBigMoney price, boolean inCost, boolean addExp) {
        PartPurchaseDto partPurchaseDto = createDocPart(new PartPurchaseDto(), src, null, quantity, price, null, null);
        partPurchaseDto.setInCost(inCost);
        partPurchaseDto.setAddExp(addExp);
        return partPurchaseDto;
    }

    public InvoiceDto getInvoice(long invoiceId) throws GamaApiException {
        APIResult<InvoiceDto> apiResult = inventoryApi.getInvoice(new IdRequest(invoiceId));
        assertThat(apiResult.getError()).isNull();
        InvoiceDto invoice = apiResult.getData();
        clearCaches();
        assertThat(invoice.getId()).isNotNull();
        return invoice;
    }

    public DocPartInvoice createDocPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price) {
        return createDocPartInvoice(src, null, quantity, price, src.getVat());
    }

    public DocPartInvoice createDocPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price) {
        return createDocPartInvoice(src, sn, quantity, price, src.getVat());
    }

    public DocPartInvoice createDocPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price, VATRate vatRate) {
        return createDocPartInvoice(src, null, quantity, price, vatRate);
    }

    public DocPartInvoice createDocPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price, Double discount) {
        return createDocPartInvoice(src, null, quantity, price, src.getVat(), discount);
    }

    public DocPartInvoice createDocPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price, VATRate vatRate, Double discount) {
        return createDocPartInvoice(src, null, quantity, price, vatRate, discount);
    }


    public DocPartInvoice createDocPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price, VATRate vatRate) {
        return createDocPartInvoice(src, sn, quantity, price, vatRate, null);
    }

    public DocPartInvoice createDocPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price, Double discount) {
        return createDocPartInvoice(src, sn, quantity, price, src.getVat(), discount);
    }

    public DocPartInvoice createDocPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price, VATRate vatRate, Double discount) {
        DocPartInvoice docPartInvoice = createDocPart(new DocPartInvoice(), src, sn, quantity, price, null, null);
        if (src.getParts() != null) {
            docPartInvoice.setParts(src.getParts().stream().map(pp -> new DocPartInvoiceSubpart(pp, pp.getQuantity())).collect(Collectors.toList()));
        }
        docPartInvoice.setVat(vatRate);
        docPartInvoice.setDiscount(discount);
        docPartInvoice.setTaxable(src.isTaxable());
        return docPartInvoice;
    }

    public PartInvoiceDto createPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price) {
        return createPartInvoice(src, null, quantity, price, src.getVat());
    }

    public PartInvoiceDto createPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price) {
        return createPartInvoice(src, sn, quantity, price, src.getVat());
    }

    public PartInvoiceDto createPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price, VATRate vatRate) {
        return createPartInvoice(src, null, quantity, price, vatRate);
    }

    public PartInvoiceDto createPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price, Double discount) {
        return createPartInvoice(src, null, quantity, price, src.getVat(), discount);
    }

    public PartInvoiceDto createPartInvoice(PartDto src, BigDecimal quantity, GamaBigMoney price, VATRate vatRate,
                                            Double discount) {
        return createPartInvoice(src, null, quantity, price, vatRate, discount);
    }

    public PartInvoiceDto createPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price, VATRate vatRate) {
        return createPartInvoice(src, sn, quantity, price, vatRate, null);
    }

    public PartInvoiceDto createPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price, Double discount) {
        return createPartInvoice(src, sn, quantity, price, src.getVat(), discount);
    }

    public PartInvoiceDto createPartInvoice(PartDto src, PartSN sn, BigDecimal quantity, GamaBigMoney price, VATRate vatRate, Double discount) {
        PartInvoiceDto docPartInvoice = createDocPart(new PartInvoiceDto(), src, sn, quantity, price, null, null);
        if (src.getParts() != null) {
            docPartInvoice.setParts(src.getParts().stream().map(pp -> new PartInvoiceSubpartDto(pp, pp.getQuantity())).collect(Collectors.toList()));
        }
        docPartInvoice.setVat(vatRate);
        docPartInvoice.setDiscount(discount);
        docPartInvoice.setTaxable(src.isTaxable());
        return docPartInvoice;
    }

    public InventoryDto getInventory(long inventoryId) throws GamaApiException {
        APIResult<InventoryDto> apiResult = inventoryApi.getInventory(new IdRequest(inventoryId));
        assertThat(apiResult.getError()).isNull();
        InventoryDto inventory = apiResult.getData();
        clearCaches();
        assertThat(inventory.getId()).isNotNull();
        return inventory;
    }

    public DocPartInventory createDocPartInventory(DocWarehouse w, PartDto src, PartSN sn, boolean isChange, BigDecimal quantity, GamaMoney costTotal) {
        DocPartInventory part = createDocPart(new DocPartInventory(), src, sn, null, null, null, null);
        part.setChange(isChange);
        part.setWarehouse(w);
        if (isChange) {
            part.setQuantity(quantity);
            part.setCostTotal(costTotal);
        } else {
            part.setQuantityRemainder(quantity);
        }
        return part;
    }

    public PartInventoryDto createPartInventory(WarehouseDto w, PartDto src, PartSN sn, boolean isChange, BigDecimal quantity, GamaMoney costTotal) {
        PartInventoryDto part = createDocPart(new PartInventoryDto(), src, sn, null, null, null, null);

        part.setChange(isChange);
        part.setWarehouse(w);
        if (isChange) {
            part.setQuantity(quantity);
            part.setCostTotal(costTotal);
        } else {
            part.setQuantityRemainder(quantity);
        }
        return part;
    }

    public EstimateDto getEstimate(long estimateId) throws GamaApiException {
        APIResult<EstimateDto> apiResult = inventoryApi.getEstimate(new IdRequest(estimateId));
        assertThat(apiResult.getError()).isNull();
        EstimateDto estimate = apiResult.getData();
        clearCaches();
        assertThat(estimate.getId()).isNotNull();
        return estimate;
    }

    public PartEstimateDto createPartEstimate(PartDto part, BigDecimal quantity, GamaBigMoney price) {
        return createPartEstimate(part, null, quantity, price);
    }

    public PartEstimateDto createPartEstimate(PartDto part, PartSN sn, BigDecimal quantity, GamaBigMoney price) {
        PartEstimateDto partEstimate = createBaseDocPartDto(new PartEstimateDto(), part, sn, price);
        partEstimate.setQuantity(quantity);
        if (price != null && quantity != null) {
            partEstimate.setTotal(GamaMoneyUtils.toMoney(price.multipliedBy(quantity)));
        }
        if (part.getParts() != null) {
            partEstimate.setParts(part.getParts().stream().map(PartEstimateSubpartDto::new).collect(Collectors.toList()));
        }
        return partEstimate;
    }

    public OrderDto getOrder(long orderId) throws GamaApiException {
        APIResult<OrderDto> apiResult = inventoryApi.getOrder(new IdRequest(orderId));
        assertThat(apiResult.getError()).isNull();
        OrderDto order = apiResult.getData();
        clearCaches();
        assertThat(order.getId()).isNotNull();
        return order;
    }

    public PartOrderDto createPartOrder(PartDto part, BigDecimal quantity, GamaBigMoney price) {
        return createPartOrder(part, null, quantity, price);
    }

    public PartOrderDto createPartOrder(PartDto part, PartSN sn, BigDecimal quantity, GamaBigMoney price) {
        PartOrderDto partOrder = createBaseDocPartDto(new PartOrderDto(), part, sn, price);
        partOrder.setQuantity(quantity);
        if (price != null && quantity != null) {
            partOrder.setTotal(GamaMoneyUtils.toMoney(price.multipliedBy(quantity)));
        }
        return partOrder;
    }

    public CounterpartyDto createCounterparty(String name, DebtType type, String account) {
        return createCounterparty(name, null, type, account, null, null);
    }

    public CounterpartyDto createCounterparty(String name, DebtType type, String account, String bankAccount) {
        return createCounterparty(name, null, type, account, null, null, bankAccount);
    }

    public CounterpartyDto createCounterparty(String name, DebtType type, String account, String bankAccount, String noDebtAccount) {
        return createCounterparty(name, null, type, account, null, null, bankAccount, noDebtAccount);
    }

    public CounterpartyDto createCounterparty(String name, String comCode, DebtType type, String account) {
        return createCounterparty(name, comCode, type, account, null, null);
    }

    public CounterpartyDto createCounterparty(String name,
                                              DebtType type, String account,
                                              DebtType type2, String account2) {
        return createCounterparty(name, null, type, account, type2, account2);
    }

    public CounterpartyDto createCounterparty(String name, String comCode,
                                              DebtType type, String account,
                                              DebtType type2, String account2) {
        return createCounterparty(name, comCode, type, account, type2, account2, null);
    }

    public CounterpartyDto createCounterparty(String name, String comCode,
                                              DebtType type, String account,
                                              DebtType type2, String account2,
                                              String bankAccount) {
        return createCounterparty(name, comCode, type, account, type2, account2, bankAccount, null);
    }

    public CounterpartyDto createCounterparty(String name, String comCode,
                                              DebtType type, String account,
                                              DebtType type2, String account2,
                                              String bankAccount,
                                              String accountNoDebt) {
        CounterpartyDto counterparty = new CounterpartyDto();

        counterparty.setName(name);
        counterparty.setComCode(comCode);
        if (type != null && account != null)
            counterparty.setAccount(type, new GLOperationAccount(account, "Account " + account));
        if (type2 != null && account2 != null)
            counterparty.setAccount(type2, new GLOperationAccount(account2, "Account " + account));
        if (StringHelper.hasValue(accountNoDebt))
            counterparty.setNoDebtAccount(new GLOperationAccount(accountNoDebt, "Account" + accountNoDebt));

        if (StringHelper.hasValue(bankAccount))
            counterparty.setBanks(Collections.singletonList(new DocBankAccount(bankAccount)));

        counterparty = counterpartySqlMapper.toDto(dbServiceSQL.saveEntityInCompany(counterpartySqlMapper.toEntity(counterparty)));
        clearCaches();
        assertThat(counterparty.getId()).isNotNull();
        return counterparty;
    }

    public CounterpartyBuilder createCounterpartyBuilder() {
        return new CounterpartyBuilder();
    }


    public class CounterpartyBuilder {
        Long foreignId;
        String name;
        String comCode;
        String vatCode;
        DebtType type;
        String account;
        DebtType type2;
        String account2;
        String bankAccount;
        String accountNoDebt;
        List<GamaMoney> debts;
        List<Location> locations;
        Set<String> labels;

        public CounterpartyBuilder foreignId(long foreignId) {
            this.foreignId = foreignId;
            return this;
        }
        public CounterpartyBuilder name(String name) {
            this.name = name;
            return this;
        }
        public CounterpartyBuilder comCode(String comCode) {
            this.comCode = comCode;
            return this;
        }
        public CounterpartyBuilder vatCode(String vatCode) {
            this.vatCode = vatCode;
            return this;
        }
        public CounterpartyBuilder account(DebtType type, String account) {
            this.type = type;
            this.account = account;
            return this;
        }
        public CounterpartyBuilder account2(DebtType type, String account) {
            this.type2 = type;
            this.account2 = account;
            return this;
        }
        public CounterpartyBuilder bankAccount(String bankAccount) {
            this.bankAccount = bankAccount;
            return this;
        }
        public CounterpartyBuilder accountNoDebt(String accountNoDebt) {
            this.accountNoDebt = accountNoDebt;
            return this;
        }
        public CounterpartyBuilder debts(List<GamaMoney> debts) {
            this.debts = debts;
            return this;
        }
        public CounterpartyBuilder locations(List<Location> locations) {
            this.locations = locations;
            return this;
        }
        public CounterpartyBuilder labels(Set<String> labels) {
            this.labels = labels;
            return this;
        }
        public CounterpartySql build() {
            CounterpartySql entity = new CounterpartySql();
            entity.setForeignId(foreignId);
            entity.setName(name);
            entity.setComCode(comCode);
            entity.setVatCode(vatCode);
            if (type != null && account != null)
                entity.setAccount(type, new GLOperationAccount(account, "Account " + account));
            if (type2 != null && account2 != null)
                entity.setAccount(type2, new GLOperationAccount(account2, "Account " + account));
            if (StringHelper.hasValue(accountNoDebt))
                entity.setNoDebtAccount(new GLOperationAccount(accountNoDebt, "Account" + accountNoDebt));

            if (StringHelper.hasValue(bankAccount)) entity.setBanks(Collections.singletonList(new DocBankAccount(bankAccount)));

            if (type != null && debts != null) entity.getDebts().put(type.toString(), debts);
            entity.setLabels(labels);
            entity.setLocations(locations);

            dbServiceSQL.saveEntityInCompany(entity);
            clearCaches();
            assertThat(entity.getId()).isNotNull();
            return entity;
        }

    }

    public BankAccountDto createBankAccount(String bankAccount, String glNumber) {
        return createBankAccount(bankAccount, null, null, Lists.newArrayList(glNumber));
    }

    public BankAccountDto createBankAccount(String bankAccount, String currency, String glNumber) {
        return createBankAccount(bankAccount, null, Lists.newArrayList(currency), Lists.newArrayList(glNumber));
    }

    public BankAccountDto createBankAccount(String bankAccount, DocBank docBank, String currency, String glNumber) {
        return createBankAccount(bankAccount, docBank, Lists.newArrayList(currency), Lists.newArrayList(glNumber));
    }

    public BankAccountDto createBankAccount(String bankAccount, List<String> currency, List<String> glNumber) {
        return createBankAccount(bankAccount, null, Lists.newArrayList(currency), glNumber);
    }

    public BankAccountDto createBankAccount(String bankAccount, DocBank docBank, List<String> currency, List<String> glNumber) {
        BankAccountDto entity = new BankAccountDto();
        entity.setAccount(bankAccount);
        GLMoneyAccount moneyAccount = new GLMoneyAccount();
        moneyAccount.setAccounts(new ArrayList<>());
        if (currency == null) {
            moneyAccount.getAccounts().add(new GLCurrencyAccount(null, new GLOperationAccount(glNumber.get(0), "Bank " + glNumber.get(0))));
        } else {
            for (int i = 0; i < currency.size(); i++) {
                moneyAccount.getAccounts().add(new GLCurrencyAccount(currency.get(i), new GLOperationAccount(glNumber.get(i), "Bank " + glNumber.get(i))));
            }
        }
        entity.setMoneyAccount(moneyAccount);
        entity.setBank(docBank);
        entity = bankAccountSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(bankAccountSqlMapper.toEntity(entity)));
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public CashDto createCash(String name, String glNumber) {
        return createCash(name, null, Lists.newArrayList(glNumber));
    }

    public CashDto createCash(String name, String currency, String glNumber) {
        return createCash(name, Lists.newArrayList(currency), Lists.newArrayList(glNumber));
    }

    public CashDto createCash(String name, List<String> currency, List<String> glNumber) {
        CashDto entity = new CashDto();
        entity.setName(name);
        GLMoneyAccount moneyAccount = new GLMoneyAccount();
        moneyAccount.setAccounts(new ArrayList<>());
        if (currency == null) {
            moneyAccount.getAccounts().add(new GLCurrencyAccount(null, new GLOperationAccount(glNumber.get(0), "Cash " + glNumber.get(0))));
        } else {
            for (int i = 0; i < glNumber.size(); i++) {
                moneyAccount.getAccounts().add(new GLCurrencyAccount(currency.get(i), new GLOperationAccount(glNumber.get(i), "Cash " + glNumber.get(i))));
            }
        }
        entity.setMoneyAccount(moneyAccount);
        entity = cashSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(cashSqlMapper.toEntity(entity)));
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public GLAccountSql createGLAccount(String number, String name) {
        return createGLAccount(number, name, null, GLAccountType.ASSETS, false);
    }

    public GLAccountSql createGLAccount(String number, String name, GLAccountType type) {
        return createGLAccount(number, name, null, type, false);
    }

    public GLAccountSql createGLAccount(String number, String name, String parent, GLAccountType type) {
        return createGLAccount(number, name, parent, type, false);
    }

    public GLAccountSql createGLAccount(String number, String name, String parent, GLAccountType type, boolean inner) {
        GLAccountSql entity = new GLAccountSql();
        entity.setCompanyId(getCompanyId());
        entity.setNumber(number);
        entity.setName(name);
        entity.setType(type);
        entity.setInner(inner);

        dbServiceSQL.saveEntityInCompany(entity);
        clearCaches();

        assertThat(entity.getId()).isNotNull();
        return entity;
    }

    public ResponsibilityCenterSql createRC(String name) {
        ResponsibilityCenterSql entity = new ResponsibilityCenterSql();
        entity.setName(name);

        entity = dbServiceSQL.saveEntityInCompany(entity);
        assertThat(entity.getId()).isNotNull();

        clearCaches();
        return entity;
    }


    public DocRC createDocRC(ResponsibilityCenterSql rc) {
        return new DocRC(rc.getId(), rc.getName());
    }

    public ManufacturerDto createManufacturer(String name) {
        ManufacturerSql entity = new ManufacturerSql();
        entity.setName(name);

        dbServiceSQL.saveEntityInCompany(entity);
        clearCaches();
        assertThat(entity.getId()).isNotNull();
        return manufacturerSqlMapper.toDto(entity);
    }

    public void importJson(String filename, EntityType entityType, DataFormatType format) {
        String json = StringHelper.readFromFile("imports" + File.separator + filename);
        assertThat(json).isNotNull();

        String name = storageService.upload(json, null, null, null);
        assertThat(name).isNotNull();
        clearCaches();

        taskQueueService.queueTask(new ImportTask(getCompanyId(), name, 0, entityType, false, format));
        assertThat(deferredTasksCount(ConstWorkers.IMPORT_QUEUE)).isEqualTo(1);
        runAllDeferredTasks(ConstWorkers.IMPORT_QUEUE);

        clearCaches();
    }

    //Used in JPA

    public PageRequest moneyPageRequest(AccountType type, Long id, String currency) {
        return moneyPageRequest(type, id, currency, LocalDate.of(2015, 1, 1), LocalDate.of(2015, 12, 31));
    }

    public PageRequest moneyPageRequest(AccountType type, Long id, String currency, LocalDate dateFrom, LocalDate dateTo) {
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.ORIGIN_ID, id));
        request.getConditions().add(new PageRequestCondition(CustomSearchType.ACCOUNT_TYPE, type));
        request.getConditions().add(new PageRequestCondition(CustomSearchType.CURRENCY, currency));
        request.setOrder("mainIndex");
        request.setRefresh(true);
        request.setDateRange(true);
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);
        return request;
    }

    public RoleDto createRole(String name, Permission... permissions) throws GamaApiException {
        RoleDto role = new RoleDto();
        role.setName(name);
        role.setPermissions(Arrays.stream(permissions).map(Permission::toString).collect(Collectors.toSet()));

        APIResult<RoleDto> apiResult = employeeApi.saveRole(role);
        assertThat(apiResult.getError()).isNull();
        clearCaches();

        role = apiResult.getData();
        assertThat(role.getId()).isNotNull();

        runAllDeferredTasks();
        assertThat(deferredTasksCount()).isEqualTo(0);
        clearCaches();

        return role;
    }

    // SQL methods

    public CounterpartySql createCounterpartySql(String name, String comCode) {
        return createCounterpartySql(name, comCode, null);
    }

    public CounterpartySql createCounterpartySql(String name, DebtType type, String account) {
        return createCounterpartySql(name, null, type, account, null, null);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, DebtType type, String account) {
        return createCounterpartySql(name, comCode, type, account, null, null);
    }

    public CounterpartySql createCounterpartySql(String name, DebtType type, String account,
                                                 DebtType type2, String account2) {
        return createCounterpartySql(name, null, type, account, type2, account2);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, DebtType type, String account,
                                                 DebtType type2, String account2) {
        return createCounterpartySql(null, name, comCode, null, type, account, type2, account2,
                null, null, null, null);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, String vatCode) {
        return createCounterpartySql(name, comCode, vatCode, null);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, String vatCode, DebtType type) {
        return createCounterpartySql(name, comCode, vatCode, type, null);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, String vatCode, DebtType type, List<GamaMoney> debtsMoneyList) {
        return createCounterpartySql(name, comCode, vatCode, type, debtsMoneyList, null, null);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, String vatCode, DebtType type, List<GamaMoney> debtsMoneyList,
                                                 Set<String> labels) {
        return createCounterpartySql(name, comCode, vatCode, type, debtsMoneyList, null, labels);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, String vatCode, DebtType type, List<GamaMoney> debtsMoneyList,
                                                 List<Location> locations) {
        return createCounterpartySql(name, comCode, vatCode, type, debtsMoneyList, locations, null);
    }

    public CounterpartySql createCounterpartySql(String name, String comCode, String vatCode, DebtType type, List<GamaMoney> debtsMoneyList,
                                                 List<Location> locations, Set<String> labels) {
        return createCounterpartySql(null, name, comCode, vatCode, type, null, null, null,
                null, debtsMoneyList, locations, labels);
    }

    public CounterpartySql createCounterpartySql(String name, DebtType type, String account, String bankAccount) {
        return createCounterpartySql(null, name, null, null, type, account, null, null,
                bankAccount, null, null, null);
    }

    public CounterpartySql createCounterpartySql(Long foreignId, String name, String comCode) {
        return createCounterpartySql(foreignId, name, comCode,
                null, null, null, null, null, null, null, null, null);
    }

    public CounterpartySql createCounterpartySql(Long foreignId, String name, String comCode, String vatCode, DebtType type,
                                                 String account, DebtType type2, String account2, String bankAccount,
                                                 List<GamaMoney> debtsMoneyList, List<Location> locations, Set<String> labels) {
        CounterpartyDto counterpartyDto = new CounterpartyDto();
        counterpartyDto.setForeignId(foreignId);
        counterpartyDto.setName(name);
        counterpartyDto.setComCode(comCode);
        counterpartyDto.setVatCode(vatCode);
        counterpartyDto.setLocations(locations);
        if (type != null && account != null)
            counterpartyDto.setAccount(type, new GLOperationAccount(account, "Account " + account));
        if (type2 != null && account2 != null)
            counterpartyDto.setAccount(type2, new GLOperationAccount(account2, "Account " + account));
        if (StringHelper.hasValue(bankAccount)) counterpartyDto.setBanks(Collections.singletonList(new DocBankAccount(bankAccount)));
        if (type != null && debtsMoneyList != null) counterpartyDto.getDebts().put(type.toString(), debtsMoneyList);
        if (labels != null) counterpartyDto.setLabels(labels);

        CounterpartySql counterpartySql = dbServiceSQL.saveEntityInCompany(counterpartySqlMapper.toEntity(counterpartyDto));
        clearCaches();
        assertThat(counterpartySql.getId()).isNotNull();
        return counterpartySql;
    }

    public <E extends BaseCompanySql> List<E> queryInCompany(Class<E> type, long companyId)  {
        return queryInCompany(type, companyId, null);
    }

    public <E extends BaseCompanySql> List<E> queryInCompany(Class<E> type, long companyId, String graphName)  {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cr = cb.createQuery(type);
        Root<E> root = cr.from(type);
        cr.select(root);
        cr.where(cb.equal(root.get("companyId"), companyId));

        TypedQuery<E> query = entityManager.createQuery(cr);
        if (StringHelper.hasValue(graphName)) {
            query = query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
        }
        List<E> entityListSql = query.getResultList();

        if (entityListSql == null || entityListSql.isEmpty()) return null;
        return entityListSql;
    }

    protected ResponsibilityCenterDto createRCSql(String name) throws GamaApiException {
        return createRCSql(name, null);
    }

    protected ResponsibilityCenterDto createRCSql(String name, ResponsibilityCenterDto parent) throws GamaApiException {
        ResponsibilityCenterDto rc = new ResponsibilityCenterDto();
        rc.setDescription("Description");
        rc.setName(name);
        rc.setDepth(parent == null ? 0 : parent.getDepth() + 1);
        rc.setParent(parent);
        return glApi.saveRC(rc).getData();
    }

    protected GLOperationAccount createGLOperationAccount(GLAccountSql glAccountSql) {
        return new GLOperationAccount(glAccountSql.getNumber(), glAccountSql.getName());
    }

    protected GLOpeningBalanceOperationDto createOBO(GLOperationAccount account, int debit, int credit, ResponsibilityCenterDto... rcs) {
        GLOpeningBalanceOperationDto glOpeningBalanceOperationDto = new GLOpeningBalanceOperationDto();
        glOpeningBalanceOperationDto.setAccount(account);
        glOpeningBalanceOperationDto.setDebit(GamaMoney.parse("EUR " + debit));
        glOpeningBalanceOperationDto.setCredit(GamaMoney.parse("EUR " + credit));
        if (rcs != null) {
            List<DocRC> docRCList = new ArrayList<>();
            for (ResponsibilityCenterDto rc : rcs) {
                docRCList.add(new DocRC(rc));
            }
            glOpeningBalanceOperationDto.setRc(docRCList);
        }
        return glOpeningBalanceOperationDto;
    }

    protected GLOpeningBalanceDto createOB() {
        GLOpeningBalanceDto openingBalance = new GLOpeningBalanceDto();
        openingBalance.setDate(LocalDate.parse("2014-12-31"));
        openingBalance.setNumber("123");
        openingBalance.setNote("Note");
        openingBalance.setIBalance(true);
        openingBalance.setCompanyId(getCompanyId());
        openingBalance.setFinishedGL(true);
        openingBalance.setBalances(new ArrayList<>());
        return openingBalance;
    }

    protected GLOperationDto createGLO(GLOperationAccount accountDebit, List<DocRC> debitRC,
                                       GLOperationAccount accountCredit, List<DocRC> creditRC, int money) {
        return createGLO(accountDebit, debitRC, accountCredit, creditRC, GamaMoney.parse("EUR " + money));
    }

    protected GLOperationDto createGLO(GLOperationAccount accountDebit, List<DocRC> debitRC,
                                       GLOperationAccount accountCredit, List<DocRC> creditRC, GamaMoney money) {
        GLOperationDto glOperationDto = new GLOperationDto();
        glOperationDto.setDebit(accountDebit);
        glOperationDto.setCredit(accountCredit);
        glOperationDto.setDebitRC(debitRC);
        glOperationDto.setCreditRC(creditRC);

        return new GLOperationDto(accountDebit, debitRC, accountCredit, creditRC, money);
    }

    protected List<DocRC> rcList(ResponsibilityCenterDto... rcs) {
        List<DocRC> docRCList = new ArrayList<>();
        for (ResponsibilityCenterDto rc : rcs) {
            docRCList.add(new DocRC(rc));
        }
        return docRCList;
    }

    protected DoubleEntryDto createDE(String date) {
        return createDE(LocalDate.parse(date));
    }

    protected DoubleEntryDto createDE(LocalDate date) {
        DoubleEntryDto doubleEntry = new DoubleEntryDto();
        doubleEntry.setDate(date);
        doubleEntry.setNumber("456");
        doubleEntry.setContent("Content");
        doubleEntry.setCompanyId(getCompanyId());
        doubleEntry.setFinishedGL(true);
        doubleEntry.setOperations(new ArrayList<>());
        return doubleEntry;
    }

    protected TupleTest createTuple (String operationType, int rcId, String rcName, String number, String name,
                                     String currency, int obDeb, int obCred) {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("operation_type",operationType);
        r1.put("rc_id", BigInteger.valueOf(rcId));
        r1.put("rc_name",rcName);
        r1.put("number",number);
        r1.put("name",name);
        r1.put("currency",currency);
        r1.put("ob_deb",new BigDecimal(obDeb));
        r1.put("ob_cred",new BigDecimal(obCred));
        return new TupleTest(r1);
    }

    protected IntermediateBalanceRequest createIBRequest(int yearToClose) {
        IntermediateBalanceRequest request = new IntermediateBalanceRequest();
        request.setYearToClose(yearToClose);
        return request;
    }

    protected void saveBalance (GLOpeningBalanceDto balance) {
        dbServiceSQL.saveWithCounter(glOpeningBalanceSqlMapper.toEntity(balance));
    }

    public static class TupleTest implements Tuple {

        private final Map<String, Object> tuple;

        public TupleTest(Map<String, Object> tuple) {
            this.tuple = tuple;
        }

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <X> X get(String alias, Class<X> type) {
            return (X) tuple.get(alias);
        }

        @Override
        public Object get(String alias) {
            return null;
        }

        @Override
        public <X> X get(int i, Class<X> type) {
            return null;
        }

        @Override
        public Object get(int i) {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public List<TupleElement<?>> getElements() {
            return null;
        }
    }

    //jpa debts tests
    public TypedQuery<CounterpartySql> queryCounterpartyWithReminderInCompany() {
        return queryCounterpartyWithReminderInCompany(null);
    }


    public TypedQuery<CounterpartySql> queryCounterpartyWithReminderInCompany(DebtType debtType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CounterpartySql> cr = cb.createQuery(CounterpartySql.class);
        Root<CounterpartySql> root = cr.from(CounterpartySql.class);
        cr.select(root);

        Predicate where = cb.equal(root.get("companyId"), getCompanyId());
        Predicate reminder = cb.isTrue(cb.function("jsonb_path_exists", Boolean.class, root.get("debts"),
                cb.literal("$.*[*] ? (@.amount != 0)")));

        where = cb.and(where, reminder);

        if (debtType != null) {
            Predicate type = cb.isTrue(cb.function("jsonb_path_exists", Boolean.class, root.get("debts"),
                    cb.literal(DebtType.VENDOR.equals(debtType) ? "$.V[*]" : "$.C[*]")));
            where = cb.and(where, type);
        }
        cr.where(where);

        return entityManager.createQuery(cr);
    }

    public TypedQuery<DebtCoverageSql> queryDebtCoverageInCompany() {
        return queryDebtCoverageInCompany(null);
    }

    public TypedQuery<DebtCoverageSql> queryDebtCoverageInCompany(Boolean docFinished) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DebtCoverageSql> cr = cb.createQuery(DebtCoverageSql.class);
        Root<DebtCoverageSql> root = cr.from(DebtCoverageSql.class);
        cr.select(root);

        Predicate where = cb.equal(root.get("companyId"), getCompanyId());

        if (docFinished != null) {
            Predicate   finished = cb.equal(root.get("finished"), docFinished);
            where = cb.and(where, finished);
        }
        cr.where(where);

        return entityManager.createQuery(cr);
    }

    public List<DebtNowSql> getDebtNow(DebtType type, String currency) {
        return entityManager.createQuery(
                        "SELECT c FROM " + DebtNowSql.class.getName() + " c" +
                                " WHERE companyId = :companyId" +
                                " AND type = :type" +
                                " AND initial.currency = :currency" +
                                " ORDER BY doc.date, id",
                        DebtNowSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("type", type)
                .setParameter("currency", currency)
                .getResultList();
    }

    public List<DebtNowSql> getDebtNow(long counterpartyId, DebtType type, String currency) {
        return entityManager.createQuery(
                        "SELECT c FROM " + DebtNowSql.class.getName() + " c" +
                                " WHERE companyId = :companyId" +
                                " AND counterparty.id = :counterpartyId" +
                                " AND type = :type" +
                                " AND initial.currency = :currency" +
                                " ORDER BY doc.date, id",
                        DebtNowSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("counterpartyId", counterpartyId)
                .setParameter("type", type)
                .setParameter("currency", currency)
                .getResultList();
    }

    public List<DebtCoverageDto> getDebtCoverage(long counterpartyId, DebtType type, String currency) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery(
                                "SELECT c FROM " + DebtCoverageSql.class.getName() + " c" +
                                        " WHERE companyId = :companyId" +
                                        " AND counterparty.id = :counterpartyId" +
                                        " AND type = :type" +
                                        " AND amount.currency = :currency" +
                                        " ORDER BY doc.date, id",
                                DebtCoverageSql.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("counterpartyId", counterpartyId)
                        .setParameter("type", type)
                        .setParameter("currency", currency)
                        .getResultStream()
                        .map(debtCoverageSqlMapper::toDto)
                        .collect(Collectors.toList()));
    }

    public List<DebtHistoryDto> getDebtHistory(long counterpartyId, DebtType type, String currency) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery(
                                "SELECT c FROM " + DebtHistorySql.class.getName() + " c" +
                                        " WHERE companyId = :companyId" +
                                        " AND counterparty.id = :counterpartyId" +
                                        " AND type = :type" +
                                        " AND exchange.currency = :currency" +
                                        " ORDER BY doc.date, id",
                                DebtHistorySql.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("counterpartyId", counterpartyId)
                        .setParameter("type", type)
                        .setParameter("currency", currency)
                        .getResultStream()
                        .map(debtHistorySqlMapper::toDto)
                        .collect(Collectors.toList()));
    }

    public List<MoneyHistoryDto> getMoneyHistory(AccountType accountType, long accountId) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery(
                                "SELECT c FROM " + MoneyHistorySql.class.getName() + " c" +
                                        " WHERE companyId = :companyId" +
                                        " AND accountId = :accountId" +
                                        " AND accountType = :accountType" +
                                        " ORDER BY doc.date, id",
                                MoneyHistorySql.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("accountId", accountId)
                        .setParameter("accountType", accountType)
                        .getResultStream()
                        .map(moneyHistorySqlMapper::toDto)
                        .collect(Collectors.toList()));
    }

    public List<MoneyHistoryDto> getMoneyHistory(AccountType accountType, long accountId, String currency) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery(
                                "SELECT c FROM " + MoneyHistorySql.class.getName() + " c" +
                                        " WHERE companyId = :companyId" +
                                        " AND accountId = :accountId" +
                                        " AND accountType = :accountType" +
                                        " AND exchange.currency = :currency" +
                                        " ORDER BY doc.date, id",
                                MoneyHistorySql.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("accountId", accountId)
                        .setParameter("accountType", accountType)
                        .setParameter("currency", currency)
                        .getResultStream()
                        .map(moneyHistorySqlMapper::toDto)
                        .collect(Collectors.toList()));
    }

    public void saveInventoryNow(InventoryNowDto inventoryNow) {
        dbServiceSQL.saveEntityInCompany(inventoryNowSqlMapper.toEntity(inventoryNow));
        clearCaches();
    }

    public List<InventoryNowDto> getInventoryNow(long partId, long warehouseId) {
        return getInventoryNow(partId, warehouseId, null);
    }

    public List<InventoryNowDto> getInventoryNow(long partId, long warehouseId, PartSN sn) {
        return dbServiceSQL.executeAndReturnInTransaction(em -> {
            StringJoiner sj = new StringJoiner(" ");
            sj.add("SELECT n FROM " + InventoryNowSql.class.getName() + " n");
            sj.add("WHERE companyId = :companyId");
            sj.add("AND part.id = :partId");
            sj.add("AND warehouse.id = :warehouseId");
            if (sn != null) sj.add("AND sn.sn = :sn");
            sj.add(" ORDER BY doc.date, id");

            TypedQuery<InventoryNowSql> q = entityManager.createQuery(sj.toString(), InventoryNowSql.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("partId", partId);
            q.setParameter("warehouseId", warehouseId);
            if (sn != null) q.setParameter("sn", sn.getSn());
            return q.getResultStream().map(inventoryNowSqlMapper::toDto).collect(Collectors.toList());
        });
    }

    public List<InventoryHistoryDto> getInventoryHistory(long partId) {
        return getInventoryHistory(partId, null);
    }

    public List<InventoryHistoryDto> getInventoryHistory(long partId, Long warehouseId) {
        return dbServiceSQL.executeAndReturnInTransaction(em -> {
            StringJoiner sj = new StringJoiner(" ");
            sj.add("SELECT h FROM " + InventoryHistorySql.class.getName() + " h");
            sj.add("WHERE companyId = :companyId");
            sj.add("AND part.id = :partId");
            if (warehouseId != null) sj.add("AND warehouse.id = :warehouseId");
            sj.add(" ORDER BY doc.date, id");

            TypedQuery<InventoryHistorySql> q = entityManager.createQuery(sj.toString(), InventoryHistorySql.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("partId", partId);
            if (warehouseId != null) q.setParameter("warehouseId", warehouseId);
            return q.getResultStream().map(inventoryHistorySqlMapper::toDto).collect(Collectors.toList());
        });
    }

    public <E extends EntitySql> void checkStatistics(Class<E> type, int insert, int update, int delete) {
        EntityStatistics statistics = entityManager.unwrap(Session.class).getSessionFactory()
                .getStatistics().getEntityStatistics(type.getName());

        assertThat(statistics.getInsertCount()).isEqualTo(insert);
        assertThat(statistics.getUpdateCount()).isEqualTo(update);
        assertThat(statistics.getDeleteCount()).isEqualTo(delete);
    }

    public void clearStatistics() {
        entityManager.unwrap(Session.class).getSessionFactory().getStatistics().clear();
    }
}
