package lt.gama.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.ReportBalanceIntervalRequest;
import lt.gama.api.request.ReportBalanceRequest;
import lt.gama.api.request.ReportInterval;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.i.*;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.MoneyHistorySqlMapper;
import lt.gama.model.sql.base.*;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.GamaMoney_;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.doc.*;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.gl.GLCurrencyAccount;
import lt.gama.model.type.gl.GLMoneyAccount;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyBalanceInterval;
import lt.gama.report.RepMoneyDetail;
import lt.gama.report.RepMoneyDetailCurrency;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static lt.gama.api.request.ReportInterval.INTERVAL_PATTERN;

@Service
public class MoneyAccountService {

    private static final Logger log = LoggerFactory.getLogger(MoneyAccountService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final CurrencyService currencyService;
    private final DBServiceSQL dbServiceSQL;
    private final MoneyHistorySqlMapper moneyHistorySqlMapper;
    private final Auth auth;

    @SuppressWarnings("rawtypes")
    final static Map<AccountType, Class<? extends BaseMoneyAccountSql>> ACCOUNT_TYPE_CLASS = Map.of(
            AccountType.BANK, BankAccountSql.class,
            AccountType.CASH, CashSql.class,
            AccountType.EMPLOYEE, EmployeeSql.class
    );


    public MoneyAccountService(CurrencyService currencyService,
                               DBServiceSQL dbServiceSQL,
                               MoneyHistorySqlMapper moneyHistorySqlMapper,
                               Auth auth) {
        this.currencyService = currencyService;
        this.dbServiceSQL = dbServiceSQL;
        this.moneyHistorySqlMapper = moneyHistorySqlMapper;
        this.auth = auth;
    }

    private <D extends IBaseDocument, F extends BaseCompanyDto, E extends BaseCompanySql & IMoneyAccount<F>>
    D finishOpeningBalanceBlock(D document, AccountType accountType, E account,
                                GamaMoney amount, GamaMoney baseAmount, Exchange exchange) {
        if (GamaMoneyUtils.isZero(amount)) return document;

        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            String currency = amount.getCurrency();

            // write history
            MoneyHistorySql history = new MoneyHistorySql();
            history.setAccountType(accountType);
            history.setAccountId(account.getId());
            history.setDoc(Doc.of(document));

            @SuppressWarnings("rawtypes")
            Class<? extends BaseMoneyAccountSql> clazz;
            if (account instanceof EmployeeSql) {
                clazz = EmployeeSql.class;
                history.setEmployee(entityManager.getReference(EmployeeSql.class, account.getId()));
            } else if (account instanceof CashSql) {
                clazz = CashSql.class;
                history.setCash(entityManager.getReference(CashSql.class, account.getId()));
            } else if (account instanceof BankAccountSql) {
                clazz = BankAccountSql.class;
                history.setBankAccount(entityManager.getReference(BankAccountSql.class, account.getId()));
            } else {
                throw new IllegalArgumentException("No MoneyAccount class");
            }
            history.setAmount(amount);
            history.setBaseAmount(baseAmount);
            history.setExchange(exchange);
            dbServiceSQL.saveEntityInCompany(history);

            //noinspection unchecked
            updateMoneyRemainder(account.getId(), clazz, amount, currency);
            return document;
        });
    }


    public <D extends IBaseDocument, F extends BaseCompanyDto, E extends BaseCompanySql & IMoneyAccount<F>>
    D finishOpeningBalance(final D document, final AccountType accountType, final E account,
                           final GamaMoney amount, final GamaMoney baseAmount, final Exchange exchange) {
        if (GamaMoneyUtils.isZero(amount)) return document;

        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
        Validators.checkNotNull(account, "No account");
        Validators.checkNotNull(accountType, "No account type");

        final long companyId = auth.getCompanyId();

        if (account.getCompanyId() != companyId || document.getCompanyId() != companyId) {
            throw new GamaUnauthorizedException("Unauthorized access");
        }
        return finishOpeningBalanceBlock(document, accountType, account, amount, baseAmount, exchange);
    }

    public BaseMoneyRateInfluenceSql finishRateInfluence(String currency, BaseMoneyRateInfluenceSql document,
                                                         BaseMoneyBalanceSql balance, AccountType accountType) {
        if (GamaMoneyUtils.isZero(balance.getBaseFixAmount())) return document;

        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
        Validators.checkNotNull(balance, "No balance");
        Validators.checkNotNull(accountType, "No account type");

        return rateInfluenceBlock(true, currency, document.getClass(), document.getId(), accountType, balance, false);
    }

    public BaseMoneyRateInfluenceSql recallRateInfluence(String currency, BaseMoneyRateInfluenceSql document,
                                                         BaseMoneyBalanceSql balance, AccountType accountType) {
        if (GamaMoneyUtils.isZero(balance.getBaseFixAmount())) return document;

        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
        Validators.checkNotNull(balance, "No balance");
        Validators.checkNotNull(accountType, "No account type");

        return rateInfluenceBlock(false, currency, document.getClass(), document.getId(), accountType, balance, true);
    }

    public <F> List<RepMoneyBalance<F>> genRateInfluence(LocalDate date, AccountType accountType) {
        Validators.checkNotNull(date, "No date");
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        String baseCurrency = Validators.checkNotNull(companySettings.getCurrency(), "No base currency").getCode();
        Class<?> accountClass = Validators.checkNotNull(ACCOUNT_TYPE_CLASS.get(accountType), "Wrong accountType=" + accountType);

        List<RepMoneyBalance<F>> balances = new ArrayList<>();
        Map<String, Exchange> exchangeMap = new HashMap<>();
        RepMoneyBalance<F> repMoneyBalance = null;

        try {
            String selectAccount;
            String groupByAccount;
            String orderByAccount;

            if (accountType == AccountType.CASH) {
                selectAccount = "A.name AS name, A.cashier AS cashier,";
                groupByAccount = "A.name, A.cashier,";
                orderByAccount = "A.name, A.cashier,";
            } else if (accountType == AccountType.BANK) {
                selectAccount = "A.account AS account, A.bank.name AS name,";
                groupByAccount = "A.account, A.bank.name,";
                orderByAccount = "A.account, A.bank.name,";
            } else if (accountType == AccountType.EMPLOYEE) {
                selectAccount = "A.name AS name, A.office AS office, A.department AS department,";
                groupByAccount = "A.name, A.office, A.department,";
                orderByAccount = "A.name, A.office, A.department,";
            } else {
                throw new GamaException("No account type");
            }

            TypedQuery<Tuple> q = entityManager.createQuery(
                    "SELECT H.accountId AS id, H.exchange.currency AS currency," +
                            " " + selectAccount +
                            " SUM(H.amount.amount) AS remainder, SUM(H.baseAmount.amount) AS baseRemainder " +
                            " FROM " + MoneyHistorySql.class.getName() + " H " +
                            " JOIN " + accountClass.getName() + " A ON A.id = H.accountId " +
                            " WHERE H.companyId = :companyId " +
                            " AND H.accountType = :accountType " +
                            " AND H.doc.date <= :date " +
                            " AND H.exchange.currency <> H.baseAmount.currency " +
                            " GROUP BY H.accountId, " + groupByAccount + "H.exchange.currency " +
                            " HAVING (SUM(H.amount.amount) IS NOT NULL AND SUM(H.amount.amount) <> 0) OR " +
                                " (SUM(H.baseAmount.amount) IS NOT NULL AND SUM(H.baseAmount.amount) <> 0) " +
                            " ORDER BY H.accountId, " + orderByAccount + "H.exchange.currency", Tuple.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("accountType", accountType);
            q.setParameter("date", date);

            List<Tuple> results = q.getResultList();

            for (Tuple o : results) {
                switch (accountType) {
                    case CASH -> {
                        CashDto cash = new CashDto();
                        cash.setId(o.get("id", Long.class));
                        cash.setDb(DBType.POSTGRESQL);
                        cash.setName(o.get("name", String.class));
                        cash.setCashier(o.get("cashier", String.class));
                        //noinspection unchecked
                        repMoneyBalance = new RepMoneyBalance(cash);
                    }
                    case BANK -> {
                        BankAccountDto bank = new BankAccountDto();
                        if (bank.getBank() == null) bank.setBank(new DocBank());
                        bank.setId(o.get("id", Long.class));
                        bank.setDb(DBType.POSTGRESQL);
                        bank.setName(o.get("account", String.class));
                        bank.getBank().setName(o.get("name", String.class));
                        //noinspection unchecked
                        repMoneyBalance = new RepMoneyBalance(bank);
                    }
                    case EMPLOYEE -> {
                        EmployeeDto employee = new EmployeeDto();
                        employee.setId(o.get("id", Long.class));
                        employee.setDb(DBType.POSTGRESQL);
                        employee.setName(o.get("name", String.class));
                        employee.setOffice(o.get("office", String.class));
                        employee.setDepartment(o.get("department", String.class));
                        //noinspection unchecked
                        repMoneyBalance = new RepMoneyBalance(employee);
                    }
                    default -> throw new GamaException("No or wrong account type");
                }

                String currency = o.get("currency", String.class);
                GamaMoney remainder = GamaMoney.ofNullable(currency, o.get("remainder", BigDecimal.class));
                GamaMoney baseRemainder = GamaMoney.ofNullable(baseCurrency, o.get("baseRemainder", BigDecimal.class));

                Exchange exchange = exchangeMap.get(currency);
                if (exchange == null) {
                    exchange = new Exchange(currency);
                    exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings, exchange, date), "No exchange");
                    exchangeMap.put(currency, exchange);
                }

                repMoneyBalance.setCurrency(currency);
                repMoneyBalance.setOpening(remainder);
                repMoneyBalance.setBaseOpening(baseRemainder);
                repMoneyBalance.setExchange(exchange);
                repMoneyBalance.setFix(GamaMoneyUtils.subtract(exchange.exchange(remainder), baseRemainder));

                balances.add(repMoneyBalance);
            }
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return balances;
    }

    private <F extends BaseCompanyDto, E extends IMoneyAccount<F>> void updateCurrency(E account, String currency) {
        if (account.getMoneyAccount() == null) account.setMoneyAccount(new GLMoneyAccount());
        account.getUsedCurrencies().add(currency);
    }

    private Long updateMoneyHistory(final boolean finished, final IBaseDocument entity,
                                    final GamaMoney amount, final GamaMoney baseAmount, final String currency, final Exchange exchange,
                                    final ICounterparty counterparty, final IDocEmployee employee, final ICash cash,
                                    final IBankAccount account, final IBankAccount account2,
                                    final AccountType accountType) {
        Long accountId = accountType == AccountType.EMPLOYEE && Validators.isValid(employee)
                ? Validators.checkNotNull(dbServiceSQL.getId(EmployeeSql.class, employee.getId(), employee.getDb()),
                "No Employee " + employee.getId() + " " + employee.getDb())
                : accountType == AccountType.CASH && Validators.isValid(cash)
                ? Validators.checkNotNull(dbServiceSQL.getId(CashSql.class, cash.getId(), cash.getDb()),
                "No Cash " + cash.getId() + " " + cash.getDb())
                : accountType == AccountType.BANK && Validators.isValid(account)
                ? Validators.checkNotNull(dbServiceSQL.getId(BankAccountSql.class, account.getId(), account.getDb()),
                "No BankAccount " + account.getId() + " " + account.getDb())
                : null;
        if (accountId == null) throw new GamaException("No accountId");

        if (finished) {
            // write history
            MoneyHistorySql history = new MoneyHistorySql();
            history.setAccountType(accountType);
            history.setAccountId(accountId);
            history.setDoc(Doc.of(entity));
            if (Validators.isValid(counterparty)) {
                long id = Validators.checkNotNull(dbServiceSQL.getId(CounterpartySql.class, counterparty.getId(), counterparty.getDb()),
                        "No Counterparty " + counterparty.getId() + " " + counterparty.getDb());
                history.setCounterparty(entityManager.getReference(CounterpartySql.class, id));
            }
            if (Validators.isValid(employee)) {
                long id = Validators.checkNotNull(dbServiceSQL.getId(EmployeeSql.class, employee.getId(), employee.getDb()),
                        "No Employee " + employee.getId() + " " + employee.getDb());
                history.setEmployee(entityManager.getReference(EmployeeSql.class, id));
            }
            if (Validators.isValid(cash)) {
                long id = Validators.checkNotNull(dbServiceSQL.getId(CashSql.class, cash.getId(), cash.getDb()),
                        "No Cash " + cash.getId() + " " + cash.getDb());
                history.setCash(entityManager.getReference(CashSql.class, id));
            }
            if (Validators.isValid(account)) {
                long id = Validators.checkNotNull(dbServiceSQL.getId(BankAccountSql.class, account.getId(), account.getDb()),
                        "No BankAccount " + account.getId() + " " + account.getDb());
                history.setBankAccount(entityManager.getReference(BankAccountSql.class, id));
            }
            if (Validators.isValid(account2)) {
                long id = Validators.checkNotNull(dbServiceSQL.getId(BankAccountSql.class, account2.getId(), account2.getDb()),
                        "No BankAccount " + account2.getId() + " " + account2.getDb());
                history.setBankAccount(entityManager.getReference(BankAccountSql.class, id));
            }
            history.setAmount(amount != null ? amount : GamaMoney.of(currency, BigDecimal.ZERO));
            history.setBaseAmount(baseAmount);
            history.setExchange(exchange);
            history = dbServiceSQL.saveEntityInCompany(history);
            accountId = history.getAccountId();
        } else {
            // remove from history
            int deleted = entityManager.createQuery(
                            "DELETE FROM " + MoneyHistorySql.class.getName() + " h" +
                                    " WHERE h.companyId = :companyId" +
                                    " AND h.doc.id = :documentId" +
                                    " AND h.doc.db = :parentDb" +
                                    " AND h.accountType = :accountType" +
                                    " AND h.accountId = :accountId" +
                                    " AND h.exchange.currency = :currency")
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("documentId", entity.getId())
                    .setParameter("parentDb", entity.getDb() != null ? entity.getDb() : DBType.DATASTORE)
                    .setParameter("accountType", accountType)
                    .setParameter("accountId", accountId)
                    .setParameter("currency", currency)
                    .executeUpdate();

            log.info("deleted " + MoneyHistorySql.class.getSimpleName() + ": " + deleted);
        }
        return accountId;
    }

    private <F extends BaseCompanyDto, E extends BaseCompanySql & IId<Long> & ICompany & IMoneyAccount<F>>
    void updateMoneyRemainder(final long accountId, Class<E> accountClass, final GamaMoney sum, final String currency) {
        // write remainder into account entity, i.e. BankAccount, Cash or Employee
        // and update used currencies list
        dbServiceSQL.executeInTransaction(entityManager -> {
            E accountBase = dbServiceSQL.getAndCheck(accountClass, accountId);
            accountBase.updateRemainder(sum);
            updateCurrency(accountBase, currency);
            dbServiceSQL.saveEntityInCompany(accountBase);
        });
    }

    private <T extends BaseMoneyRateInfluenceSql, B extends BaseMoneyBalanceSql>
    T rateInfluenceBlock(final boolean finished, final String currency,
                         final Class<T> documentClass, final long docId,
                         final AccountType accountType, final B accountBalance, boolean negate) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            T entity = dbServiceSQL.getById(documentClass, docId);
            if (BooleanUtils.isTrue(accountBalance.getFinished()) == finished) return entity;

            final GamaMoney baseSum = negate ? GamaMoneyUtils.negated(accountBalance.getBaseFixAmount()) : accountBalance.getBaseFixAmount();

            // mark as finished
            accountBalance.setFinished(finished);

            updateMoneyHistory(finished, entity, null, baseSum, currency, accountBalance.getExchange(),
                    accountBalance.getCounterparty(), accountBalance.getEmployee(), accountBalance.getCash(),
                    accountBalance.getBankAccount(), null, accountType);

            return entity;
        });
    }

    private <F extends BaseCompanyDto, E extends BaseCompanySql & IMoneyAccount<F>, T extends BaseMoneyDocumentSql>
    T changeOperationBlock(boolean finished, String currency,
                           Class<T> documentClass, long docId,
                           AccountType accountType, Class<E> accountClass,
                           boolean negate) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            T entity = dbServiceSQL.getById(documentClass, docId);
            if (entity.isFinishedMoneyType(accountType) == finished) return entity;

            final GamaMoney sum = negate ? GamaMoneyUtils.negated(entity.getAmount()) : entity.getAmount();
            final GamaMoney baseSum = negate ? GamaMoneyUtils.negated(entity.getBaseAmount()) : entity.getBaseAmount();

            // mark as finished
            entity.setFinishedMoneyType(accountType, finished);

            long accountId = updateMoneyHistory(finished, entity, sum, baseSum, currency, entity.getExchange(),
                    entity.getCounterparty(), entity.getEmployee(), entity.getCash(),
                    entity.getBankAccount(), entity.getBankAccount2(), accountType);

            updateMoneyRemainder(accountId, accountClass, sum, currency);

            return entity;
        });
    }

    private <F extends BaseCompanyDto, E extends BaseCompanySql & IMoneyAccount<F>>
    BaseMoneyDocumentSql changeOperation(final boolean state, final String currency, final BaseMoneyDocumentSql document,
                                         final AccountType accountType, final Class<E> accountClass, final boolean negate) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            // generate balance records
            if (document.isFinishedMoneyType(accountType) != state) {
                if (GamaMoneyUtils.isZero(document.getAmount())) {
                    document.setFinishedMoneyType(accountType, state);
                } else {
                    final long docId = document.getId();
                    final Class<? extends BaseMoneyDocumentSql> docClass = document.getClass();

                    return changeOperationBlock(state, currency, docClass, docId, accountType, accountClass, negate);
                }
            }
            return document;
        });
    }

    public <F extends BaseCompanyDto, E extends BaseCompanySql & IMoneyAccount<F>>
    BaseMoneyDocumentSql finishOperation(final String currency, BaseMoneyDocumentSql document,
                                         AccountType accountType, Class<E> accountClass, boolean negate) {
        return changeOperation(true, currency, document, accountType, accountClass, negate);
    }

    public <F extends BaseCompanyDto, E extends BaseCompanySql & IMoneyAccount<F>>
    BaseMoneyDocumentSql recallOperation(final String currency, final BaseMoneyDocumentSql document,
                                         AccountType accountType, Class<E> accountClass, boolean negate) {
        return changeOperation(false, currency, document, accountType, accountClass, !negate);
    }

    public <F> List<RepMoneyBalance<F>> reportBalance(ReportBalanceRequest request, AccountType accountType) {
        CompanySettings settings = dbServiceSQL.getCompanySettings(auth.getCompanyId());

        LocalDate dateFrom = DateUtils.max(settings.getStartAccounting(), request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        List<RepMoneyBalance<F>> report = new ArrayList<>();
        Map<String, Exchange> exchangeMap = new HashMap<>();
        RepMoneyBalance<F> repMoneyBalance;

        try {
            var q = entityManager.createNativeQuery(
                    StringHelper.loadSQLFromFile("money", "money_balance_" + accountType + ".sql"), Tuple.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("dateFrom", dateFrom);
            q.setParameter("dateTo", dateTo.plusDays(1));
            q.setParameter("accountId", 0L);
            q.setParameter("currency", "");

            @SuppressWarnings("unchecked")
            List<Tuple> results = addScalar(q, accountType).getResultList();

            for (Tuple o : results) {
                String currency = o.get("currency", String.class);
                String bCurrency = o.get("b_currency", String.class);

                GLMoneyAccount glAccount = new GLMoneyAccount();
                glAccount.setAccounts(Collections.singletonList(new GLCurrencyAccount(currency, new GLOperationAccount(
                        o.get("account_number", String.class), o.get("account_name", String.class)))));

                switch (accountType) {
                    case CASH -> {
                        CashDto cash = new CashDto();
                        cash.setId(o.get("account_id", BigInteger.class).longValue());
                        cash.setName(o.get("name", String.class));
                        cash.setCashier(o.get("cashier", String.class));
                        cash.setMoneyAccount(glAccount);
                        //noinspection unchecked
                        repMoneyBalance = new RepMoneyBalance(cash);
                    }
                    case BANK -> {
                        BankAccountDto bank = new BankAccountDto();
                        bank.setId(o.get("account_id", BigInteger.class).longValue());
                        bank.setName(o.get("account", String.class));
                        bank.setMoneyAccount(glAccount);
                        //noinspection unchecked
                        repMoneyBalance = new RepMoneyBalance(bank);
                    }
                    case EMPLOYEE -> {
                        EmployeeDto employee = new EmployeeDto();
                        employee.setId(o.get("account_id", BigInteger.class).longValue());
                        employee.setName(o.get("name", String.class));
                        employee.setOffice(o.get("office", String.class));
                        employee.setDepartment(o.get("department", String.class));
                        employee.setMoneyAccount(glAccount);
                        //noinspection unchecked
                        repMoneyBalance = new RepMoneyBalance(employee);
                    }
                    default -> throw new GamaException("No or wrong account type");
                }

                Exchange exchange = exchangeMap.get(currency);
                if (exchange == null) {
                    exchange = Validators.checkNotNull(currencyService.currencyExchange(
                            settings, new Exchange(o.get("currency", String.class)), dateTo), "No exchange");
                    exchangeMap.put(currency, exchange);
                }

                repMoneyBalance.setCurrency(currency);
                repMoneyBalance.setExchange(exchange);
                repMoneyBalance.setOpening(GamaMoney.ofNullable(currency, BigDecimalUtils.notNull(o.get("ob", BigDecimal.class))));
                repMoneyBalance.setDebit(GamaMoney.ofNullable(currency, o.get("debit", BigDecimal.class)));
                repMoneyBalance.setCredit(GamaMoney.ofNullable(currency, o.get("credit", BigDecimal.class)));
                repMoneyBalance.setBaseOpening(GamaMoney.ofNullable(bCurrency, BigDecimalUtils.notNull(o.get("b_ob", BigDecimal.class))));
                repMoneyBalance.setBaseDebit(GamaMoney.ofNullable(bCurrency, o.get("b_debit", BigDecimal.class)));
                repMoneyBalance.setBaseCredit(GamaMoney.ofNullable(bCurrency, o.get("b_credit", BigDecimal.class)));
                repMoneyBalance.setBaseNowOpening(exchange.exchange(repMoneyBalance.getOpening()));
                repMoneyBalance.setBaseNowDebit(exchange.exchange(repMoneyBalance.getDebit()));
                repMoneyBalance.setBaseNowCredit(exchange.exchange(repMoneyBalance.getCredit()));

                report.add(repMoneyBalance);
            }

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return report;
    }

    public <F> PageResponse<MoneyHistoryDto, RepMoneyDetail<F>> reportFlow(PageRequest request, AccountType accountType) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate dateFrom = DateUtils.max(companySettings.getStartAccounting(), request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        long id = (Long) Validators.checkNotNull(PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.ORIGIN_ID), "No id");
        String currency = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.CURRENCY);

        PageResponse<MoneyHistoryDto, RepMoneyDetail<F>> response = dbServiceSQL.queryPage(
                request, MoneyHistorySql.class, MoneyHistorySql.GRAPH_ALL, moneyHistorySqlMapper,
                (cb, root) -> whereReportFlow(request, cb, root, id, currency, accountType),
                this::orderReportFlow,
                this::selectIdsReportFlow);

        if (request.isRefresh()) {

            RepMoneyDetail<F> detail = new RepMoneyDetail<>();
            detail.setAmount(new HashMap<>());
            detail.setDateFrom(dateFrom);
            detail.setDateTo(dateTo);

            try {
                var q = entityManager.createNativeQuery(
                        StringHelper.loadSQLFromFile("money", "money_balance_" + accountType + ".sql"), Tuple.class);
                q.setParameter("companyId", auth.getCompanyId());
                q.setParameter("dateFrom", dateFrom);
                q.setParameter("dateTo", dateTo.plusDays(1));
                q.setParameter("accountId", id);
                q.setParameter("currency", currency != null ? currency : "");

                @SuppressWarnings("unchecked")
                List<Tuple> results = addScalar(q, accountType).getResultList();

                if (!results.isEmpty()) {
                    Tuple r = results.get(0);
                    Long accountId = r.get("account_id", BigInteger.class).longValue();
                    if (accountType == AccountType.CASH) {
                        // noinspection unchecked
                        detail.setAccount((F) new DocCash(
                                accountId, DBType.POSTGRESQL, r.get("name", String.class), r.get("cashier", String.class)));
                    } else if (accountType == AccountType.BANK) {
                        DocBank docBank = new DocBank();
                        docBank.setName(r.get("bank_name", String.class));
                        // noinspection unchecked
                        detail.setAccount((F) new BankAccountDto(
                                accountId, DBType.POSTGRESQL, r.get("account", String.class), docBank));
                    } else if (accountType == AccountType.EMPLOYEE) {
                        // noinspection unchecked
                        detail.setAccount((F) new DocEmployee(
                                accountId, DBType.POSTGRESQL, r.get("name", String.class)));
                    } else {
                        throw new GamaException("No account type");
                    }

                    Set<String> usedCurrencies = new HashSet<>();
                    r.get("used_currencies", ArrayNode.class).forEach(c -> usedCurrencies.add(c.textValue()));
                    detail.setUsedCurrencies(usedCurrencies);
                }

                for (Tuple o : results) {
                    RepMoneyDetailCurrency report = new RepMoneyDetailCurrency();

                    String c = o.get("currency", String.class);
                    String bc = o.get("b_currency", String.class);

                    report.setOpening(GamaMoney.ofNullable(c, o.get("ob", BigDecimal.class)));
                    report.setDebit(GamaMoney.ofNullable(c, o.get("debit", BigDecimal.class)));
                    report.setCredit(GamaMoney.ofNullable(c, o.get("credit", BigDecimal.class)));
                    report.setBaseOpening(GamaMoney.ofNullable(bc, o.get("b_ob", BigDecimal.class)));
                    report.setBaseDebit(GamaMoney.ofNullable(bc, o.get("b_debit", BigDecimal.class)));
                    report.setBaseCredit(GamaMoney.ofNullable(bc, o.get("b_credit", BigDecimal.class)));

                    detail.getAmount().put(c, report);
                }
            } catch (Exception e) {
                log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }

            response.setAttachment(detail);
        }
        return response;
    }

    @SuppressWarnings("rawtypes")
    private NativeQuery addScalar(Query q, AccountType accountType) {
        NativeQuery nq = q.unwrap(NativeQuery.class);
        nq.addScalar("account_id");
        nq.addScalar("currency");
        nq.addScalar("ob");
        nq.addScalar("debit");
        nq.addScalar("credit");
        nq.addScalar("b_currency");
        nq.addScalar("b_ob");
        nq.addScalar("b_debit");
        nq.addScalar("b_credit");
        nq.addScalar("account_number");
        nq.addScalar("account_name");
        nq.addScalar("used_currencies"); //TODO spring migrate, JsonNodeBinaryType.INSTANCE);
        if (accountType == AccountType.CASH) {
            nq.addScalar("name");
            nq.addScalar("cashier");
        } else if (accountType == AccountType.BANK) {
            nq.addScalar("account");
            nq.addScalar("bank_name");
        } else if (accountType == AccountType.EMPLOYEE) {
            nq.addScalar("name");
            nq.addScalar("office");
            nq.addScalar("department");
        } else {
            throw new GamaException("No account type");
        }
        return nq;
    }

    private Predicate whereReportFlow(PageRequest request, CriteriaBuilder cb, Root<?> root, long id, String currency, AccountType accountType) {
        Predicate where = cb.and(cb.equal(root.get("accountId"), id), cb.equal(root.get("accountType"), accountType));
        if (currency != null) where = cb.and(where, cb.equal(root.get("exchange").get("currency"), currency));
        if (StringHelper.hasValue(request.getFilter())) {
            String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
            Predicate contentFilter = cb.or(
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get("counterparty").get("name"))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get("employee").get("name"))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get("cash").get("name"))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get("bankAccount").get("account"))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get("bankAccount").get("bank").get("name"))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get("bankAccount2").get("account"))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get("bankAccount2").get("bank").get("name"))), "%" + filter + "%")
                    );
            where = where == null ? contentFilter : cb.and(where, contentFilter);
        }
        return where;
    }

    private List<Order> orderReportFlow(CriteriaBuilder cb, Root<?> root) {
        return Arrays.asList(
                cb.asc(root.get("doc").get(Doc_.DATE)),
                cb.desc(root.get("amount").get(GamaMoney_.AMOUNT)),
                cb.asc(root.get("doc").get(Doc_.SERIES)),  // nulls last
                cb.asc(root.get("doc").get(Doc_.ORDINAL)), // nulls last
                cb.asc(root.get("doc").get(Doc_.NUMBER)),  // nulls last
                cb.asc(root.get("doc").get(Doc_.ID)));
    }

    private List<Selection<?>> selectIdsReportFlow(CriteriaBuilder cb, Root<?> root) {
        return Arrays.asList(
                root.get("doc").get(Doc_.DATE),
                root.get("amount").get(GamaMoney_.AMOUNT),
                root.get("doc").get(Doc_.SERIES),
                root.get("doc").get(Doc_.ORDINAL),
                root.get("doc").get(Doc_.NUMBER),
                root.get("doc").get(Doc_.ID),
                root.get("id").alias("id"));
    }

    public RepMoneyBalanceInterval reportBalanceInterval(ReportBalanceIntervalRequest request, AccountType accountType) {
        CompanySettings settings = dbServiceSQL.getCompanySettings(auth.getCompanyId());

        LocalDate dateFrom = DateUtils.max(settings.getStartAccounting(), request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        try {
            @SuppressWarnings("unchecked")
            List<Tuple> queryResult = entityManager.createNativeQuery(
                            StringHelper.loadSQLFromFile("money", "money_interval_" + accountType + ".sql"), Tuple.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("dateFrom", dateFrom)
                    .setParameter("dateTo", dateTo)
                    .setParameter("accountId", request.getAccountId() == null ? 0 : request.getAccountId())
                    .setParameter("timeInterval", (request.getInterval() == null ? ReportInterval.MONTH : request.getInterval()).name().toLowerCase())
                    .getResultList();
            if (CollectionsHelper.isEmpty(queryResult)) return null;

            class QueryResult {
                final long accountId;
                final String accountName;
                final LocalDate date;
                final BigDecimal baseBalance;
                final BigDecimal baseDebit;
                final BigDecimal baseCredit;

                public QueryResult(long accountId, String accountName, LocalDate date, BigDecimal baseBalance, BigDecimal baseDebit, BigDecimal baseCredit) {
                    this.accountId = accountId;
                    this.accountName = accountName;
                    this.date = date;
                    this.baseBalance = baseBalance;
                    this.baseDebit = baseDebit;
                    this.baseCredit = baseCredit;
                }

                public long getAccountId() {
                    return accountId;
                }

                public String getAccountName() {
                    return accountName;
                }

                public LocalDate getDate() {
                    return date;
                }

                public BigDecimal getBaseBalance() {
                    return baseBalance;
                }
            }

            List<QueryResult> queryData = queryResult.stream()
                    .map(record -> new QueryResult(
                            record.get("account_id", BigInteger.class).longValue(),
                            record.get("account_name", String.class),
                            record.get("dt", java.sql.Date.class).toLocalDate(),
                            record.get("base_balance_amount", BigDecimal.class),
                            record.get("base_debit_amount", BigDecimal.class),
                            record.get("base_credit_amount", BigDecimal.class)))
                    .toList();

            CompanySettings companySettings = auth.getSettings();
            String language = companySettings.getLanguage();
            String country = companySettings.getCountry();
            if ("lt".equals(language)) {
                country = "LT";
            } else if ("en".equals(language) &&
                    !"US".equals(country) && !"GB".equals(country) && !"CA".equals(country) && !"AU".equals(country)) {
                country = "GB";
            }
            Locale locale = Locale.of(language, country);
            ReportInterval interval = request.getInterval() != null ? request.getInterval() : ReportInterval.MONTH;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(INTERVAL_PATTERN.get(interval), locale);

            // extract and format labels
            Set<LocalDate> labelsSet = queryData.stream()
                    .map(QueryResult::getDate)
                    .collect(Collectors.toSet());

            if (interval != ReportInterval.DAY) {
                LocalDate dateMin = labelsSet.stream().min(Comparator.comparing(e -> e)).get();
                LocalDate dateMax = labelsSet.stream().max(Comparator.comparing(e -> e)).get();
                LocalDate date = dateMin;
                while (date.isBefore(dateMax)) {
                    date = switch (interval) {
                        case YEAR -> date.plusYears(1);
                        case QUARTER -> date.plusMonths(3);
                        case MONTH -> date.plusMonths(1);
                        case WEEK -> date.plusWeeks(1);
                        default -> dateMax;
                    };
                    labelsSet.add(date);
                }
            }
            List<LocalDate> labels = labelsSet.stream().sorted().toList();

            // keep server order
            List<Long> accounts = queryData.stream()
                    .map(QueryResult::getAccountId)
                    .distinct()
                    .toList();

            var datasets = new RepMoneyBalanceInterval.Dataset[accounts.size()];
            for (int i = 0; i < accounts.size(); ++i) {
                datasets[i] = new RepMoneyBalanceInterval.Dataset(labels.size());
            }

            queryData.forEach(record -> {
                int datasetIndex = accounts.indexOf(record.getAccountId());
                int valueIndex = labels.indexOf(record.getDate());
                datasets[datasetIndex].setAccountName(record.getAccountName());
                datasets[datasetIndex].setAccountId(record.getAccountId());
                datasets[datasetIndex].getData()[valueIndex] = new RepMoneyBalanceInterval.DatasetData(record.date, record.baseBalance, record.baseDebit, record.baseCredit);
            });

            return new RepMoneyBalanceInterval(
                    labels.stream().map(dt -> dt.format(dateTimeFormatter)).toList(),
                    Arrays.asList(datasets));

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return null;
    }
}
