package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.ReportBalanceRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.i.IPermission;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.EmployeeOpeningBalanceDto;
import lt.gama.model.dto.documents.EmployeeOperationDto;
import lt.gama.model.dto.documents.EmployeeRateInfluenceDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.EmployeeOpeningBalanceSql;
import lt.gama.model.sql.documents.EmployeeOperationSql;
import lt.gama.model.sql.documents.EmployeeRateInfluenceSql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.DocumentDoubleEntry;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CounterDesc;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.EmployeeType;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyDetail;
import lt.gama.service.ex.rt.GamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gama
 * Created by valdas on 15-05-12.
 */
@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final MoneyAccountService moneyAccountService;
    private final StorageService storageService;
    private final DocumentService documentService;
    private final GLOperationsService glOperationsService;
    private final AccountService accountService;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final EmployeeOpeningBalanceSqlMapper employeeOpeningBalanceSqlMapper;
    private final EmployeeOperationSqlMapper employeeOperationSqlMapper;
    private final EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final CurrencyService currencyService;


    EmployeeService(MoneyAccountService moneyAccountService,
                    StorageService storageService,
                    DocumentService documentService,
                    GLOperationsService glOperationsService,
                    AccountService accountService,
                    EmployeeSqlMapper employeeSqlMapper,
                    DoubleEntrySqlMapper doubleEntrySqlMapper,
                    Auth auth,
                    DBServiceSQL dbServiceSQL,
                    EmployeeOpeningBalanceSqlMapper employeeOpeningBalanceSqlMapper,
                    EmployeeOperationSqlMapper employeeOperationSqlMapper,
                    EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper,
                    CounterpartySqlMapper counterpartySqlMapper, CurrencyService currencyService) {
        this.moneyAccountService = moneyAccountService;
        this.storageService = storageService;
        this.documentService = documentService;
        this.glOperationsService = glOperationsService;
        this.accountService = accountService;
        this.employeeSqlMapper = employeeSqlMapper;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.employeeOpeningBalanceSqlMapper = employeeOpeningBalanceSqlMapper;
        this.employeeOperationSqlMapper = employeeOperationSqlMapper;
        this.employeeRateInfluenceSqlMapper = employeeRateInfluenceSqlMapper;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.currencyService = currencyService;
    }

    public EmployeeDto saveEmployee(EmployeeDto request, IPermission permission) {
        // we need to preserve remainders in one transaction
        final Long id = request.getId();
        final long companyId = auth.getCompanyId();
        final boolean isCompanyAdmin = auth.checkPermission(Permission.ADMIN);
        final boolean isAccountManager = isCompanyAdmin || permission.checkPermission(Permission.ACCOUNT);
        final boolean isAccountant = isCompanyAdmin || auth.checkPermission(Permission.GL);
        final boolean isSettingsAdmin = isCompanyAdmin || auth.checkPermission(Permission.SETTINGS);

        return employeeSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            EmployeeSql employee = id == null || id == 0 ? null : dbServiceSQL.getAndCheck(EmployeeSql.class, id);
            if (employee != null) {
                Validators.checkDocumentVersion(employee, request, auth.getLanguage());

                // check if employee type not changed to API
                if (employee.isActive() && !Objects.equals(employee.getType(), request.getType()) && request.getType() == EmployeeType.API) {
                    throw new GamaException("Can't change active employee/login type to API");
                }

                // if administrator role was removed from employee
                //  - check if there is at least one employee with administrator role in company
                if (isAccountManager && employee.getUnionPermissions().contains(Permission.ADMIN.toString()) &&
                        !request.getUnionPermissions().contains(Permission.ADMIN.toString())) {

                    long hasAdminRole = ((Number) em.createNativeQuery(
                            "SELECT COUNT(e.id) FROM employee e" +
                                    " INNER JOIN employee_roles er ON e.id = er.employee_id" +
                                    " INNER JOIN roles r ON r.id = er.roles_id" +
                                    " WHERE jsonb_path_exists(r.permissions, '$[*] ? (@ == $permission)'," +
                                        " jsonb_build_object('permission', :permission))" +
                                    " AND e.id <> :id" +
                                    " AND e.company_id = :companyId" +
                                    " AND (e.archive IS null OR e.archive = false)" +
                                    " AND (e.hidden IS null OR e.hidden = false)")
                            .setParameter("id", id)
                            .setParameter("companyId", auth.getCompanyId())
                            .setParameter("permission", Permission.ADMIN.toString())
                            .getSingleResult()).longValue();

                    if (hasAdminRole == 0) {
                        throw new GamaException("At least one employee in company must have administrator role");
                    }
                }

                if (isSettingsAdmin) {
                    if (!StringHelper.isEquals(request.getExportId(), employee.getExportId())) {
                        // if new value not empty check if new value not used
                        if (StringHelper.hasValue(request.getExportId())) {
                            ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, BankAccountSql.class, request.getExportId()));
                            if (imp != null)
                                throw new GamaException("Ex.id in use already");
                        }

                        // if old value not empty delete it
                        if (StringHelper.hasValue(employee.getExportId())) {
                            ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, BankAccountSql.class, employee.getExportId()));
                            if (imp != null) entityManager.remove(imp);
                        }

                        // create new record with new value
                        ImportSql imp = new ImportSql(companyId, BankAccountSql.class, request.getExportId(), id, DBType.POSTGRESQL);
                        dbServiceSQL.saveEntity(imp);
                    }
                    employee.setExportId(request.getExportId());
                }

                if (isAccountManager && request.isActive()) {
                    // update employee info in accounts if needed
                    if (accountService.checkNeedUpdate(request, employee)) {
                        accountService.updateEmployeeInfo(request);
                    }
                }

                employee.setName(request.getName());
                employee.setBanks(request.getBanks());
                employee.setContacts(request.getContacts());
                employee.setMoneyAccount(request.getMoneyAccount());
                employee.setEmployeeId(request.getEmployeeId());
                employee.setOffice(request.getOffice());
                employee.setDepartment(request.getDepartment());
                employee.setType(request.getType());
                employee.setAddress(request.getAddress());
                employee.setCf(request.getCf());
                employee.setLabels(request.getLabels());

                employee.setTranslation(request.getTranslation());

                if (isAccountManager) {
                    employee.setRoles(CollectionsHelper.streamOf(request.getRoles())
                            .map(role -> entityManager.getReference(RoleSql.class, role.getId()))
                            .collect(Collectors.toSet()));
                    employee.setActive(request.isActive());
                    employee.setEmail(StringHelper.isEmpty(request.getEmail()) ? null : request.getEmail().toLowerCase().trim());
                }
                if (isCompanyAdmin) {
                    employee.setType(request.getType());
                }
                if (isAccountant) {
                    employee.setMoneyAccount(request.getMoneyAccount());
                }
                employee.setArchive(request.getArchive());

            } else {
                employee = dbServiceSQL.saveEntityInCompany(employeeSqlMapper.toEntity(request));
            }
            return employee;
        }));
    }

    public EmployeeOpeningBalanceDto saveEmployeeOpeningBalance(EmployeeOpeningBalanceDto request) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkOpeningBalanceDate(companySettings, request, auth.getLanguage());

        return employeeOpeningBalanceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            if (request.getId() != null) {
                EmployeeOpeningBalanceSql entity = dbServiceSQL.getById(EmployeeOpeningBalanceSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            CollectionsHelper.streamOf(request.getEmployees())
                    .forEach(balance -> {
                        currencyService.checkBaseMoneyDocumentExchange(request.getDate(), balance);
                        balance.setCompanyId(companyId);
                    });

            return dbServiceSQL.saveWithCounter(employeeOpeningBalanceSqlMapper.toEntity(request));
        }));
    }

    public EmployeeOperationDto saveEmployeeOperation(EmployeeOperationDto request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, request, auth.getLanguage());
        Validators.checkValid(request.getEmployee(), TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeId, auth.getLanguage()));

        DocumentDoubleEntry<EmployeeOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                EmployeeOperationSql entity = dbServiceSQL.getAndCheck(EmployeeOperationSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }
            EmployeeOperationSql entity = employeeOperationSqlMapper.toEntity(request);

            // uuid need for generating printing form
            if (entity.getUuid() == null) entity.setUuid(UUID.randomUUID());

            currencyService.checkBaseMoneyDocumentExchange(entity.getDate(), entity);

            CounterDesc desc = null;
            if (companySettings.getCounter() != null) {
                if (GamaMoneyUtils.isPositive(entity.getAmount())) {
                    desc = companySettings.getCounterByClass(entity.getClass(), "+");
                } else if (GamaMoneyUtils.isNegative(entity.getAmount())) {
                    desc = companySettings.getCounterByClass(entity.getClass(), "-");
                }
            }
            entity = dbServiceSQL.saveWithCounter(entity, desc);

            EmployeeSql employee = dbServiceSQL.getByIdOrForeignId(EmployeeSql.class, entity.getEmployee().getId(), entity.getEmployee().getDb());
            CounterpartySql counterparty = Validators.isValid((entity.getCounterparty()))
                    ? dbServiceSQL.getByIdOrForeignId(CounterpartySql.class, entity.getCounterparty().getId(), entity.getCounterparty().getDb())
                    : null;

            EmployeeOperationDto dto = employeeOperationSqlMapper.toDto(entity);
            dto.setCounterparty(counterpartySqlMapper.toDto(counterparty));
            dto.setEmployee(employeeSqlMapper.toDto(employee));

            DoubleEntrySql doubleEntry = glOperationsService.finishEmployeeOperation(employee.getName(), employee.getMoneyAccount(),
                    dto, request.getDoubleEntry(), false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });
        EmployeeOperationDto result = employeeOperationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));

        // generate printing form
        documentService.generatePrintForm(result);

        return result;
    }

    public EmployeeRateInfluenceDto saveEmployeeRateInfluence(EmployeeRateInfluenceDto request) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, request, auth.getLanguage());

        DocumentDoubleEntry<EmployeeRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                EmployeeRateInfluenceSql entity = dbServiceSQL.getAndCheck(EmployeeRateInfluenceSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            EmployeeRateInfluenceSql entity = employeeRateInfluenceSqlMapper.toEntity(request);

            final LocalDate date = entity.getDate();
            if (entity.getAccounts() != null) {
                entity.getAccounts().removeIf(balance -> GamaMoneyUtils.isZero(balance.getBaseFixAmount()));
                entity.getAccounts().forEach(balance -> {
                    currencyService.checkBaseMoneyDocumentExchange(date, balance, true);
                    balance.setCompanyId(companyId);
                });
            }

            // uuid need for generating printing form
            if (entity.getUuid() == null) entity.setUuid(UUID.randomUUID());

            entity = dbServiceSQL.saveWithCounter(entity);

            DoubleEntrySql doubleEntry = glOperationsService.finishMoneyRateInfluence(EmployeeSql.class,
                    employeeRateInfluenceSqlMapper.toDto(entity), request.getDoubleEntry(), false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        EmployeeRateInfluenceDto result = employeeRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public EmployeeOpeningBalanceDto finishEmployeeOpeningBalance(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<EmployeeOpeningBalanceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            EmployeeOpeningBalanceSql document = dbServiceSQL.getAndCheck(EmployeeOpeningBalanceSql.class, id);
            Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());
            return new DocumentDoubleEntry<>(documentService.finish(document));
        });
        return employeeOpeningBalanceSqlMapper.toDto(pair.getDocument());
    }

    public EmployeeOperationDto finishEmployeeOperation(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<EmployeeOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            EmployeeOperationSql document = dbServiceSQL.getAndCheck(EmployeeOperationSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finish(document, finishGL);
        });
        EmployeeOperationDto result = employeeOperationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public EmployeeRateInfluenceDto finishEmployeeRateInfluence(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<EmployeeRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            EmployeeRateInfluenceSql document = dbServiceSQL.getAndCheck(EmployeeRateInfluenceSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finish(document, finishGL);
        });
        EmployeeRateInfluenceDto result = employeeRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public EmployeeOperationDto recallEmployeeOperation(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<EmployeeOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            EmployeeOperationSql document = dbServiceSQL.getAndCheck(EmployeeOperationSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recall(document);
        });
        EmployeeOperationDto result = employeeOperationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public EmployeeRateInfluenceDto recallEmployeeRateInfluence(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<EmployeeRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            EmployeeRateInfluenceSql document = dbServiceSQL.getAndCheck(EmployeeRateInfluenceSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recall(document);
        });
        EmployeeRateInfluenceDto result = employeeRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public EmployeeOpeningBalanceDto importEmployeeOpeningBalance(long id, String fileName) {
        EmployeeOpeningBalanceDto document =
                employeeOpeningBalanceSqlMapper.toDto(dbServiceSQL.getAndCheck(EmployeeOpeningBalanceSql.class, id));
        if (BooleanUtils.isTrue(document.getFinished())) return document;
        return storageService.importAdvanceOpeningBalance(document, fileName);
    }

    public List<RepMoneyBalance<EmployeeDto>> reportEmployeeBalance(ReportBalanceRequest request) {
        return moneyAccountService.reportBalance(request, AccountType.EMPLOYEE);
    }

    public PageResponse<MoneyHistoryDto, RepMoneyDetail<EmployeeDto>> reportEmployeeFlow(PageRequest request) {
        return moneyAccountService.reportFlow(request, AccountType.EMPLOYEE);
    }
}
