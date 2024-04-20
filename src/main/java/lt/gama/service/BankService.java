package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.ReportBalanceRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.BankApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.impexp.entity.*;
import lt.gama.model.dto.documents.BankOpeningBalanceDto;
import lt.gama.model.dto.documents.BankOperationDto;
import lt.gama.model.dto.documents.BankRateInfluenceDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.documents.BankOpeningBalanceSql;
import lt.gama.model.sql.documents.BankOperationSql;
import lt.gama.model.sql.documents.BankRateInfluenceSql;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.ImportSql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.DocumentDoubleEntry;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyDetail;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.tasks.ImportBankTask;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static lt.gama.helpers.Validators.checkDocumentDate;

/**
 * Gama
 * Created by valdas on 15-03-16.
 */
@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private static final int BANK_IMPORT_PACKAGE_SIZE = 25;

    enum ISO20022CAMT {

        CAMT052("camt.052."),
        CAMT053("camt.053.");

        private final String value;

        ISO20022CAMT(String value) {
            this.value = value;
        }

            public String toString() {
            return value;
        }
    }

    @PersistenceContext
    protected EntityManager entityManager;

    private final MoneyAccountService moneyAccountService;
    private final StorageService storageService;
    private final DocumentService documentService;
    private final GLOperationsService glOperationsService;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final BankAccountSqlMapper bankAccountSqlMapper;
    private final BankOpeningBalanceSqlMapper bankOpeningBalanceSqlMapper;
    private final BankOperationSqlMapper bankOperationSqlMapper;
    private final BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final TaskQueueService taskQueueService;
    private final CurrencyService currencyService;

    BankService(MoneyAccountService moneyAccountService,
                StorageService storageService,
                DocumentService documentService,
                GLOperationsService glOperationsService,
                DoubleEntrySqlMapper doubleEntrySqlMapper,
                Auth auth,
                DBServiceSQL dbServiceSQL,
                BankAccountSqlMapper bankAccountSqlMapper,
                BankOpeningBalanceSqlMapper bankOpeningBalanceSqlMapper,
                BankOperationSqlMapper bankOperationSqlMapper,
                BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper,
                CounterpartySqlMapper counterpartySqlMapper,
                EmployeeSqlMapper employeeSqlMapper,
                TaskQueueService taskQueueService, CurrencyService currencyService) {
        this.moneyAccountService = moneyAccountService;
        this.storageService = storageService;
        this.documentService = documentService;
        this.glOperationsService = glOperationsService;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.bankAccountSqlMapper = bankAccountSqlMapper;
        this.bankOpeningBalanceSqlMapper = bankOpeningBalanceSqlMapper;
        this.bankOperationSqlMapper = bankOperationSqlMapper;
        this.bankRateInfluenceSqlMapper = bankRateInfluenceSqlMapper;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.taskQueueService = taskQueueService;
        this.currencyService = currencyService;
    }

    public BankAccountDto saveBankAccount(BankAccountDto request) {
        final Long id = request.getId();
        final long companyId = auth.getCompanyId();
        final boolean isCompanyAdmin = auth.checkPermission(Permission.ADMIN);
        final boolean isAccountant = isCompanyAdmin || auth.checkPermission(Permission.GL);
        final boolean isSettingsAdmin = isCompanyAdmin || auth.checkPermission(Permission.SETTINGS);

        return bankAccountSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            BankAccountSql entity;
            if (id == null || id == 0) {
                entity = new BankAccountSql();
            } else {
                entity = dbServiceSQL.getAndCheck(BankAccountSql.class, id);
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());
            }

            if (isSettingsAdmin) {
                if (id != null && !StringHelper.isEquals(request.getExportId(), entity.getExportId())) {
                    // if new value not empty check if new value not used
                    if (StringHelper.hasValue(request.getExportId())) {
                        ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, BankAccountSql.class, request.getExportId()));
                        if (imp != null)
                            throw new GamaException("Ex.id in use already");
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
            entity.setBank(request.getBank());
            entity.setInvoice(request.isInvoice());
            if (isAccountant) {
                entity.setMoneyAccount(request.getMoneyAccount());
            }
            entity.setArchive(request.getArchive());
            entity.setCards(request.getCards());
            return dbServiceSQL.saveEntityInCompany(entity);
        }));
    }

    public BankOpeningBalanceDto saveBankOpeningBalance(BankOpeningBalanceDto request) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkOpeningBalanceDate(companySettings, request, auth.getLanguage());

        return bankOpeningBalanceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                BankOpeningBalanceSql entity = dbServiceSQL.getAndCheck(BankOpeningBalanceSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            CollectionsHelper.streamOf(request.getBankAccounts())
                    .forEach(balance -> {
                        currencyService.checkBaseMoneyDocumentExchange(request.getDate(), balance);
                        balance.setCompanyId(companyId);
                    });

            return dbServiceSQL.saveWithCounter(bankOpeningBalanceSqlMapper.toEntity(request));
        }));
    }

    public BankOperationDto saveBankOperation(BankOperationDto request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        checkDocumentDate(companySettings, request, auth.getLanguage());
        Validators.checkValid(request.getBankAccount(), "No bank account");

        DocumentDoubleEntry<BankOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                BankOperationSql entity = dbServiceSQL.getAndCheck(BankOperationSql.class, request.getId(), BankOperationSql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }
            BankOperationSql entity = bankOperationSqlMapper.toEntity(request);

            // uuid need for generating printing form
            if (entity.getUuid() == null) entity.setUuid(UUID.randomUUID());

            currencyService.checkBaseMoneyDocumentExchange(entity.getDate(), entity);

            entity = dbServiceSQL.saveWithCounter(entity);

            BankAccountSql bankAccount = dbServiceSQL.getByIdOrForeignId(BankAccountSql.class,
                    entity.getBankAccount().getId(), entity.getBankAccount().getDb());

            CounterpartySql counterparty = Validators.isValid((entity.getCounterparty()))
                    ? dbServiceSQL.getByIdOrForeignId(CounterpartySql.class, entity.getCounterparty().getId(), entity.getCounterparty().getDb())
                    : null;
            EmployeeSql employee = Validators.isValid((entity.getEmployee()))
                    ? dbServiceSQL.getByIdOrForeignId(EmployeeSql.class, entity.getEmployee().getId(), entity.getEmployee().getDb())
                    : null;

            BankOperationDto dto = bankOperationSqlMapper.toDto(entity);
            dto.setCounterparty(counterpartySqlMapper.toDto(counterparty));
            dto.setEmployee(employeeSqlMapper.toDto(employee));

            DoubleEntrySql doubleEntry = glOperationsService.finishBankOperation(bankAccount.getMoneyAccount(),
                    dto, request.getDoubleEntry(), false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        BankOperationDto result = bankOperationSqlMapper.toDto(pair.getDocument());
        result.setCounterparty(request.getCounterparty());
        result.setEmployee(request.getEmployee());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));

        return result;
    }

    public BankRateInfluenceDto saveBankRateInfluence(BankRateInfluenceDto request) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        checkDocumentDate(companySettings, request, auth.getLanguage());

        DocumentDoubleEntry<BankRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                BankRateInfluenceSql entity = dbServiceSQL.getAndCheck(BankRateInfluenceSql.class, request.getId(), BankRateInfluenceSql.GRAPH_ALL);
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            BankRateInfluenceSql entity = bankRateInfluenceSqlMapper.toEntity(request);

            final LocalDate date = request.getDate();
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

            DoubleEntrySql doubleEntry = glOperationsService.finishMoneyRateInfluence(BankAccountSql.class,
                    bankRateInfluenceSqlMapper.toDto(entity), request.getDoubleEntry(), false);

            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });

        BankRateInfluenceDto result = bankRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    /**
     * Bank account remainders are set as initials of next day
     */
    public BankOpeningBalanceDto finishBankOpeningBalance(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<BankOpeningBalanceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            BankOpeningBalanceSql document = dbServiceSQL.getAndCheck(BankOpeningBalanceSql.class, id, BankOpeningBalanceSql.GRAPH_ALL);
            Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());
            return new DocumentDoubleEntry<>(documentService.finish(document));
        });
        return bankOpeningBalanceSqlMapper.toDto(pair.getDocument());
    }

    public BankOperationDto finishBankOperation(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<BankOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            BankOperationSql document = dbServiceSQL.getAndCheck(BankOperationSql.class, id, BankOperationSql.GRAPH_ALL);
            checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finish(document, finishGL);
        });
        BankOperationDto result = bankOperationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public BankRateInfluenceDto finishBankRateInfluence(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<BankRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            BankRateInfluenceSql document = dbServiceSQL.getAndCheck(BankRateInfluenceSql.class, id, BankRateInfluenceSql.GRAPH_ALL);
            checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.finish(document, finishGL);
        });
        BankRateInfluenceDto result = bankRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public BankOperationDto recallBankOperation(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<BankOperationSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            BankOperationSql document = dbServiceSQL.getAndCheck(BankOperationSql.class, id, BankOperationSql.GRAPH_ALL);
            checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recall(document);
        });
        BankOperationDto result = bankOperationSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public BankRateInfluenceDto recallBankRateInfluence(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<BankRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            BankRateInfluenceSql document = dbServiceSQL.getAndCheck(BankRateInfluenceSql.class, id, BankRateInfluenceSql.GRAPH_ALL);
            checkDocumentDate(companySettings, document, auth.getLanguage());
            return documentService.recall(document);
        });
        BankRateInfluenceDto result = bankRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public BankOpeningBalanceDto importOpeningBalance(long id, String fileName) {
        BankOpeningBalanceDto entity =
                bankOpeningBalanceSqlMapper.toDto(dbServiceSQL.getAndCheck(BankOpeningBalanceSql.class, id, BankOpeningBalanceSql.GRAPH_ALL));
        if (BooleanUtils.isTrue(entity.getFinished())) return entity;
        return storageService.importBankOpeningBalance(entity, fileName);
    }

    public List<RepMoneyBalance<BankAccountDto>> reportBankBalance(ReportBalanceRequest request) {
        return moneyAccountService.reportBalance(request, AccountType.BANK);
    }


    public PageResponse<MoneyHistoryDto, RepMoneyDetail<BankAccountDto>> reportBankFlow(PageRequest request) {
        return moneyAccountService.reportFlow(request, AccountType.BANK);
    }

    public ISO20022Statements parseOperations(BankApi.BankFileType type, String fileName) {
        try (InputStream is = Channels.newInputStream(
                Validators.checkNotNull(storageService.gcsFileReadChannel(Validators.checkNotNull(fileName, "No file name")),
                        "Not found: " + fileName + " in " + auth.getCompanyId()))) {
            return switch (type) {
                case ISO20022 -> parseISO20022(is);
                case PAYPAL_CSV_TAB -> parsePayPal(CsvDelimiter.Tab, is);
                case PAYPAL_CSV_COMMA -> parsePayPal(CsvDelimiter.Comma, is);
                case REVOLUT -> parseRevolut(is);
                case SALARY_TAB -> parseSalary(CsvDelimiter.Tab, is);
            };
        } catch (IOException e) {
            throw new GamaException(e.getMessage(), e);
        } finally {
            storageService.deleteFile(fileName);
        }
    }

    public ISO20022Statements importOperations(ISO20022Statements statements) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        if (statements.getStatements() == null || statements.getStatements().size() == 0) return null;

        // check all dates before
        for (ISO20022Statement statement : statements.getStatements()) {
            if (statement.getItems() == null || statement.getItems().size() == 0) continue;
            for (ISO20022Record item : statement.getItems()) {
                checkDocumentDate(companySettings, item.getEntry().getBookingDate().toLocalDate(), auth.getLanguage());
            }
        }

        List<ISO20022Record> items = new ArrayList<>();
        List<String> tasksId = new ArrayList<>();

        int nr = 1;
        int start = 1;

        for (ISO20022Statement statement : statements.getStatements()) {
            if (statement.getItems() == null || statement.getItems().size() == 0) continue;

            DocBankAccount bankAccount = Validators.checkNotNull(statement.getBankAccount(), "No Bank Account");
            long bankAccountId = Validators.checkNotNull(bankAccount.getId(), "No Bank Account Id");

            for (ISO20022Record item : statement.getItems()) {
                if (!item.isImported() && (item.getEntry().isFees() || item.getDetail().isLinked())) {

                    item.setImported(true);

                    if (items.size() == BANK_IMPORT_PACKAGE_SIZE) {
                        tasksId.add(taskQueueService.queueTask(new ImportBankTask(companyId, bankAccountId, items, start)));
                        items = new ArrayList<>();
                        start = nr + 1;
                    }

                    items.add(item);

                    nr++;
                }
            }

            if (items.size() > 0) {
                tasksId.add(taskQueueService.queueTask(new ImportBankTask(companyId, bankAccountId, items, start)));
            }

            statements.setTaskIds(tasksId);
        }

        return statements;
    }

    private Element getElement(Element doc, String ns, String... nodes) {
        if (doc == null) return null;
        Element result = doc;
        NodeList nodeList;
        for (String node : nodes) {
            nodeList = result.getElementsByTagNameNS(ns, node);
            if (nodeList == null || nodeList.getLength() == 0) return null;
            result = (Element) nodeList.item(0);
        }
        return result;
    }

    private String getNodeValue(Node node) {
        return node == null ? null : node.getTextContent();
    }

    private String getElementValue(Element doc, String ns, String... nodes) {
        return getNodeValue(getElement(doc, ns, nodes));
    }

    private GamaMoney getMoney(Element doc, String ns, String... nodes) {
        Element element = getElement(doc, ns, nodes);
        if (element != null) {
            String amount = element.getTextContent();
            String currency = element.getAttribute("Ccy");
            if (StringHelper.isEmpty(currency)) currency = element.getAttributeNS(ns, "Ccy");
            return GamaMoney.parse(currency + " " + amount);
        }
        return null;
    }

    private GamaMoney getMoneyWithCurrency(Element doc, String ns, String currency, String... nodes) {
        Element element = getElement(doc, ns, nodes);
        if (element != null) {
            String amount = element.getTextContent();
            return GamaMoney.parse(currency + " " + amount);
        }
        return null;
    }

    private LocalDateTime getDateTime(Element doc, String ns, String... nodes) {
        Element element = getElement(doc, ns, nodes);
        if (element != null) {
            String dt = element.getTextContent();
            return DateUtils.parseLocalDateTime(dt);
        }
        return null;
    }

    private LocalDate getDate(Element doc, String ns, String... nodes) {
        Element element = getElement(doc, ns, nodes);
        if (element != null) {
            String dt = element.getTextContent();
            return DateUtils.parseLocalDate(dt);
        }
        return null;
    }

    private LocalDateTime getDateOrDateTime(Element doc, String ns, String... nodes) {
        LocalDateTime dtTm = null;
        Element element = getElement(doc, ns, nodes);
        if (element != null) {
            dtTm = getDateTime(element, ns, "DtTm");
            if (dtTm == null) {
                LocalDate dt = getDate(element, ns, "Dt");
                if (dt != null) dtTm = dt.atStartOfDay();
            }
        }
        return dtTm;
    }

    private Integer getInteger(Element doc, String ns, String... nodes) {
        Element element = getElement(doc, ns, nodes);
        if (element != null) {
            String text = element.getTextContent();
            return Integer.valueOf(text);
        }
        return null;
    }

    private boolean isCredit(Element doc, String ns, String... nodes) {
        Element element = getElement(doc, ns, nodes);
        if (element != null) {
            String text = element.getTextContent();
            if ("CRDT".equals(text)) return true;
            else if ("DBIT".equals(text)) return false;
        }
        throw new GamaException("Wrong XML format - credit/debit");
    }

    private Map<String, DocBankAccount> makeBankAccountMap() {
        return dbServiceSQL.makeQueryInCompany(BankAccountSql.class).getResultStream()
                .filter(b -> BooleanUtils.isNotTrue(b.getArchive()))
                .collect(Collectors.toMap(b -> StringHelper.normalizeBankAccount(b.getAccountCompressed()), DocBankAccount::new));
    }

    private Map<String, DocBankAccount> makeBankAccountCardMap() {
        Map<String, DocBankAccount> map = new HashMap<>();
        dbServiceSQL.makeQueryInCompany(BankAccountSql.class).getResultStream()
                .filter(bankAccount -> BooleanUtils.isNotTrue(bankAccount.getArchive()))
                .forEach(bankAccount -> {
                    var docBankAccount = new DocBankAccount(bankAccount);
                    map.put(StringHelper.normalizeBankAccount(bankAccount.getAccount()), docBankAccount);
                    CollectionsHelper.streamOf(bankAccount.getCards())
                            .filter(Objects::nonNull)
                            .forEach(bankCard ->
                                    map.put(StringHelper.trim(bankCard.getNumber1()) + '\u0001' + StringHelper.trim(bankCard.getNumber2()), docBankAccount));
                });
        return map;
    }

    static class CounterpartyMap {
        final Map<String, DocCounterparty> bankAccount = new HashMap<>();
        final Map<String, DocCounterparty> comCode = new HashMap<>();
    }

    private CounterpartyMap makeCounterpartyMap() {
        CounterpartyMap map = new CounterpartyMap();
        dbServiceSQL.executeInTransaction(entityManager ->
            dbServiceSQL.makeQueryInCompany(CounterpartySql.class).getResultStream()
                    .filter(counterparty -> BooleanUtils.isNotTrue(counterparty.getArchive()))
                    .forEach(counterparty -> {
                        DocCounterparty docCounterparty = new DocCounterparty(counterparty);
                        CollectionsHelper.streamOf(counterparty.getBanks())
                                .filter(Objects::nonNull)
                                .forEach(bankAccount ->
                                    map.bankAccount.put(StringHelper.normalizeBankAccount(bankAccount.getAccountCompressed()), docCounterparty));
                        if (StringHelper.hasValue(counterparty.getComCode()))
                            map.comCode.put(counterparty.getComCode(), docCounterparty);
                    }));

        if (entityManager.getTransaction().isActive()) entityManager.flush();
        entityManager.clear();
        return map;
    }

    private Map<String, DocEmployee> makeEmployeeMap() {
        Map<String, DocEmployee> map = new HashMap<>();
        dbServiceSQL.executeInTransaction(entityManager ->
                dbServiceSQL.makeQueryInCompany(EmployeeSql.class).getResultStream()
                        .filter(employee -> BooleanUtils.isNotTrue(employee.getArchive()))
                        .filter(employee -> CollectionsHelper.hasValue(employee.getBanks()))
                        .forEach(employee -> {
                            DocEmployee docEmployee = new DocEmployee(employee);
                            employee.getBanks().forEach(bankAccount -> map.put(StringHelper.normalizeBankAccount(bankAccount.getAccountCompressed()), docEmployee));
                        }));

        if (entityManager.getTransaction().isActive()) entityManager.flush();
        entityManager.clear();
        return map;
    }

    private ISO20022Statements parseStatements(Element doc, String ns, ISO20022CAMT version) {
        Map<String, DocBankAccount> bankAccountMap = makeBankAccountMap();
        CounterpartyMap counterpartyMap = makeCounterpartyMap();
        Map<String, DocEmployee> employeeMap = makeEmployeeMap();

        ISO20022Statements statements = new ISO20022Statements();

        statements.setMsgId(getElementValue(doc, ns, "GrpHdr", "MsgId"));
        statements.setCreatedOn(getDateTime(doc, ns, "GrpHdr", "CreDtTm"));

        NodeList eStmtList = doc.getElementsByTagNameNS(ns, version == ISO20022CAMT.CAMT052 ? "Rpt" : "Stmt");

        if (eStmtList.getLength() > 0) {
            statements.setStatements(new ArrayList<>());
            for (int i = 0, len = eStmtList.getLength(); i < len; ++i) {
                statements.getStatements().add(parseStatement(bankAccountMap, counterpartyMap, employeeMap, (Element) eStmtList.item(i), ns));
            }
        }
        return statements;
    }

    private ISO20022Statement parseStatement(Map<String, DocBankAccount> bankAccountMap,
                                             CounterpartyMap counterpartyMap,
                                             Map<String, DocEmployee> employeeMap,
                                             Element doc, String ns) {
        long start = System.currentTimeMillis();

        ISO20022Statement statement = new ISO20022Statement();

        statement.setPeriodFrom(getDateTime(doc, ns, "FrToDt", "FrDtTm"));
        statement.setPeriodTo(getDateTime(doc, ns, "FrToDt", "ToDtTm"));

        Element eAcct = getElement(doc, ns, "Acct");
        String accountNumber = getElementValue(eAcct, ns, "Id", "IBAN");
        if (StringHelper.isEmpty(accountNumber)) accountNumber = getElementValue(eAcct, ns, "Id", "Othr", "Id");
        Validators.checkArgument(StringHelper.hasValue(accountNumber), "No bank account found");

        statement.setBankAccount(getBankAccountEx(bankAccountMap, accountNumber));

        String currency = getElementValue(eAcct, ns, "Ccy");

        // .. / Stmt / Bal : balance records [1..n]
        // .. / Stmt / Bal / Tp / CdOrPrtry / Cd : OPBD - opening balance or CLBD - closing balance
        // .. / Stmt / Bal / Amt : amount (decimal number) [1..1]
        // .. / Stmt / Bal / CdtDbtInd : CRDT - credit or DBIT - debit [1..1]
        // .. / Stmt / Bal / Dt / Dt or DtTm : ISODate or ISODateTime [1..1]

        NodeList eStmtBal = doc.getElementsByTagNameNS(ns, "Bal");
        for (int i = 0, len = eStmtBal.getLength(); i < len; ++i) {
            Element bal = (Element) eStmtBal.item(i);
            String balanceType = getElementValue(bal, ns, "Tp", "CdOrPrtry", "Cd");
            GamaMoney amount = getMoney(bal, ns, "Amt");
            LocalDateTime dtTm = getDateOrDateTime(bal, ns, "Dt");
            boolean isCredit = isCredit(bal, ns, "CdtDbtInd");
            if ("OPBD".equals(balanceType)) {
                statement.setOpeningDateTime(dtTm);
                if (isCredit) {
                    statement.setOpeningBalanceCredit(amount);
                } else {
                    statement.setOpeningBalanceDebit(amount);
                }
            } else if ("CLBD".equals(balanceType)) {
                statement.setClosingDateTime(dtTm);
                if (isCredit) {
                    statement.setClosingBalanceCredit(amount);
                } else {
                    statement.setClosingBalanceDebit(amount);
                }
            }
        }

        // .. / Stmt / TxsSummry : Transactions Summary [0..1]
        // .. / Stmt / TxsSummry / TtlCdtNtries / Sum : credit amount [0..1]
        // .. / Stmt / TxsSummry / TtlCdtNtries / NbOfNtries : number of operations [0..1]
        // .. / Stmt / TxsSummry / TtlDbtNtries / Sum : debit amount [0..1]
        // .. / Stmt / TxsSummry / TtlDbtNtries / NbOfNtries : number of operations [0..1]
        // .. / Stmt / TxsSummry / TtlNtries / NbOfNtries : number of operations [0..1]
        // .. / Stmt / TxsSummry / TtlNtries / Sum : amount [0..1]
        // .. / Stmt / TxsSummry / TtlNtries / CdtDbtInd : CRDT - credit or DBIT - debit [0..1]

        Element eTxsSummry = getElement(doc, ns, "TxsSummry");
        if (eTxsSummry != null) {
            statement.setCreditTotal(getMoneyWithCurrency(eTxsSummry, ns, currency, "TtlCdtNtries", "Sum"));
            statement.setCreditCount(getInteger(eTxsSummry, ns, "TtlCdtNtries", "NbOfNtries"));
            statement.setDebitTotal(getMoneyWithCurrency(eTxsSummry, ns, currency, "TtlDbtNtries", "Sum"));
            statement.setDebitCount(getInteger(eTxsSummry, ns, "TtlDbtNtries", "NbOfNtries"));

            if (getElementValue(eTxsSummry, ns, "TtlNtries", "CdtDbtInd") != null) {
                boolean isCredit = isCredit(eTxsSummry, ns, "TtlNtries", "CdtDbtInd");
                GamaMoney total = getMoneyWithCurrency(eTxsSummry, ns, currency, "TtlNtries", "Sum");
                Integer count = getInteger(eTxsSummry, ns, "TtlNtries", "NbOfNtries");
                if (isCredit) {
                    statement.setCreditTotalTotal(total);
                    statement.setTotalCount(count);
                } else {
                    statement.setDebitTotalTotal(total);
                    statement.setTotalCount(count);
                }
            }
        }

        // .. / Stmt / Ntry : operation record

        NodeList eNtryList = doc.getElementsByTagNameNS(ns, "Ntry");
        if (eNtryList.getLength() > 0) {
            statement.setItems(new ArrayList<>());

            for (int i = 0, len = eNtryList.getLength(); i < len; ++i) {
                ISO20022Entry entry = parseEntry(accountNumber, bankAccountMap, counterpartyMap, employeeMap, (Element) eNtryList.item(i), ns);
                if (entry != null && entry.getDetails() != null && entry.getDetails().size() > 0) {
                    for (ISO20022EntryDetail detail : entry.getDetails()) {
                        ISO20022Record record = new ISO20022Record();
                        record.setUuid(UUID.randomUUID());
                        record.setDetail(detail);
                        record.setEntry(entry);
                        if (detail.isFees() && !entry.isFees()) entry.setFees(true);
                        entry.setDetails(null);
                        statement.getItems().add(record);
                    }
                }
            }
        }

        log.info("Statement parsed in " + (System.currentTimeMillis() - start) + "ms");
        return statement;
    }

    private ISO20022Entry parseEntry(String currentAccount,
                                     Map<String, DocBankAccount> bankAccountMap,
                                     CounterpartyMap counterpartyMap,
                                     Map<String, DocEmployee> employeeMap,
                                     Element doc, String ns) {

        ISO20022Entry entry = new ISO20022Entry();

        // .. Amt : Amount with currency (Ccy) [1..1]
        // .. CdtDbtInd : CreditDebitIndicator [1..1]

        boolean isCredit = isCredit(doc, ns, "CdtDbtInd");
        if (isCredit) {
            entry.setCredit(getMoney(doc, ns, "Amt"));
        } else {
            entry.setDebit(getMoney(doc, ns, "Amt"));
        }

        // .. BookgDt : BookingDate [0..1]
        // .. BookgDt / Dt or DtTm : ISODate or ISODateTime [1..1]

        entry.setBookingDate(getDateOrDateTime(doc, ns, "BookgDt"));

        // .. ValDt : ValueDate [0..1]
        // .. ValDt / Dt or DtTm : ISODate or ISODateTime [1..1]

        entry.setValueDate(getDateOrDateTime(doc, ns, "ValDt"));

        // .. BkTxCd : BankTransactionCode [1..1]
        // .. BkTxCd / Domn / Cd : first part of code
        // .. BkTxCd / Domn / Fmly / Cd : second part
        // .. BkTxCd / Domn / Fmly / SubFmlyCd : third part

        entry.setCodeDomain(getElementValue(doc, ns, "BkTxCd", "Domn", "Cd"));
        entry.setCodeFamily(getElementValue(doc, ns, "BkTxCd", "Domn", "Fmly", "Cd"));
        entry.setCodeSubFamily(getElementValue(doc, ns, "BkTxCd", "Domn", "Fmly", "SubFmlyCd"));
        process(entry);

        // .. NtryDtls : EntryDetails [1..1]
        // .. NtryDtls / TxDtls : TransactionDetails [0..n]

        //Element eNtryDtls = getElement(doc, ns, "NtryDtls");
        NodeList eNtryDtlsList = doc.getElementsByTagNameNS(ns, "NtryDtls");
        if (eNtryDtlsList.getLength() > 0) {
            for (int i = 0, len = eNtryDtlsList.getLength(); i < len; ++i) {
                NodeList txDetailsList = ((Element) eNtryDtlsList.item(i)).getElementsByTagNameNS(ns, "TxDtls");
                if (txDetailsList.getLength() > 0) {
                    entry.setDetails(new ArrayList<>());
                    for (int i2 = 0, len2 = txDetailsList.getLength(); i2 < len2; ++i2) {
                        entry.getDetails().add(parseEntryDetail(currentAccount, bankAccountMap, counterpartyMap,
                                employeeMap, (Element) txDetailsList.item(i2), ns, isCredit));
                    }
                }
            }
        }

        return entry;
    }

    private void process(ISO20022Entry entry) {
        entry.setCash("CWDL".equals(entry.getCodeSubFamily()) || "CDPT".equals(entry.getCodeSubFamily()) ||
                (("PMNT".equals(entry.getCodeDomain()) && "CCRD".equals(entry.getCodeFamily()) && "OTHR".equals(entry.getCodeSubFamily()))));

        entry.setFees(("LDAS".equals(entry.getCodeDomain()) && "MCOP".equals(entry.getCodeFamily()) && "COMT".equals(entry.getCodeSubFamily())) ||
                ("LDAS".equals(entry.getCodeDomain()) && "MDOP".equals(entry.getCodeFamily()) && "COMT".equals(entry.getCodeSubFamily())) ||
                "CHRG".equals(entry.getCodeSubFamily()) ||
                "FEES".equals(entry.getCodeSubFamily()) ||
                ("PMNT".equals(entry.getCodeDomain()) && "MDOP".equals(entry.getCodeFamily()) && "OTHR".equals(entry.getCodeSubFamily())));
    }

    private void process(ISO20022EntryDetail detail, String currentAccount,
                         Map<String, DocBankAccount> bankAccountMap,
                         CounterpartyMap counterpartyMap,
                         Map<String, DocEmployee> employeeMap) {
        if (detail.getPartyOrgId() != null) {
            // try to find company by id
            DocCounterparty counterparty = counterpartyMap.comCode.get(detail.getPartyOrgId());
            if (counterparty == null && detail.getPartyIBAN() != null) {
                // if not try to find company by bank account
                counterparty = getCounterpartyByBankAccount(counterpartyMap, detail.getPartyIBAN());
                if (counterparty == null) {
                    if (StringHelper.isEqualsBankAccounts(currentAccount, detail.getPartyIBAN())) {
                        detail.setFees(true);
                        detail.setLinked(true);
                        detail.setAutoLinked(true);
                    } else {
                        DocBankAccount account = getBankAccount(bankAccountMap, detail.getPartyIBAN());
                        if (account != null) {
                            detail.setAccount2(account);
                            detail.setLinked(true);
                            detail.setAutoLinked(true);
                        }
                    }
                }
            }
            if (counterparty != null) {
                detail.setCounterparty(counterparty);
                detail.setLinked(true);
                detail.setAutoLinked(true);
            }

        } else if (detail.getPartyIBAN() != null) {

            // try to find company first by bank account - if not then try to find employee by bank account
            DocCounterparty counterparty = getCounterpartyByBankAccount(counterpartyMap, detail.getPartyIBAN());
            if (counterparty != null) {
                detail.setCounterparty(counterparty);
                detail.setLinked(true);
                detail.setAutoLinked(true);
            }
            if (!detail.isLinked()) {
                DocEmployee employee = getEmployeeByBankAccount(employeeMap, detail.getPartyIBAN());
                if (employee != null) {
                    detail.setEmployee(employee);
                    detail.setLinked(true);
                    detail.setAutoLinked(true);
                }
            }
            if (!detail.isLinked()) {
                if (StringHelper.isEqualsBankAccounts(currentAccount, detail.getPartyIBAN())) {
                    detail.setFees(true);
                    detail.setLinked(true);
                    detail.setAutoLinked(true);
                } else {
                    DocBankAccount account = getBankAccount(bankAccountMap, detail.getPartyIBAN());
                    if (account != null) {
                        detail.setAccount2(account);
                        detail.setLinked(true);
                        detail.setAutoLinked(true);
                    }
                }
            }
        }
    }

    private ISO20022EntryDetail parseEntryDetail(String currentAccount,
                                                 Map<String, DocBankAccount> bankAccountMap,
                                                 CounterpartyMap counterpartyMap,
                                                 Map<String, DocEmployee> employeeMap,
                                                 Element doc, String ns, boolean isCredit) {

        ISO20022EntryDetail detail = new ISO20022EntryDetail();

        // .. NtryDtls / TxDtls / Refs : References [0..1]
        // .. NtryDtls / TxDtls / Refs / EndToEndId : EndToEndIdentification [0..1]

        detail.setOpNumber(getElementValue(doc, ns, "Refs", "EndToEndId"));

        // .. NtryDtls / TxDtls / Amt : Amount [1..1] - !!!
        // .. NtryDtls / TxDtls / CdtDbtInd : CreditDebitIndicator [1..1] - !!!

        //detail.setAmount(getMoney(doc, ns, "Amt"));

        // .. NtryDtls / TxDtls / AmtDtls : AmountDetails [0..1]
        // .. NtryDtls / TxDtls / AmtDtls / InstdAmt : InstructedAmount [0..1]
        // .. NtryDtls / TxDtls / AmtDtls / InstdAmt / Amt : Amount with currency (Ccy) [1..1]
        // .. NtryDtls / TxDtls / AmtDtls / InstdAmt / CcyXchg : CurrencyExchange [0..1]

        // CcyXchg / SrcCcy : SourceCurrency [1..1]
        // CcyXchg / TrgtCcy : TargetCurrency [0..1]
        // CcyXchg / UnitCcy : UnitCurrency [0..1]
        // CcyXchg / XchgRate : ExchangeRate (decimal number) (ExchangeRate = UnitCurrency/QuotedCurrency) [1..1]
        // CcyXchg / CtrctId : ContractIdentification [0..1]
        // CcyXchg / QtnDt : QuotationDate (ISODateTime) [0..1]

        // .. NtryDtls / TxDtls / AmtDtls / PrtryAmt : ProprietaryAmount [0..n]
        // .. NtryDtls / TxDtls / AmtDtls / PrtryAmt / Tp : Type - always 'EQUIVALENT' [1..1]
        // .. NtryDtls / TxDtls / AmtDtls / PrtryAmt / Amt : amount with currency [1..1]
        // .. NtryDtls / TxDtls / AmtDtls / PrtryAmt / CcyXchg : CurrencyExchange [0..1]

        detail.setProprietaryAmount(getMoney(doc, ns, "AmtDtls", "PrtryAmt", "Amt"));

        // .. NtryDtls / TxDtls / AmtDtls / TxAmt : TransactionAmount [0..n]
        // .. NtryDtls / TxDtls / AmtDtls / TxAmt / Tp : Type - always 'EQUIVALENT' [1..1]
        // .. NtryDtls / TxDtls / AmtDtls / TxAmt / Amt : amount with currency [1..1]
        // .. NtryDtls / TxDtls / AmtDtls / TxAmt / CcyXchg : CurrencyExchange [0..1]

        detail.setAmount(getMoney(doc, ns, "AmtDtls", "TxAmt", "Amt"));

        // .. NtryDtls / TxDtls / RltdPties : RelatedParties [0..1]
        // .. NtryDtls / TxDtls / RltdPties / Dbtr : Debtor [0..1]
        // .. NtryDtls / TxDtls / RltdPties / Dbtr / Nm : Name [0..1]
        // .. NtryDtls / TxDtls / RltdPties / Dbtr / Id / OrgId / Othr / Id : OrganisationIdentification [0..*]
        // .. NtryDtls / TxDtls / RltdPties / Dbtr / Id / PrvtId / Othr / Id : PrivateIdentification [0..*]
        // .. NtryDtls / TxDtls / RltdPties / DbtrAcct : DebtorAccount [0..1]
        // .. NtryDtls / TxDtls / RltdPties / DbtrAcct / Id / IBAN : IBAN [1..1]

        if (isCredit) {
            detail.setPartyName(getElementValue(doc, ns, "RltdPties", "Dbtr", "Nm"));
            detail.setPartyOrgId(getElementValue(doc, ns, "RltdPties", "Dbtr", "Id", "OrgId", "Othr", "Id"));
            detail.setPartyPrivateId(getElementValue(doc, ns, "RltdPties", "Dbtr", "Id", "PrvtId", "Othr", "Id"));
            detail.setPartyIBAN(getElementValue(doc, ns, "RltdPties", "DbtrAcct", "Id", "IBAN"));
            process(detail, currentAccount, bankAccountMap, counterpartyMap, employeeMap);
        }

        // .. NtryDtls / TxDtls / RltdPties / Cdtr : Creditor [0..1]
        // .. NtryDtls / TxDtls / RltdPties / Cdtr / Nm : Name [0..1]
        // .. NtryDtls / TxDtls / RltdPties / Cdtr / Id / OrgId / Othr / Id : OrganisationIdentification [0..*]
        // .. NtryDtls / TxDtls / RltdPties / Cdtr / Id / PrvtId / Othr / Id : PrivateIdentification [0..*]
        // .. NtryDtls / TxDtls / RltdPties / CdtrAcct : CreditorAccount [0..1]
        // .. NtryDtls / TxDtls / RltdPties / CdtrAcct / Id / IBAN : IBAN [1..1]

        else { // if (!isCredit) {
            detail.setPartyName(getElementValue(doc, ns, "RltdPties", "Cdtr", "Nm"));
            detail.setPartyOrgId(getElementValue(doc, ns, "RltdPties", "Cdtr", "Id", "OrgId", "Othr", "Id"));
            detail.setPartyPrivateId(getElementValue(doc, ns, "RltdPties", "Cdtr", "Id", "PrvtId", "Othr", "Id"));
            detail.setPartyIBAN(getElementValue(doc, ns, "RltdPties", "CdtrAcct", "Id", "IBAN"));
            process(detail, currentAccount, bankAccountMap, counterpartyMap, employeeMap);
        }

        // .. NtryDtls / TxDtls / RltdAgts : RelatedAgents [0..1]
        // .. NtryDtls / TxDtls / RltdAgts / DbtrAgt / FinInstnId : DebtorAgent [0..1]
        // .. NtryDtls / TxDtls / RltdAgts / CdtrAgt / FinInstnId : CreditorAgent [0..1]

        // FinInstnId : FinancialInstitutionIdentification [1..1]
        // FinInstnId / BIC : BIC [1..1]
        // FinInstnId / Nm : Name [1..1]

        // .. NtryDtls / TxDtls / RmtInf : RemittanceInformation [0..1]
        // .. NtryDtls / TxDtls / RmtInf / Ustrd : operation info [0..1]
        // .. NtryDtls / TxDtls / RmtInf / Strd / CdtrRefInf / Ref : operation info [0..1]

        StringBuilder sb = new StringBuilder();

        String paymentId = getElementValue(doc, ns, "RmtInf", "Strd", "CdtrRefInf", "Ref");
        if (StringHelper.hasValue(paymentId)) {
            detail.setPaymentCode(true);
            sb.append(paymentId).append(' ');
            if (!detail.isLinked() && !Validators.isValid(detail.getCounterparty()) && !Validators.isValid(detail.getEmployee())) {
                // try to find invoice document with the same paymentId
                checkInvoiceByPaymentId(detail, StringHelper.firstWord(paymentId));
            }
        }

        String note = getElementValue(doc, ns, "RmtInf", "Ustrd");
        if (StringHelper.hasValue(note)) sb.append(note);

        // if no counterparty and no employee and no paymentId but has note - try to find invoice by paymentId in note
        if (!detail.isLinked() && !Validators.isValid(detail.getCounterparty()) && !Validators.isValid(detail.getEmployee()) &&
                StringHelper.isEmpty(paymentId) && StringHelper.hasValue(note)) {
            checkInvoiceByPaymentId(detail, StringHelper.firstWord(note));
        }

        // special case if credit card payments
        String card = getElementValue(doc, ns, "Refs", "Prtry", "Tp");
        if (StringHelper.hasValue(card)) sb.append(" ").append(card);

        String ref = getElementValue(doc, ns, "Refs", "Prtry", "Ref");
        if (StringHelper.hasValue(ref)) sb.append(" ").append(ref);

        if (sb.length() > 0) detail.setNote(sb.toString().trim());

        return detail;
    }

    private void checkInvoiceByPaymentId(ISO20022EntryDetail detail, String paymentId) {
        // try to find invoice document with the same paymentId
        @SuppressWarnings("unchecked")
        List<BigInteger> list = entityManager.createNativeQuery("""
                        SELECT c.id
                        FROM counterparties c
                        JOIN documents d ON c.id = d.counterparty_id
                        JOIN invoice i ON i.id = d.id
                        WHERE i.payment_id = :paymentId
                        AND d.company_id = :companyId
                        AND (d.archive IS null OR d.archive = false)
                        AND (d.hidden IS null OR d.hidden = false)
                        ORDER BY d.date DESC,
                            d.ordinal DESC,
                            d.series DESC,
                            d.number DESC,
                            d.id DESC
                        LIMIT 1
                        """)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("paymentId", StringHelper.firstWord(paymentId))
                .getResultList();
        long counterpartyId = list != null && list.size() == 1 ? list.get(0).longValue() : 0;

        if (counterpartyId != 0) {
            detail.setCounterparty(new DocCounterparty(entityManager.getReference(CounterpartySql.class, counterpartyId)));
            detail.setLinked(true);
            detail.setAutoLinked(true);
        }
    }

    public ISO20022Statements parseISO20022(InputStream is) {
        try {
            Document document = XMLUtils.document(is, true);
            Element root = document.getDocumentElement();
            String ns = root.getNamespaceURI();
            ISO20022CAMT version = StringHelper.hasValue(ns) &&
                    ns.contains(ISO20022CAMT.CAMT052.toString()) ? ISO20022CAMT.CAMT052 : ISO20022CAMT.CAMT053;

            Element stmt = (Element) root.getElementsByTagNameNS(ns,
                    version == ISO20022CAMT.CAMT052 ? "BkToCstmrAcctRpt" : "BkToCstmrStmt").item(0);

            return parseStatements(stmt, ns, version);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new GamaException("Wrong XML", e);
        }
    }

    public ISO20022Statements parsePayPal(CsvDelimiter delimiter, InputStream is) {

        CSVFormat format = CSVFormat.DEFAULT
                .withHeader()
                .withIgnoreSurroundingSpaces()
                .withAllowMissingColumnNames();

        if (delimiter == CsvDelimiter.Tab) format = format.withDelimiter('\t');

        try (CSVParser parser = new CSVParser(new InputStreamReader(new BOMInputStream(is), StandardCharsets.UTF_8), format)) {

            ISO20022Statements statements = new ISO20022Statements();
            ISO20022Statement statement = new ISO20022Statement();
            statements.setStatements(new ArrayList<>());
            statements.getStatements().add(statement);

            statement.setItems(new ArrayList<>());

            Map<String, DocBankAccount> bankAccountMap = makeBankAccountMap();
            CounterpartyMap counterpartyMap = makeCounterpartyMap();

            for (final CSVRecord record : parser) {

                ISO20022Record r = new ISO20022Record();
                statement.getItems().add(r);

                ISO20022Entry entry = new ISO20022Entry();
                ISO20022EntryDetail detail = new ISO20022EntryDetail();

                r.setEntry(entry);
                r.setDetail(detail);

                LocalDate date = CSVRecordUtils.getLocalDateDMY(record, "Date");
                LocalTime time = CSVRecordUtils.getLocalTime(record, "Time");
                LocalDateTime bookingDate = null;
                if (date != null) {
                    if (time == null) time = LocalTime.of(0, 0, 0);
                    bookingDate = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                            time.getHour(), time.getMinute(), time.getSecond());

                    if (DateUtils.compare(bookingDate, false, statement.getPeriodFrom(), false) < 0) {
                        statement.setPeriodFrom(bookingDate);
                    } else if (DateUtils.compare(bookingDate, false, statement.getPeriodTo(), true) > 0) {
                        statement.setPeriodTo(bookingDate);
                    }
                }
                entry.setBookingDate(bookingDate);

                // name = "Bank Account" if it's Withdraw Funds to Bank Account and Gross value is negative
                String name = CSVRecordUtils.getString(record, "Name");
                String currency = CSVRecordUtils.getString(record, "Currency");
                GamaMoney gross = CSVRecordUtils.getMoney(record, "Gross", currency);
                GamaMoney fee = CSVRecordUtils.getMoney(record, "Fee", currency);
                String from = CSVRecordUtils.getString(record, "From Email Address");
                String to = CSVRecordUtils.getString(record, "To Email Address");
                String id = CSVRecordUtils.getString(record, "Transaction ID");
                String note = CSVRecordUtils.getString(record, "Type");

                if (GamaMoneyUtils.isPositive(gross)) {
                    entry.setCredit(gross);
                    detail.setPartyIBAN(from);
                    detail.setCounterparty(getCounterpartyByBankAccount(counterpartyMap, from));
                    if (!Validators.isValid(statement.getBankAccount())) {
                        statement.setBankAccount(getBankAccountEx(bankAccountMap, to));
                    }
                } else if (GamaMoneyUtils.isNegative(gross)) {
                    entry.setDebit(gross.negated());
                    detail.setPartyIBAN(to);
                    detail.setCounterparty(getCounterpartyByBankAccount(counterpartyMap, to));
                    if (!Validators.isValid(statement.getBankAccount())) {
                        statement.setBankAccount(getBankAccountEx(bankAccountMap, from));
                    }
                }
                if (Validators.isValid(detail.getCounterparty())) {
                    detail.setLinked(true);
                    detail.setAutoLinked(true);
                }

                detail.setOpNumber(id);
                detail.setPartyName(name);
                detail.setNote(note);

                if (GamaMoneyUtils.isNonZero(fee)) {
                    r = new ISO20022Record();
                    statement.getItems().add(r);

                    entry = new ISO20022Entry();
                    detail = new ISO20022EntryDetail();

                    r.setEntry(entry);
                    r.setDetail(detail);

                    entry.setFees(true);
                    entry.setBookingDate(bookingDate);

                    if (GamaMoneyUtils.isPositive(fee)) {
                        entry.setCredit(fee);
                    } else if (GamaMoneyUtils.isNegative(fee)) {
                        entry.setDebit(fee.negated());
                    }
                    detail.setOpNumber(id);
                    detail.setPartyName(name);
                    detail.setNote(note);
                }
            }

            return statements;

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    public ISO20022Statements parseRevolut(InputStream is) {

        CSVFormat format = CSVFormat.DEFAULT
                .withHeader()
                .withIgnoreSurroundingSpaces()
                .withAllowMissingColumnNames();

        try (CSVParser parser = new CSVParser(new InputStreamReader(is, StandardCharsets.UTF_8), format)) {

            ISO20022Statements statements = new ISO20022Statements();
            ISO20022Statement statement = new ISO20022Statement();
            statements.setStatements(new ArrayList<>());
            statements.getStatements().add(statement);

            statement.setItems(new ArrayList<>());

            Map<String, DocBankAccount> bankAccountCardMap = makeBankAccountCardMap();

            int opId = 0;

            for (final CSVRecord record : parser) {

                ISO20022Record r = new ISO20022Record();
                statement.getItems().add(r);

                ISO20022Entry entry = new ISO20022Entry();
                ISO20022EntryDetail detail = new ISO20022EntryDetail();

                r.setEntry(entry);
                r.setDetail(detail);

                LocalDate date = CSVRecordUtils.getLocalDate(record, "Date");
                if (date == null) date = CSVRecordUtils.getLocalDate(record, "Date completed (UTC)");
                LocalTime time = CSVRecordUtils.getLocalTime(record, "Time");
                LocalDateTime bookingDate = null;
                if (date != null) {
                    if (time == null) time = LocalTime.of(0, 0, 0);
                    bookingDate = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                            time.getHour(), time.getMinute(), time.getSecond());

                    if (DateUtils.compare(bookingDate, false, statement.getPeriodFrom(), false) < 0) {
                        statement.setPeriodFrom(bookingDate);
                    } else if (DateUtils.compare(bookingDate, false, statement.getPeriodTo(), true) > 0) {
                        statement.setPeriodTo(bookingDate);
                    }
                }
                entry.setBookingDate(bookingDate);

                String account = CSVRecordUtils.getString(record, "Account");
                String name = CSVRecordUtils.getString(record, "Description");
                String currency = CSVRecordUtils.getString(record, "Payment currency");
                GamaMoney gross = CSVRecordUtils.getMoney(record, "Amount", currency);
                GamaMoney fee = CSVRecordUtils.getMoney(record, "Fee", currency);
                String cc = CSVRecordUtils.getString(record, "Card number");
                String reference = CSVRecordUtils.getString(record, "Reference");
                String payer = CSVRecordUtils.getString(record, "Payer");

                StringJoiner sj = new StringJoiner(" ");
                if (StringHelper.hasValue(payer)) sj.add(payer);
                if (StringHelper.hasValue(cc)) sj.add(cc);
                if (StringHelper.hasValue(reference)) sj.add(reference);
                String note = sj.toString();

                if (!Validators.isValid(statement.getBankAccount()) && StringHelper.hasValue(account)) {
                    statement.setBankAccount(getBankAccountEx(bankAccountCardMap, account));
                }
                if (!Validators.isValid(statement.getBankAccount()) && StringHelper.hasValue(cc)) {
                    statement.setBankAccount(getBankCardAccount(bankAccountCardMap, cc));
                }

                if (GamaMoneyUtils.isPositive(gross)) {
                    entry.setCredit(gross);
                } else if (GamaMoneyUtils.isNegative(gross)) {
                    entry.setDebit(gross.negated());
                }

                detail.setOpNumber(String.valueOf(++opId));
                detail.setPartyName(name);
                detail.setNote(note);

                if (GamaMoneyUtils.isNonZero(fee)) {
                    r = new ISO20022Record();
                    statement.getItems().add(r);

                    entry = new ISO20022Entry();
                    detail = new ISO20022EntryDetail();

                    r.setEntry(entry);
                    r.setDetail(detail);

                    entry.setFees(true);
                    entry.setBookingDate(bookingDate);

                    if (GamaMoneyUtils.isPositive(fee)) {
                        entry.setCredit(fee);
                    } else if (GamaMoneyUtils.isNegative(fee)) {
                        entry.setDebit(fee.negated());
                    }
                    detail.setOpNumber(String.valueOf(++opId));
                    detail.setPartyName(name);
                    detail.setNote(note);
                } else if ("FEE".equalsIgnoreCase(CSVRecordUtils.getString(record, "Type"))) {
                    entry.setFees(true);
                }
            }

            return statements;

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    public ISO20022Statements parseSalary(CsvDelimiter delimiter, InputStream is) {

        CSVFormat format = CSVFormat.DEFAULT
                .withHeader("From", "Name", "Account", "Amount", "Note")
                .withFirstRecordAsHeader()
                .withIgnoreSurroundingSpaces()
                .withAllowMissingColumnNames()
                .withDelimiter('\t');

        try (CSVParser parser = new CSVParser(new InputStreamReader(is, StandardCharsets.UTF_8), format)) {

            ISO20022Statements statements = new ISO20022Statements();
            ISO20022Statement statement = new ISO20022Statement();
            statements.setStatements(new ArrayList<>());
            statements.getStatements().add(statement);

            statement.setItems(new ArrayList<>());

            String baseCurrency = auth.getSettings().getCurrency().getCode();
            Map<String, DocEmployee> employeeMap = makeEmployeeMap();
            Map<String, DocBankAccount> bankAccountMap = makeBankAccountMap();

            LocalDateTime now = DateUtils.now();

            statement.setPeriodFrom(now);
            statement.setPeriodTo(now);

            for (final CSVRecord record : parser) {

                String from = CSVRecordUtils.getString(record, "From");
                String name = CSVRecordUtils.getString(record, "Name");
                String to = CSVRecordUtils.getString(record, "Account");
                String note = CSVRecordUtils.getString(record, "Note");
                GamaMoney amount = CSVRecordUtils.getMoney(record, "Amount", baseCurrency);

                if (statement.getBankAccount() == null) {
                    statement.setBankAccount(getBankAccountEx(bankAccountMap, from));
                }

                ISO20022Record r = new ISO20022Record();
                statement.getItems().add(r);

                ISO20022Entry entry = new ISO20022Entry();
                ISO20022EntryDetail detail = new ISO20022EntryDetail();

                r.setEntry(entry);
                r.setDetail(detail);
                r.setNoDebt(true);

                entry.setBookingDate(now);

                entry.setDebit(amount);

                detail.setEmployee(getEmployeeByBankAccount(employeeMap, to));
                if (Validators.isValid(detail.getEmployee())) {
                    detail.setLinked(true);
                    detail.setAutoLinked(true);
                }

                detail.setPartyName(name);
                if (StringHelper.hasValue(to)) {
                    detail.setPartyIBAN(to);
                    detail.setAccount2(new DocBankAccount(to));
                }
                detail.setNote(note);
            }

            return statements;

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    private DocBankAccount getBankCardAccount(Map<String, DocBankAccount> bankAccountCardMap, String bankCard) {
        if (CollectionsHelper.isEmpty(bankAccountCardMap) || StringHelper.isEmpty(bankCard)) return null;
        String[] numbers = bankCard.split("\\*+");
        if (numbers.length != 2) return null;
        DocBankAccount bankAccount = bankAccountCardMap.get(numbers[0] + '\u0001' + numbers[1]);
        if (bankAccount != null) return bankAccount;

        throw new GamaException("Bank account " + bankCard + " not found");
    }

    private DocBankAccount getBankAccount(Map<String, DocBankAccount> bankAccountMap, String accountNumber) {
        if (CollectionsHelper.isEmpty(bankAccountMap) || StringHelper.isEmpty(accountNumber)) return null;
        return bankAccountMap.get(StringHelper.normalizeBankAccount(accountNumber));
    }

    private DocBankAccount getBankAccountEx(Map<String, DocBankAccount> bankAccountMap, String accountNumber) {
        DocBankAccount bankAccount = getBankAccount(bankAccountMap, accountNumber);
        if (bankAccount != null) return bankAccount;

        throw new GamaException("Bank account " + accountNumber + " not found");
    }

    private DocCounterparty getCounterpartyByBankAccount(CounterpartyMap counterpartyMap, String accountNumber) {
        if (CollectionsHelper.isEmpty(counterpartyMap.bankAccount) || StringHelper.isEmpty(accountNumber)) return null;
        return counterpartyMap.bankAccount.get(StringHelper.normalizeBankAccount(accountNumber));
    }

    private DocEmployee getEmployeeByBankAccount(Map<String, DocEmployee> employeeMap, String accountNumber) {
        if (CollectionsHelper.isEmpty(employeeMap) || StringHelper.isEmpty(accountNumber)) return null;
        return employeeMap.get(StringHelper.normalizeBankAccount(accountNumber));
    }

    public enum CsvDelimiter {
        Tab, Comma
    }
}