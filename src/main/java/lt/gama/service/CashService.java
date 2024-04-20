package lt.gama.service;

import jakarta.persistence.EntityManager;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.ReportBalanceRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.CashOpeningBalanceDto;
import lt.gama.model.dto.documents.CashOperationDto;
import lt.gama.model.dto.documents.CashRateInfluenceDto;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.documents.CashOpeningBalanceSql;
import lt.gama.model.sql.documents.CashOperationSql;
import lt.gama.model.sql.documents.CashRateInfluenceSql;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.DocumentDoubleEntry;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CounterDesc;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyDetail;
import lt.gama.service.ex.rt.GamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Gama
 * Created by valdas on 15-05-10.
 */
@Service
public class CashService {

    private static final Logger log = LoggerFactory.getLogger(CashService.class);


    private final MoneyAccountService moneyAccountService;
    private final StorageService storageService;
    private final DocumentService documentService;
    private final GLOperationsService glOperationsService;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final CashSqlMapper cashSqlMapper;
    private final CashOperationSqlMapper cashOperationSqlMapper;
    private final CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper;
    private final CashOpeningBalanceSqlMapper cashOpeningBalanceSqlMapper;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final CurrencyService currencyService;


    CashService(MoneyAccountService moneyAccountService,
                StorageService storageService,
                DocumentService documentService,
                GLOperationsService glOperationsService,
                DoubleEntrySqlMapper doubleEntrySqlMapper,
                Auth auth,
                DBServiceSQL dbServiceSQL,
                CashSqlMapper cashSqlMapper,
                CashOperationSqlMapper cashOperationSqlMapper,
                CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper,
                CashOpeningBalanceSqlMapper cashOpeningBalanceSqlMapper,
                CounterpartySqlMapper counterpartySqlMapper,
                EmployeeSqlMapper employeeSqlMapper, CurrencyService currencyService) {
        this.moneyAccountService = moneyAccountService;
        this.storageService = storageService;
        this.documentService = documentService;
        this.glOperationsService = glOperationsService;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.cashSqlMapper = cashSqlMapper;
        this.cashOperationSqlMapper = cashOperationSqlMapper;
        this.cashRateInfluenceSqlMapper = cashRateInfluenceSqlMapper;
        this.cashOpeningBalanceSqlMapper = cashOpeningBalanceSqlMapper;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.currencyService = currencyService;
    }

    public CashDto saveCash(CashDto request) {
        final Long id = request.getId();
        final long companyId = auth.getCompanyId();
        final boolean isCompanyAdmin = auth.checkPermission(Permission.ADMIN);
        final boolean isAccountant = isCompanyAdmin || auth.checkPermission(Permission.GL);
        final boolean isSettingsAdmin = isCompanyAdmin || auth.checkPermission(Permission.SETTINGS);

        return cashSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CashSql entity;
            if (id == null || id == 0) {
                entity = new CashSql();
            } else {
                entity = dbServiceSQL.getAndCheck(CashSql.class, id);
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());
            }

            if (isSettingsAdmin) {
                if (id != null && !StringHelper.isEquals(request.getExportId(), entity.getExportId())) {
                    // if new value not empty check if new value not used
                    if (StringHelper.hasValue(request.getExportId())) {
                        ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, BankAccountSql.class, request.getExportId()));
                        if (imp != null) throw new GamaException("Ex.id in use already");
                    }

                    // if old value not empty delete it
                    if (StringHelper.hasValue(entity.getExportId())) {
                        ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, BankAccountSql.class, entity.getExportId()));
                        if (imp != null) entityManager.remove(imp);
                    }

                    // create new record with new value
                    ImportSql imp = new ImportSql(companyId, BankAccountSql.class, request.getExportId(), id, DBType.POSTGRESQL);
                    dbServiceSQL.saveEntity(imp);
                }
                entity.setExportId(request.getExportId());
            }

            entity.setName(request.getName());
            entity.setCashier(request.getCashier());
            if (isAccountant) {
                entity.setMoneyAccount(request.getMoneyAccount());
            }
            entity.setArchive(request.getArchive());
            return dbServiceSQL.saveEntityInCompany(entity);
        }));
    }


    public CashOpeningBalanceDto saveCashOpeningBalance(CashOpeningBalanceDto request) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkOpeningBalanceDate(companySettings, request, auth.getLanguage());

        return cashOpeningBalanceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            if (request.getId() != null) {
                CashOpeningBalanceSql entity = dbServiceSQL.getAndCheck(CashOpeningBalanceSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + request.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            CollectionsHelper.streamOf(request.getCashes()).forEach(balance -> {
                currencyService.checkBaseMoneyDocumentExchange(request.getDate(), balance);
                balance.setCompanyId(companyId);
            });
            return dbServiceSQL.saveWithCounter(cashOpeningBalanceSqlMapper.toEntity(request));
        }));
    }

    public CashOperationDto saveCashOperation(CashOperationDto request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, request, auth.getLanguage());
        Validators.checkValid(request.getCash(), "No cash");

        DocumentDoubleEntry<CashOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                CashOperationSql entity = dbServiceSQL.getAndCheck(CashOperationSql.class, request.getId(), CashOperationSql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }
            CashOperationSql entity = cashOperationSqlMapper.toEntity(request);

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

            CashSql cash = dbServiceSQL.getByIdOrForeignId(CashSql.class,
                    entity.getCash().getId(), entity.getCash().getDb());

            CounterpartySql counterparty = Validators.isValid((entity.getCounterparty()))
                    ? dbServiceSQL.getByIdOrForeignId(CounterpartySql.class, entity.getCounterparty().getId(), entity.getCounterparty().getDb())
                    : null;
            EmployeeSql employee = Validators.isValid((entity.getEmployee()))
                    ? dbServiceSQL.getByIdOrForeignId(EmployeeSql.class, entity.getEmployee().getId(), entity.getEmployee().getDb())
                    : null;

            CashOperationDto dto = cashOperationSqlMapper.toDto(entity);
            dto.setCounterparty(counterpartySqlMapper.toDto(counterparty));
            dto.setEmployee(employeeSqlMapper.toDto(employee));

            DoubleEntrySql doubleEntry = glOperationsService.finishCashOperation(cash.getMoneyAccount(),
                    dto, request.getDoubleEntry(), false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        CashOperationDto result = cashOperationSqlMapper.toDto(pair.getDocument());
        result.setCounterparty(request.getCounterparty());
        result.setEmployee(request.getEmployee());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));

        // generate printing form
        documentService.generatePrintForm(result);

        return result;
    }

    public CashRateInfluenceDto saveCashRateInfluence(CashRateInfluenceDto request) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, request, auth.getLanguage());

        DocumentDoubleEntry<CashRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                CashRateInfluenceSql entity = dbServiceSQL.getAndCheck(CashRateInfluenceSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            CashRateInfluenceSql entity = cashRateInfluenceSqlMapper.toEntity(request);

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

            DoubleEntrySql doubleEntry = glOperationsService.finishMoneyRateInfluence(CashSql.class,
                    cashRateInfluenceSqlMapper.toDto(entity), request.getDoubleEntry(), false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        CashRateInfluenceDto result = cashRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }


    public CashOpeningBalanceDto finishCashOpeningBalance(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<CashOpeningBalanceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CashOpeningBalanceSql document = dbServiceSQL.getAndCheck(CashOpeningBalanceSql.class, id);
            Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());
            return new DocumentDoubleEntry<>(documentService.finish(document));
        });
        return cashOpeningBalanceSqlMapper.toDto(pair.getDocument());
    }

    public CashOperationDto finishCashOperation(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<CashOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CashOperationSql document = dbServiceSQL.getAndCheck(CashOperationSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finish(document, finishGL);
        });
        CashOperationDto result = cashOperationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public CashRateInfluenceDto finishCashRateInfluence(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<CashRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CashRateInfluenceSql document = dbServiceSQL.getAndCheck(CashRateInfluenceSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finish(document, finishGL);
        });
        CashRateInfluenceDto result = cashRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public CashOperationDto recallCashOperation(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<CashOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CashOperationSql document = dbServiceSQL.getAndCheck(CashOperationSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recall(document);
        });
        CashOperationDto result = cashOperationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public CashRateInfluenceDto recallCashRateInfluence(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<CashRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CashRateInfluenceSql document = dbServiceSQL.getAndCheck(CashRateInfluenceSql.class, id);
            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recall(document);
        });
        CashRateInfluenceDto result = cashRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public CashOpeningBalanceDto importOpeningBalance(long id, String fileName) {
        CashOpeningBalanceDto entity = cashOpeningBalanceSqlMapper.toDto(dbServiceSQL.getAndCheck(CashOpeningBalanceSql.class, id));
        if (BooleanUtils.isTrue(entity.getFinished())) return entity;
        return storageService.importCashOpeningBalance(entity, fileName);
    }

    public List<RepMoneyBalance<CashDto>> reportCashBalance(ReportBalanceRequest request) {
        return moneyAccountService.reportBalance(request, AccountType.CASH);
    }

    public PageResponse<MoneyHistoryDto, RepMoneyDetail<CashDto>> reportCashFlow(PageRequest request) {
        return moneyAccountService.reportFlow(request, AccountType.CASH);
    }
}
