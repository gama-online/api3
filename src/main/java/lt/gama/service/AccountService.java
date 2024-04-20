package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lt.gama.Constants;
import lt.gama.api.ex.GamaApiServerErrorException;
import lt.gama.api.request.CreateCompanyRequest;
import lt.gama.api.request.LoginRequest;
import lt.gama.api.response.AccountInfoResponse;
import lt.gama.api.response.ApiLoginResponse;
import lt.gama.api.response.CompanyInfoResponse;
import lt.gama.api.response.LoginResponse;
import lt.gama.auth.i.IAuth;
import lt.gama.auth.impl.Auth;
import lt.gama.auth.service.AESCipherService;
import lt.gama.auth.service.TokenService;
import lt.gama.helpers.*;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.RoleDto;
import lt.gama.model.i.IEmployee;
import lt.gama.model.mappers.EmployeeSqlMapper;
import lt.gama.model.mappers.RoleSqlMapper;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.EmployeeSql_;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.AccountSql_;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.Contact;
import lt.gama.model.type.Contact.ContactSubtype;
import lt.gama.model.type.Contact.ContactType;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.auth.*;
import lt.gama.model.type.cf.CFDescription;
import lt.gama.model.type.enums.CFValueType;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.EmployeeType;
import lt.gama.model.type.enums.Permission;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.service.ex.GamaServerErrorException;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaNotFoundException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import lt.gama.service.repo.AccountRepository;
import lt.gama.service.repo.CompanyRepository;
import lt.gama.service.repo.EmployeeRepository;
import lt.gama.service.repo.RoleRepository;
import lt.gama.tasks.DeleteCompanyDocumentsTask;
import lt.gama.tasks.DeleteCompanyTask;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author valdas
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @PersistenceContext
    EntityManager entityManager;
    
    @Value("${gama.init.login}") private String gamaInitLogin;
    @Value("${gama.init.password}") private String gamaInitPassword;
    @Value("${gama.url}") private String gamaUrl;

    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final DBServiceSQL dbServiceSQL;

    private final Auth auth;
    private final MailService mailService;
    private final AccountingService accountingService;
    private final GLService glServiceSQL;
    private final RoleSqlMapper roleSqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final TokenService tokenService;
    private final AESCipherService aesCipherService;
    private final TaskQueueService taskQueueService;

    AccountService(CompanyRepository companyRepository,
                   RoleRepository roleRepository,
                   MailService mailService,
                   AccountingService accountingService,
                   GLService glServiceSQL,
                   Auth auth,
                   DBServiceSQL dbServiceSQL,
                   RoleSqlMapper roleSqlMapper,
                   EmployeeSqlMapper employeeSqlMapper,
                   AuthSettingsCacheService authSettingsCacheService,
                   TokenService tokenService,
                   AESCipherService aesCipherService,
                   TaskQueueService taskQueueService,
                   EmployeeRepository employeeRepository,
                   AccountRepository accountRepository) {
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.mailService = mailService;
        this.accountingService = accountingService;
        this.glServiceSQL = glServiceSQL;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.roleSqlMapper = roleSqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.authSettingsCacheService = authSettingsCacheService;
        this.tokenService = tokenService;
        this.aesCipherService = aesCipherService;
        this.taskQueueService = taskQueueService;
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void initData() {
        try {
            // Company #1
            CompanySql company1 = new CompanySql();
            company1.setName("Test Company with long name #1");
            company1.setActiveAccounts(1);
            company1.setCode("123456");
            company1.setVatCode("LT111111");
            company1.setLocations(new ArrayList<>());
            company1.getLocations().add(
                    new Location("Head Office", "Address Line 1", "Address Line 2",
                            "Address Line 3", "0001", "Kaunas", null, "Lietuva"));
            company1.setContacts(new ArrayList<>());
            NameContact contact = new NameContact();
            contact.setName("Admin A");
            contact.setContacts(new ArrayList<>());
            contact.getContacts().add(new Contact(ContactType.phone, ContactSubtype.work, "123-4567"));
            contact.getContacts().add(new Contact(ContactType.phone, ContactSubtype.home, "133-3333"));
            contact.getContacts().add(new Contact(ContactType.email, ContactSubtype.work, "valdas@company1.lt"));
            company1.getContacts().add(contact);

            company1.setSettings(new CompanySettings());
            company1.getSettings().setRegion("LT");
            company1.getSettings().setLanguage("en");
            company1.getSettings().setCountry("US");
            company1.getSettings().setCurrency(new CurrencySettings());
            company1.getSettings().getCurrency().setCode("USD");
            company1.getSettings().setDecimal(2);
            company1.getSettings().setDecimalPrice(2);
            company1.getSettings().setDecimalCost(2);

            company1 = companyRepository.save(company1);
            Validators.checkNotNull(company1.getId());

            auth.setCompanyId(company1.getId());
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            RoleDto role11 = new RoleDto("Administrator");
            role11.setPermissions(new HashSet<>());
            role11.getPermissions().add(Permission.ADMIN.toString());
            role11 = roleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(roleSqlMapper.toEntity(role11)));
            Validators.checkNotNull(role11.getId());

            EmployeeDto employee1 = new EmployeeDto("Valdas");
            employee1.setType(EmployeeType.ACCOUNTANT);
            employee1.setActive(Boolean.TRUE);
            employee1.setEmail(gamaInitLogin);
            employee1.setRoles(new HashSet<>());
            employee1.getRoles().add(new EmployeeRole(role11.getId(), role11.getName(), role11.getPermissions()));
            employee1 = employeeSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(employeeSqlMapper.toEntity(employee1)));
            Validators.checkNotNull(employee1.getId());

            // Company #2
            CompanySql company2 = new CompanySql();
            company2.setName("Test Company #2");
            company2.setActiveAccounts(1);
            company2.setCode("234567");
            company2.setVatCode("LT22222222");
            company2.setLocations(new ArrayList<>());
            company2.getLocations().add(
                    new Location("Main Office", "Address Line 2-1",
                            "Address Line 2-2", "Address Line 2-3", "0002",
                            "Babtai", "Kauno raj.", "Lietuva"));
            company2.setContacts(new ArrayList<>());
            contact = new NameContact();
            contact.setName("Admin B");
            contact.setContacts(new ArrayList<>());
            contact.getContacts().add(new Contact(ContactType.phone, ContactSubtype.work, "222-4567"));
            contact.getContacts().add(new Contact(ContactType.email, ContactSubtype.work, "valdas@company1.lt"));
            company2.getContacts().add(contact);

            company2.setSettings(new CompanySettings());
            company2.getSettings().setRegion("LT");
            company2.getSettings().setLanguage("en");
            company2.getSettings().setCountry("LT");
            company2.getSettings().setCurrency(new CurrencySettings());
            company2.getSettings().getCurrency().setCode("EUR");
            company2.getSettings().setDecimal(2);
            company2.getSettings().setDecimalPrice(2);
            company2.getSettings().setDecimalCost(2);
            company2.getSettings().setAccYear(2015);
            company2.getSettings().setAccMonth(1);
            company2.getSettings().setDocType(new HashMap<>());
            company2.getSettings().setGl(new CompanySettingsGL());
            company2.getSettings().getGl().setAccVATPay(new GLOperationAccount("4492", "Mokėtinas pridėtinės vertės mokestis"));
            company2.getSettings().getGl().setAccVATRec(new GLOperationAccount("2441", "Gautinas pridėtinės vertės mokestis"));
            company2.getSettings().getGl().setAccBankOther(new GLOperationAccount("6810", "Kitos finansinės ir investicinės veiklos sąnaudos"));
            company2.getSettings().getGl().setAccPurchaseExpense(new GLOperationAccount("6204", "Atidėjinių sąnaudos"));
            company2.getSettings().getGl().setAccTemp(new GLOperationAccount("390", "Suvestinės sąskaitos"));
            company2.getSettings().getGl().setAccProfitLoss(new GLOperationAccount("3411", "Pelno (nuostolių) ataskaitoje pripažintas ataskaitinių metų grynasis nepaskirstytasis pelnas (nuostoliai)"));

            company2.getSettings().setCfPart(new ArrayList<>());
            CFDescription cfPart1 = new CFDescription();
            cfPart1.setKey("COLOR");
            cfPart1.setLabel("Spalva");
            cfPart1.setOrder(1);
            cfPart1.setType(CFValueType.STRING);
            company2.getSettings().getCfPart().add(cfPart1);
            CFDescription cfPart2 = new CFDescription();
            cfPart2.setKey("SIZE");
            cfPart2.setLabel("Dydis");
            cfPart2.setOrder(2);
            cfPart2.setType(CFValueType.NUMBER);
            company2.getSettings().getCfPart().add(cfPart2);
            CFDescription cfPart3 = new CFDescription();
            cfPart3.setKey("WATERPROOF");
            cfPart3.setLabel("Atsparus vandeniui");
            cfPart3.setOrder(3);
            cfPart3.setType(CFValueType.BOOLEAN);
            company2.getSettings().getCfPart().add(cfPart3);
            company2.getSettings().setDocNames("en");
            company2 = companyRepository.save(company2);
            Validators.checkNotNull(company2.getId());
            authSettingsCacheService.remove(company2.getId());
            auth.setCompanyId(company2.getId());
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            RoleDto role21 = new RoleDto("Administrator");
            role21.setPermissions(new HashSet<>());
            role21.getPermissions().add(Permission.ADMIN.toString());
            role21 = roleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(roleSqlMapper.toEntity(role21)));
            Validators.checkNotNull(role21.getId());

            RoleDto role22 = new RoleDto("RO");
            role22.setPermissions(new HashSet<>());
            role22.getPermissions().add(Permission.EMPLOYEE_R.toString());
            role22.getPermissions().add(Permission.BANK_R.toString());
            role22.getPermissions().add(Permission.CASH_R.toString());
            role22.getPermissions().add(Permission.PART_R.toString());
            role22.getPermissions().add(Permission.COUNTERPARTY_R.toString());
            role22.getPermissions().add(Permission.DOCUMENT_R.toString());
            role22.getPermissions().add(Permission.EMPLOYEE_OP_R.toString());
            role22.getPermissions().add(Permission.BANK_OP_R.toString());
            role22.getPermissions().add(Permission.CASH_OP_R.toString());
            role22 = roleSqlMapper.toDto(roleRepository.save(roleSqlMapper.toEntity(role22)));
            Validators.checkNotNull(role22.getId());

            EmployeeDto employee2 = new EmployeeDto("Admin");
            employee2.setType(EmployeeType.ACCOUNTANT);
            employee2.setActive(Boolean.TRUE);
            employee2.setEmail(gamaInitLogin);
            employee2.setRoles(new HashSet<>());
            employee2.getRoles().add(new EmployeeRole(role21.getId(), role21.getName(), role21.getPermissions()));
            employee2.getRoles().add(new EmployeeRole(role22.getId(), role22.getName(), role22.getPermissions()));
            employee2.setContacts(new HashSet<>());
            employee2.getContacts().add(new Contact(Contact.ContactType.email, Contact.ContactSubtype.work, gamaInitLogin));
            employee2 = employeeSqlMapper.toDto(employeeRepository.save(employeeSqlMapper.toEntity(employee2)));
            Validators.checkNotNull(employee2.getId());

            EmployeeDto employee3 = new EmployeeDto("Petras");
            employee3.setContacts(new HashSet<>());
            employee3.getContacts().add(new Contact(Contact.ContactType.mobile, Contact.ContactSubtype.work, "0 123 456789"));
            employee3.getContacts().add(new Contact(Contact.ContactType.mobile, Contact.ContactSubtype.home, "0 987 654321"));
            employee3 = employeeSqlMapper.toDto(employeeRepository.save(employeeSqlMapper.toEntity(employee3)));
            Validators.checkNotNull(employee3.getId());

            // Account
            AccountSql account = new AccountSql(gamaInitLogin);
            account.setAdmin(Boolean.TRUE);
            account.setCompanies(new ArrayList<>());
            account.getCompanies().add(new AccountInfo(company1, employee1));
            account.getCompanies().add(new AccountInfo(company2, employee2));
            account.setCompanyIndex(1);
            setSaltAndPassword(account, gamaInitPassword);
            accountRepository.save(account);
        } catch (GamaServerErrorException e) {
            throw new GamaApiServerErrorException(e);
        }
    }

    @Transactional
    public CompanySql companyCreate(CreateCompanyRequest request) {
        if (StringHelper.isEmpty(request.getLanguage())) request.setLanguage("lt");
        if (StringHelper.isEmpty(request.getCountry())) request.setCountry("LT");
        if (StringHelper.isEmpty(request.getTimeZone())) request.setTimeZone("Europe/Vilnius");

        CompanySql company = new CompanySql();
        company.setName(StringHelper.trim(request.getCompanyName()));
        company.setCode(StringHelper.trim(request.getCode()));
        company.setVatCode(StringHelper.trim(request.getVatCode()));
        company.setActiveAccounts(0);

        company.setRegistrationAddress(request.getRegistrationAddress());
        company.setBusinessAddress(request.getBusinessAddress());

        company.setSubscriberName(StringHelper.trim(request.getName()));
        company.setSubscriberEmail(StringHelper.trim(request.getLogin()).toLowerCase());

        company.setSettings(new CompanySettings());
        company.getSettings().setRegion(request.getCountry());
        company.getSettings().setLanguage(request.getLanguage());
        company.getSettings().setCurrency(new CurrencySettings());
        company.getSettings().getCurrency().setCode("EUR");
        company.getSettings().setDecimal(2);
        company.getSettings().setDecimalPrice(2);
        company.getSettings().setDecimalCost(2);
        company.getSettings().setAccYear(DateUtils.date().getYear());
        company.getSettings().setAccMonth(1);
        company.getSettings().setDocType(new HashMap<>());
        company.getSettings().setGl(new CompanySettingsGL());
        if ("lt".equals(request.getLanguage())) {
            company.getSettings().getGl().setAccVATPay(new GLOperationAccount("4492", "Mokėtinas pridėtinės vertės mokestis"));
            company.getSettings().getGl().setAccVATRec(new GLOperationAccount("2441", "Gautinas pridėtinės vertės mokestis"));
            company.getSettings().getGl().setAccBankOther(new GLOperationAccount("6810", "Kitos finansinės ir investicinės veiklos sąnaudos"));
            company.getSettings().getGl().setAccPurchaseExpense(new GLOperationAccount("6204", "Atidėjimų sąnaudos"));
            company.getSettings().getGl().setAccTemp(new GLOperationAccount("390", "Bendra sąskaitų suvestinė"));
            company.getSettings().getGl().setAccCurrRatePos(new GLOperationAccount("5803", "Teigiama valiutų kursų pokyčio įtaka"));
            company.getSettings().getGl().setAccCurrRateNeg(new GLOperationAccount("6803", "Neigiama valiutų kursų pokyčio įtaka"));
            company.getSettings().getGl().setAccProfitLoss(new GLOperationAccount("3411", "Pelno (nuostolių) ataskaitoje pripažintas ataskaitinių metų grynasis nepaskirstytasis pelnas (nuostoliai)"));

            company.getSettings().getGl().setProductAsset(new GLOperationAccount("2040", "Pirktų prekių, skirtų perparduoti, įsigijimo savikaina"));
            company.getSettings().getGl().setProductIncome(new GLOperationAccount("5000", "Parduotų prekių pajamos"));
            company.getSettings().getGl().setProductExpense(new GLOperationAccount("6000", "Parduotų prekių savikaina"));

            company.getSettings().getGl().setServiceIncome(new GLOperationAccount("5001", "Suteiktų paslaugų pajamos"));
            company.getSettings().getGl().setServiceExpense(new GLOperationAccount("6001", "Suteiktų paslaugų savikaina"));

            company.getSettings().getGl().setCounterpartyCustomer(new GLOperationAccount("2410", "Pirkėjų skolų vertė"));
            company.getSettings().getGl().setCounterpartyVendor(new GLOperationAccount("4430", "Skolos tiekėjams už prekes ir paslaugas"));
        }
        company.getSettings().setLanguage(StringHelper.trim(request.getLanguage()));
        company.getSettings().setCountry(StringHelper.trim(request.getCountry()));
        company.getSettings().setTimeZone(StringHelper.trim(request.getTimeZone()));


        if (BooleanUtils.isTrue(request.getActivateSubscription())) {
            company.setStatus(CompanyStatusType.SUBSCRIBER);
            company.setSubscriptionDate(LocalDate.now());
        }

        if ("LT".equals(request.getCountry())) {
            company.getSettings().setTimeZone("Europe/Vilnius");
        }

        company.getSettings().setDocNames(StringHelper.trim(request.getLanguage()));

        company = dbServiceSQL.saveEntity(company);
        Validators.checkNotNull(company.getId());

        authSettingsCacheService.remove(company.getId());
        auth.setCompanyId(company.getId());
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

        RoleDto role = new RoleDto("lt".equals(request.getLanguage()) ? "Darbuotojas" : "Worker");
        role.setPermissions(Set.of(
                Permission.EMPLOYEE_M.toString(),
                Permission.PART_M.toString(),
                Permission.COUNTERPARTY_M.toString(),
                Permission.BANK_M.toString(),
                Permission.CASH_M.toString(),
                Permission.BANK_OP_M.toString(),
                Permission.CASH_OP_M.toString(),
                Permission.EMPLOYEE_OP_M.toString(),
                Permission.DOCUMENT_M.toString()));
        role = roleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(roleSqlMapper.toEntity(role)));
        Validators.checkNotNull(role.getId());

        role = new RoleDto("lt".equals(request.getLanguage()) ? "Vadovas" : "Manager");
        role.setPermissions(Set.of(
                // everything like 'Worker'
                Permission.EMPLOYEE_M.toString(),
                Permission.PART_M.toString(),
                Permission.COUNTERPARTY_M.toString(),
                Permission.BANK_M.toString(),
                Permission.CASH_M.toString(),
                Permission.BANK_OP_M.toString(),
                Permission.CASH_OP_M.toString(),
                Permission.EMPLOYEE_OP_M.toString(),
                Permission.DOCUMENT_M.toString(),
                // plus all balances and product cost
                Permission.COUNTERPARTY_B.toString(),
                Permission.PART_B.toString(),
                Permission.PART_S.toString(),
                Permission.EMPLOYEE_B.toString(),
                Permission.BANK_B.toString(),
                Permission.CASH_B.toString()));
        role = roleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(roleSqlMapper.toEntity(role)));
        Validators.checkNotNull(role.getId());

        role = new RoleDto("lt".equals(request.getLanguage()) ? "Apskaitininkas" : "Accountant");
        role.setPermissions(Set.of(
                Permission.GL.toString(),
                Permission.SETTINGS.toString()));
        role = roleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(roleSqlMapper.toEntity(role)));
        Validators.checkNotNull(role.getId());

        role = new RoleDto("lt".equals(request.getLanguage()) ? "Sistemos Administratorius" : "System Administrator");
        role.setPermissions(Set.of(Permission.ADMIN.toString()));
        role = roleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(roleSqlMapper.toEntity(role)));
        Validators.checkNotNull(role.getId());

        // create employee with "System Administrator" role
        EmployeeDto employee = new EmployeeDto(request.getName());
        if (BooleanUtils.isTrue(request.getAccountant())) employee.setType(EmployeeType.ACCOUNTANT);
        employee.setActive(Boolean.TRUE);
        employee.setEmail(request.getLogin());
        employee.setRoles(new HashSet<>());
        employee.getRoles().add(new EmployeeRole(role.getId(), role.getName(), role.getPermissions()));

        EmployeeSql e = dbServiceSQL.saveEntityInCompany(employeeSqlMapper.toEntity(employee));
        activateAccount(request.getLogin(), new AccountInfo(company, e), DateUtils.date());

        if (BooleanUtils.isTrue(request.getUseStdChartOfAccounts())) {
            glServiceSQL.templateAccount();
        }

        return company;
    }

    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;

    private int getDefaultCompanyIndex(AccountSql account, Integer companyIndex) {
        return companyIndex != null ? companyIndex
                : account.getCompanyIndex() != null ? account.getCompanyIndex()
                : 0;
    }

    private ApiLoginResponse generateApiLoginResponse(AccountSql account, Integer companyIndex, boolean genRefreshToken) {
        IAuth auth = createAuth(account, companyIndex);

        int defaultCompanyIndex = getDefaultCompanyIndex(account, companyIndex);
        AccountInfo defaultCompany = account.getCompanies().get(defaultCompanyIndex);

        ApiLoginResponse loginResponse = new ApiLoginResponse();
        loginResponse.setCompanyIndex(defaultCompanyIndex);
        loginResponse.setToken(tokenService.createToken(auth));
        loginResponse.setName(defaultCompany.getEmployeeName());
        loginResponse.setCompanyName(defaultCompany.getCompanyName());

        if (genRefreshToken) {
            Instant refreshTokenExpiration = Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS);
            String uuid = generateAndSaveRefreshToken(account, refreshTokenExpiration);
            String token = tokenService.createRefreshToken(auth.getId(), uuid, defaultCompany.getCompanyId(), refreshTokenExpiration);
            loginResponse.setRefresh(token);
        } else {
            loginResponse.setRefresh(account.getRefreshToken());
        }

        return loginResponse;
    }

    @Transactional
    public ApiLoginResponse apiLogin(LoginRequest loginParam) {
        try {
            AccountSql account = Validators.checkNotNull(checkAccount(loginParam.getName(), loginParam.getPassword()),
                    TranslationService.getInstance().translate(TranslationService.DB.WrongLogin, auth.getLanguage()));
            int companyIndex = getDefaultCompanyIndex(account, loginParam.getCompanyIndex());
            AccountInfo company = account.getCompanies().get(companyIndex);
            Validators.checkArgument(BooleanUtils.isTrue(company.getApi()), "No API account");
            return generateApiLoginResponse(account, companyIndex, true);
            
        } catch (Exception e) {
            throw new GamaUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.Unauthorized, auth.getLanguage()), e);
        }
    }

    @Transactional
    public ApiLoginResponse apiRefresh() {
        try {
            IAuth authRefresh = auth;
            if (authRefresh == null || BooleanUtils.isNotTrue(authRefresh.getRefresh())
                    || StringHelper.isEmpty(authRefresh.getId()) || StringHelper.isEmpty(authRefresh.getUuid())) {
                throw new GamaUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.WrongToken, auth.getLanguage()));
            }
    
            AccountSql account = findAccount(authRefresh.getId());
            if (account == null || CollectionsHelper.isEmpty(account.getCompanies()) ||
                    !Objects.equals(account.getRefreshToken(), authRefresh.getUuid())) {
                throw new GamaUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.WrongToken, auth.getLanguage()));
            }
    
            int companyIndex = findCompanyIndexById(account, authRefresh.getCompanyId());
            return generateApiLoginResponse(account, companyIndex, false);
        
        } catch (Exception e) {
            throw new GamaUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.Unauthorized, auth.getLanguage()), e);
        }
    }

    public int findCompanyIndexById(AccountSql account, long companyId) {
        int companyIndex = 0;
        for (int i = 0; i < account.getCompanies().size(); i++) {
            if (account.getCompanies().get(i).getCompanyId() == companyId) {
                companyIndex = i;
                break;
            }
        }
        return companyIndex;
    }

    public void setSaltAndPassword(AccountSql account, String password) throws GamaServerErrorException {
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);

        account.setSalt(Base64.encodeBase64String(salt));
        account.setPassword(aesCipherService.generateHash(salt, password));
    }

    @Transactional
    public AccountSql createPassword(String login, String password) throws GamaServerErrorException {
        AccountSql account = dbServiceSQL.getById(AccountSql.class, login);
        if (account != null) {
            setSaltAndPassword(account, password);
            dbServiceSQL.saveEntity(account);
            return account;
        } else {
            throw new GamaNotFoundException(String.format("Invalid user id (%s)", login));
        }
    }

    @Transactional
    public boolean resetPassword(String email) {
        AccountSql account = findAccount(email);
        if (account == null) {
            return false;
        }
        generateResetToken(account);

        AccountInfo defaultCompany = account.getCompanies().get(account.getCompanyIndex() == null ? 0 : account.getCompanyIndex());

        String host = gamaUrl;
        String name = defaultCompany.getEmployeeName();
        String msgBody;

        CompanySettings companySettings = authSettingsCacheService.get(defaultCompany.getCompanyId());
        msgBody = MessageFormat.format(TranslationService.getInstance().translate(
                    TranslationService.MAIL.ResetPassword, companySettings.getLanguage()), 
                host, URLEncoder.encode(account.getResetToken(), StandardCharsets.UTF_8));

        CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, defaultCompany.getCompanyId()), "No company");

        String senderAddress = company.getEmail() != null && !company.getEmail().isEmpty() ? company.getEmail() : Constants.DEFAULT_SENDER_EMAIL;
        String senderName = company.getBusinessName();
        String senderCC = company.getCcEmail();

        log.info(account.getId() + msgBody);

        mailService.sendMail(senderAddress, senderName, account.getId(), name,
                TranslationService.getInstance().translate(TranslationService.MAIL.ResetPasswordSubject, companySettings.getLanguage()),
                null, msgBody, null, null, senderCC);
        return true;
    }

    private boolean checkPassword(AccountSql account, String password) {
        try {
            return (account != null)
                    && (account.getPassword().equals(aesCipherService.generateHash(Base64.decodeBase64(account.getSalt()), password)));
        } catch (GamaServerErrorException e) {
            return false;
        }
    }

    @Transactional
    public void changePassword(String login, String oldPassword, String newPassword) throws GamaServerErrorException {
        AccountSql account = dbServiceSQL.getById(AccountSql.class, login);
        if (account != null) {
            if (checkPassword(account, oldPassword)) {
                setSaltAndPassword(account, newPassword);
                dbServiceSQL.saveEntity(account);
            } else {
                throw new GamaNotFoundException("Wrong password");
            }
        } else {
            throw new GamaNotFoundException(String.format("Invalid user id (%s)", login));
        }
    }

    public AccountSql findAccount(String login) {
        return dbServiceSQL.getById(AccountSql.class, login);
    }

    public AccountSql checkAccount(String login, String password) {
        AccountSql account = findAccount(login);
        return checkPassword(account, password) ? account : null;
    }

    public LoginResponse loginAdmin(String login, String password) {
        AccountSql account = checkAccount(login, password);
        Validators.checkArgument(account != null && BooleanUtils.isTrue(account.getAdmin()), "No Admin account");
        return login(account, null, false);
    }

    @Transactional
    public LoginResponse login(String login, String password, Integer companyIndex) {
        AccountSql account = checkAccount(login, password);
        return login(account, companyIndex, true);
    }

    public LoginResponse login(AccountSql account, Integer companyIndex) {
        return login(account, companyIndex, true);
    }

    public LoginResponse login(AccountSql account, Integer companyIndex, boolean genRefreshToken) {
        if (account == null || CollectionsHelper.isEmpty(account.getCompanies()))
            throw new GamaUnauthorizedException("Invalid account record");

        ApiLoginResponse apiLoginResponse = generateApiLoginResponse(account, companyIndex, genRefreshToken);

        AccountInfo defaultCompany = account.getCompanies().get(apiLoginResponse.getCompanyIndex());
        AccountInfoResponse defaultCompanyResponse = new AccountInfoResponse(apiLoginResponse.getCompanyIndex(),
                defaultCompany.getCompanyName(),
                Front.CompanySettings(authSettingsCacheService.get(defaultCompany.getCompanyId())),
                defaultCompany.getEmployeeName(), defaultCompany.getPermissions());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setCompanyIndex(apiLoginResponse.getCompanyIndex());
        loginResponse.setAccessToken(apiLoginResponse.getToken());
        loginResponse.setRefreshToken(apiLoginResponse.getRefresh());
        loginResponse.setDefaultCompany(defaultCompanyResponse);
        loginResponse.setCompanies(account.getCompanies().stream().map(c -> new CompanyInfoResponse(c.getCompanyName())).toList());

        return loginResponse;
    }

    @Transactional
    public String generateAndSaveRefreshToken(AccountSql account, Instant exp) {
        String token = UUID.randomUUID().toString();
        account.setRefreshToken(token);
        Instant refreshTokenDate = DateUtils.instant().plus(tokenService.params().refreshTokenInSeconds(), ChronoUnit.SECONDS);
        Instant expiration = exp == null || exp.isAfter(refreshTokenDate) ? refreshTokenDate : exp;
        account.setRefreshTokenDate(expiration.atZone(ZoneOffset.UTC).toLocalDateTime());
        return token;
    }

    public AccountSql findAccountByRefreshToken(String token) {
        AccountSql account = entityManager.createQuery(
                        "SELECT a FROM " + AccountSql.class.getName() + " a" +
                                " WHERE refreshToken = :refreshToken" +
                                " AND (archive IS null OR archive = false)",
                        AccountSql.class)
                .setParameter("refreshToken", token)
                .getResultStream()
                .findAny()
                .orElse(null);
        if ((account != null) && (account.getRefreshTokenDate() != null)
                && (account.getRefreshTokenDate().isAfter(DateUtils.now()))) {
            return account;
        }
        throw new GamaUnauthorizedException("Token invalid or expired");
    }

    public LoginResponse impersonate(long companyId, long employeeId) {
        CompanySql company = dbServiceSQL.getById(CompanySql.class, companyId);
        EmployeeSql employee = dbServiceSQL.getAndCheck(EmployeeSql.class, employeeId, EmployeeSql.GRAPH_ALL);

        var authImpersonated = createAuth(auth.getId(), company, employee);
        authImpersonated.setImpersonated(true);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setImpersonated(true);
        loginResponse.setAccessToken(tokenService.createToken(authImpersonated));
        loginResponse.setDefaultCompany(new AccountInfoResponse(0, company.getBusinessName(),
                Front.CompanySettings(authSettingsCacheService.get(companyId)),
                employee.getName(), employee.getUnionPermissions()));
        loginResponse.setCompanies(Collections.singletonList(new CompanyInfoResponse(company.getBusinessName())));

        return loginResponse;
    }

    public LoginResponse impersonate(String accountId) {
        AccountSql account = Validators.checkNotNull(dbServiceSQL.getById(AccountSql.class, accountId), "No account with id=" + accountId);
        int defaultCompanyIndex = getDefaultCompanyIndex(account, null);
        IAuth auth = createAuth(account, defaultCompanyIndex);
        auth.setImpersonated(true);

        CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company with id=" + auth.getCompanyId());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setImpersonated(true);
        loginResponse.setAccessToken(tokenService.createToken(auth));
        loginResponse.setCompanyIndex(defaultCompanyIndex);
        loginResponse.setDefaultCompany(new AccountInfoResponse(defaultCompanyIndex, company.getBusinessName(),
                Front.CompanySettings(authSettingsCacheService.get(auth.getCompanyId())),
                auth.getName(), auth.getPermissions()));
        loginResponse.setCompanies(account.getCompanies().stream().map(c -> new CompanyInfoResponse(c.getCompanyName())).toList());

        return loginResponse;
    }

    public LoginResponse changeCompany(int companyIndex) {
        Validators.checkArgument(companyIndex >= 0, "Invalid company index (#100)");
        AccountSql account = Validators.checkNotNull(dbServiceSQL.getById(AccountSql.class, auth.getId()));
        Validators.checkArgument(account.getCompanies() != null, "No companies in account: " + auth.getId());
        Validators.checkArgument(companyIndex < account.getCompanies().size(), "Invalid company index (#200)");

        ApiLoginResponse apiLoginResponse = generateApiLoginResponse(account, companyIndex, false);
        AccountInfo defaultCompany = account.getCompanies().get(companyIndex);
        AccountInfoResponse defaultCompanyResponse = new AccountInfoResponse(companyIndex, defaultCompany.getCompanyName(),
                Front.CompanySettings(authSettingsCacheService.get(defaultCompany.getCompanyId())),
                defaultCompany.getEmployeeName(), defaultCompany.getPermissions());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setImpersonated(auth.getImpersonated());
        loginResponse.setCompanyIndex(apiLoginResponse.getCompanyIndex());
        loginResponse.setAccessToken(apiLoginResponse.getToken());
        loginResponse.setRefreshToken(account.getRefreshToken());
        loginResponse.setDefaultCompany(defaultCompanyResponse);
        loginResponse.setCompanies(account.getCompanies().stream().map(c -> new CompanyInfoResponse(c.getCompanyName())).toList());

        return loginResponse;
    }

    public void logout(String token) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            AccountSql account = findAccountByRefreshToken(token);
            if (account != null) {
                account.setRefreshToken(null);
            }
        });
    }

    public String generateResetToken(AccountSql account) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            String token = UUID.randomUUID().toString();
            account.setResetToken(token);
            account.setResetTokenDate(DateUtils.instant()
                    .plus(tokenService.params().resetTokenInSeconds(), ChronoUnit.SECONDS)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime());
            return token;
        });
    }

    public AccountSql findAccountByResetToken(String token) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            AccountSql account = entityManager.createQuery(
                            "SELECT a FROM " + AccountSql.class.getName() + " a" +
                                    " WHERE resetToken = :resetToken" +
                                    " AND (archive IS null OR archive = false)",
                            AccountSql.class)
                    .setParameter("resetToken", token)
                    .getResultStream()
                    .findAny()
                    .orElse(null);
            if ((account != null) && (account.getResetTokenDate() != null)
                    && (account.getResetTokenDate().isAfter(DateUtils.now()))) {
                // clear tokenDate not token because token is index but tokenDate
                // not - so it will be faster
                account.setResetTokenDate(null);
                return account;
            }
            return null;
        });
    }

    public void checkPermission(AccountSql account, int companyIndex, Permission permission) {
        if ((account.getAdmin() == null || !account.getAdmin())
                && !account.getCompanies().get(companyIndex).getPermissions().contains(permission.toString())) {
            throw new GamaUnauthorizedException("Access denied - You are not authorized to perform this action");
        }
    }

    public AccountSql findAccountByEmployee(long employeeId) {
        try {
            return (AccountSql) entityManager.createNativeQuery(
                    "SELECT DISTINCT a.* FROM accounts a, jsonb_array_elements(a.companies) b" +
                            " WHERE CAST(b->>'employeeId' AS bigint) = :employeeId" +
                            " AND (a.archive IS null OR a.archive = false)",
                            AccountSql.class)
                    .setParameter("employeeId", employeeId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void removeEmployeeFromAccounts(long employeeId) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            // one employee can't have several accounts, but check in any case
            @SuppressWarnings("unchecked")
            List<AccountSql> accounts = entityManager.createNativeQuery(
                            "SELECT DISTINCT a.* FROM accounts a, jsonb_array_elements(a.companies) b" +
                                    " WHERE CAST(b->>'employeeId' AS bigint) = :employeeId" +
                                    " AND (a.archive IS null OR a.archive = false)",
                            AccountSql.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();

            CompanySql accountPayer = null;
            for (AccountSql account : accounts) {
                final long companyIdSaved = account.getCompanies().get(account.getCompanyIndex() == null ? 0 : account.getCompanyIndex()).getCompanyId();
                account.getCompanies().removeIf(accountInfo -> accountInfo.getEmployeeId() == employeeId);
                if (Validators.isValid(account.getPayer())) {
                    accountPayer = account.getPayer();
                }
                if (CollectionsHelper.isEmpty(account.getCompanies())) {
                    // delete Account entity without companies
                    entityManager.remove(account);
                } else {
                    account.setCompanyIndex(findCompanyIndexById(account, companyIdSaved));
                }
            }

            final long employeeCompanyId;
            final EmployeeSql employee = Validators.checkNotNull(dbServiceSQL.getById(EmployeeSql.class, employeeId), "No employee");
            employee.setActive(null);
            employeeCompanyId = employee.getCompanyId();
            final Long payerCompanyId = accountPayer != null ? accountPayer.getId() : null;

            CompanySql employeeCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, employeeCompanyId), "No employee company");
            employeeCompany.setActiveAccounts(employeeCompany.getActiveAccounts() - 1);
            if (employeeCompany.getActiveAccounts() < 0) {
                employeeCompany.setActiveAccounts(0);
                log.error("Connections < 0, companyId=" + employeeCompany.getId());
            }
            if (payerCompanyId != null) {
                CompanySql payerCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, payerCompanyId), "No payer company");

                AccountingUtils.modifyConnection(employeeCompany, new CompanyAccount(payerCompany, 1));
                AccountingUtils.modifyConnection(payerCompany, new CompanyAccount(employeeCompany, -1));

                payerCompany.setCurrentTotal(accountingService.companyAmountToPay(payerCompany));
            }
            employeeCompany.setCurrentTotal(accountingService.companyAmountToPay(employeeCompany));
        });
    }

    public String activateAccount(final String email, final AccountInfo accountInfo, final LocalDate date) {
        Validators.checkNotNull(email, "No account email");

        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            String generatedPassword = null;

            // try to find account with same email
            AccountSql account = findAccount(email);
            if (account != null && BooleanUtils.isTrue(accountInfo.getApi())) {
                throw new GamaServerErrorException("Only one API type login allowed per account");
            }
            if (account == null) {
                account = new AccountSql();
            }
            if (account.getCompanies() == null) {
                account.setCompanies(new ArrayList<>());
            }
            if (account.getCompanies().stream().anyMatch(acc -> Objects.equals(acc.getCompanyId(), accountInfo.getCompanyId()))) {
                throw new GamaServerErrorException("the same login " + email + " in company " + accountInfo.getCompanyId() + " already exists");
            }

            account.getCompanies().add(accountInfo);
            account.setCompanyIndex(0);

            String host = gamaUrl;
            String name = accountInfo.getEmployeeName();

            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, accountInfo.getCompanyId()), "No company");
            final String language = company.getSettings().getLanguage();
            String senderAddress = company.getEmail() != null && !company.getEmail().isEmpty() ? company.getEmail() : Constants.DEFAULT_SENDER_EMAIL;
            String senderName = company.getBusinessName();
            String senderCC = company.getCcEmail();
            String msgBody;

            if (account.getId() == null) {
                // if new
                account.setId(email);
                if (BooleanUtils.isTrue(account.getCompanies().get(0).getApi())) {
                    generatedPassword = UUID.randomUUID().toString();
                    setSaltAndPassword(account, generatedPassword);
                } else {
                    generateResetToken(account);
                    msgBody = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.MAIL.CreatedCompany, language),
                            company.getBusinessName(), host, URLEncoder.encode(account.getResetToken(), StandardCharsets.UTF_8));
                    mailService.sendMail(senderAddress, senderName, account.getId(), name,
                            TranslationService.getInstance().translate(TranslationService.MAIL.CreatedCompanySubject, language),
                            null, msgBody, null, null, senderCC);
                    log.info(account.getId() + ": " + msgBody);
                }
            } else {
                // if already exists
                msgBody = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.MAIL.AddedCompany, language),
                        company.getBusinessName(), host);
                mailService.sendMail(senderAddress, senderName, account.getId(), name,
                        TranslationService.getInstance().translate(TranslationService.MAIL.AddedCompanySubject, language),
                        null, msgBody, null, null, senderCC);
                log.info(account.getId() + msgBody);
            }

            dbServiceSQL.saveEntity(account);

            final long employeeCompanyId;
            EmployeeSql employee = Validators.checkNotNull(dbServiceSQL.getById(EmployeeSql.class, accountInfo.getEmployeeId()),
                    "No employee with id=" + accountInfo.getEmployeeId());
            if (!email.equalsIgnoreCase(employee.getEmail())) employee.setEmail(email);
            employee.setActive(true);
            employeeCompanyId = employee.getCompanyId();
            final Long payerCompanyId = account.getPayer() != null ? account.getPayer().getId() : null;

            CompanySql employeeCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, employeeCompanyId), "No account company");
            employeeCompany.setActiveAccounts(employeeCompany.getActiveAccounts() + 1);

            if (payerCompanyId != null) {
                CompanySql payerCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, payerCompanyId), "No payer company");

                AccountingUtils.modifyConnection(employeeCompany, new CompanyAccount(payerCompany, -1));
                AccountingUtils.modifyConnection(payerCompany, new CompanyAccount(employeeCompany, 1));

                payerCompany.setCurrentTotal(accountingService.companyAmountToPay(payerCompany));

                accountingService.updateConnections(payerCompany, date);
            }

            employeeCompany.setCurrentTotal(accountingService.companyAmountToPay(employeeCompany));
            accountingService.updateConnections(employeeCompany, date);

            return generatedPassword;
        });
    }

    /**
     * Update company info in the Accounts
     * Only company Name, Code, VAT code and Business Address are updated.
     * @param companyId company id
     */
    public void updateCompanyInfo(final long companyId) {
        dbServiceSQL.executeInTransaction(entityManager ->
            entityManager.createNativeQuery(
                            "WITH acc AS (" +
                                    "  SELECT a.id," +
                                    "   CASE WHEN cd.id IS NOT null" +
                                    "    THEN default_company || jsonb_build_object('companyName', cd.business_name, 'code', cd.code, 'vatCode', cd.vat_code)" +
                                    "    ELSE default_company END AS default_company_updated," +
                                    "   jsonb_agg(" +
                                    "    CASE WHEN c.id IS NOT null" +
                                    "     THEN company || jsonb_build_object('companyName', c.business_name, 'code', c.code, 'vatCode', c.vat_code)" +
                                    "     ELSE company END) AS companies_updated" +
                                    "  FROM accounts a" +
                                    "  CROSS JOIN jsonb_array_elements(companies) AS company" +
                                    "  LEFT JOIN companies cd ON cd.id = CAST(default_company->>'companyId' AS bigint) AND cd.id = :companyId" +
                                    "  LEFT JOIN companies c ON c.id = CAST(company->>'companyId' AS bigint) AND c.id = :companyId" +
                                    "  GROUP BY a.id, cd.id, default_company" +
                                    " )" +
                                    " UPDATE accounts a SET" +
                                    "  default_company = default_company_updated," +
                                    "  companies = companies_updated" +
                                    " FROM acc" +
                                    " WHERE a.id = acc.id")
                    .setParameter("companyId", companyId)
                    .executeUpdate());
    }

    private void removeCompanyFromAccounts(long companyId) {
        @SuppressWarnings("unchecked")
        List<AccountSql> accounts = entityManager.createNativeQuery(
                        "SELECT DISTINCT a.* FROM accounts a, jsonb_array_elements(a.companies) b" +
                                " WHERE CAST(b->>'companyId' AS bigint) = :companyId" +
                                " AND (a.archive IS null OR a.archive = false)",
                        AccountSql.class)
                .setParameter("companyId", companyId)
                .getResultList();

        for (AccountSql account : accounts) {
            long companyIdSaved = account.getCompanies()
                    .get(account.getCompanyIndex() == null ? 0 : account.getCompanyIndex()).getCompanyId();
            account.getCompanies().removeIf(info -> info.getCompanyId() == companyId);
            if (CollectionsHelper.isEmpty(account.getCompanies())) {
                entityManager.remove(account);
                return;
            }
            if (companyIdSaved == companyId) {
                account.setCompanyIndex(0);
            } else {
                account.setCompanyIndex(findCompanyIndexById(account, companyIdSaved));
            }
        }
    }

    private void suspendAllEmployees(long companyId) {
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

        try {
            int updated = entityManager.createQuery(
                            "UPDATE " + EmployeeSql.class.getName() + " a" +
                                    " SET " + EmployeeSql_.ACTIVE + " = false" +
                                    " WHERE " + EmployeeSql_.ACTIVE + " = true" +
                                    " AND " + EmployeeSql_.COMPANY_ID + " = :companyId" +
                                    " AND (a.archive IS null OR a.archive = false)")
                    .setParameter("companyId", companyId)
                    .executeUpdate();
            log.info(updated + " " + EmployeeSql.class.getSimpleName() + " records updated");
        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    public void removeCompany(long companyId) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            removeCompanyFromAccounts(companyId);
            suspendAllEmployees(companyId);
        });
    }

    public void deleteCompanyDocuments(long companyId) {
        taskQueueService.queueTask(new DeleteCompanyDocumentsTask(companyId));
    }

    public void deleteCompany(long companyId) {
        taskQueueService.queueTask(new DeleteCompanyTask(companyId));
    }

    private void checkAndUpdateEmployee(AccountInfo accountInfo, IEmployee employee) {
        if (!Objects.equals(accountInfo.getEmployeeName(), employee.getName())) {
            accountInfo.setEmployeeName(employee.getName());
        }
        if (!Objects.equals(accountInfo.getEmployeeOffice(), employee.getOffice())) {
            accountInfo.setEmployeeOffice(employee.getOffice());
        }
        if (!Objects.equals(accountInfo.getPermissions(), employee.getUnionPermissions())) {
            accountInfo.setPermissions(employee.getUnionPermissions());
        }
    }

    public void updateEmployeeInfo(final IEmployee employee) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            @SuppressWarnings("unchecked")
            List<AccountSql> accounts = entityManager.createNativeQuery(
                            "SELECT DISTINCT a.* FROM accounts a, jsonb_array_elements(a.companies) b" +
                                    " WHERE CAST(b->>'employeeId' AS bigint) = :employee" +
                                    " AND (a.archive IS null OR a.archive = false)",
                            AccountSql.class)
                    .setParameter("employee", employee.getId())
                    .getResultList();
            for (AccountSql account : accounts) {
                for (AccountInfo info : account.getCompanies()) {
                    if (Objects.equals(info.getEmployeeId(), employee.getId())) {
                        checkAndUpdateEmployee(info, employee);
                        break;
                    }
                }
            }
        });
    }

    public boolean checkNeedUpdate(IEmployee employee1, IEmployee employee2) {
        return !Objects.equals(employee1.getName(), employee2.getName()) ||
                !Objects.equals(employee1.getOffice(), employee2.getOffice()) ||
                !Objects.equals(employee1.getUnionPermissions(), employee2.getUnionPermissions());
    }

    public void updateLastLogin(final String email) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            entityManager.createNativeQuery(
                            "UPDATE companies " +
                                    " SET last_login = LOCALTIMESTAMP" +
                                    " WHERE id = :companyId")
                    .setParameter("companyId", auth.getCompanyId())
                    .executeUpdate();

            entityManager.createNativeQuery(
                            "WITH acc AS(" +
                                    " SELECT id," +
                                    " CASE WHEN CAST(default_company->>'companyId' AS bigint) = :companyId" +
                                    " THEN default_company || jsonb_build_object('lastLogin', to_jsonb(LOCALTIMESTAMP))" +
                                    " ELSE default_company END AS default_company_updated," +
                                    " jsonb_agg(" +
                                    " CASE WHEN CAST(company->>'companyId' AS bigint) = :companyId" +
                                    " THEN company || jsonb_build_object('lastLogin', to_jsonb(LOCALTIMESTAMP))" +
                                    " ELSE company END) AS companies_updated" +
                                    " FROM accounts, jsonb_array_elements(companies) AS company" +
                                    " WHERE id = :email" +
                                    " GROUP BY id, default_company" +
                                    ")" +
                                    " UPDATE accounts a SET" +
                                    " last_login = LOCALTIMESTAMP," +
                                    " default_company = default_company_updated," +
                                    " companies = companies_updated" +
                                    " FROM acc" +
                                    " WHERE a.id = acc.id")
                    .setParameter("email", email)
                    .setParameter("companyId", auth.getCompanyId())
                    .executeUpdate();
        });
    }

    /**
     * Clear Payer from account entity.
     * Employee company +1 payer connection;
     * Payer company -1 employee connection.
     * @param account account to clear payer info
     */
    private void clearPayer(AccountSql account, final LocalDate date) {
        if (account.getPayer() == null || account.getPayer().getId() == null) return;

        final long payerCompanyId = account.getPayer().getId();

        for (final AccountInfo accountInfo : account.getCompanies()) {

            final Long employeeCompanyId = accountInfo.getCompanyId();
            if (employeeCompanyId == payerCompanyId) continue;

            try {
                dbServiceSQL.executeInTransaction(entityManager -> {
                    CompanySql employeeCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, employeeCompanyId), "No employee company {0}", employeeCompanyId);
                    CompanySql payerCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, payerCompanyId), "No old payer company {0}", payerCompanyId);

                    AccountingUtils.modifyConnection(employeeCompany, new CompanyAccount(payerCompany, 1));
                    AccountingUtils.modifyConnection(payerCompany, new CompanyAccount(employeeCompany, -1));

                    accountingService.updateConnections(employeeCompany, date);
                    accountingService.updateConnections(payerCompany, date);
                });
            } catch (Exception e) {
                log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }

        account.setPayer(null);
    }

    /**
     * Assign Payer to account entity.
     * Employee company -1 payer connection;
     * Payer company +1 employee connection.
     * @param account account to clear payer info
     */
    private void assignPayer(AccountSql account, final long payerCompanyId, final LocalDate date) {
        if (account.getPayer() != null && account.getPayer().getId() != null) {
            clearPayer(account, date);
        }
        for (final AccountInfo accountInfo : account.getCompanies()) {

            final Long employeeCompanyId = accountInfo.getCompanyId();
            if (employeeCompanyId == payerCompanyId) continue;

            dbServiceSQL.executeInTransaction(entityManager -> {
                CompanySql employeeCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, employeeCompanyId), "No employee company {0}", employeeCompanyId);
                CompanySql payerCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, payerCompanyId), "No payer company {0}", payerCompanyId);

                AccountingUtils.modifyConnection(employeeCompany, new CompanyAccount(payerCompany, -1));
                AccountingUtils.modifyConnection(payerCompany, new CompanyAccount(employeeCompany, 1));

                accountingService.updateConnections(employeeCompany, date);
                accountingService.updateConnections(payerCompany, date);
            });
        }

        CompanySql payerCompany = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, payerCompanyId), "No payer company {0}", payerCompanyId);
        account.setPayer(payerCompany);
    }

    public AccountSql assignPayer(final String email, final Long payerId, LocalDate date) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            AccountSql account = Validators.checkNotNull(dbServiceSQL.getById(AccountSql.class, email), "No account {0}", email);
            if (payerId == null) clearPayer(account, date);
            else assignPayer(account, payerId, date);
            return account;
        });
    }

    public AccountSql setDefaultCompany(final String email, final int companyIndex) {
        Validators.checkArgument(companyIndex >= 0, "Invalid company index (#300)");
        AccountSql account = Validators.checkNotNull(dbServiceSQL.getById(AccountSql.class, email),
                "No account exists for " + email);
        Validators.checkArgument(account.getCompanies() != null && companyIndex < account.getCompanies().size(),
                "Wrong company index " + companyIndex);
        account.setCompanyIndex(companyIndex);
        return account;
    }

    public int changeAccountEmail(String oldMail, String newMail) {
        Validators.checkArgument(StringHelper.hasValue(oldMail), "No old email");
        Validators.checkArgument(StringHelper.hasValue(newMail), "No new email");

        Validators.checkArgument(findAccount(newMail) == null, "Account with new email {0} already exists", newMail);
        Validators.checkNotNull(findAccount(oldMail), "No account with old email {0}", oldMail);

        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            long count = entityManager.createQuery(
                            "SELECT COUNT(" + EmployeeSql_.EMAIL + ")" +
                                    " FROM " + EmployeeSql.class.getName() + " a" +
                                    " WHERE " + EmployeeSql_.EMAIL + " = :newMail" +
                                    " AND (a.archive IS null OR a.archive = false)",
                            Long.class)
                    .setParameter("newMail", newMail)
                    .getSingleResult();
            Validators.checkArgument(count == 0, "Where are employees with new email: {0} already", newMail);

            // 1) update email in Account
            int accounts = entityManager.createQuery(
                            "UPDATE " + AccountSql.class.getName() + " a" +
                                    " SET " + AccountSql_.ID + " = :newMail" +
                                    " WHERE " + AccountSql_.ID + " = :oldMail")
                    .setParameter("newMail", newMail)
                    .setParameter("oldMail", oldMail)
                    .executeUpdate();

            // 2) update email in Employee
            int employees = entityManager.createQuery(
                            "UPDATE " + EmployeeSql.class.getName() + " a" +
                                    " SET " + EmployeeSql_.EMAIL + " = :newMail" +
                                    " WHERE " + EmployeeSql_.EMAIL + " = :oldMail" +
                                    " AND (archive IS NULL OR archive = false)")
                    .setParameter("newMail", newMail)
                    .setParameter("oldMail", oldMail)
                    .executeUpdate();

            log.info(accounts + " Accounts and " + employees + " Employees updated");
            return employees;
        });
    }

    public void updateAccountPermissions(RoleDto role) {
        // update Employees roles in Accounts
        dbServiceSQL.executeInTransaction(entityManager -> entityManager.createNativeQuery(
                "WITH ep AS (" +
                        "  SELECT e.id, e.foreign_id, jsonb_agg(DISTINCT p) permissions" +
                        "  FROM employee e" +
                        "  JOIN employee_roles er ON er.employee_id = e.id" +
                        "  JOIN roles r ON r.id = er.roles_id" +
                        "  CROSS JOIN jsonb_array_elements(r.permissions) AS p" +
                        "  WHERE e.company_id = :companyId" +
                        "   AND e.active = true" +
                        "   AND (e.archive IS null OR e.archive = false)" +
                        "  GROUP BY e.id" +
                        " )," +
                        " acc AS(" +
                        "  SELECT a.id," +
                        "   CASE WHEN def_ep.id IS NOT NULL AND CAST(default_company->>'companyId' AS bigint) = :companyId" +
                        "    THEN default_company || jsonb_build_object('permissions', def_ep.permissions, 'employeeId', def_ep.id, 'employeeDb', 'P')" +
                        "    ELSE default_company END AS default_company_updated," +
                        "   jsonb_agg(" +
                        "    CASE WHEN comp_ep.id IS NOT NULL AND CAST(company->>'companyId' AS bigint) = :companyId" +
                        "     THEN company || jsonb_build_object('permissions', to_jsonb(comp_ep.permissions), 'employeeId', comp_ep.id, 'employeeDb', 'P')" +
                        "     ELSE company END) AS companies_updated" +
                        "  FROM accounts a" +
                        "  CROSS JOIN jsonb_array_elements(companies) AS company" +
                        "  LEFT JOIN ep def_ep ON def_ep.id = CAST(default_company->>'employeeId' AS bigint)" +
                        "  LEFT JOIN ep comp_ep ON comp_ep.id = CAST(company->>'employeeId' AS bigint)" +
                        "  GROUP BY a.id, default_company, def_ep.id, def_ep.permissions" +
                        " )" +
                        " UPDATE accounts a SET" +
                        "  default_company = default_company_updated," +
                        "  companies = companies_updated" +
                        " FROM acc" +
                        " WHERE a.id = acc.id")
                .setParameter("companyId", auth.getCompanyId())
                .executeUpdate());
    }

    public IAuth createAuth(AccountSql account, Integer companyIndex) {
        if (companyIndex == null) companyIndex = Objects.requireNonNullElse(account.getCompanyIndex(), 0);
        AccountInfo defaultCompany = account.getCompanies().get(companyIndex);

        IAuth auth = new Auth();
        auth.setUuid(UUID.randomUUID().toString());
        auth.setId(account.getId());
        auth.setName(defaultCompany.getEmployeeName());
        auth.setEmployeeId(defaultCompany.getEmployeeId());
        auth.setCompanyId(defaultCompany.getCompanyId());
        auth.setPermissions(defaultCompany.getPermissions());
        auth.setAdmin(account.getAdmin());
        return auth;
    }

    public IAuth createAuth(String email, CompanySql company, EmployeeSql employee) {
        IAuth auth = new Auth();
        auth.setUuid(UUID.randomUUID().toString());
        auth.setId(email);
        auth.setEmployeeId(employee.getId());
        auth.setName(employee.getName());
        auth.setCompanyId(company.getId());
        auth.setPermissions(employee.getUnionPermissions());
        return auth;
    }
}
