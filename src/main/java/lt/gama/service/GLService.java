package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lt.gama.api.request.GLAccountRequest;
import lt.gama.api.request.GLReportBalanceRequest;
import lt.gama.api.request.IntermediateBalanceRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.documents.GLOpeningBalanceDto;
import lt.gama.model.dto.documents.items.GLOpeningBalanceOperationDto;
import lt.gama.model.dto.entities.*;
import lt.gama.model.i.IGLOperation;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.documents.items.GLOperationSql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.report.RepTrialBalance;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GLService {

    private static final Logger log = LoggerFactory.getLogger(GLService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final DBServiceSQL dbServiceSQL;
    private final GLAccountSqlMapper glAccountSqlMapper;
    private final GLSaftAccountSqlMapper glSaftAccountSqlMapper;
    private final GLOpeningBalanceSqlMapper glOpeningBalanceSqlMapper;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final StorageService storageService;
    private final GLOperationsService glOperationsService;
    private final GLUtilsService glUtilsService;
    private final Auth auth;
    private final ResponsibilityCenterSqlMapper responsibilityCenterSqlMapper;
    private final DebtCorrectionSqlMapper debtCorrectionSqlMapper;
    private final DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final BankAccountSqlMapper bankAccountSqlMapper;
    private final CashSqlMapper cashSqlMapper;
    private final EmployeeOperationSqlMapper employeeOperationSqlMapper;
    private final BankOperationSqlMapper bankOperationSqlMapper;
    private final CashOperationSqlMapper cashOperationSqlMapper;
    private final EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper;
    private final BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper;
    private final CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper;
    private final InventorySqlMapper inventorySqlMapper;
    private final TransProdSqlMapper transportationSqlMapper;
    private final PurchaseSqlMapper purchaseSqlMapper;
    private final InvoiceSqlMapper invoiceSqlMapper;
    private final DocsMappersService docsMappersService;

    
    public GLService(DBServiceSQL dbServiceSQL,
                     GLAccountSqlMapper glAccountSqlMapper,
                     GLSaftAccountSqlMapper glSaftAccountSqlMapper,
                     GLOpeningBalanceSqlMapper glOpeningBalanceSqlMapper,
                     DoubleEntrySqlMapper doubleEntrySqlMapper,
                     StorageService storageService,
                     GLOperationsService glOperationsService,
                     GLUtilsService glUtilsService,
                     Auth auth,
                     ResponsibilityCenterSqlMapper responsibilityCenterSqlMapper,
                     DebtCorrectionSqlMapper debtCorrectionSqlMapper,
                     DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper,
                     EmployeeSqlMapper employeeSqlMapper,
                     BankAccountSqlMapper bankAccountSqlMapper,
                     CashSqlMapper cashSqlMapper,
                     EmployeeOperationSqlMapper employeeOperationSqlMapper,
                     BankOperationSqlMapper bankOperationSqlMapper,
                     CashOperationSqlMapper cashOperationSqlMapper,
                     EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper,
                     BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper,
                     CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper,
                     InventorySqlMapper inventorySqlMapper,
                     TransProdSqlMapper transportationSqlMapper,
                     PurchaseSqlMapper purchaseSqlMapper,
                     InvoiceSqlMapper invoiceSqlMapper,
                     DocsMappersService docsMappersService) {
        this.dbServiceSQL = dbServiceSQL;
        this.glAccountSqlMapper = glAccountSqlMapper;
        this.glSaftAccountSqlMapper = glSaftAccountSqlMapper;
        this.glOpeningBalanceSqlMapper = glOpeningBalanceSqlMapper;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.storageService = storageService;
        this.glOperationsService = glOperationsService;
        this.glUtilsService = glUtilsService;
        this.auth = auth;
        this.responsibilityCenterSqlMapper = responsibilityCenterSqlMapper;
        this.debtCorrectionSqlMapper = debtCorrectionSqlMapper;
        this.debtRateInfluenceSqlMapper = debtRateInfluenceSqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.bankAccountSqlMapper = bankAccountSqlMapper;
        this.cashSqlMapper = cashSqlMapper;
        this.employeeOperationSqlMapper = employeeOperationSqlMapper;
        this.bankOperationSqlMapper = bankOperationSqlMapper;
        this.cashOperationSqlMapper = cashOperationSqlMapper;
        this.employeeRateInfluenceSqlMapper = employeeRateInfluenceSqlMapper;
        this.bankRateInfluenceSqlMapper = bankRateInfluenceSqlMapper;
        this.cashRateInfluenceSqlMapper = cashRateInfluenceSqlMapper;
        this.inventorySqlMapper = inventorySqlMapper;
        this.transportationSqlMapper = transportationSqlMapper;
        this.purchaseSqlMapper = purchaseSqlMapper;
        this.invoiceSqlMapper = invoiceSqlMapper;
        this.docsMappersService = docsMappersService;
    }

    public void templateAccount() {
        dbServiceSQL.executeInTransaction(entityManager -> {
            entityManager.createQuery("DELETE FROM " + GLAccountSql.class.getName() + " a WHERE a.companyId = :companyId")
                    .setParameter("companyId", auth.getCompanyId())
                    .executeUpdate();
            List<GLAccountSql> templates = entityManager.createQuery(
                            "SELECT a FROM " + GLAccountSql.class.getName() + " a WHERE a.companyId = 0", GLAccountSql.class)
                    .getResultList();
            templates.forEach(entity -> {
                GLAccountSql glAccount = new GLAccountSql();
                glAccount.setCompanyId(auth.getCompanyId());
                glAccount.setNumber(entity.getNumber());
                glAccount.setName(entity.getName());
                glAccount.setParent(entity.getParent());
                glAccount.setDepth(entity.getDepth());
                glAccount.setInner(entity.isInner());
                glAccount.setType(entity.getType());
                entityManager.persist(glAccount);
            });
        });
    }

    public PageResponse<GLSaftAccountDto, Void> getGLSaftAccounts() {
        List<GLSaftAccountSql> accounts = entityManager.createQuery(
                        "SELECT a FROM " + GLSaftAccountSql.class.getName() + " a" +
                                " WHERE (a.archive IS null OR a.archive = false)" +
                                " AND (a.hidden IS null OR a.hidden = false)",
                        GLSaftAccountSql.class)
                .getResultList();
        PageResponse<GLSaftAccountDto, Void> response = new PageResponse<>();
        response.setMore(false);
        response.setTotal(accounts.size());
        response.setItems(accounts.stream().map(glSaftAccountSqlMapper::toDto).collect(Collectors.toList()));
        return response;
    }

    public void assignSaft() {
        dbServiceSQL.executeInTransaction(entityManager ->
                entityManager.createNativeQuery(
                                "UPDATE gl_accounts a" +
                                        " SET saft_number =  b.number" +
                                        " FROM gl_saft_accounts b" +
                                        " WHERE a.company_id = :companyId" +
                                        " AND a.number = b.number AND b.inner = false" +
                                        " AND (a.archive IS null OR a.archive = false)" +
                                        " AND (a.hidden IS null OR a.hidden = false)")
                        .setParameter("companyId", auth.getCompanyId())
                        .executeUpdate());
    }

    public GLAccountDto getAccount(String number) {
        // get account by number
        List<GLAccountSql> glAccounts = entityManager.createQuery(
                        "SELECT a FROM " + GLAccountSql.class.getName() + " a WHERE a.companyId = :companyId AND a.number = :number",
                        GLAccountSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("number", number)
                .getResultList();

        if (CollectionsHelper.isEmpty(glAccounts)) {
            return null;
        }
        if (glAccounts.size() > 1) {
            throw new GamaException("Wrong DB data: Found " + glAccounts.size() + " G.L. accounts with the same number " + number);
        }
        return this.glAccountSqlMapper.toDto(glAccounts.get(0));
    }

    public void deleteAccount(GLAccountRequest request) {
        final GLAccountDto parent = request.getParent();
        final GLAccountDto model = request.getModel();
        dbServiceSQL.executeInTransaction(entityManager -> {
            if (parent != null && parent.getId() != null) {
                GLAccountSql entityParent = Validators.checkNotNull(dbServiceSQL.getById(GLAccountSql.class, parent.getId()), "No parent GLAccount");
                entityParent.setInner(parent.isInner());
            }
            GLAccountSql entity = Validators.checkNotNull(dbServiceSQL.getById(GLAccountSql.class, model.getId()), "No GLAccount");
            entity.setArchive(true);
        });
    }

    private GLAccountSql saveAccountInTransaction(GLAccountRequest request) {
        final GLAccountDto parent = request.getParent();
        final GLAccountDto model = request.getModel();

        if (StringHelper.isEmpty(model.getNumber())) {
            throw new GamaException(
                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountNumber, auth.getLanguage()));
        }

        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            // check if number not exists already
            List<GLAccountSql> glAccounts = entityManager.createQuery(
                            "SELECT a FROM " + GLAccountSql.class.getName() + " a WHERE a.companyId = :companyId AND a.number = :number",
                            GLAccountSql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("number", model.getNumber())
                    .getResultList();

            if (glAccounts != null) {
                for (GLAccountSql acc : glAccounts) {
                    if (BooleanUtils.isTrue(acc.getArchive())) continue;
                    if (model.getId() != null && model.getId().equals(acc.getId())) continue;
                    if (model.getNumber().equals(acc.getNumber())) {
                        throw new GamaException(
                                MessageFormat.format(
                                        TranslationService.getInstance().translate(TranslationService.GL.SameGLAccountNumberFound, auth.getLanguage()), model.getNumber()));
                    }
                }
            }

            GLAccountSql account = model.getId() == null ? null : dbServiceSQL.getById(GLAccountSql.class, model.getId());
            GLAccountDto accountDto = glAccountSqlMapper.toDto(account);

            if (accountDto == null || !accountDto.equals(model)) {
                if (parent != null && parent.getId() != null) {
                    GLAccountSql entityParent = dbServiceSQL.getAndCheck(GLAccountSql.class, parent.getId());
                    entityParent.setInner(parent.isInner());
                }
                if (account == null) {
                    account = new GLAccountSql();
                }
                account.setCompanyId(auth.getCompanyId());
                account.setDepth(model.getDepth());
                account.setInner(model.isInner());
                account.setType(model.getType());
                account.setName(model.getName());
                account.setNumber(model.getNumber());
                account.setParent(model.getParent());
                account.setTranslation(model.getTranslation());
                if (model.getSaftAccount() != null && StringHelper.hasValue(model.getSaftAccount().getNumber())) {
                    account.setSaftAccount(entityManager.getReference(GLSaftAccountSql.class, model.getSaftAccount().getNumber()));
                }
                entityManager.persist(account);
            }

            return account;
        });
    }

    public GLAccountDto saveAccount(GLAccountRequest request) {
        GLAccountSql account = saveAccountInTransaction(request);
        return glAccountSqlMapper.toDto(account);
    }

    private Integer countQueryRC(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT rc.id) FROM resp_centers rc");
        sj.add("WHERE rc.company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        makeQueryRC(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void makeQueryRC(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        sj.add("AND (rc.archive IS null OR rc.archive = false)");
        Boolean hidden = (Boolean) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.HIDDEN);
        if (BooleanUtils.isNotTrue(hidden)) sj.add("AND (rc.hidden IS null OR rc.hidden = false)");
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("rc.name ILIKE :filter");
            sj.add("OR trim(unaccent(rc.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(rc.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter().trim()).toLowerCase() + "%");
        }
    }

    private void makeOrderedTreeRC(StringJoiner sj, Map<String, Object> params) {
        sj.add("WITH RECURSIVE a(id, company_id, archive, hidden, name, description, parent, depth, path) AS (");
        sj.add("SELECT id, company_id, archive, hidden, name, description, parent, 0, ARRAY[ROW(rc.name, rc.id)]");
        sj.add("FROM resp_centers rc");
        sj.add("WHERE company_id = :companyId and parent IS NULL");
        sj.add("UNION ALL");
        sj.add("SELECT rc.id, rc.company_id, rc.archive, rc.hidden, rc.name, rc.description, rc.parent, a.depth + 1, path || ROW(rc.name, rc.id)");
        sj.add("FROM resp_centers rc");
        sj.add("JOIN a ON a.id = rc.parent");
        sj.add(")");
        params.put("companyId", auth.getCompanyId());
    }

    private Query dataQueryRC(PageRequest request) {
        int cursor = request.getCursor();

        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner("\n");
        makeOrderedTreeRC(sj, params);
        sj.add("SELECT rc.* AS id FROM a");
        sj.add("JOIN resp_centers rc ON rc.id = a.id");
        makeQueryRC(request, sj, params);
        sj.add("ORDER BY a.path");

        Query query = entityManager.createNativeQuery(sj.toString(), ResponsibilityCenterSql.class)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1);
        params.forEach(query::setParameter);
        return query;
    }

    public PageResponse<ResponsibilityCenterDto, Void> listRC(PageRequest request) {
        return dbServiceSQL.queryPage(request, ResponsibilityCenterSql.class, null,
                responsibilityCenterSqlMapper,
                null,
                () -> countQueryRC(request),
                resp -> dataQueryRC(request));
    }

    private void makeParentIdTreeRC(StringJoiner sj, Map<String, Object> params) {
        sj.add("WITH RECURSIVE a(id, ids) AS (");
        sj.add("SELECT id, ARRAY[rc.id]");
        sj.add("FROM resp_centers rc");
        sj.add("WHERE company_id = :companyId AND parent IS NULL");
        sj.add("UNION ALL");
        sj.add("SELECT rc.id, ids || rc.id");
        sj.add("FROM resp_centers rc");
        sj.add("JOIN a ON a.id = rc.parent");
        sj.add(")");
        params.put("companyId", auth.getCompanyId());
    }

    private void makeAncestorsIdTreeRC(StringJoiner sj, Map<String, Object> params) {
        sj.add("WITH RECURSIVE b(id, parent) AS (");
        sj.add("SELECT id, parent");
        sj.add("FROM resp_centers rc");
        sj.add("WHERE company_id = :companyId AND parent IS NOT NULL");
        sj.add("UNION ALL");
        sj.add("SELECT rc.id, b.parent");
        sj.add("FROM resp_centers rc");
        sj.add("JOIN b ON b.id = rc.parent");
        sj.add("), b2(id, ids) AS (");
        sj.add("SELECT b.parent, ARRAY_AGG(b.id)");
        sj.add("FROM b");
        sj.add("GROUP BY b.parent");
        sj.add("), a(id, ids) AS (");
        sj.add("SELECT rc.id, ids || rc.id");
        sj.add("FROM resp_centers rc");
        sj.add("LEFT JOIN b2 ON b2.id = rc.id");
        sj.add(")");
        params.put("companyId", auth.getCompanyId());
    }

    public Integer updateHiddenStatus(Long id, boolean status) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            Map<String, Object> params = new HashMap<>();
            StringJoiner sj = new StringJoiner("\n");
            if (status) {
                makeParentIdTreeRC(sj, params);
            } else {
                makeAncestorsIdTreeRC(sj, params);
            }
            sj.add("UPDATE resp_centers r SET hidden = :status");
            sj.add("FROM a");
            sj.add("WHERE a.id = r.id AND :id = ANY(ids)");
            params.put("status", status);
            params.put("id", id);

            Query query = entityManager.createNativeQuery(sj.toString());
            params.forEach(query::setParameter);

            return query.executeUpdate();
        });
    }

    public Integer hideRC(Long id) {
        return updateHiddenStatus(id, true);
    }

    public Integer showRC(Long id) {
        return updateHiddenStatus(id, false);
    }

    private Integer updateArchiveStatus(Long id, boolean status) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            Map<String, Object> params = new HashMap<>();
            StringJoiner sj = new StringJoiner("\n");
            if (status) {
                makeParentIdTreeRC(sj, params);
            } else {
                makeAncestorsIdTreeRC(sj, params);
            }
            sj.add("UPDATE resp_centers r SET archive = :status");
            sj.add("FROM a");
            sj.add("WHERE a.id = r.id AND :id = ANY(ids)");
            params.put("status", status);
            params.put("id", id);

            Query query = entityManager.createNativeQuery(sj.toString());
            params.forEach(query::setParameter);

            return query.executeUpdate();
        });
    }

    public Integer deleteRC(Long id) {
        return updateArchiveStatus(id, true);
    }

    public Integer undeleteRC(Long id) {
        return updateArchiveStatus(id, false);
    }

    public List<ResponsibilityCenterSql> filterRC(String filter, int maxItems) {
        //noinspection unchecked
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            Map<String, Object> params = new HashMap<>();
            StringJoiner sj = new StringJoiner(" ");

            sj.add("SELECT rc.* FROM resp_centers rc");
            sj.add("WHERE company_id = :companyId");
            params.put("companyId", auth.getCompanyId());
            sj.add("AND (archive IS null OR archive = false)");
            sj.add("AND (hidden IS null OR hidden = false)");
            if (StringHelper.hasValue(filter)) {
                sj.add("AND (");
                sj.add("unaccent(trim(name)) ILIKE :filter");
                sj.add("OR trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
                sj.add(")");
                params.put("filter", "%" + StringUtils.stripAccents(filter).toLowerCase() + "%");
            }
            sj.add("ORDER BY unaccent(trim(name))");
            Query query = entityManager.createNativeQuery(sj.toString(), ResponsibilityCenterSql.class);
            params.forEach(query::setParameter);

            return query.setMaxResults(maxItems).getResultList();
        });
    }

    public Integer countRC() {
        return entityManager.createQuery(
                        "SELECT COUNT(*) FROM " + ResponsibilityCenterSql.class.getName() + " a" +
                                " WHERE " +  ResponsibilityCenterSql_.COMPANY_ID + " = :companyId" +
                                " AND (archive IS null OR archive = false)" +
                                " AND (hidden IS null OR hidden = false)", Long.class)
                .setParameter("companyId", auth.getCompanyId())
                .getSingleResult()
                .intValue();
    }

    public GLOpeningBalanceDto countIntermediateBalance(IntermediateBalanceRequest request) {
        int yearToClose = request.getYearToClose();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        LocalDate yearToCloseEndDate = LocalDate.of( yearToClose + 1, companySettings.getAccMonth() , 1).minusDays(1);

        if (yearToClose >= companySettings.getAccYear() || yearToClose < companySettings.getStartAccounting().getYear())
            throw new GamaException("yearToClose " + yearToClose +
                    " is not from period " + companySettings.getAccYear() + " - " +
                    companySettings.getStartAccounting().getYear());

        List<Tuple> qResult = getIB(yearToCloseEndDate, companySettings.getStartAccounting());
        Map<String, IBData> balanceDataByAccount = encodeFromDB(qResult);

        List<GLOpeningBalanceOperationDto> opList = new ArrayList<>();
        balanceDataByAccount.values().forEach(ibData -> makeOpeningBalanceOp(opList, ibData));

        GLOpeningBalanceDto intBalance = new GLOpeningBalanceDto();
        intBalance.setDate(yearToCloseEndDate);
        intBalance.setIBalance(true);
        intBalance.setAutoNumber(true);
        intBalance.setFinishedGL(true);
        intBalance.setBalances(opList);

        return glOpeningBalanceSqlMapper.toDto(
                dbServiceSQL.saveWithCounter(glOpeningBalanceSqlMapper.toEntity(intBalance)));
    }

    private void makeOpeningBalanceOp(List<GLOpeningBalanceOperationDto> opList, IBData ibData) {
        BigDecimal amount = ibData.getAmount();
        final String currency = ibData.getCurrency();
        if (ibData.getRcs() != null) {
            ibData.getRcs().stream()
                    .filter(rc -> BigDecimalUtils.isNonZero(rc.getAmount()))
                    .forEach(rc ->
                            opList.add(new GLOpeningBalanceOperationDto(
                                    new GLOperationAccount(ibData.getNumber(), ibData.getName()),
                                    new DocRC(rc.getId().longValue(), rc.getName()),
                                    BigDecimalUtils.isPositive(rc.getAmount()) ? GamaMoney.of(currency, rc.getAmount()) : null,
                                    BigDecimalUtils.isNegative(rc.getAmount()) ? GamaMoney.of(currency, rc.getAmount().negate()) : null
                            )));
            BigDecimal rcAmount = ibData.getRcs().stream()
                    .map(IBRc::getAmount)
                    .filter(BigDecimalUtils::isNonZero)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amount = BigDecimalUtils.subtract(amount, rcAmount);
        }
        if (BigDecimalUtils.isNonZero(amount)) {
            opList.add(new GLOpeningBalanceOperationDto(
                    new GLOperationAccount(ibData.getNumber(), ibData.getName()),
                    BigDecimalUtils.isPositive(amount) ? GamaMoney.of(currency, amount) : null,
                    BigDecimalUtils.isNegative(amount) ? GamaMoney.of(currency, amount.negate()) : null
            ));
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Tuple> getIB(LocalDate yearToCloseEndDate, LocalDate startAccounting) {
        return entityManager
                .createNativeQuery(StringHelper.loadSQLFromFile("gl", "ob_intermediate.sql"), Tuple.class)
                .setParameter("dateTo", yearToCloseEndDate)
                .setParameter("startAccounting", startAccounting.minusDays(1))
                .setParameter("companyId", auth.getCompanyId())
                .getResultList();
    }

    protected Map<String, IBData> encodeFromDB(List<Tuple> qResult) {
        Map<String, IBData> iBDataByAccount = new TreeMap<>();
        for (Tuple o : qResult) {
            String number = o.get("number", String.class);
            IBData iBData = iBDataByAccount.get(number);
            if (iBData == null) {
                iBData = new IBData();
                iBData.setNumber(o.get("number", String.class));
                iBData.setName(o.get("name", String.class));
                iBData.setCurrency(o.get("currency", String.class));
                iBDataByAccount.put(number, iBData);
            }

            String operationType = o.get("operation_type", String.class);
            BigDecimal amount = BigDecimalUtils.subtract(
                    o.get("ob_deb", BigDecimal.class),
                    o.get("ob_cred", BigDecimal.class));

            if ("all".equals(operationType)) {
                iBData.setAmount(amount);
            } else {
                iBData.getRcs().add(new IBRc(
                        o.get("rc_id", BigInteger.class),
                        o.get("rc_name", String.class),
                        amount));
            }
        }

        return iBDataByAccount;
    }

    public static class IBData {
        private String currency;
        private String number;
        private String name;
        private BigDecimal amount;
        private List<IBRc> rcs = new LinkedList<>();

        // generated

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public List<IBRc> getRcs() {
            return rcs;
        }

        public void setRcs(List<IBRc> rcs) {
            this.rcs = rcs;
        }
    }

    public static class IBRc {
        private BigInteger id;
        private String name;
        private BigDecimal amount;

        public IBRc(BigInteger id, String name, BigDecimal amount) {
            this.id = id;
            this.name = name;
            this.amount = amount;
        }

        // generated

        public BigInteger getId() {
            return id;
        }

        public void setId(BigInteger id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public List<RepTrialBalance> reportTrialBalance(GLReportBalanceRequest request) {
        LocalDate dateFrom = request.getDateFrom();
        LocalDate dateTo = request.getDateTo();
        boolean withRC = BooleanUtils.isTrue(request.getWithRC());
        boolean withHiddenRC = BooleanUtils.isTrue(request.getWithHiddenRC());

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        LocalDate startAccounting = companySettings.getStartAccounting();
        Validators.checkNotNull(startAccounting, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoAccountingStartDate, auth.getLanguage()));
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        var query = entityManager
                .createNativeQuery(StringHelper.loadSQLFromFile("gl", withRC ? "trial_balance_rc.sql" : "trial_balance.sql"), Tuple.class)
                .setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo)
                .setParameter("startAccounting", startAccounting.minusDays(1))
                .setParameter("companyId", auth.getCompanyId());
        if (withRC) query.setParameter("hiddenRC", withHiddenRC);

        @SuppressWarnings("unchecked")
        List<Tuple> r = query.getResultList();

        List<RepTrialBalance> report = new ArrayList<>(r.size());

        for (Tuple o : r) {
            RepTrialBalance repTrialBalance = withRC ?
                    new RepTrialBalance(
                            o.get("rc_name", String.class),
                            o.get("rc_id", BigInteger.class),
                            o.get("number", String.class),
                            o.get("name", String.class),
                            o.get("currency", String.class),
                            o.get("ob", BigDecimal.class),
                            o.get("debit", BigDecimal.class),
                            o.get("credit", BigDecimal.class)
                    ) :
                    new RepTrialBalance(
                            o.get("number", String.class),
                            o.get("name", String.class),
                            o.get("currency", String.class),
                            o.get("ob", BigDecimal.class),
                            o.get("debit", BigDecimal.class),
                            o.get("credit", BigDecimal.class)
                    );
            report.add(repTrialBalance);
        }
        return report;
    }

    public PageResponse<DoubleEntryDto, RepTrialBalance> reportFlow(PageRequest request) {
        LocalDate dateFrom = Validators.checkNotNull(request.getDateFrom(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateFrom, auth.getLanguage()));
        LocalDate dateTo = Validators.checkNotNull(request.getDateTo(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateTo, auth.getLanguage()));
        Validators.checkArgument(!dateFrom.isAfter(dateTo), MessageFormat.format(
                TranslationService.getInstance().translate(TranslationService.VALIDATORS.DateFromAfterDateEnd, auth.getLanguage()),
                dateFrom, dateTo));

        Long rcId = (Long) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.GL_RC);

        // need to check all parameters before query because they will be transformed
        final String accountNumber = Validators.checkNotNull((String) PageRequestUtils.getFieldValue(request.getConditions(),
                CustomSearchType.GL_ACCOUNTS.getField()), "No account");

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        LocalDate startAccounting = companySettings.getStartAccounting();
        Validators.checkNotNull(startAccounting, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoAccountingStartDate, auth.getLanguage()));

        PageResponse<DoubleEntryDto, RepTrialBalance> response = new PageResponse<>();

        if (request.isRefresh()) {
            var q = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("gl", rcId != null ? "flow_rc.sql" : "flow.sql"), Tuple.class)
                    .setParameter("dateFrom", dateFrom)
                    .setParameter("dateTo", dateTo)
                    .setParameter("startAccounting", startAccounting.minusDays(1))
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("accountNumber", accountNumber);
            if (rcId != null) q.setParameter("rcId", rcId);
            @SuppressWarnings("unchecked")
            List<Tuple> results = q.getResultList();

            if (results.size() == 1) {
                Tuple o = results.get(0);
                RepTrialBalance repTrialBalance = rcId != null ?
                        new RepTrialBalance(
                                o.get("rc_name", String.class),
                                o.get("rc_id", BigInteger.class),
                                o.get("number", String.class),
                                o.get("name", String.class),
                                o.get("currency", String.class),
                                o.get("ob", BigDecimal.class),
                                o.get("debit", BigDecimal.class),
                                o.get("credit", BigDecimal.class)
                        ) :
                        new RepTrialBalance(
                                o.get("number", String.class),
                                o.get("name", String.class),
                                o.get("currency", String.class),
                                o.get("ob", BigDecimal.class),
                                o.get("debit", BigDecimal.class),
                                o.get("credit", BigDecimal.class)
                        );
                response.setAttachment(repTrialBalance);
            }
        }

        if (request.getTotal() <= 0) {
            var cq = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("gl", rcId != null ? "flow_count_rc.sql" : "flow_count.sql"));
            cq.setParameter("dateFrom", dateFrom);
            cq.setParameter("dateTo", dateTo);
            cq.setParameter("accountNumber", accountNumber);
            cq.setParameter("companyId", auth.getCompanyId());
            if (rcId != null) cq.setParameter("rcId", rcId);

            Number cr = (Number) cq.getSingleResult();

            if (cr != null) {
                response.setTotal(cr.intValue());
            }
        } else {
            response.setTotal(request.getTotal());
        }

        int cursor;
        if (request.getCursor() == null) {
            cursor = 0;
        } else {
            cursor = request.getCursor();
        }

        if (request.isBackward() && cursor >= request.getPageSize()) {
            cursor = cursor - request.getPageSize();
        }

        var idq = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("gl", rcId != null ? "flow_ids_rc.sql" : "flow_ids.sql"), Tuple.class);
        idq.setParameter("dateFrom", dateFrom);
        idq.setParameter("dateTo", dateTo);
        idq.setParameter("accountNumber", accountNumber);
        idq.setParameter("companyId", auth.getCompanyId());
        if (rcId != null) idq.setParameter("rcId", rcId);

        idq.setMaxResults(request.getPageSize() + 1);
        idq.setFirstResult(cursor);

        @SuppressWarnings("unchecked")
        List<Tuple> biIds = idq.getResultList();

        if (biIds.size() > request.getPageSize()) {
            response.setMore(true);
            biIds.remove(biIds.size() - 1);
        }

        if (!biIds.isEmpty()) {
            List<Long> lIds = biIds.stream().map(x -> x.get("id", Number.class).longValue()).collect(Collectors.toList());
            response.setItems(entityManager
                    .createQuery("SELECT DISTINCT de " +
                                    " FROM " + DoubleEntrySql.class.getName() + " de " +
                                    " LEFT JOIN FETCH de." + DoubleEntrySql_.OPERATIONS +
                                    " LEFT JOIN FETCH de." + DoubleEntrySql_.PARENT_COUNTERPARTY +
                                    " WHERE de.id IN :ids " +
                                    " ORDER BY de.date, de.ordinal, de.number, de.id",
                            DoubleEntrySql.class)
                    .setParameter("ids", lIds)
                    .getResultStream()
                    .map(doubleEntrySqlMapper::toDto)
                    .collect(Collectors.toList()));
        }

        if (request.isBackward() && cursor == 0) response.setMore(false);
        if (!request.isBackward()) cursor = cursor + request.getPageSize();

        response.setCursor(cursor);

        return response;
    }

    public List<GLOperationDto> closeProfitLoss(LocalDate date) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkNotNull(companySettings.getGl(), TranslationService.getInstance().translate(TranslationService.GL.NoGLSettings, auth.getLanguage()));

        Validators.checkValid(companySettings.getGl().getAccProfitLoss(), TranslationService.getInstance().translate(TranslationService.GL.NoProfitLossAcc, auth.getLanguage()));
        Validators.checkValid(companySettings.getGl().getAccTemp(), TranslationService.getInstance().translate(TranslationService.GL.NoInterimAcc, auth.getLanguage()));

        LocalDate startAccounting = companySettings.getStartAccounting();
        Validators.checkReportDate(startAccounting, date, auth.getLanguage());

        List<Tuple> qResult = getPL(date, startAccounting);
        Map<String, IBData> balanceDataByAccount = encodeFromDB(qResult);

        List<GLOpeningBalanceOperationDto> balances = new ArrayList<>();
        balanceDataByAccount.values().forEach(data -> makeOpeningBalanceOp(balances, data));

        List<GLOperationDto> operations = new ArrayList<>();
        if (CollectionsHelper.isEmpty(balances)) return operations;

        GamaMoney total = null;

        for (GLOpeningBalanceOperationDto balance : balances) {
            GamaMoney sum = GamaMoneyUtils.subtract(balance.getDebit(), balance.getCredit());

            total = GamaMoneyUtils.add(total, sum);

            if (GamaMoneyUtils.isPositive(sum)) {
                operations.add(new GLOperationDto(companySettings.getGl().getAccTemp(), null, balance.getAccount(), balance.getRc(), sum));
            } else if (GamaMoneyUtils.isNegative(sum)) {
                operations.add(new GLOperationDto(balance.getAccount(), balance.getRc(), companySettings.getGl().getAccTemp(), null, sum.negated()));
            }
        }

        if (GamaMoneyUtils.isPositive(total)) {
            operations.add(new GLOperationDto(companySettings.getGl().getAccProfitLoss(), companySettings.getGl().getAccTemp(), total));
        } else if (GamaMoneyUtils.isNegative(total)) {
            operations.add(new GLOperationDto(companySettings.getGl().getAccTemp(), companySettings.getGl().getAccProfitLoss(), total.negated()));
        }
        return operations;
    }

    @SuppressWarnings("unchecked")
    protected List<Tuple> getPL(LocalDate date, LocalDate startAccounting) {
        return entityManager
                .createNativeQuery(StringHelper.loadSQLFromFile("gl", "close_profit_loss_rc.sql"), Tuple.class)
                .setParameter("dateTo", date)
                .setParameter("startAccounting", startAccounting.minusDays(1))
                .setParameter("companyId", auth.getCompanyId())
                .getResultList();
    }

    public DoubleEntryDto finish(long id, DBType parentDbType, Boolean parent, String graphName) {
        return doubleEntrySqlMapper.toDto(glUtilsService.updateState(id, parentDbType, parent, true, true));
    }

    public DoubleEntryDto recall(long id, DBType parentDbType, Boolean parent, String graphName) {
        return doubleEntrySqlMapper.toDto(glUtilsService.updateState(id, parentDbType, parent, false, true));
    }

    public GLOpeningBalanceDto finishOpeningBalance(long id, String graphName) {
        return updateOpeningBalanceState(id, true, graphName);
    }

    public GLOpeningBalanceDto recallOpeningBalance(long id, String graphName) {
        return updateOpeningBalanceState(id, false, graphName);
    }

    private GLOpeningBalanceDto updateOpeningBalanceState(long id, boolean finish, String graphName) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        return glOpeningBalanceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            GLOpeningBalanceSql document = dbServiceSQL.getAndCheck(GLOpeningBalanceSql.class, id, graphName);
            Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());
            if (BooleanUtils.isTrue(document.getFinishedGL()) != finish) document.setFinishedGL(finish);
            return document;
        }));
    }

    public GLOpeningBalanceDto importOpeningBalance(long id, String fileName) {
        GLOpeningBalanceDto entity = glOpeningBalanceSqlMapper.toDto(dbServiceSQL.getAndCheck(GLOpeningBalanceSql.class, id));
        if (BooleanUtils.isTrue(entity.getFinishedGL())) return entity;
        return storageService.importGLOpeningBalance(entity, fileName);
    }

    public DoubleEntrySql regenerateDoubleEntry(DoubleEntrySql doubleEntry) {
        if (doubleEntry.getParentId() == null) {
            return glUtilsService.saveDoubleEntry(doubleEntry);
        }

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (BooleanUtils.isNotTrue(doubleEntry.getFrozen())) {

            IBaseDocument document;
            var docClass = doubleEntry.getParentType() != null
                    ? docsMappersService.getDocumentClass(doubleEntry.getParentDb(), doubleEntry.getParentType())
                    : dbServiceSQL.getById(BaseDocumentSql.class, doubleEntry.getParentId()).getClass();

            @SuppressWarnings("unchecked")
            var docClassSql = (Class<BaseDocumentSql>) docClass;
            document = dbServiceSQL.getById(docClassSql, doubleEntry.getParentId(), docsMappersService.getGraphName(docClassSql));

            Validators.checkDocumentDate(companySettings, document, auth.getLanguage());
            document.setDoubleEntry(doubleEntrySqlMapper.toDto(doubleEntry));

            if (document instanceof BankOperationSql) {
                if (Validators.isValid(((BankOperationSql) document).getBankAccount())) {
                    BankAccountDto bankAccount = bankAccountSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(BankAccountSql.class,
                            ((BankOperationSql) document).getBankAccount().getId(), ((BankOperationSql) document).getBankAccount().getDb()));
                    return glOperationsService.finishBankOperation(bankAccount.getMoneyAccount(),
                            bankOperationSqlMapper.toDto((BankOperationSql) document), null, false);
                }
            } else if (document instanceof CashOperationSql) {
                if (Validators.isValid(((CashOperationSql) document).getCash())) {
                    CashDto cash = cashSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(CashSql.class,
                            ((CashOperationSql) document).getCash().getId(), ((CashOperationSql) document).getCash().getDb()));
                    return glOperationsService.finishCashOperation(cash.getMoneyAccount(),
                            cashOperationSqlMapper.toDto((CashOperationSql) document), null, false);
                }
            } else if (document instanceof EmployeeOperationSql) {
                if (Validators.isValid(((EmployeeOperationSql) document).getEmployee())) {
                    EmployeeDto employee = employeeSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(EmployeeSql.class,
                            ((EmployeeOperationSql) document).getCash().getId(), ((EmployeeOperationSql) document).getCash().getDb()));
                    return glOperationsService.finishEmployeeOperation(employee.getName(), employee.getMoneyAccount(),
                            employeeOperationSqlMapper.toDto((EmployeeOperationSql) document), null, false);
                }

            } else if (document instanceof EmployeeRateInfluenceSql) {
                return glOperationsService.finishMoneyRateInfluence(EmployeeSql.class,
                        employeeRateInfluenceSqlMapper.toDto((EmployeeRateInfluenceSql) document), null, false);
            } else if (document instanceof BankRateInfluenceSql) {
                return glOperationsService.finishMoneyRateInfluence(BankAccountSql.class,
                        bankRateInfluenceSqlMapper.toDto((BankRateInfluenceSql) document), null, false);
            } else if (document instanceof CashRateInfluenceSql) {
                return glOperationsService.finishMoneyRateInfluence(CashSql.class,
                        cashRateInfluenceSqlMapper.toDto((CashRateInfluenceSql) document), null, false);

            } else if (document instanceof SalarySql) {
                return glOperationsService.finishSalary((SalarySql) document, null, false);

            } else if (document instanceof DebtCorrectionSql) {
                return glOperationsService.finishDebtCorrection(debtCorrectionSqlMapper.toDto((DebtCorrectionSql) document), null, false);
            } else if (document instanceof DebtRateInfluenceSql) {
                return glOperationsService.finishDebtRateInfluence(debtRateInfluenceSqlMapper.toDto((DebtRateInfluenceSql) document), null, false);


            } else if (document instanceof InvoiceSql invoice) {
                return glOperationsService.finishInvoice(invoiceSqlMapper.toDto(invoice), null, false);
            } else if (document instanceof InventorySql inventory) {
                return glOperationsService.finishInventory(inventorySqlMapper.toDto(inventory), null, false);
            } else if (document instanceof TransProdSql transProd) {
                return glOperationsService.finishTransProd(transportationSqlMapper.toDto(transProd), null, false);
            } else if (document instanceof PurchaseSql purchase) {
                return glOperationsService.finishPurchase(purchaseSqlMapper.toDto(purchase), null, false);
            }
        }

        return glUtilsService.saveDoubleEntry(doubleEntry);
    }
}
