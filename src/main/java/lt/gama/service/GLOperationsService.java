package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.dto.base.BaseMoneyBalanceDto;
import lt.gama.model.dto.base.BaseMoneyRateInfluenceDto;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.dto.documents.items.PartInvoiceDto;
import lt.gama.model.dto.entities.*;
import lt.gama.model.i.IGLOperation;
import lt.gama.model.i.IId;
import lt.gama.model.i.IMoneyAccount;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseMoneyAccountSql;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.SalarySql;
import lt.gama.model.sql.documents.SalarySql_;
import lt.gama.model.sql.documents.items.EmployeeChargeSql;
import lt.gama.model.sql.documents.items.EmployeeChargeSql_;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLCurrencyAccount;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLMoneyAccount;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.inventory.VATCodeTotal;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.tasks.UpdateVatCodeGLAccountsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gama
 * Created by valdas on 15-04-28.
 */
@Service
public class GLOperationsService {

    private static final Logger log = LoggerFactory.getLogger(GLOperationsService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final GLGenService glGenService;
    private final GLUtilsService glUtilsService;
    private final DBServiceSQL dbServiceSQL;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final BankAccountSqlMapper bankAccountSqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final Auth auth;
    private final PartSqlMapper partSqlMapper;
    private final TaskQueueService taskQueueService;


    public GLOperationsService(GLGenService glGenService,
                               GLUtilsService glUtilsService,
                               DBServiceSQL dbServiceSQL,
                               DoubleEntrySqlMapper doubleEntrySqlMapper,
                               CounterpartySqlMapper counterpartySqlMapper,
                               BankAccountSqlMapper bankAccountSqlMapper,
                               EmployeeSqlMapper employeeSqlMapper,
                               Auth auth,
                               PartSqlMapper partSqlMapper,
                               TaskQueueService taskQueueService) {
        this.glGenService = glGenService;
        this.glUtilsService = glUtilsService;
        this.dbServiceSQL = dbServiceSQL;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.bankAccountSqlMapper = bankAccountSqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.auth = auth;
        this.partSqlMapper = partSqlMapper;
        this.taskQueueService = taskQueueService;
    }

    private DoubleEntrySql getDoubleEntry(DoubleEntryDto documentDoubleEntry, IBaseDocument document) {
        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));

        if (documentDoubleEntry == null && document.getDoubleEntry() == null) {
            return glUtilsService.getDoubleEntryByParentId(document.getId());
        }

        DoubleEntrySql doubleEntry = doubleEntrySqlMapper.toEntity(documentDoubleEntry != null ? documentDoubleEntry : document.getDoubleEntry());
        if (BooleanUtils.isNotTrue(doubleEntry.getFrozen())) {
            return doubleEntry;
        }

        doubleEntry.setParentNumber(document.getNumber());
        if (Validators.isValid(document.getCounterparty())) {
            doubleEntry.setParentCounterparty(entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()));
        }
        doubleEntry.setParentType(document.getDocumentType());
        doubleEntry.setParentId(document.getId());
        doubleEntry.setCompanyId(document.getCompanyId());
        if (doubleEntry.getDate() == null) {
            doubleEntry.setDate(document.getDate());
        }
        return doubleEntry;
    }

    private CounterpartyDto getCounterparty(IBaseDocument document) {
        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
        return Validators.checkNotNull(
                counterpartySqlMapper.toDto(
                        dbServiceSQL.getByIdOrForeignId(CounterpartySql.class, document.getCounterparty().getId(),
                                document.getCounterparty().getDb())),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyWithId, auth.getLanguage()),
                        document.getCounterparty().getId()));
    }

    private DoubleEntrySql baseOperations(final IBaseDocument document,
                                          final DoubleEntryDto documentDoubleEntry,
                                          final boolean checkOnly,
                                          final Boolean finishGL,
                                          final Function<DoubleEntrySql, DoubleEntrySql> function) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DoubleEntrySql doubleEntry = getDoubleEntry(documentDoubleEntry, document);
            if (doubleEntry != null) {
                boolean finishedGL = BooleanUtils.isTrue(doubleEntry.getFinishedGL());
                boolean frozen = BooleanUtils.isTrue(doubleEntry.getFrozen());
                if (finishedGL || frozen) {
                    if (frozen) {
                        doubleEntry.setDate(document.getDate());
                        if (!checkOnly && BooleanUtils.isTrue(finishGL)) {
                            if (StringHelper.isEmpty(doubleEntry.getContent())) {
                                doubleEntry.setContent(document.makeContent(companySettings));
                            }
                            doubleEntry = glUtilsService.updateFinishedGL(doubleEntry, true);
                        } else {
                            doubleEntry = dbServiceSQL.saveWithCounter(doubleEntry);
                        }
                    }
                    document.setDoubleEntry(doubleEntrySqlMapper.toDto(doubleEntry));
                    return doubleEntry;
                }
            }
            doubleEntry = function.apply(doubleEntry);
            return doubleEntry == null ? null : checkOnly ? doubleEntry : glUtilsService.saveDoubleEntry(doubleEntry);
        });
    }

    private DoubleEntrySql finishDocument(final IBaseDocument document,
                                          final List<GLOperationDto> operations,
                                          final Boolean finishGL) {
        if (document == null || operations == null) return null;
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DoubleEntrySql entity = glUtilsService.getDoubleEntryByParentId(document.getId());
            if (entity != null && BooleanUtils.isTrue(entity.getFinishedGL())) {
                log.error(MessageFormat.format("Operation {0} {1} is finished already",
                        DoubleEntrySql.class.getSimpleName(), entity.getId()));
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
            }

            entity = initDeData(finishGL, document, entity, operations);
            entity = glUtilsService.updateFinishedGL(entity, BooleanUtils.isTrue(finishGL));
            return entity;
        });
    }

    private void genPartAccounts(final Collection<? extends BaseDocPartDto> docParts,
                                 final Map<Long, GLOperationAccount> assets,
                                 final Map<Long, GLDC> expense,
                                 final Map<Long, GLDC> income,
                                 final Map<Long, GLOperationAccount> vatRec,
                                 final Map<Long, GLOperationAccount> vatPay) {
        Map<Long, List<BaseDocPartDto>> partMap = docParts.stream()
                .flatMap(part -> {
                    if (!(part instanceof PartInvoiceDto)) return Stream.of(part);
                    return Stream.concat(Stream.of(part), CollectionsHelper.streamOf(((PartInvoiceDto) part).getParts()));
                })
                .collect(Collectors.groupingBy(BaseDocPartDto::getId));

        Collection<PartDto> parts = dbServiceSQL.queryByIds(PartSql.class, null, partMap.keySet()).getResultList().stream().map(partSqlMapper::toDto).toList();

        for (PartDto part : parts) {
            // update G.L. accounts in document parts
            partMap.get(part.getId()).forEach(docPart -> {
                docPart.setAccountAsset(part.getAccountAsset());
                docPart.setGlIncome(part.getGlIncome());
                docPart.setGlExpense(part.getGlExpense());
                docPart.setType(part.getType());
            });

            PartType partType = Validators.checkNotNull(part.getType(), MessageFormat.format(TranslationService.getInstance().translate(TranslationService.GL.NoPartType, auth.getLanguage()), part.toMessage()));
            if (income != null) {
                GLDC accountIncome = part.getGlIncome();
                Validators.checkArgument(Validators.isPartialValid(accountIncome), MessageFormat.format(TranslationService.getInstance().translate(TranslationService.GL.NoPartIncomeAcc, auth.getLanguage()), part.toMessage()));
                income.put(part.getId(), accountIncome);
            }
            if (partType == PartType.PRODUCT || partType == PartType.PRODUCT_SN) {
                if (assets != null) {
                    GLOperationAccount accountAsset = part.getAccountAsset();
                    Validators.checkArgument(Validators.isValid(accountAsset), MessageFormat.format(TranslationService.getInstance().translate(TranslationService.GL.NoPartAssetAcc, auth.getLanguage()), part.toMessage()));
                    assets.put(part.getId(), accountAsset);
                }
            }
            if (expense != null) {
                GLDC accountExpense = part.getGlExpense();
                Validators.checkArgument(Validators.isPartialValid(accountExpense), MessageFormat.format(TranslationService.getInstance().translate(TranslationService.GL.NoPartExpenseAcc, auth.getLanguage()), part.toMessage()));
                expense.put(part.getId(), accountExpense);
            }
            if (vatRec != null) {
                GLOperationAccount accountVAT = part.getGlVATRec();
                if (Validators.isValid(accountVAT)) vatRec.put(part.getId(), accountVAT);
            }
            if (vatPay != null) {
                GLOperationAccount accountVAT = part.getGlVATPay();
                if (Validators.isValid(accountVAT)) vatPay.put(part.getId(), accountVAT);
            }
        }
    }

    private DoubleEntrySql initDeData(Boolean finishGL, IBaseDocument document, DoubleEntrySql doubleEntry, List<GLOperationDto> operations) {
        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));

        if (!Validators.isValid(doubleEntry)) {
            doubleEntry = new DoubleEntrySql();
            doubleEntry.setCompanyId(document.getCompanyId());
            doubleEntry.setAutoNumber(true);
        }

        doubleEntry.setParentType(document.getDocumentType());
        doubleEntry.setParentId(document.getId());
        doubleEntry.setParentDb(document.getDb());
        doubleEntry.setContent(document.makeContent(Validators.checkNotNull(auth.getSettings(), "No company settings")));
        doubleEntry.setDate(document.getDate());
        doubleEntry.setFrozen(null);
        doubleEntry.setParentNumber(document.getNumber());
        if (Validators.isValid(document.getCounterparty())) {
            doubleEntry.setParentCounterparty(entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()));
        }

        doubleEntry.setFinishedGL(finishGL);
        glUtilsService.mergeGLOperation(doubleEntry, operations);

        return doubleEntry;
    }

    public void checkPurchase(PurchaseDto document) {
        purchase(document, null, true, false);
    }

    public DoubleEntrySql finishPurchase(PurchaseDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return purchase(document, documentDoubleEntry, false, finishGL);
    }

    private DoubleEntrySql purchase(PurchaseDto document, DoubleEntryDto documentDoubleEntry, boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (CollectionsHelper.isEmpty(document.getParts())) {
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {
                Validators.checkNotNull(document.getCounterparty(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoVendor, auth.getLanguage()));
                CounterpartyDto counterparty = getCounterparty(document);

                GLOperationAccount accountCounterparty = BooleanUtils.isTrue(document.getNoDebt()) ?
                        counterparty.getNoDebtAccount() : counterparty.getAccount(DebtType.VENDOR);
                Validators.checkArgument(Validators.isValid(accountCounterparty),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoVendorAcc, auth.getLanguage()),
                                counterparty.getName()));

                GLOperationAccount accountVATRec = companySettings.getGl().getAccVATRec();
                GLOperationAccount accountVATPay = companySettings.getGl().getAccVATPay();

                if (companySettings.isVatPayer()) {
                    Validators.checkArgument(Validators.isValid(accountVATRec), TranslationService.getInstance().translate(TranslationService.GL.NoVATRecAcc, auth.getLanguage()));
                    Validators.checkArgument(Validators.isValid(accountVATPay), TranslationService.getInstance().translate(TranslationService.GL.NoVATPayAcc, auth.getLanguage()));
                }

                GLOperationAccount accountCurrRateNeg = companySettings.getGl().getAccCurrRateNeg();
                GLOperationAccount accountCurrRatePos = companySettings.getGl().getAccCurrRatePos();

                if (GamaMoneyUtils.isNegative(document.getBaseTotal()) && !Objects.equals(document.getExchange().getBase(), document.getExchange().getCurrency())) {
                    Validators.checkArgument(Validators.isValid(accountCurrRateNeg), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRateNegAcc, auth.getLanguage()));
                    Validators.checkArgument(Validators.isValid(accountCurrRatePos), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRatePosAcc, auth.getLanguage()));
                }

                GLOperationAccount accountTemp = companySettings.getGl().getAccTemp();
                if (GamaMoneyUtils.isNegative(document.getBaseSubtotal())) {
                    Validators.checkArgument(Validators.isValid(accountTemp), TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));
                }

                Map<Long, GLOperationAccount> accountsAsset = new HashMap<>();
                Map<Long, GLDC> accountsExpense = new HashMap<>();
                Map<Long, GLOperationAccount> accountsVATRec = new HashMap<>();
                Map<Long, GLOperationAccount> accountsVATPay = new HashMap<>();

                genPartAccounts(document.getParts(), accountsAsset, accountsExpense, null, accountsVATRec, accountsVATPay);

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry,
                            accountCounterparty, accountVATRec, accountVATPay, accountsAsset, accountsExpense, accountsVATRec, accountsVATPay,
                            companySettings.getGl().getAccPurchaseExpense(), accountCurrRateNeg, accountCurrRatePos, accountTemp);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public void checkInvoice(InvoiceDto document) {
        invoice(document, null, true, false);
    }

    public DoubleEntrySql finishInvoice(InvoiceDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return invoice(document, documentDoubleEntry, false, finishGL);
    }

    private DoubleEntrySql invoice(InvoiceDto document, DoubleEntryDto documentDoubleEntry, boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (CollectionsHelper.isEmpty(document.getParts())) {
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {
                Validators.checkNotNull(document.getCounterparty(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCustomer, auth.getLanguage()));
                CounterpartyDto counterparty = getCounterparty(document);

                GLOperationAccount accountCounterparty = BooleanUtils.isTrue(document.getNoDebt()) ?
                        counterparty.getNoDebtAccount() : counterparty.getAccount(DebtType.CUSTOMER);
                Validators.checkArgument(Validators.isValid(accountCounterparty),
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.GL.NoCustomerAcc, auth.getLanguage()), counterparty.getName()));

                GLOperationAccount accountVAT = companySettings.getGl().getAccVATPay();
                if (companySettings.isVatPayer()) {
                    Validators.checkArgument(Validators.isValid(accountVAT), TranslationService.getInstance().translate(TranslationService.GL.NoVATPayAcc, auth.getLanguage()));
                }

                GLOperationAccount accountCurrRateNeg = companySettings.getGl().getAccCurrRateNeg();
                GLOperationAccount accountCurrRatePos = companySettings.getGl().getAccCurrRatePos();

                if (GamaMoneyUtils.isNegative(document.getBaseTotal()) && !Objects.equals(document.getExchange().getBase(), document.getExchange().getCurrency())) {
                    Validators.checkArgument(Validators.isValid(accountCurrRateNeg), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRateNegAcc, auth.getLanguage()));
                    Validators.checkArgument(Validators.isValid(accountCurrRatePos), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRatePosAcc, auth.getLanguage()));
                }

                Map<Long, GLDC> accountsIncome = new HashMap<>();
                Map<Long, GLOperationAccount> accountsAsset = new HashMap<>();
                Map<Long, GLDC> accountsExpense = new HashMap<>();
                Map<Long, GLOperationAccount> accountsVATRec = new HashMap<>();
                Map<Long, GLOperationAccount> accountsVATPay = new HashMap<>();

                genPartAccounts(document.getParts(), accountsAsset, accountsExpense, accountsIncome, accountsVATRec, accountsVATPay);

                GLOperationAccount accountTemp = companySettings.getGl().getAccTemp();
                if (BooleanUtils.isTrue(document.getEcr())) {
                    Validators.checkArgument(Validators.isValid(accountTemp), TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));
                }

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry,
                            accountCounterparty, accountVAT, accountsIncome, accountsAsset, accountsExpense,
                            accountsVATRec, accountsVATPay, accountTemp);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public void checkBankOperation(GLMoneyAccount glMoneyAccount, BankOperationDto document) {
        bankOperation(glMoneyAccount, document, null, true, false);
    }

    public DoubleEntrySql finishBankOperation(GLMoneyAccount glMoneyAccount, BankOperationDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return bankOperation(glMoneyAccount, document, documentDoubleEntry,false, finishGL);
    }

    private GLOperationAccount getCounterpartyAccount(CounterpartyDto counterparty, DebtType debtType) {
        GLOperationAccount account = counterparty.getAccount(debtType);
        if (Validators.isValid(account)) return account;

        account = counterparty.getAccount(debtType == DebtType.CUSTOMER ? DebtType.VENDOR : DebtType.CUSTOMER);
        Validators.checkArgument(Validators.isValid(account),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.GL.NoCustomerNoVendorAcc, auth.getLanguage()),
                        counterparty.getName()));
        return account;
    }

    private DoubleEntrySql bankOperation(GLMoneyAccount glMoneyAccount,
                                         BankOperationDto document, DoubleEntryDto documentDoubleEntry,
                                         boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (GamaMoneyUtils.isZero(document.getAmount())) {
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {
                GLMoneyAccount moneyAccount = Validators.checkNotNull(glMoneyAccount,
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                document.getCurrency()));
                GLCurrencyAccount accountBank = Validators.checkNotNull(moneyAccount.getGLAccount(document.getCurrency()),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                document.getCurrency()));
                Validators.checkArgument(Validators.isValid(accountBank.getAccount()),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                document.getCurrency()));

                GLOperationAccount account = null;
                boolean noDebt = BooleanUtils.isTrue(document.getNoDebt());
                if (noDebt) {
                    account = companySettings.getGl().getAccTemp();
                    Validators.checkArgument(Validators.isValid(account),
                            TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));
                }

                if (Validators.isValid(document.getCounterparty())) {
                    // counterparty
                    Validators.checkNotNull(document.getCounterparty(),
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty, auth.getLanguage()));
                    CounterpartyDto counterparty = getCounterparty(document);

                    if (noDebt) {
                        if (Validators.isValid(counterparty.getNoDebtAccount())) {
                            account = counterparty.getNoDebtAccount();
                        }

                    } else {
                        account = getCounterpartyAccount(counterparty, document.getDebtType());
                    }

                } else if (Validators.isValid(document.getEmployee())) {
                    // employee
                    EmployeeDto employee = Validators.checkNotNull(employeeSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(EmployeeSql.class,
                                    document.getEmployee().getId(), document.getEmployee().getDb())),
                            "Can't find employee {0}", document.getEmployee().getId());

                    if (noDebt) {
                        if (Validators.isValid(companySettings.getGl().getEmployeeNoDebt())) {
                            account = companySettings.getGl().getEmployeeNoDebt();
                        }

                    } else {
                        GLMoneyAccount employeeMoneyAccount = Validators.checkNotNull(employee.getMoneyAccount(),
                                MessageFormat.format(
                                        TranslationService.getInstance().translate(TranslationService.GL.NoEmployeeAdvAcc, auth.getLanguage()),
                                        employee.getName()));
                        account = Validators.checkNotNull(employeeMoneyAccount.getGLAccount(document.getAmount().getCurrency()),
                                MessageFormat.format(
                                        TranslationService.getInstance().translate(TranslationService.GL.NoEmployeeAdvAcc, auth.getLanguage()),
                                        employee.getName())).getAccount();
                        Validators.checkArgument(Validators.isValid(account), MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoEmployeeAdvAcc, auth.getLanguage()),
                                employee.getName()));
                    }

                } else if (Validators.isValid(document.getBankAccount2())) {
                    // to another account (internal money transfer)
                    BankAccountDto bankAccount2 = Validators.checkNotNull(bankAccountSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(BankAccountSql.class,
                                    document.getBankAccount2().getId(), document.getBankAccount2().getDb())),
                            "Can't find bank account {0}", document.getBankAccount2().getId());

                    GLMoneyAccount moneyAccount2 = Validators.checkNotNull(bankAccount2.getMoneyAccount(),
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                    bankAccount2.getAccount()));
                    account = moneyAccount2.getGLAccount(document.getAmount().getCurrency()).getAccount();
                    Validators.checkArgument(Validators.isValid(account), MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                            bankAccount2.getAccount()));

                } else {
                    // other operation
                    account = companySettings.getGl().getAccBankOther();
                    Validators.checkArgument(Validators.isValid(account), TranslationService.getInstance().translate(TranslationService.GL.NoBankOtherAcc, auth.getLanguage()));
                }

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry, accountBank.getAccount(), account);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public void checkCashOperation(GLMoneyAccount glMoneyAccount, CashOperationDto document) {
        cashOrder(glMoneyAccount, document, null, true, false);
    }

    public DoubleEntrySql finishCashOperation(GLMoneyAccount glMoneyAccount, CashOperationDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return cashOrder(glMoneyAccount, document, documentDoubleEntry, false, finishGL);
    }

    private DoubleEntrySql cashOrder(GLMoneyAccount glMoneyAccount, CashOperationDto document, DoubleEntryDto documentDoubleEntry, boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (GamaMoneyUtils.isZero(document.getAmount())) {
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {
                GLCurrencyAccount accountCash = Validators.checkNotNull(glMoneyAccount.getGLAccount(document.getCurrency()),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                document.getCurrency()));
                Validators.checkArgument(Validators.isValid(accountCash.getAccount()),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                document.getCurrency()));

                Validators.checkArgument(Validators.isValid(document.getCounterparty()) || Validators.isValid(document.getEmployee()), "Order must have counterparty or employee");

                GLOperationAccount account = null;
                boolean noDebt = BooleanUtils.isTrue(document.getNoDebt());
                if (noDebt) {
                    account = companySettings.getGl().getAccTemp();
                    Validators.checkArgument(Validators.isValid(account),
                            TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));
                }

                if (Validators.isValid(document.getCounterparty())) {
                    // counterparty
                    Validators.checkNotNull(document.getCounterparty(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty, auth.getLanguage()));
                    CounterpartyDto counterparty = getCounterparty(document);

                    if (noDebt) {
                        if (Validators.isValid(counterparty.getNoDebtAccount())) {
                            account = counterparty.getNoDebtAccount();
                        }

                    } else {
                        account = getCounterpartyAccount(counterparty, document.getDebtType());
                    }

                } else if (Validators.isValid(document.getEmployee())) {
                    // employee
                    EmployeeDto employee = Validators.checkNotNull(employeeSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(EmployeeSql.class,
                            document.getEmployee().getId(), document.getEmployee().getDb())),
                            "Can't find employee {0}", document.getEmployee().getId());
                    if (noDebt) {
                        if (Validators.isValid(companySettings.getGl().getEmployeeNoDebt())) {
                            account = companySettings.getGl().getEmployeeNoDebt();
                        }
                    } else {
                        GLMoneyAccount employeeMoneyAccount = Validators.checkNotNull(employee.getMoneyAccount(),
                                MessageFormat.format(
                                        TranslationService.getInstance().translate(TranslationService.GL.NoEmployeeAdvAcc, auth.getLanguage()),
                                        employee.getName()));
                        account = Validators.checkNotNull(employeeMoneyAccount.getGLAccount(document.getAmount().getCurrency()),
                                MessageFormat.format(
                                        TranslationService.getInstance().translate(TranslationService.GL.NoEmployeeAdvAcc, auth.getLanguage()),
                                        employee.getName())).getAccount();
                        Validators.checkArgument(Validators.isValid(account),
                                MessageFormat.format(
                                        TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountForFor, auth.getLanguage()),
                                        employee.getName(), document.getAmount().getCurrency()));
                    }

                } else {
                    return null;
                }

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry, accountCash.getAccount(), account);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public void checkEmployeeOperation(String name, GLMoneyAccount glMoneyAccount, EmployeeOperationDto document) {
        employeeOperation(name, glMoneyAccount, document, null, true, false);
    }

    public DoubleEntrySql finishEmployeeOperation(String name, GLMoneyAccount glMoneyAccount, EmployeeOperationDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return employeeOperation(name, glMoneyAccount, document, documentDoubleEntry, false, finishGL);
    }

    private DoubleEntrySql employeeOperation(String name, GLMoneyAccount glMoneyAccount, EmployeeOperationDto document, DoubleEntryDto documentDoubleEntry, boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (GamaMoneyUtils.isZero(document.getAmount())) {
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {
                GLMoneyAccount moneyAccount = Validators.checkNotNull(glMoneyAccount,
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountForFor, auth.getLanguage()),
                                name, document.getCurrency()));

                GLCurrencyAccount accountEmployee = Validators.checkNotNull(moneyAccount.getGLAccount(document.getCurrency()),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountForFor, auth.getLanguage()),
                                name, document.getCurrency()));
                Validators.checkArgument(Validators.isValid(accountEmployee.getAccount()),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountForFor, auth.getLanguage()),
                                name, document.getCurrency()));


                GLOperationAccount account = null;
                GLOperationAccount tempAccount = null;
                boolean noDebt = BooleanUtils.isTrue(document.getNoDebt());
                if (noDebt) {
                    account = companySettings.getGl().getAccTemp();
                    Validators.checkArgument(Validators.isValid(account),
                            TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));
                }

                if (document.getCounterparty() == null || document.getCounterparty().getId() == null) {
                    tempAccount = companySettings.getGl().getAccTemp();
                    Validators.checkArgument(Validators.isValid(tempAccount),
                            TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));

                } else {
                    Validators.checkNotNull(document.getCounterparty(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty, auth.getLanguage()));
                    CounterpartyDto counterparty = getCounterparty(document);

                    if (noDebt) {
                        if (Validators.isValid(counterparty.getNoDebtAccount())) {
                            account = counterparty.getNoDebtAccount();
                        }

                    } else {
                        account = getCounterpartyAccount(counterparty, document.getDebtType());
                    }
                }

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry, accountEmployee.getAccount(), account, tempAccount);
                    return finishDocument(document, operations, finishGL);
                }
            }

            return doubleEntry;
        });
    }

    public void checkDebtCorrection(DebtCorrectionDto document) {
        debtCorrection(document, null, true, false);
    }

    public DoubleEntrySql finishDebtCorrection(DebtCorrectionDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return debtCorrection(document, documentDoubleEntry, false, finishGL);
    }

    private DoubleEntrySql debtCorrection(DebtCorrectionDto document, DoubleEntryDto documentDoubleEntry, final boolean checkOnly, final Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        Validators.checkNotNull(companySettings.getGl(), TranslationService.getInstance().translate(TranslationService.GL.NoGLSettings, auth.getLanguage()));
        if (BooleanUtils.isTrue(document.getCorrection())) {
            Validators.checkArgument(Validators.isValid(companySettings.getGl().getAccCurrRateNeg()), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRateNegAcc, auth.getLanguage()));
            Validators.checkArgument(Validators.isValid(companySettings.getGl().getAccCurrRatePos()), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRatePosAcc, auth.getLanguage()));
        }

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (GamaMoneyUtils.isZero(document.getBaseAmount())) {
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {
                CounterpartyDto counterparty = getCounterparty(document);

                GLOperationAccount accountVendor = counterparty.getAccount(DebtType.VENDOR);
                GLOperationAccount accountCustomer = counterparty.getAccount(DebtType.CUSTOMER);
                Validators.checkArgument(Validators.isValid(accountVendor) || Validators.isValid(accountCustomer),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.GL.NoCustomerNoVendorAcc, auth.getLanguage()),
                                counterparty.getName()));

                if (!checkOnly) {
                    if (!Validators.isValid(accountVendor)) accountVendor = null;
                    if (!Validators.isValid(accountCustomer)) accountCustomer = null;

                    GLOperationAccount debit = null;
                    GLOperationAccount credit = null;

                    if (document.getDebit() == DebtType.VENDOR) debit = accountVendor;
                    else if (document.getDebit() == DebtType.CUSTOMER) debit = accountCustomer;
                    if (document.getCredit() == DebtType.VENDOR) credit = accountVendor;
                    else if (document.getCredit() == DebtType.CUSTOMER) credit = accountCustomer;

                    if (debit == null) {
                        debit = BooleanUtils.isTrue(document.getCorrection()) ? companySettings.getGl().getAccCurrRateNeg() : credit;
                    }
                    if (credit == null) {
                        credit = BooleanUtils.isTrue(document.getCorrection()) ? companySettings.getGl().getAccCurrRatePos() : debit;
                    }

                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry, debit, credit);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public void checkInventory(InventoryDto document) {
        inventory(document, null, true, false);
    }

    public DoubleEntrySql finishInventory(InventoryDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return inventory(document, documentDoubleEntry, false, finishGL);
    }

    public DoubleEntrySql inventory(InventoryDto document, DoubleEntryDto documentDoubleEntry, boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (CollectionsHelper.isEmpty(document.getParts())) {
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {
                Map<Long, GLDC> accountsIncome = new HashMap<>();
                Map<Long, GLOperationAccount> accountsAsset = new HashMap<>();
                Map<Long, GLDC> accountsExpense = new HashMap<>();
                genPartAccounts(document.getParts(), accountsAsset, accountsExpense, accountsIncome, null, null);

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry, accountsIncome,
                            accountsAsset, accountsExpense);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public void checkTransProd(TransProdDto document) {
        transProd(document, null, true, false);
    }

    public DoubleEntrySql finishTransProd(TransProdDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return transProd(document, documentDoubleEntry, false, finishGL);
    }

    private DoubleEntrySql transProd(TransProdDto document, DoubleEntryDto documentDoubleEntry, boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (document.getPartsFrom() == null || document.getPartsFrom().size() == 0) {
                // do not need to generate double-entry if no parts
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {

                GLOperationAccount account = companySettings.getGl().getAccTemp();
                Validators.checkArgument(Validators.isValid(account), TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));

                Map<Long, GLOperationAccount> accountsAsset = new HashMap<>();
                Map<Long, GLDC> accountsExpense = new HashMap<>();

                genPartAccounts(CollectionsHelper.concat(document.getPartsFrom(), document.getPartsTo()), accountsAsset, accountsExpense, null, null, null);

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry, accountsAsset, accountsExpense, account);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    @SuppressWarnings("rawtypes")
    public void checkMoneyRateInfluence(Class<? extends IMoneyAccount> accountClass, BaseMoneyRateInfluenceDto document) {
        moneyDocRateInfluence(accountClass, document, document.getDoubleEntry(), true, false);
    }

    @SuppressWarnings("rawtypes")
    public DoubleEntrySql finishMoneyRateInfluence(Class<? extends IMoneyAccount> accountClass,
                                                   BaseMoneyRateInfluenceDto document, DoubleEntryDto documentDoubleEntry,
                                                   Boolean finishGL) {
        return moneyDocRateInfluence(accountClass, document, documentDoubleEntry, false, finishGL);
    }

    @SuppressWarnings("rawtypes")
    private DoubleEntrySql moneyDocRateInfluence(Class<? extends IMoneyAccount> accountClass,
                                                 BaseMoneyRateInfluenceDto document, DoubleEntryDto documentDoubleEntry,
                                                 boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        Validators.checkNotNull(companySettings.getGl(), TranslationService.getInstance().translate(TranslationService.GL.NoGLSettings, auth.getLanguage()));
        Validators.checkArgument(Validators.isValid(companySettings.getGl().getAccCurrRateNeg()), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRateNegAcc, auth.getLanguage()));
        Validators.checkArgument(Validators.isValid(companySettings.getGl().getAccCurrRatePos()), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRatePosAcc, auth.getLanguage()));

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (document.getAccounts() == null || document.getAccounts().size() == 0) {
                // do not need to generate double-entry if no accounts
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {

                GLOperationAccount accCurrRateNeg = companySettings.getGl().getAccCurrRateNeg();
                Validators.checkArgument(Validators.isValid(accCurrRateNeg), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRateNegAcc, auth.getLanguage()));
                GLOperationAccount accCurrRatePos = companySettings.getGl().getAccCurrRatePos();
                Validators.checkArgument(Validators.isValid(accCurrRatePos), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRatePosAcc, auth.getLanguage()));

                // read accounts - need for G.L. accounts
                Set<Long> accountsKeys = new HashSet<>();
                for (BaseMoneyBalanceDto account : document.getAccounts()) {
                    accountsKeys.add(account.getAccountId());
                }

                Map<Long, ? extends IMoneyAccount<?>> accounts;

                if (BaseMoneyAccountSql.class.isAssignableFrom(accountClass)) {
                    @SuppressWarnings("unchecked")
                    Class<BaseMoneyAccountSql<?>> c = (Class<BaseMoneyAccountSql<?>>) accountClass;
                    accounts = dbServiceSQL.queryByIds(c, null, accountsKeys)
                            .getResultStream()
                            .collect(Collectors.toMap(IId::getId, Function.identity()));
                } else {
                    log.error("No " + accountClass.getSimpleName() + " accounts");
                    throw new GamaException("No " + accountClass.getSimpleName() + "  accounts");
                }

                if (CollectionsHelper.isEmpty(accounts)) {
                    return doubleEntry;
                }

                for (BaseMoneyBalanceDto balance : document.getAccounts()) {

                    IMoneyAccount<?> account = Validators.checkNotNull(accounts.get(balance.getAccountId()),
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                    balance.getAccountName()));

                    if (GamaMoneyUtils.isZero(balance.getBaseAmount()) && GamaMoneyUtils.isZero(balance.getBaseFixAmount()))
                        continue;

                    String currency = balance.getExchange().getCurrency();
                    GLCurrencyAccount accountBank = Validators.checkNotNull(account.getMoneyAccount().getGLAccount(currency),
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountForFor, auth.getLanguage()),
                                    balance.getAccountName(), currency));
                    Validators.checkArgument(Validators.isValid(accountBank.getAccount()),
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountForFor, auth.getLanguage()),
                                    balance.getAccountName(), currency));
                }

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document, doubleEntry, accounts, accCurrRateNeg, accCurrRatePos);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public void checkDebtRateInfluence(DebtRateInfluenceDto document) {
        debtRateInfluence(document, null, true, false);
    }

    public DoubleEntrySql finishDebtRateInfluence(DebtRateInfluenceDto document, DoubleEntryDto documentDoubleEntry, Boolean finishGL) {
        return debtRateInfluence(document, documentDoubleEntry, false, finishGL);
    }

    private DoubleEntrySql debtRateInfluence(DebtRateInfluenceDto document, DoubleEntryDto documentDoubleEntry, boolean checkOnly, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        Validators.checkNotNull(companySettings.getGl(), TranslationService.getInstance().translate(TranslationService.GL.NoGLSettings, auth.getLanguage()));
        Validators.checkArgument(Validators.isValid(companySettings.getGl().getAccCurrRateNeg()), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRateNegAcc, auth.getLanguage()));
        Validators.checkArgument(Validators.isValid(companySettings.getGl().getAccCurrRatePos()), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRatePosAcc, auth.getLanguage()));

        return baseOperations(document, documentDoubleEntry, checkOnly, finishGL, doubleEntry -> {

            if (document.getAccounts() == null || document.getAccounts().size() == 0) {
                // do not need to generate double-entry if no accounts
                if (!checkOnly) {
                    doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                    return glUtilsService.saveDoubleEntry(doubleEntry);
                }

            } else {

                GLOperationAccount accCurrRateNeg = companySettings.getGl().getAccCurrRateNeg();
                Validators.checkArgument(Validators.isValid(accCurrRateNeg), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRateNegAcc, auth.getLanguage()));
                GLOperationAccount accCurrRatePos = companySettings.getGl().getAccCurrRatePos();
                Validators.checkArgument(Validators.isValid(accCurrRatePos), TranslationService.getInstance().translate(TranslationService.GL.NoCurrRatePosAcc, auth.getLanguage()));

                // read accounts - need for G.L. accounts
                Set<Long> accountsKeys = new HashSet<>();
                for (BaseMoneyBalanceDto account : document.getAccounts()) {
                    accountsKeys.add(account.getAccountId());
                }

                Map<Long, CounterpartySql> accounts = dbServiceSQL.queryByIds(CounterpartySql.class, null, accountsKeys)
                                            .getResultStream().collect(Collectors.toMap(CounterpartySql::getId, Function.identity()));
                if (accounts.size() == 0) {
                    return doubleEntry;
                }

                for (DebtBalanceDto balance : document.getAccounts()) {

                    CounterpartySql account = Validators.checkNotNull(accounts.get(balance.getAccountId()),
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                    balance.getAccountName()));

                    if (GamaMoneyUtils.isZero(balance.getBaseFixAmount())) continue;

                    Validators.checkNotNull(balance.getType(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDebtType, auth.getLanguage()));
                    String currency = balance.getExchange().getCurrency();
                    GLOperationAccount operationAccount = Validators.checkNotNull(account.getAccount(balance.getType()),
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                    currency));
                    Validators.checkArgument(Validators.isValid(operationAccount),
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountFor, auth.getLanguage()),
                                    currency));
                }

                if (!checkOnly) {
                    List<GLOperationDto> operations = glGenService.generateDoubleEntry(document,
                            doubleEntry, accounts, accCurrRateNeg, accCurrRatePos);
                    doubleEntry = finishDocument(document, operations, finishGL);
                }
            }
            return doubleEntry;
        });
    }

    public DoubleEntrySql finishSalary(SalarySql document, DoubleEntryDto documentDoubleEntry, final Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings.isDisableGL()) return null;

        return baseOperations(document, documentDoubleEntry, false, finishGL, doubleEntry -> {

            final List<GLOperationDto> operations = new ArrayList<>();

            if (document.getId() != null) {
                dbServiceSQL.executeInTransaction(entityManager -> entityManager.createQuery(
                        "SELECT a FROM " + EmployeeChargeSql.class.getName() + " a" +
                                " WHERE " + EmployeeChargeSql_.SALARY + "." + SalarySql_.ID + " = :salaryId" +
                                " AND " + EmployeeChargeSql_.COMPANY_ID + " = :companyId",
                                EmployeeChargeSql.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("salaryId", document.getId())
                        .getResultStream()
                        .forEach(charge -> {
                            if (charge.getOperations() != null) {
                                operations.addAll(charge.getOperations());
                                double sortOrder = 0;
                                for (GLOperationDto op : operations) op.setSortOrder(sortOrder += 10.0);
                            }
                        }));
            }

            if (CollectionsHelper.isEmpty(operations)) {
                // do not need to generate double-entry if no operations
                doubleEntry = initDeData(finishGL, document, doubleEntry, null);
                doubleEntry = glUtilsService.saveDoubleEntry(doubleEntry);
            } else {
                // optimize operations
                Map<String, GLOperationDto> map = new HashMap<>();

                for (GLOperationDto operation : operations) {
                    String key = operation.getDebit().getNumber() + '\u0001' + operation.getCredit().getNumber();
                    GLOperationDto op = map.get(key);
                    if (op == null) {
                        map.put(key, operation);
                    } else {
                        op.setAmount(GamaMoneyUtils.add(op.getAmount(), operation.getAmount()));
                    }
                }

                List<GLOperationDto> ops = new ArrayList<>(map.values());
                ops.sort(Comparator.comparing((IGLOperation o) -> o.getDebit().getNumber()).thenComparing(o -> o.getCredit().getNumber()));

                doubleEntry = finishDocument(document, ops, finishGL);
            }
            return doubleEntry;
        });
    }

    public void updateVatCodeGLAccounts(List<VATCodeTotal> vatCodeTotals) {
        if (vatCodeTotals == null || vatCodeTotals.size() == 0 ||
                vatCodeTotals.stream().noneMatch(x -> Validators.isPartialValid(x.getGl()))) return;

        taskQueueService.queueTask(new UpdateVatCodeGLAccountsTask(auth.getCompanyId(), vatCodeTotals));
    }
}
