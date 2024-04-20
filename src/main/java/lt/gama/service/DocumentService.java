package lt.gama.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.WriteChannel;
import jakarta.persistence.EntityManager;
import lt.gama.ConstWorkers;
import lt.gama.api.APIResult;
import lt.gama.api.response.TaskResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.base.BaseMoneyRateInfluenceDto;
import lt.gama.model.dto.documents.DebtCorrectionDto;
import lt.gama.model.dto.documents.DebtRateInfluenceDto;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.i.IFinished;
import lt.gama.model.i.IMoneyAccount;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.base.BaseMoneyRateInfluenceSql;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.documents.items.*;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.CashSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.DocumentDoubleEntry;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.ProcessingStatusType;
import lt.gama.model.type.inventory.WarehouseTagged;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaNotFoundException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

import static lt.gama.ConstWorkers.DOCS_PRINT_FOLDER;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;


/**
 * Gama
 * Created by valdas on 15-06-09.
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);


    private final GLOperationsService glOperationsService;
    private final InventoryService inventoryService;
    private final InventoryCheckService inventoryCheckService;
    private final CurrencyService currencyService;
    private final TemplateService templateService;
    private final StorageService storageService;
    private final DBServiceSQL dbServiceSQL;
    private final GLUtilsService glUtilsService;
    private final DebtService debtService;
    private final MoneyAccountService moneyAccountService;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final Auth auth;
    private final DebtCorrectionSqlMapper debtCorrectionSqlMapper;
    private final DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper;
    private final DocsMappersService docsMappersService;
    private final EmployeeOperationSqlMapper employeeOperationSqlMapper;
    private final BankOperationSqlMapper bankOperationSqlMapper;
    private final CashOperationSqlMapper cashOperationSqlMapper;
    private final InventorySqlMapper inventorySqlMapper;
    private final TransProdSqlMapper transportationSqlMapper;
    private final PurchaseSqlMapper purchaseSqlMapper;
    private final InvoiceSqlMapper invoiceSqlMapper;
    private final ObjectMapper objectMapper;


    DocumentService(GLOperationsService glOperationsService,
                    InventoryService inventoryService,
                    InventoryCheckService inventoryCheckService,
                    CurrencyService currencyService,
                    TemplateService templateService,
                    StorageService storageService,
                    DBServiceSQL dbServiceSQL,
                    GLUtilsService glUtilsService,
                    DebtService debtService,
                    MoneyAccountService moneyAccountService,
                    DoubleEntrySqlMapper doubleEntrySqlMapper,
                    EmployeeSqlMapper employeeSqlMapper,
                    Auth auth,
                    DebtCorrectionSqlMapper debtCorrectionSqlMapper,
                    DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper,
                    DocsMappersService docsMappersService,
                    EmployeeOperationSqlMapper employeeOperationSqlMapper,
                    BankOperationSqlMapper bankOperationSqlMapper,
                    CashOperationSqlMapper cashOperationSqlMapper,
                    InventorySqlMapper inventorySqlMapper,
                    TransProdSqlMapper transportationSqlMapper,
                    PurchaseSqlMapper purchaseSqlMapper,
                    InvoiceSqlMapper invoiceSqlMapper, ObjectMapper objectMapper) {
        this.glOperationsService = glOperationsService;
        this.inventoryService = inventoryService;
        this.inventoryCheckService = inventoryCheckService;
        this.currencyService = currencyService;
        this.templateService = templateService;
        this.storageService = storageService;
        this.dbServiceSQL = dbServiceSQL;
        this.glUtilsService = glUtilsService;
        this.debtService = debtService;
        this.moneyAccountService = moneyAccountService;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.auth = auth;
        this.debtCorrectionSqlMapper = debtCorrectionSqlMapper;
        this.debtRateInfluenceSqlMapper = debtRateInfluenceSqlMapper;
        this.docsMappersService = docsMappersService;
        this.employeeOperationSqlMapper = employeeOperationSqlMapper;
        this.bankOperationSqlMapper = bankOperationSqlMapper;
        this.cashOperationSqlMapper = cashOperationSqlMapper;
        this.inventorySqlMapper = inventorySqlMapper;
        this.transportationSqlMapper = transportationSqlMapper;
        this.purchaseSqlMapper = purchaseSqlMapper;
        this.invoiceSqlMapper = invoiceSqlMapper;
        this.objectMapper = objectMapper;
    }

    public DocumentDoubleEntry<EmployeeOperationSql> finish(EmployeeOperationSql document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getEmployee(), "employee", document.toString(), auth.getLanguage());
        Validators.checkNotNull(document.getExchange(), "No currency exchange info in {0}", document);

        EmployeeDto employee = employeeSqlMapper.toDto(dbServiceSQL.getById(EmployeeSql.class, document.getEmployee().getId()));

        // check if everything for double-entry registration is ok
        glOperationsService.checkEmployeeOperation(employee.getName(), employee.getMoneyAccount(), employeeOperationSqlMapper.toDto(document));

        // generate debt balance records
        if (BooleanUtils.isNotTrue(document.getNoDebt())) {
            if (Validators.isValid(document.getCounterparty())) {
                document = debtService.finishDebt(document);
            }
        }

        // money balance
        String currency = document.getCurrency();
        document = (EmployeeOperationSql) moneyAccountService.finishOperation(currency, document,
                AccountType.EMPLOYEE, EmployeeSql.class, false);

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishEmployeeOperation(employee.getName(), employee.getMoneyAccount(),
                employeeOperationSqlMapper.toDto(document), null, finishGL);

        document.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

        // mark as finished
        document.setFullyFinished();
        return new DocumentDoubleEntry<>(dbServiceSQL.saveEntityInCompany(document), doubleEntry);
    }

    public DocumentDoubleEntry<EmployeeOperationSql> recall(EmployeeOperationSql document) {
        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) new DocumentDoubleEntry<>(document);

        final long id = document.getId();

        Validators.checkValid(document.getEmployee(), "employee", document.toString(), auth.getLanguage());
        Validators.checkNotNull(document.getExchange(), "No currency exchange info in {0}", document);

        // recall debt balance records
        if (BooleanUtils.isNotTrue(document.getNoDebt())) {
            if (Validators.isValid(document.getCounterparty())) {
                document = debtService.recallDebt(document);
            }
        }

        // money balance
        String currency = document.getCurrency();

        document = (EmployeeOperationSql) moneyAccountService.recallOperation(currency, document,
                AccountType.EMPLOYEE, EmployeeSql.class, false);

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();
        return new DocumentDoubleEntry<>(dbServiceSQL.saveEntityInCompany(document), doubleEntry);
    }

    public EmployeeOpeningBalanceSql finish(EmployeeOpeningBalanceSql document) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return document;

        if (document.getEmployees() != null) {
            for (EmployeeOpeningBalanceEmployeeSql accountBalance : document.getEmployees()) {
                EmployeeSql employee = dbServiceSQL.getById(EmployeeSql.class,
                        Validators.checkNotNull(accountBalance.getAccountId(), "No employee id"));
                document = moneyAccountService.finishOpeningBalance(document, AccountType.EMPLOYEE,
                        employee, accountBalance.getAmount(), accountBalance.getBaseAmount(), accountBalance.getExchange());
            }
        }

        // mark as finished
        document.setFullyFinished();

        return document;
    }

    public DocumentDoubleEntry<BankOperationSql> finish(BankOperationSql document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getBankAccount(), "bank account", document.toString(), auth.getLanguage());

        BankAccountSql bankAccount = dbServiceSQL.getById(BankAccountSql.class, document.getBankAccount().getId());

        String currency = document.getCurrency();

        // check if everything for double-entry registration is ok
        glOperationsService.checkBankOperation(bankAccount.getMoneyAccount(), bankOperationSqlMapper.toDto(document));

        // generate debt balance records
        if (BooleanUtils.isNotTrue(document.getNoDebt())) {
            if (Validators.isValid(document.getCounterparty())) {
                // debt
                document = debtService.finishDebt(document);
            } else if (Validators.isValid(document.getEmployee())) {
                // money balance, history - employee
                document = (BankOperationSql) moneyAccountService.finishOperation(currency, document,
                        AccountType.EMPLOYEE, EmployeeSql.class, true);
            }
        }

        // money balance - bank account
        document = (BankOperationSql) moneyAccountService.finishOperation(currency, document,
                AccountType.BANK, BankAccountSql.class, false);

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishBankOperation(bankAccount.getMoneyAccount(),
                bankOperationSqlMapper.toDto(document), null, finishGL);

        document.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

        // mark as finished
        document.setFullyFinished();
        return new DocumentDoubleEntry<>(dbServiceSQL.saveEntityInCompany(document), doubleEntry);
    }

    public DocumentDoubleEntry<BankOperationSql> recall(BankOperationSql document) {
        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) return new DocumentDoubleEntry<>(document);

        final long id = document.getId();

        Validators.checkValid(document.getBankAccount(), "bank account", document.toString(), auth.getLanguage());

        String currency = document.getCurrency();

        // recall debt balance records
        if (BooleanUtils.isNotTrue(document.getNoDebt())) {
            if (Validators.isValid(document.getCounterparty())) {
                // debt - counterparty
                document = debtService.recallDebt(document);
            } else if (Validators.isValid(document.getEmployee())) {
                // money balance - employee
                document = (BankOperationSql) moneyAccountService.recallOperation(currency, document,
                        AccountType.EMPLOYEE, EmployeeSql.class, true);
            }
        }

        // recall money balance - bank account
        document = (BankOperationSql) moneyAccountService.recallOperation(currency, document,
                AccountType.BANK, BankAccountSql.class, false);

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);

        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();
        return new DocumentDoubleEntry<>(dbServiceSQL.saveEntityInCompany(document), doubleEntry);
    }

    public BankOpeningBalanceSql finish(BankOpeningBalanceSql document) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return document;

        if (document.getBankAccounts() != null) {
            for (final BankOpeningBalanceBankSql accountBalance : document.getBankAccounts()) {
                 moneyAccountService.finishOpeningBalance(document, AccountType.BANK, accountBalance.getBankAccount(),
                         accountBalance.getAmount(), accountBalance.getBaseAmount(), accountBalance.getExchange());
            }
        }

        // mark as finished
        document.setFullyFinished();

        return document;
    }

    public DocumentDoubleEntry<CashOperationSql> finish(CashOperationSql document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getCash(), "cash", document.toString(), auth.getLanguage());

        CashSql cash = dbServiceSQL.getById(CashSql.class, document.getCash().getId());

        String currency = document.getCurrency();

        // check if everything for double-entry registration is ok
        glOperationsService.checkCashOperation(cash.getMoneyAccount(), cashOperationSqlMapper.toDto(document));

        // generate debt balance records
        if (BooleanUtils.isNotTrue(document.getNoDebt())) {
            if (Validators.isValid(document.getCounterparty())) {
                // debt
                document = debtService.finishDebt(document);
            } else if (Validators.isValid(document.getEmployee())) {
                // money balance - employee
                document = (CashOperationSql) moneyAccountService.finishOperation(currency, document,
                        AccountType.EMPLOYEE, EmployeeSql.class, true);
            }
        }

        // money balance - cash
        document = (CashOperationSql) moneyAccountService.finishOperation(currency, document,
                AccountType.CASH, CashSql.class, false);

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishCashOperation(cash.getMoneyAccount(),
                cashOperationSqlMapper.toDto(document), null, finishGL);

        document.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

        // mark as finished
        document.setFullyFinished();
        return new DocumentDoubleEntry<>(dbServiceSQL.saveEntityInCompany(document), doubleEntry);
    }

    public DocumentDoubleEntry<CashOperationSql> recall(CashOperationSql document) {
        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) return new DocumentDoubleEntry<>(document);

        final long id = document.getId();

        Validators.checkValid(document.getCash(), "cash", document.toString(), auth.getLanguage());

        String currency = document.getCurrency();

        // recall debt balance records
        if (BooleanUtils.isNotTrue(document.getNoDebt())) {
            if (Validators.isValid(document.getCounterparty())) {
                // debt - counterparty
                document = debtService.recallDebt(document);
            } else if (Validators.isValid(document.getEmployee())) {
                // money balance - employee
                document = (CashOperationSql) moneyAccountService.recallOperation(currency, document,
                        AccountType.EMPLOYEE, EmployeeSql.class, true);
            }
        }

        // recall money balance - cash
        document = (CashOperationSql) moneyAccountService.recallOperation(currency, document,
                AccountType.CASH, CashSql.class, false);

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();
        return new DocumentDoubleEntry<>(dbServiceSQL.saveEntityInCompany(document), doubleEntry);
    }

    public CashOpeningBalanceSql finish(CashOpeningBalanceSql document) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return document;

        //final LocalDate calcDate = document.getDate().plusDays(1);
        if (document.getCashes() != null) {
            for (final CashOpeningBalanceCashSql accountBalance : document.getCashes()) {
                CashSql cash = dbServiceSQL.getByIdOrForeignId(CashSql.class,
                        Validators.checkNotNull(accountBalance.getAccountId(), "No cash id"),
                        accountBalance.getCash().getDb());
                moneyAccountService.finishOpeningBalance(document, AccountType.CASH,
                        cash, accountBalance.getAmount(), accountBalance.getBaseAmount(), accountBalance.getExchange());
            }
        }

        // mark as finished
        document.setFullyFinished();

        return document;
    }

    public DocumentDoubleEntry<InvoiceSql> finishSQL(InvoiceSql document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getCounterparty(), "counterparty", document.toString(), auth.getLanguage());
        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());
        }
        Validators.checkNotNull(document.getExchange(), "No currency exchange info in {0}", document);

        inventoryCheckService.checkPartUuids(document.getParts(), true);
        inventoryCheckService.checkPartLinkUuidsEntity(document.getParts(), InvoicePartSql::new);

        // Check if enough inventory (not in transaction).
        inventoryCheckService.checkInventorySQL(document, document.getParts(),
                new WarehouseTagged(document.getWarehouse(), document.getTag()));

        // check if everything for double-entry registration is ok
        glOperationsService.checkInvoice(invoiceSqlMapper.toDto(document));

        // generate inventory / warehouse records, calculate cost
        document = inventoryService.finishInvoiceSQL(document);

        // generate debt balance records
        document = debtService.finishDebt(document);

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishInvoice(invoiceSqlMapper.toDto(document), null, finishGL);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as finished
        document.setFullyFinished();

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public DocumentDoubleEntry<InvoiceSql> recallSQL(InvoiceSql document) {
        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getCounterparty(), "counterparty", document.toString(), auth.getLanguage());
        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());
        }

        final long id = document.getId();

        // Check if enough inventory (not in transaction).
        inventoryCheckService.checkInventoryRecallSQL(document, document.getParts(),
                new WarehouseTagged(document.getWarehouse(), document.getTag()));

        // recall inventory
        document = inventoryService.recallInvoiceSQL(document);

        // recall debt
        document = debtService.recallDebt(document);

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.DATASTORE, true, false, false);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public DocumentDoubleEntry<PurchaseSql> finishSQL(PurchaseSql document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getCounterparty(), "counterparty", document.toString(), auth.getLanguage());
        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());
        }
        Validators.checkNotNull(document.getExchange(), "No currency exchange info in {0}", document);

        inventoryCheckService.checkPartUuids(document.getParts(), false);

        // Check if enough inventory (not in transaction).
        inventoryCheckService.checkInventorySQL(document, document.getParts(),
                new WarehouseTagged(document.getWarehouse(), document.getTag()));

        // check if everything for double-entry registration is ok
        glOperationsService.checkPurchase(purchaseSqlMapper.toDto(document));

        // generate inventory / warehouse records (and calculate cost)
        document = inventoryService.finishPurchaseSQL(document);

        // generate debt balance records
        document = debtService.finishDebt(document);

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishPurchase(purchaseSqlMapper.toDto(document), null, finishGL);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as finished
        document.setFullyFinished();

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public DocumentDoubleEntry<PurchaseSql> recallSQL(PurchaseSql document) {
        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getCounterparty(), "counterparty", document.toString(), auth.getLanguage());
        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());
        }

        final long id = document.getId();

        // Check if enough inventory (not in transaction).
        inventoryCheckService.checkInventoryRecallSQL(document, document.getParts(), new WarehouseTagged(document.getWarehouse(), document.getTag()));

        // recall inventory
        document = inventoryService.recallPurchaseSQL(document);

        // recall debt
        document = debtService.recallDebt(document);

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public InventoryOpeningBalanceSql finishSQL(InventoryOpeningBalanceSql document) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return document;

        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());
        }

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        String code = companySettings.getCurrency().getCode();

        if (CollectionsHelper.hasValue(document.getParts())) {
            for (InventoryOpeningBalancePartSql part : document.getParts()) {
                Validators.checkArgument(Validators.isValid(part),
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NoPartIdIn, auth.getLanguage()), part));
                Validators.checkArgument(part.getCostTotal() != null &&
                                part.getCostTotal().getCurrency().equals(code),
                        "No base currency found - %s", part);
            }
        }

        inventoryCheckService.checkPartUuids(document.getParts(), false);

        // generate inventory / warehouse records (and calculate cost)
        document = inventoryService.finishOpeningBalanceSQL(document);

        // mark as finished
        document.setFullyFinished();

        // need save there - used in link finish
        return dbServiceSQL.saveEntityInCompany(document);
    }

    public DoubleEntrySql finish(DoubleEntrySql document) {
        return glUtilsService.updateFinishedGL(document, true);
    }


    public GLOpeningBalanceSql finish(GLOpeningBalanceSql document) {
        if (BooleanUtils.isNotTrue(document.getFinishedGL())) {
            document.setFinishedGL(true);
            dbServiceSQL.saveEntityInCompany(document);
        }
        return document;
    }

    public DocumentDoubleEntry<TransProdSql> finishSQL(TransProdSql document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        // check if fully or partially reserved already
        Validators.checkArgument(BooleanUtils.isNotTrue(document.getReserved()) && BooleanUtils.isNotTrue(document.getReservedParts()),
                TranslationService.getInstance().translate(TranslationService.INVENTORY.ReservedNoFinish, auth.getLanguage()));

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseTo(), "warehouse to/production", document.toString(), auth.getLanguage());

        inventoryCheckService.checkPartUuids(document.getPartsFrom(), false);
        inventoryCheckService.checkPartUuids(document.getPartsTo(), false);

        // Check if enough inventory (not in transaction).
        inventoryCheckService.checkInventorySQL(document, document.getPartsFrom(),
                new WarehouseTagged(document.getWarehouseFrom(), document.getTagFrom()));

        // check if everything for double-entry registration is ok
        glOperationsService.checkTransProd(transportationSqlMapper.toDto(document));

        // generate inventory / warehouse records (and calculate cost)
        document = inventoryService.finishTransProdSQL(document);

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishTransProd(transportationSqlMapper.toDto(document), null, finishGL);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as finished
        document.setFullyFinished();

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public DocumentDoubleEntry<TransProdSql> recallSQL(TransProdSql document) {
        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseTo(), "warehouse to/production", document.toString(), auth.getLanguage());

        final long id = document.getId();

        // Check (not in transaction) if enough inventory
        if (CollectionsHelper.hasValue(document.getPartsTo())) {
            inventoryCheckService.checkInventoryRecallSQL(document, document.getPartsTo(),
                    new WarehouseTagged(document.getWarehouseTo(), document.getTagTo()));
        } else {
            inventoryCheckService.checkInventoryRecallSQL(document, document.getPartsFrom(),
                    new WarehouseTagged(document.getWarehouseTo(), document.getTagTo()));
        }

        // recall inventory
        document = inventoryService.recallTransProdSQL(document);

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public TransProdSql reserveSQL(TransProdSql document) {
        if (BooleanUtils.isTrue(document.getFinishedPartsFrom())) return document;

        if (BooleanUtils.isTrue(document.getReservedParts())) {
            if (BooleanUtils.isNotTrue(document.getReserved())) {
                document.setReserved(true);
            }
            return document;
        }

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseReserved(), "warehouse for reservations", document.toString(), auth.getLanguage());

        // generate inventory / warehouse records and order documents if needed
        document = inventoryService.reserveTransProdSQL(document);

        // generate order documents
        document = inventoryService.genOrdersFromTransProdSQL(document);

        return document;
    }

    public TransProdSql recallReserveSQL(TransProdSql document) {
        // check if finished already - do nothing - need to recall before
        if (BooleanUtils.isTrue(document.getFinished())) return document;

        // check if not reserved already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getReserved())) return document;

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseReserved(), "warehouse for reservations", document.toString(), auth.getLanguage());

        //recall reservation
        document = inventoryService.recallReserveTransProdSQL(document);

        // mark as unreserved
        document.setReserved(null);
        document.setReservedParts(null);
        if (document.getPartsFrom() != null) document.getPartsFrom().forEach(p -> p.setReserved(false));

        return document;
    }

    public DocumentDoubleEntry<InventorySql> finishSQL(InventorySql document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

        inventoryCheckService.checkPartUuids(document.getParts(), false);

        // Check if enough inventory (not in transaction).
        inventoryCheckService.checkInventorySQL(document, document.getParts(),
                new WarehouseTagged(document.getWarehouse().getId(), document.getWarehouse().getName()));

        // check if everything for double-entry registration is ok
        glOperationsService.checkInventory(inventorySqlMapper.toDto(document));

        // generate inventory / warehouse records (and calculate cost)
        document = inventoryService.finishInventorySQL(document);

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishInventory(inventorySqlMapper.toDto(document), null, finishGL);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as finished
        document.setFullyFinished();

        // need save there - used in link finish
        return new DocumentDoubleEntry<>(dbServiceSQL.saveEntityInCompany(document), doubleEntry);
    }

    public DocumentDoubleEntry<InventorySql> recallSQL(InventorySql document) {
        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) return new DocumentDoubleEntry<>(document);

        Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

        final long id = document.getId();

        // Check if enough inventory (not in transaction).
        inventoryCheckService.checkInventoryRecallSQL(document, document.getParts(),
                new WarehouseTagged(document.getWarehouse(), document.getTag()));

        // recall inventory
        document = inventoryService.recallInventorySQL(document);

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public EstimateSql finishSQL(EstimateSql document) {
        document.setFinished(true);
        inventoryCheckService.checkPartUuids(document.getParts(), false);
        inventoryCheckService.checkPartLinkUuidsEntity(document.getParts(), EstimatePartSql::new);
        return dbServiceSQL.saveEntityInCompany(document);
    }

    private <E extends IFinished> E findUnfinished(Collection<E> balances) {
        if (balances != null) {
            for (E balance : balances) {
                if (BooleanUtils.isNotTrue(balance.getFinished())) return balance;
            }
        }
        return null;
    }

    private <E extends IFinished> E findFinished(Collection<E> balances) {
        if (balances != null) {
            for (E balance : balances) {
                if (BooleanUtils.isTrue(balance.getFinished())) return balance;
            }
        }
        return null;
    }

    public DocumentDoubleEntry<BankRateInfluenceSql> finish(BankRateInfluenceSql document, Boolean finishGL) {
        return finish(AccountType.BANK, BankAccountSql.class, document, finishGL);
    }

    public DocumentDoubleEntry<BankRateInfluenceSql> recall(BankRateInfluenceSql document) {
        return recall(AccountType.BANK, document);
    }

    public DocumentDoubleEntry<CashRateInfluenceSql> finish(CashRateInfluenceSql document, Boolean finishGL) {
        return finish(AccountType.CASH, CashSql.class, document, finishGL);
    }

    public DocumentDoubleEntry<CashRateInfluenceSql> recall(CashRateInfluenceSql document) {
        return recall(AccountType.CASH, document);
    }

    public DocumentDoubleEntry<EmployeeRateInfluenceSql> finish(EmployeeRateInfluenceSql document, Boolean finishGL) {
        return finish(AccountType.EMPLOYEE, EmployeeSql.class, document, finishGL);
    }

    public DocumentDoubleEntry<EmployeeRateInfluenceSql> recall(EmployeeRateInfluenceSql document) {
        return recall(AccountType.EMPLOYEE, document);
    }

    private <E extends BaseMoneyRateInfluenceSql, D extends BaseMoneyRateInfluenceDto>
    DocumentDoubleEntry<E> finish(AccountType accountType, Class<? extends IMoneyAccount<?>> accountClass,
                                  E document, Boolean finishGL) {
        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return new DocumentDoubleEntry<>(document);

        // check if everything for double-entry registration is ok
        @SuppressWarnings("unchecked")
        IBaseMapper<D, E> mapper = (IBaseMapper<D, E>) docsMappersService.getOrNull(document.getClass());
        glOperationsService.checkMoneyRateInfluence(accountClass, mapper.toDto(document));

        if (document.getAccounts() != null) {
            while (findUnfinished(document.getAccounts()) != null) {

                BaseMoneyBalanceSql balance = findUnfinished(document.getAccounts());
                if (balance == null) {
                    document.setFinished(true);
                    break;
                }

                String currency = balance.getExchange().getCurrency();
                // noinspection unchecked
                document = (E) moneyAccountService.finishRateInfluence(currency, document, balance, accountType);
            }
        }

        // generate G.L operations
        DoubleEntrySql doubleEntry = glOperationsService.finishMoneyRateInfluence(accountClass,
                mapper.toDto(document), document.getDoubleEntry(), finishGL);

        document.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

        // mark as finished
        document.setFullyFinished();
        document = dbServiceSQL.saveEntityInCompany(document);

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    private <E extends BaseMoneyRateInfluenceSql>
    DocumentDoubleEntry<E> recall(AccountType accountType, E document) {
        // check if not finished already - prevent run the same transaction second time
        if (findFinished(document.getAccounts()) == null) return new DocumentDoubleEntry<>(document);

        final long id = document.getId();

        if (document.getAccounts() != null) {
            while(findFinished(document.getAccounts()) != null) {
                BaseMoneyBalanceSql balance = findFinished(document.getAccounts());

                if (balance == null) {
                    document.setFinished(false);
                    break;
                }

                String currency = balance.getExchange().getCurrency();
                // noinspection unchecked
                document = (E) moneyAccountService.recallRateInfluence(currency, document, balance, accountType);
            }
        }

        // recall G.L operations
        DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);
        document.setFinishedGL(doubleEntry.getFinishedGL());

        // mark as unfinished
        document.clearFullyFinished();
        document = dbServiceSQL.saveEntityInCompany(document);

        return new DocumentDoubleEntry<>(document, doubleEntry);
    }

    public <E extends IBaseDocument, D extends IBaseDocument> D getDocument(Class<E> clazz, long id, DBType db) {
        return getDocument(clazz, null, id, db);
    }

    public <D extends IBaseDocument> D getDocument(String documentType, long id, DBType db) {
        return getDocument(null, documentType, id, db);
    }

    private <E extends IBaseDocument, D extends IBaseDocument> D getDocument(Class<E> clazz, String documentType, long id, DBType db) {
        Class<? extends IBaseDocument> docClass = clazz == null && StringHelper.hasValue(documentType)
                ? docsMappersService.getDocumentClass(db, documentType)
                : clazz;
        Validators.checkNotNull(docClass, "Unknown document type");
        if (BaseDocumentSql.class.isAssignableFrom(docClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends BaseDocumentSql> clazzSql = (Class<? extends BaseDocumentSql>) docClass;
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                BaseDocumentSql entity;
                //noinspection unchecked
                entity = dbServiceSQL.getByIdOrForeignId((Class<BaseDocumentSql>) docClass, id, db, docsMappersService.getGraphName(clazzSql));
                if (entity != null) {
                    @SuppressWarnings("unchecked")
                    IBaseMapper<D, BaseDocumentSql> mapper = (IBaseMapper<D, BaseDocumentSql>) docsMappersService.getOrNull(entity.getClass());
                    D result = mapper.toDto(entity);
                    result.setDoubleEntry(doubleEntrySqlMapper.toDto(glUtilsService.getDoubleEntryByParentId(id)));
                    return result;
                }
                return null;
            });
        }
        throw new GamaException("Invalid or no document class " + docClass);
    }

    public <D extends IBaseDocument> D getDocumentSql(Class<? extends BaseDocumentSql> clazz, long id, String graphName) {
        BaseDocumentSql entity = dbServiceSQL.getById(clazz, id, graphName);
        if (entity != null) {
            @SuppressWarnings("unchecked")
            IBaseMapper<D, BaseDocumentSql> mapper = (IBaseMapper<D, BaseDocumentSql>) docsMappersService.getOrNull(entity.getClass());
            D document = mapper.toDto(entity);
            document.setDoubleEntry(doubleEntrySqlMapper.toDto(glUtilsService.getDoubleEntryByParentId(id)));
            return document;
        }
        return null;
    }

    public void deleteDocument(long id) {
        deleteDocument(id, null);
    }

    public void deleteDocument(long id, DBType db) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            BaseDocumentSql document = dbServiceSQL.getById(BaseDocumentSql.class, id);
            if (document == null) {
                log.error(this.getClass().getSimpleName() + ": Document not found, id=" + id + ", companyId=" + auth.getCompanyId());
                throw new GamaNotFoundException(MessageFormat.format(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentWithId, auth.getLanguage()), id));
            }

            if (BooleanUtils.isNotTrue(document.getFinished())) {
                DoubleEntrySql doubleEntry = glUtilsService.getDoubleEntryByParentId(id);
                if (doubleEntry != null) {
                    if (doubleEntry.getCompanyId() != auth.getCompanyId()) {
                        throw new GamaUnauthorizedException();
                    }
                    if (!BooleanUtils.isTrue(doubleEntry.getArchive())) {
                        doubleEntry.setArchive(true);
                    }
                }
                document.setArchive(true);

            } else {
                throw new GamaException("Document is finished or finishing in progress");
            }
        });
    }

    public void generatePrintForm(BaseDocumentDto document) {
        generatePrintForm(document, null);
    }

    public void generatePrintForm(BaseDocumentDto document, String subtype) {
        Validators.checkNotNull(document.getUuid(), "No document uuid");
        String outputFileName = String.format("%s-%s.pdf", document.getNumber(), document.getDate().toString());
        String filePath = storageService.getFilePath(document.getCompanyId(), DOCS_PRINT_FOLDER, templateService.filename(document.getUuid(), subtype, false, null));
        try (WriteChannel wc = storageService.gcsFileWriteChannel(filePath, APPLICATION_PDF_VALUE, true, outputFileName)) {
            if (wc != null) {
                try (OutputStream os = Channels.newOutputStream(wc)) {
                    templateService.generateDocument(document, os, subtype);
                }
                log.info(this.getClass().getSimpleName() + ": Document=" + document.getClass().getSimpleName() + ", filePath=" + filePath + " - form generated");
            } else {
                log.info(this.getClass().getSimpleName() + ": Document=" + document.getClass().getSimpleName() + ", filePath=" + filePath + " - no form");
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": IO Error - filePath=" + filePath + ", error=" + e);

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public APIResult<TaskResponse<Object>> getTaskStatus(String taskId) {
        if (StringHelper.isEmpty(taskId)) return APIResult.Error("No task Id");

        String content = storageService.getContent(ConstWorkers.TASKS_FOLDER, taskId);
        log.info(this.getClass().getSimpleName() + ": taskId='" + taskId + "', content='" + content + '\'');

        if (StringHelper.isEmpty(content)) return APIResult.Data(TaskResponse.status(ProcessingStatusType.RUNNING));

        try {
            return objectMapper.readValue(content, new TypeReference<>() {});

        } catch (Exception e) {
            log.info(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return APIResult.Error(e.getMessage());
        }
    }
}
