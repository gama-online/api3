package lt.gama.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.ReportDebtBalanceIntervalRequest;
import lt.gama.api.request.ReportInterval;
import lt.gama.api.response.PageResponse;
import lt.gama.api.response.TaskResponse;
import lt.gama.auth.i.IPermission;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.DebtCorrectionDto;
import lt.gama.model.dto.documents.DebtOpeningBalanceDto;
import lt.gama.model.dto.documents.DebtRateInfluenceDto;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.DebtCoverageDto;
import lt.gama.model.dto.entities.DebtHistoryDto;
import lt.gama.model.dto.entities.DebtNowDto;
import lt.gama.model.i.*;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDebtDocumentSql;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.base.EntitySql;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.documents.items.DebtOpeningBalanceCounterpartySql;
import lt.gama.model.sql.documents.items.DebtRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.DocumentDoubleEntry;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.Exchange_;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.doc.DocDebt;
import lt.gama.model.type.doc.Doc_;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.Permission;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.report.RepDebtBalance;
import lt.gama.report.RepDebtBalanceInterval;
import lt.gama.report.RepDebtDetail;
import lt.gama.report.RepDebtDetailCurrency;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import lt.gama.tasks.FinishDebtOpeningBalanceTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static lt.gama.api.request.ReportInterval.INTERVAL_PATTERN;

@Service
public class DebtService {

    private static final Logger log = LoggerFactory.getLogger(DebtService.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    private final DBServiceSQL dbServiceSQL;
    private final CurrencyService currencyService;
    private final StorageService storageService;
    private final TaskQueueService taskQueueService;
    private final GLOperationsService glOperationsService;
    private final GLUtilsService glUtilsService;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final DebtOpeningBalanceSqlMapper debtOpeningBalanceSqlMapper;
    private final DebtNowSqlMapper debtNowSqlMapper;
    private final DebtHistorySqlMapper debtHistorySqlMapper;
    private final Auth auth;
    private final DebtCorrectionSqlMapper debtCorrectionSqlMapper;
    private final DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper;

    public DebtService(DBServiceSQL dbServiceSQL,
                       CurrencyService currencyService,
                       StorageService storageService,
                       TaskQueueService taskQueueService,
                       GLOperationsService glOperationsService,
                       GLUtilsService glUtilsService,
                       DoubleEntrySqlMapper doubleEntrySqlMapper,
                       CounterpartySqlMapper counterpartySqlMapper,
                       DebtOpeningBalanceSqlMapper debtOpeningBalanceSqlMapper,
                       DebtNowSqlMapper debtNowSqlMapper,
                       DebtHistorySqlMapper debtHistorySqlMapper,
                       Auth auth,
                       DebtCorrectionSqlMapper debtCorrectionSqlMapper,
                       DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper) {
        this.dbServiceSQL = dbServiceSQL;
        this.currencyService = currencyService;
        this.storageService = storageService;
        this.taskQueueService = taskQueueService;
        this.glOperationsService = glOperationsService;
        this.glUtilsService = glUtilsService;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.debtOpeningBalanceSqlMapper = debtOpeningBalanceSqlMapper;
        this.debtNowSqlMapper = debtNowSqlMapper;
        this.debtHistorySqlMapper = debtHistorySqlMapper;
        this.auth = auth;
        this.debtCorrectionSqlMapper = debtCorrectionSqlMapper;
        this.debtRateInfluenceSqlMapper = debtRateInfluenceSqlMapper;
    }

    public PageResponse<CounterpartyDto, Void> pageCounterparty(PageRequest request) {
        return dbServiceSQL.queryPage(request, CounterpartySql.class, null,
                counterpartySqlMapper,
                () -> allQueryCounterparty(request),
                () -> countQueryCounterparty(request),
                resp -> dataQueryCounterparty(request, resp));
    }

    public PageResponse<CounterpartyDto, Void> listCounterparty(PageRequest request) {
        PageResponse<CounterpartyDto, Void> response = dbServiceSQL.queryPage(request, CounterpartySql.class, null,
                counterpartySqlMapper,
                () -> allQueryCounterparty(request),
                () -> countQueryCounterparty(request),
                resp -> dataQueryCounterparty(request, resp));

        if (PageRequestUtils.getFieldValue(request.getConditions(), "?detail") != null) {
            if (CollectionsHelper.hasValue(response.getItems())) {
                List<Long> ids = new ArrayList<>();
                List<CounterpartyDto> items = response.getItems();
                for (CounterpartyDto c : items) {
                    if (CollectionsHelper.hasValue(c.getDebts())) {
                        ids.add(c.getId());
                    }
                }
                if (!ids.isEmpty()) {
                    Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.REMAINDER_TYPE);
                    DebtType type = value != null ? value instanceof DebtType ? (DebtType) value : DebtType.from(value.toString()) : null;

                    StringJoiner sj = new StringJoiner(" ");
                    sj.add("SELECT dn " +
                            " FROM " +  DebtNowSql.class.getName() + " dn" +
                            " WHERE dn.companyId = :companyId " +
                            " AND dn.counterparty.id IN :ids ");
                    if (type != null) sj.add("AND type = :type");
                    sj.add("ORDER BY dn.counterparty.id, dn.doc.date, dn.doc.id");

                    TypedQuery<DebtNowSql> query = entityManager
                            .createQuery(sj.toString(), DebtNowSql.class)
                            .setParameter("companyId", auth.getCompanyId())
                            .setParameter("ids", ids);
                    if (type != null) query.setParameter("type", type);

                    List<DebtNowSql> debts = query.getResultList();
                    if (CollectionsHelper.hasValue(debts)) {
                        Map<Long, List<DebtNowDto>> mapByCounterparty = debts.stream()
                                .map(debtNowSqlMapper::toDto)
                                .collect(groupingBy(DebtNowDto::getCounterpartyId));
                        for (CounterpartyDto c: items) {
                            c.setDebtsNow(mapByCounterparty.get(c.getId()));
                        }
                    }
                }
            }
        }
        return response;
    }

    private String toCounterpartyField(String field, boolean isNativeField) {
        return CounterpartySql_.NAME.equalsIgnoreCase(field) ? "name"
                : CounterpartySql_.ID.equalsIgnoreCase(field) ? "id"
                : CounterpartySql_.SHORT_NAME.equalsIgnoreCase(field) ? (isNativeField ? "short_name" : CounterpartySql_.SHORT_NAME)
                : CounterpartySql_.COM_CODE.equalsIgnoreCase(field) ? (isNativeField ? "com_code" : CounterpartySql_.COM_CODE)
                : CounterpartySql_.VAT_CODE.equalsIgnoreCase(field) ? (isNativeField ? "vat_code" : CounterpartySql_.VAT_CODE)
                : null;
    }

    private String counterpartyNativeOrder(String field) {
        return counterpartyOrder(field, true);
    }

    private String counterpartyJPAOrder(String field) {
        return counterpartyOrder(field, false);
    }

    private String counterpartyOrder(String field, boolean isNativeField) {
        if (StringHelper.hasValue(field)) {
            String order = "";
            if (field.charAt(0) == '-') {
                order = "DESC";
                field = field.substring(1);
            }
            if (CounterpartySql_.NAME.equalsIgnoreCase(field)) {
                return "ORDER BY LOWER(TRIM(UNACCENT(a.name)))" + order + ", a.id " + order;
            }
            if (CounterpartySql_.SHORT_NAME.equalsIgnoreCase(field) ||
                    CounterpartySql_.COM_CODE.equalsIgnoreCase(field) ||
                    CounterpartySql_.VAT_CODE.equalsIgnoreCase(field)) {
                return "ORDER BY" +
                        " CASE WHEN a." + toCounterpartyField(field, isNativeField) + " IS NULL OR TRIM(" +
                            toCounterpartyField(field, isNativeField) + ") = '' THEN '\\uFFFF' ELSE " +
                            toCounterpartyField(field, isNativeField) + " END " + order +
                        ", LOWER(TRIM(UNACCENT(a.name))) " + order +
                        ", a.id " + order;
            }
        }
        return "ORDER BY LOWER(TRIM(UNACCENT(a.name))), a.id";
    }

    private void makeQueryCounterparty(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("LEFT JOIN jsonb_array_elements_text(jsonb_path_query_array(locations, '$.address')) loc ON true");
            sj.add("LEFT JOIN jsonb_array_elements_text(jsonb_path_query_array(contacts, '$.*.contact')) con ON true");
            sj.add("LEFT JOIN jsonb_array_elements_text(jsonb_path_query_array(contacts, '$.name')) con2 ON true");
        }
        sj.add("WHERE company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        sj.add("AND (archive IS null OR archive = false)");
        sj.add("AND (hidden IS null OR hidden = false)");
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("unaccent(trim(name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add("OR unaccent(com_code) ILIKE :filter");
            sj.add("OR unaccent(vat_code) ILIKE :filter");
            sj.add("OR unaccent(business_address_address1) ILIKE :filter");
            sj.add("OR unaccent(business_address_address2) ILIKE :filter");
            sj.add("OR unaccent(business_address_address3) ILIKE :filter");
            sj.add("OR unaccent(business_address_city) ILIKE :filter");
            sj.add("OR unaccent(business_address_municipality) ILIKE :filter");
            sj.add("OR unaccent(business_address_zip) ILIKE :filter");
            sj.add("OR unaccent(post_address_address1) ILIKE :filter");
            sj.add("OR unaccent(post_address_address2) ILIKE :filter");
            sj.add("OR unaccent(post_address_address3) ILIKE :filter");
            sj.add("OR unaccent(post_address_city) ILIKE :filter");
            sj.add("OR unaccent(post_address_municipality) ILIKE :filter");
            sj.add("OR unaccent(post_address_zip) ILIKE :filter");
            sj.add("OR unaccent(loc.value) ILIKE :filter");
            sj.add("OR unaccent(con.value) ILIKE :filter");
            sj.add("OR unaccent(con2.value) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter().trim()).toLowerCase() + "%");
        }
        if (StringHelper.hasValue(request.getLabel())) {
            sj.add("AND (jsonb_path_exists(labels, '$[*] ? (@ == $label)', jsonb_build_object('label', :label)))");
            params.put("label", request.getLabel());
        }

        if (request.getConditions() != null) {
            String remainderType = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.REMAINDER_TYPE);
            String ad = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.REMAINDER_AD);

            if (DebtType.VENDOR.toString().equals(remainderType)) {
                if ("A".equalsIgnoreCase(ad)) {
                    sj.add("AND (jsonb_path_exists(debts, '$.V[*] ? (@.amount > 0)'))");
                } else if ("D".equalsIgnoreCase(ad)) {
                    sj.add("AND (jsonb_path_exists(debts, '$.V[*] ? (@.amount < 0)'))");
                } else {
                    sj.add("AND (jsonb_path_exists(debts, '$.V[*]'))");
                }
            } else if (DebtType.CUSTOMER.toString().equals(remainderType)) {
                if ("A".equalsIgnoreCase(ad)) {
                    sj.add("AND (jsonb_path_exists(debts, '$.C[*] ? (@.amount < 0)'))");
                } else if ("D".equalsIgnoreCase(ad)) {
                    sj.add("AND (jsonb_path_exists(debts, '$.C[*] ? (@.amount > 0)'))");
                } else {
                    sj.add("AND (jsonb_path_exists(debts, '$.C[*]'))");
                }
            }

            Boolean withRemainderOnly = (Boolean) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.REMAINDER);
            if (BooleanUtils.isTrue(withRemainderOnly)) {
                sj.add("AND (jsonb_path_exists(debts, '$.*[*] ? (@.amount != 0)'))");
            }

            String counterpartyName = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.COUNTERPARTY_NAME);
            if (StringHelper.hasValue(counterpartyName)) {
                sj.add("AND (");
                sj.add("lower(trim(unaccent(name))) = :name");
                sj.add("OR lower(trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g'))) = :name");
                sj.add(")");
                params.put("name", EntityUtils.prepareName(counterpartyName));
            }

            String counterpartyCode = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.COUNTERPARTY_COM_CODE);
            if (StringHelper.hasValue(counterpartyCode)) {
                sj.add("AND (com_code = :comCode)");
                params.put("comCode", counterpartyCode);
            }
        }
    }

    private Query allQueryCounterparty(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT a.* FROM counterparties a");
        makeQueryCounterparty(request, sj, params);
        sj.add(counterpartyNativeOrder(request.getOrder()));

        Query query = entityManager.createNativeQuery(sj.toString(), CounterpartySql.class);
        params.forEach(query::setParameter);

        return query;
    }

    private Integer countQueryCounterparty(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT id) FROM counterparties a");
        makeQueryCounterparty(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private Query idsPageQueryCounterparty(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT LOWER(TRIM(UNACCENT(name))) as name");
        sj.add(",id");
        sj.add(",(CASE WHEN short_name IS NULL OR TRIM(short_name) = '' THEN '\\uFFFF' ELSE short_name END) AS short_name");
        sj.add(",(CASE WHEN com_code IS NULL OR TRIM(com_code) = '' THEN '\\uFFFF' ELSE com_code END) AS com_code");
        sj.add(",(CASE WHEN vat_code IS NULL OR TRIM(vat_code) = '' THEN '\\uFFFF' ELSE vat_code END) AS vat_code");
        sj.add("FROM counterparties a");
        makeQueryCounterparty(request, sj, params);
        sj.add(counterpartyNativeOrder(request.getOrder()));

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryCounterparty(PageRequest request, PageResponse<CounterpartyDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryCounterparty(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                        "SELECT a FROM " + CounterpartySql.class.getName() + " a" +
                                " WHERE id IN :ids" +
                                " " + counterpartyJPAOrder(request.getOrder()),
                                CounterpartySql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", Number.class).longValue())
                                        .collect(Collectors.toList()));
    }

    private CounterpartySql updateCounterparty(Long id, CounterpartyDto request, IPermission permission) {
        final long companyId = auth.getCompanyId();
        final boolean isCompanyAdmin = permission.checkPermission(Permission.ADMIN);
        final boolean isAccountant = isCompanyAdmin || permission.checkPermission(Permission.GL);
        final boolean isSettingsAdmin = isCompanyAdmin || permission.checkPermission(Permission.SETTINGS);

        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CounterpartySql entity = dbServiceSQL.getById(CounterpartySql.class, id);
            Validators.checkDocumentVersion(entity, request, auth.getLanguage());

            entity.setName(request.getName());
            entity.setShortName(request.getShortName());
            entity.setComCode(request.getComCode());
            entity.setVatCode(request.getVatCode());

            if (isAccountant) {
                entity.setAccounts(request.getAccounts());
                entity.setNoDebtAccount(request.getNoDebtAccount());
            }

            entity.setBanks(request.getBanks());
            entity.setRegistrationAddress(request.getRegistrationAddress());
            entity.setBusinessAddress(request.getBusinessAddress());
            entity.setPostAddress(request.getPostAddress());
            entity.setLocations(request.getLocations());
            entity.setContacts(request.getContacts());

            entity.setCredit(request.getCredit());
            entity.setCreditTerm(request.getCreditTerm());
            entity.setCategory(request.getCategory());
            entity.setDiscount(request.getDiscount());
            entity.setNote(request.getNote());
            entity.setNoDebt(request.getNoDebt());
            entity.setTaxpayerType(request.getTaxpayerType());

            entity.setArchive(request.getArchive());

            if (isSettingsAdmin && !StringHelper.isEquals(request.getExportId(), entity.getExportId())) {
                // if new value not empty check if new value not used
                if (StringHelper.hasValue(request.getExportId())) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, CounterpartySql.class, request.getExportId()));
                    if (imp != null) throw new GamaException("Ex.id in use already");
                }

                // if old value not empty delete it
                if (StringHelper.hasValue(entity.getExportId())) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, CounterpartySql.class, entity.getExportId()));
                    if (imp != null) entityManager.remove(imp);
                }

                // create new record with new value
                ImportSql imp = new ImportSql(companyId, CounterpartySql.class, request.getExportId(), id, DBType.POSTGRESQL);
                dbServiceSQL.saveEntity(imp);

                entity.setExportId(request.getExportId());
            }
            return dbServiceSQL.saveEntityInCompany(entity);
        });
    }

    public CounterpartyDto saveCounterpartySql(CounterpartyDto request, IPermission permission) {
        final Long id = request.getId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        if (!companySettings.isDisableGL() && companySettings.getGl() != null) {
            if (!Validators.isValid(request.getAccount(DebtType.VENDOR))) {
                request.setAccount(DebtType.VENDOR, companySettings.getGl().getCounterpartyVendor());
            }
            if (!Validators.isValid(request.getAccount(DebtType.CUSTOMER))) {
                request.setAccount(DebtType.CUSTOMER, companySettings.getGl().getCounterpartyCustomer());
            }
        }

        return counterpartySqlMapper.toDto(id == null
                ? dbServiceSQL.saveEntityInCompany(counterpartySqlMapper.toEntity(request))
                : updateCounterparty(id, request, permission));
    }

    public List<CounterpartyDto> checkCounterparty(CounterpartyDto request) {
        String name = StringUtils.stripAccents(request.getName());
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT a FROM " + CounterpartySql.class.getName() + " a" +
                " WHERE a." + CounterpartySql_.COMPANY_ID + " = :companyId" +
                " AND (a.archive IS null OR a.archive = false)" +
                " AND (a.hidden IS null OR a.hidden = false)");
        if (request.getId() != null && request.getId() > 0) sj.add("AND (a.id <> :id)");
        if (StringHelper.hasValue(request.getComCode()) && StringHelper.hasValue(name)) {
            sj.add("AND (a.comCode = :comCode OR lower(unaccent(trim(a.name))) = :name OR lower(trim(regexp_replace(unaccent(a.name), '[^[:alnum:]]+', ' ', 'g'))) = :name)");
        } else if (StringHelper.hasValue(request.getComCode())) {
            sj.add("AND (a.comCode = :comCode)");
        } else {
            sj.add("AND (lower(unaccent(trim(a.name))) = :name OR lower(trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g'))) = :name)");
        }

        TypedQuery<CounterpartySql> query = entityManager.createQuery(sj.toString(), CounterpartySql.class);
        query.setParameter("companyId", auth.getCompanyId());
        if (request.getId() != null && request.getId() > 0) query.setParameter("id", request.getId());
        if (StringHelper.hasValue(request.getComCode())) query.setParameter("comCode", request.getComCode());
        if (StringHelper.hasValue(name)) query.setParameter("name", EntityUtils.prepareName(name));

        List<CounterpartyDto> counterparties = query
                .setHint(QueryHints.HINT_READONLY, true)
                .getResultList().stream()
                .map(counterpartySqlMapper::toDto)
                .collect(Collectors.toList());

        return !counterparties.isEmpty() ? counterparties : null;
    }

    /**
     * Finishing debt operation.
     * p.s. Method executed in transaction. The Document must be saved elsewhere.
     * @param document SQL document
     * @return finished document
     * @param <T> document class
     */
    public <T extends BaseDebtDocumentSql> T finishDebt(T document) {
        if (BooleanUtils.isTrue(document.getFinishedDebt())) return document;

        if (BooleanUtils.isTrue(document.getNoDebt())) {
            document.setFinished(true);
            document.setFinishedDebt(true);
            return document;
        }

        if (document instanceof InvoiceSql invoice) {
            if (BooleanUtils.isTrue(invoice.getEcr())) {
                document.setFinished(true);
                document.setFinishedDebt(true);

                // write to history
                DebtHistorySql debtHistory = new DebtHistorySql(auth.getCompanyId(),
                        entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()),
                        document.getDebtType(), Doc.of(document), document.getDebt(),
                        document.getBaseDebt(), document.getExchange(), document.getDueDate());
                dbServiceSQL.saveEntityInCompany(debtHistory);
                debtHistory = new DebtHistorySql(auth.getCompanyId(),
                        entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()),
                        document.getDebtType(), Doc.of(document), GamaMoneyUtils.negated(document.getDebt()),
                        GamaMoneyUtils.negated(document.getBaseDebt()), document.getExchange(), document.getDueDate());
                dbServiceSQL.saveEntityInCompany(debtHistory);
                return document;
            }
        }

        final long id = document.getId();
        @SuppressWarnings("unchecked")
        final Class<T> type = (Class<T>) document.getClass();

        T entity = Validators.checkNotNull(dbServiceSQL.getById(type, id), "Document not found, id {0}", id);
        if (BooleanUtils.isTrue(entity.getFinishedDebt())) return entity;

        finish(entity, entity.getCounterparty().getId(), entity.getCounterparty().getDb(),
                entity.getDebtType(), entity.getExchange(),
                entity.getDebt(), entity.getBaseDebt(), false);

        entity.setFinished(true);
        entity.setFinishedDebt(true);
        return entity;
    }

    public <T extends BaseDebtDocumentSql> T recallDebt(T document) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (BooleanUtils.isNotTrue(document.getFinishedDebt())) return document;

            if (BooleanUtils.isTrue(document.getNoDebt())) {
                document.setFinished(null);
                document.setFinishedDebt(null);
                return document;
            }

            if (BooleanUtils.isNotTrue(document.getFinishedDebt())) return document;

            if (BooleanUtils.isTrue(document.getNoDebt())) {
                document.setFinished(null);
                document.setFinishedDebt(null);
                return document;
            }

            recall(document, document.getCounterparty().getId(), document.getDebtType(), document.getExchange(),
                    GamaMoneyUtils.negated(document.getDebt()), GamaMoneyUtils.negated(document.getBaseDebt()));

            document.setFinished(null);
            document.setFinishedDebt(null);
            return document;
        });
    }

    public DebtOpeningBalanceSql finishDebtOpeningBalance(final DebtOpeningBalanceSql doc) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DebtOpeningBalanceSql document = doc;
            final long documentId = document.getId();

            while (BooleanUtils.isNotTrue(document.getFinishedDebt())) {
                document = dbServiceSQL.getById(DebtOpeningBalanceSql.class, documentId);
                if (BooleanUtils.isTrue(document.getFinishedDebt())) return document;

                DebtOpeningBalanceCounterpartySql debtObCounterparty = findUnfinished(document.getCounterparties());
                if (debtObCounterparty == null) {
                    document.setFinished(true);
                    document.setFinishedDebt(true);
                    return document;
                }

                finish(document, debtObCounterparty.getCounterparty().getId(), debtObCounterparty.getCounterparty().getDb(),
                        debtObCounterparty.getType(), debtObCounterparty.getExchange(), debtObCounterparty.getAmount(),
                        debtObCounterparty.getBaseAmount(), false);
                debtObCounterparty.setFinished(true);

                if (findUnfinished(document.getCounterparties()) == null) {
                    document.setFinished(true);
                    document.setFinishedDebt(true);
                }
            }

            if (BooleanUtils.isNotTrue(document.getFinished()) || BooleanUtils.isNotTrue(document.getFinishedDebt())) {
                document.setFinished(true);
                document.setFinishedDebt(true);
            }
            return document;
        });
    }

    private DebtCorrectionSql finishDebtCorrection(long id) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DebtCorrectionSql entity = Validators.checkNotNull(dbServiceSQL.getById(DebtCorrectionSql.class, id), "Document not found, id {0}", id);
            if (BooleanUtils.isTrue(entity.getFinishedDebt())) return entity;

            if (entity.getCounterparty() != null) {
                if (entity.getDebit() != null)
                    finish(entity, entity.getCounterparty().getId(), entity.getCounterparty().getDb(), entity.getDebit(),
                            entity.getExchange(), entity.getAmount(), entity.getBaseAmount(), false);
                if (entity.getCredit() != null)
                    finish(entity, entity.getCounterparty().getId(), entity.getCounterparty().getDb(), entity.getCredit(),
                            entity.getExchange(), GamaMoneyUtils.negated(entity.getAmount()), GamaMoneyUtils.negated(entity.getBaseAmount()), false);
            }
            entity.setFinished(true);
            entity.setFinishedDebt(true);
            return entity;
        });
    }

    public DebtCorrectionDto finishDebtCorrection(DebtCorrectionDto document) {
        if (BooleanUtils.isTrue(document.getFinishedDebt())) return document;
        final long id = document.getId();
        DebtCorrectionSql entity = finishDebtCorrection(id);
        return debtCorrectionSqlMapper.toDto(entity);
    }

    private DebtCorrectionSql recallDebtCorrectionSql(long id) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DebtCorrectionSql entity = Validators.checkNotNull(dbServiceSQL.getById(DebtCorrectionSql.class, id), "Document not found, id {0}", id);
            if (BooleanUtils.isNotTrue(entity.getFinishedDebt())) return entity;

            if (BooleanUtils.isTrue(entity.getNoDebt())) {
                entity.setFinished(false);
                entity.setFinishedDebt(false);
                return entity;
            }

            if (entity.getCounterparty() != null) {
                if (entity.getDebit() != null)
                    recall(entity, entity.getCounterparty().getId(), entity.getDebit(), entity.getExchange(),
                            GamaMoneyUtils.negated(entity.getAmount()), GamaMoneyUtils.negated(entity.getBaseAmount()));
                if (entity.getCredit() != null)
                    recall(entity, entity.getCounterparty().getId(), entity.getCredit(), entity.getExchange(),
                            entity.getAmount(), entity.getBaseAmount());
            }
            entity.setFinished(false);
            entity.setFinishedDebt(false);
            return entity;
        });
    }

    public DebtCorrectionDto recallDebtCorrection(DebtCorrectionDto document) {
        if (BooleanUtils.isNotTrue(document.getFinishedDebt())) return document;
        final long id = document.getId();
        DebtCorrectionSql entity = recallDebtCorrectionSql(id);
        return debtCorrectionSqlMapper.toDto(entity);
    }

    public DebtRateInfluenceSql finishDebtRateInfluence(DebtRateInfluenceSql document, DebtRateInfluenceMoneyBalanceSql balance) {
        if (GamaMoneyUtils.isZero(balance.getBaseFixAmount())) {
            balance.setFinished(true);
            return document;
        }

        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
        Validators.checkNotNull(balance, "No balance");

        return rateInfluenceBlock(true, document.getId(), balance.getId(), false);
    }

    public DebtRateInfluenceSql recallDebtRateInfluence(DebtRateInfluenceSql document, DebtRateInfluenceMoneyBalanceSql balance) {
        if (GamaMoneyUtils.isZero(balance.getBaseFixAmount())) {
            balance.setFinished(null);
            return document;
        }

        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
        Validators.checkNotNull(balance, "No balance");

        return rateInfluenceBlock(false, document.getId(), balance.getId(), true);
    }

    private DebtRateInfluenceSql rateInfluenceBlock(boolean finished, long docId, long accountBalanceId, boolean negate) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DebtRateInfluenceSql entity = dbServiceSQL.getById(DebtRateInfluenceSql.class, docId, DebtRateInfluenceSql.GRAPH_ALL);
            if (entity.getAccounts() == null) return entity;

            DebtRateInfluenceMoneyBalanceSql accountBalance = entity.getAccounts().stream()
                    .filter(e -> e.getId() == accountBalanceId)
                    .findFirst()
                    .orElse(null);

            if (accountBalance == null || BooleanUtils.isSame(accountBalance.getFinished(), finished)) return entity;

            final GamaMoney baseSum = negate ? GamaMoneyUtils.negated(accountBalance.getBaseFixAmount()) : accountBalance.getBaseFixAmount();

            if (finished) {
                finish(entity, accountBalance.getCounterparty().getId(), accountBalance.getCounterparty().getDb(),
                        accountBalance.getType(), accountBalance.getExchange(), null, baseSum, false);
            } else {
                recall(entity, accountBalance.getCounterparty().getId(), accountBalance.getType(),
                        accountBalance.getExchange(), null, baseSum);
            }

            // mark as finished
            accountBalance.setFinished(finished);

            return entity;
        });
    }

    public <D extends IId<Long> & INumberDocument & IDb & IUuid>
    void finish(D document, Long cpId, DBType cpDb, DebtType debtType, Exchange exchange,
                final GamaMoney debtInit, final GamaMoney baseDebtInit, boolean noHistory) {
        // check if finished already - prevent run the same transaction
        if (document instanceof IDebtFinished && BooleanUtils.isTrue(((IDebtFinished) document).getFinishedDebt())) return;
        if (GamaMoneyUtils.isZero(debtInit) && GamaMoneyUtils.isZero(baseDebtInit)) return;
        if (document instanceof IDebtNoDebt && BooleanUtils.isTrue(((IDebtNoDebt) document).getNoDebt())) return;

        Validators.checkNotNull(debtType, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDebtType, auth.getLanguage()));
        Validators.checkNotNull(cpId, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyId, auth.getLanguage()));

        final LocalDate date = Validators.checkNotNull(document.getDate(), "No Document date in {0}", document);
        final LocalDate dueDate = document instanceof IDebtDueDate && ((IDebtDueDate) document).getDueDate() != null ?
                ((IDebtDueDate) document).getDueDate() : date;
        if (document instanceof ICompany) {
            Validators.checkArgument(((ICompany) document).getCompanyId() == auth.getCompanyId(), "Wrong companyId");
        }
        final String currency = exchange.getCurrency();

        final GamaMoney debt = debtInit == null ? GamaMoney.of(currency, 0.00) : debtInit;
        final GamaMoney baseDebt = GamaMoneyUtils.isNonZero(debt) && GamaMoneyUtils.isZero(baseDebtInit) ? exchange.exchange(debt) : baseDebtInit;

        dbServiceSQL.executeInTransaction(entityManager -> {
            // Renew counterparty info
            CounterpartySql counterparty = Validators.checkNotNull(dbServiceSQL.getByIdOrForeignId(CounterpartySql.class,
                    cpId, cpDb), "Counterparty not found in {0}", document);
            if (document instanceof ICompany) {
                Validators.checkArgument(counterparty.getCompanyId() == ((ICompany) document).getCompanyId(), "Wrong Counterparty id in {0}", document);
            }
            final Long counterpartyId = counterparty.getId();

            DebtCoverageSql debtCoverageSrc = new DebtCoverageSql(auth.getCompanyId(), counterparty, debtType, Doc.of(document), debt);
            entityManager.persist(debtCoverageSrc);

            List<DebtNowSql> debts = queryDebt(DebtNowSql.class, counterpartyId, debtType, currency);
            // Check if no debts to coverage
            if (CollectionsHelper.isEmpty(debts)) {
                DebtNowSql debtNow = new DebtNowSql(auth.getCompanyId(), counterparty, debtType, Doc.of(document), dueDate, debt, debt);
                entityManager.persist(debtNow);
            } else if (GamaMoneyUtils.isNonZero(debt)) {
                // sometimes DebtNowSql records of current document exists - so delete them
                debts.removeIf(debtNow -> {
                    if (Validators.isValid(debtNow.getDoc()) && Objects.equals(debtNow.getDoc().getId(), document.getId())
                            && Objects.equals(debtNow.getDoc().getDb(), document.getDb())) {
                        entityManager.remove(debtNow);
                        return true;
                    }
                    return false;
                });

                GamaMoney remainder = debt;

                boolean isNegative = GamaMoneyUtils.isNegative(debt) || GamaMoneyUtils.isNegative(baseDebt);
                for (DebtNowSql debtNow : debts) {
                    if ((isNegative && GamaMoneyUtils.isPositive(debtNow.getRemainder())) ||
                            (!isNegative && GamaMoneyUtils.isNegative(debtNow.getRemainder()))) {

                        DebtCoverageSql debtCoverageDst = null;
                        if (debtNow.getDoc().getId() != null) {
                            try {
                                debtCoverageDst = getDebt(DebtCoverageSql.class, debtNow.getDoc().getId(),
                                        debtNow.getDoc().getDb(), counterpartyId, debtType, currency);
                            } catch (GamaException e) {
                                log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
                            }
                        }

                        if (GamaMoneyUtils.isLessThan(GamaMoneyUtils.abs(remainder), GamaMoneyUtils.abs(debtNow.getRemainder()))) {
                            // if absolute value of debt amount is less than absolute value of remainder in DebtNow
                            // - use all debt amount to cover remainder
                            if (debtCoverageDst != null) {
                                DebtUtils.debtCoverageUpdateDoc(debtCoverageDst, debtCoverageSrc.getDoc(), debtCoverageSrc.getAmount(), GamaMoneyUtils.negated(remainder));
                                DebtUtils.debtCoverageUpdateDoc(debtCoverageSrc, debtCoverageDst.getDoc(), debtCoverageDst.getAmount(), remainder);
                            }
                            debtNow.setRemainder(GamaMoneyUtils.add(debtNow.getRemainder(), remainder));

                            remainder = null;

                            if (GamaMoneyUtils.isZero(debtNow.getRemainder())) entityManager.remove(debtNow);

                            break;
                        } else {
                            // if absolute value of debt amount is greater than or equal to absolute value of remainder in DebtNow
                            // - this debt covered, so update remainder and delete debtNow
                            if (debtCoverageDst != null) {
                                DebtUtils.debtCoverageUpdateDoc(debtCoverageDst, debtCoverageSrc.getDoc(), debtCoverageSrc.getAmount(), debtNow.getRemainder());
                                DebtUtils.debtCoverageUpdateDoc(debtCoverageSrc, debtCoverageDst.getDoc(), debtCoverageDst.getAmount(), GamaMoneyUtils.negated(debtNow.getRemainder()));
                            }
                            remainder = GamaMoneyUtils.add(debtNow.getRemainder(), remainder);

                            entityManager.remove(debtNow);
                        }
                    }
                    if (GamaMoneyUtils.isZero(remainder)) break;
                }
                if (GamaMoneyUtils.isNonZero(remainder)) {
                    DebtNowSql debtNow = new DebtNowSql(auth.getCompanyId(), counterparty, debtType,
                            Doc.of(document), dueDate, debt, remainder);
                    entityManager.persist(debtNow);
                }
            }

            if (!noHistory) {
                DebtHistorySql debtHistory = new DebtHistorySql(auth.getCompanyId(), counterparty, debtType,
                        Doc.of(document, date), debt, baseDebt, exchange, dueDate);
                entityManager.persist(debtHistory);
            }

            counterparty.updateDebt(debtType, debt);
        });
    }

    private <D extends IBaseDocument & ICompany & IDebtFinished>
    void recall(D document, Long cpId, final DebtType debtType, Exchange exchange, final GamaMoney debt, GamaMoney baseDebt) {
        // check if finished already - prevent run the same transaction
        if (BooleanUtils.isTrue(document.isUnfinishedDebt())) return;
        if (GamaMoneyUtils.isZero(debt) && GamaMoneyUtils.isZero(baseDebt)) return;

        Validators.checkNotNull(cpId, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyId, auth.getLanguage()));
        Validators.checkNotNull(debtType, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDebtType, auth.getLanguage()));

        dbServiceSQL.executeInTransaction(entityManager -> {
            CounterpartySql counterparty = Validators.checkNotNull(dbServiceSQL.getById(CounterpartySql.class, cpId), "No Counterparty id in {0}", document);
            Validators.checkArgument(counterparty.getCompanyId() == document.getCompanyId(), "Wrong Counterparty id in {0}", document);
            final Long counterpartyId = counterparty.getId();
            final String currency = exchange.getCurrency();

            boolean isDebtCoverage = true;
            boolean isCounterpartDebt = true;

            if (document instanceof InvoiceSql && BooleanUtils.isTrue(((InvoiceSql) document).getEcr())) {
                isDebtCoverage = false;
                isCounterpartDebt = false;
            }

            if (isDebtCoverage) {
                DebtCoverageSql debtCoverageSrc = getDebt(DebtCoverageSql.class, document.getId(), document.getDb(),
                        counterpartyId, debtType, currency);
                // check if not null - at least one time happened - not good ?!
                if (debtCoverageSrc != null) {
                    // restore or delete DebtNow
                    if (debtCoverageSrc.getDocs() != null) {
                        for (DocDebt docDebt : debtCoverageSrc.getDocs()) {
                            DebtNowSql debtNow = getDebt(DebtNowSql.class, docDebt.getDoc().getId(), docDebt.getDoc().getDb(),
                                    counterpartyId, debtType, currency);
                            if (debtNow == null) {
                                debtNow = new DebtNowSql(auth.getCompanyId(), counterparty, debtType,
                                        docDebt.getDoc(), docDebt.getDoc().getDueDate(), docDebt.getAmount(), null);
                                entityManager.persist(debtNow);
                            }
                            debtNow.setRemainder(GamaMoneyUtils.add(debtNow.getRemainder(), docDebt.getCovered()));
                            if (GamaMoneyUtils.isZero(debtNow.getRemainder())) entityManager.remove(debtNow);
                        }
                    }
                    DebtNowSql debtNow = getDebt(DebtNowSql.class, debtCoverageSrc.getDoc().getId(),
                            debtCoverageSrc.getDoc().getDb(), counterpartyId, debtType, currency);
                    if (debtNow != null) {
                        entityManager.remove(debtNow);
                    }

                    recallDebtCoverage(debtCoverageSrc);
                }
            }

            // remove from history
            deleteDebt(DebtHistorySql.class, document.getId(), document.getDb(), counterpartyId, debtType, currency);

            if (isCounterpartDebt) {
                CounterpartySql c = entityManager.find(CounterpartySql.class, counterpartyId);
                c.updateDebt(debtType, debt);
            }
        });
    }

    public static class RepDebtBalanceAttachment implements Serializable {
        private GamaMoney baseDebt;
        private GamaMoney baseDebit;
        private GamaMoney baseCredit;
        private GamaMoney baseDebtR;

        @SuppressWarnings("unused")
        RepDebtBalanceAttachment() {}

        public RepDebtBalanceAttachment(GamaMoney baseDebt, GamaMoney baseDebit, GamaMoney baseCredit, GamaMoney baseDebtR) {
            this.baseDebt = baseDebt;
            this.baseDebit = baseDebit;
            this.baseCredit = baseCredit;
            this.baseDebtR = baseDebtR;
        }

        // generated

        public GamaMoney getBaseDebt() {
            return baseDebt;
        }

        public GamaMoney getBaseDebit() {
            return baseDebit;
        }

        public GamaMoney getBaseCredit() {
            return baseCredit;
        }

        @SuppressWarnings("unused")
        public GamaMoney getBaseDebtR() {
            return baseDebtR;
        }

            public String toString() {
            return "RepDebtBalanceAttachment{" +
                    "baseDebt=" + baseDebt +
                    ", baseDebit=" + baseDebit +
                    ", baseCredit=" + baseCredit +
                    ", baseDebtR=" + baseDebtR +
                    '}';
        }
    }

    public PageResponse<RepDebtBalance, RepDebtBalanceAttachment> reportBalance(PageRequest request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate startAccounting = companySettings.getStartAccounting();
        LocalDate dateFrom = DateUtils.max(startAccounting, request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        PageResponse<RepDebtBalance, RepDebtBalanceAttachment> response = new PageResponse<>();
        List<RepDebtBalance> report = new ArrayList<>();
        Map<String, Exchange> exchangeMap = new HashMap<>();

        int cursor = request.getCursor() != null ? request.getCursor() : 0;
        if (request.isBackward() && cursor >= request.getPageSize()) cursor = cursor - request.getPageSize();

        Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.DEBT_TYPE);
        String type = value != null ? value instanceof DebtType ? value.toString() :
                Objects.equals(value, "V") || Objects.equals(value,"C") ? (String) value : "" : "";
        try {
            var q = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("debt", "debt_balance.sql"), Tuple.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("startAccounting", startAccounting.minusDays(1));
            q.setParameter("dateFrom", dateFrom);
            q.setParameter("dateTo", dateTo.plusDays(1));
            q.setParameter("counterpartyId", 0L);
            q.setParameter("currency", "");
            q.setParameter("type", type);
            q.setParameter("filter", StringHelper.hasValue(request.getFilter()) ? '%' + StringUtils.stripAccents(request.getFilter().trim()) + '%' : "");
            q.setMaxResults(request.getPageSize() + 1);
            q.setFirstResult(cursor);

            @SuppressWarnings("unchecked")
            List<Tuple> results = addScalar(q).getResultList();

            if (results.size() > request.getPageSize()) {
                response.setMore(true);
                results.remove(results.size() - 1);
            }
            if (!results.isEmpty()) {
                Tuple o = results.get(0);
                response.setTotal(o.get("total", BigInteger.class).intValue());
                String bc = o.get("b_currency", String.class);
                response.setAttachment(new RepDebtBalanceAttachment(
                        GamaMoney.ofNullable(bc, o.get("b_ob_total", BigDecimal.class)),
                        GamaMoney.ofNullable(bc, o.get("b_debit_total", BigDecimal.class)),
                        GamaMoney.ofNullable(bc, o.get("b_credit_total", BigDecimal.class)),
                        GamaMoney.ofNullable(bc, o.get("b_debt_total", BigDecimal.class))
                ));
            }

            for (Tuple o : results) {
                String accountNumber = o.get("account_number", String.class);
                String account = o.get("account", String.class);
                String c = o.get("currency", String.class);
                String bc = o.get("b_currency", String.class);
                DebtType debtType = DebtType.from(o.get("type", String.class));

                DocCounterparty counterparty = new DocCounterparty();
                counterparty.setId(o.get("counterparty_id", BigInteger.class).longValue());
                counterparty.setName(o.get("name", String.class));
                counterparty.setComCode(o.get("com_code", String.class));
                counterparty.setVatCode(o.get("vat_code", String.class));
                counterparty.setDb(DBType.POSTGRESQL);
                if (debtType != null && accountNumber != null && account != null) {
                    counterparty.getAccounts().put(debtType.toString(), new GLOperationAccount(accountNumber, account));
                }

                Exchange exchange = exchangeMap.get(c);
                if (exchange == null) {
                    exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings,
                            new Exchange(c), request.getDateTo()), "No exchange");
                    exchangeMap.put(c, exchange);
                }

                RepDebtBalance repDebtBalance = new RepDebtBalance();
                repDebtBalance.setCounterparty(counterparty);
                repDebtBalance.setType(debtType);
                repDebtBalance.setDebt(GamaMoney.ofNullable(c, o.get("ob", BigDecimal.class)));
                repDebtBalance.setDebit(GamaMoney.ofNullable(c, o.get("debit", BigDecimal.class)));
                repDebtBalance.setCredit(GamaMoney.ofNullable(c, o.get("credit", BigDecimal.class)));
                repDebtBalance.setBaseDebt(GamaMoney.ofNullable(bc, o.get("b_ob", BigDecimal.class)));
                repDebtBalance.setBaseDebit(GamaMoney.ofNullable(bc, o.get("b_debit", BigDecimal.class)));
                repDebtBalance.setBaseCredit(GamaMoney.ofNullable(bc, o.get("b_credit", BigDecimal.class)));
                repDebtBalance.setExchange(exchange);
                repDebtBalance.setBaseNowDebt(exchange.exchange(repDebtBalance.getDebt()));
                repDebtBalance.setBaseNowDebit(exchange.exchange(repDebtBalance.getDebit()));
                repDebtBalance.setBaseNowCredit(exchange.exchange(repDebtBalance.getCredit()));
                report.add(repDebtBalance);
            }

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        if (request.isBackward() && cursor == 0) response.setMore(false);
        if (!request.isBackward()) cursor = cursor + request.getPageSize();

        response.setCursor(cursor);
        response.setItems(report);

        return response;
    }

    private Predicate whereReportDetail(long counterpartyId, DebtType type, String currency, CriteriaBuilder cb, Root<?> root) {
        Predicate where = cb.equal(root.get(DebtHistorySql_.COUNTERPARTY).get(CounterpartySql_.ID), counterpartyId);
        if (type != null) where = cb.and(where, cb.equal(root.get(DebtHistorySql_.TYPE), type));
        if (currency != null) where = cb.and(where, cb.equal(root.get(DebtHistorySql_.EXCHANGE).get(Exchange_.CURRENCY), currency));

        // ignore DebtOpeningBalanceSql records in period
        where = cb.and(where, cb.notEqual(root.get(DebtHistorySql_.DOC).get(Doc_.TYPE), EntityUtils.normalizeEntityClassName(DebtOpeningBalanceSql.class)));

        return where;
    }

    private List<Order> orderDebts(CriteriaBuilder cb, Root<?> root) {
        return Arrays.asList(
                cb.asc(root.get(DebtHistorySql_.DOC).get(Doc_.DATE)),
                cb.asc(root.get(DebtHistorySql_.DOC).get(Doc_.SERIES)),
                cb.asc(root.get(DebtHistorySql_.DOC).get(Doc_.ORDINAL)),
                cb.asc(root.get(DebtHistorySql_.DOC).get(Doc_.NUMBER)),
                cb.asc(root.get(DebtHistorySql_.DOC).get(Doc_.ID)),
                cb.asc(root.get(DebtHistorySql_.ID)));
    }

    private List<Selection<?>> selectIdsDebts(CriteriaBuilder cb, Root<?> root) {
        return Arrays.asList(
                root.get(DebtHistorySql_.DOC).get(Doc_.DATE),
                root.get(DebtHistorySql_.DOC).get(Doc_.SERIES),
                root.get(DebtHistorySql_.DOC).get(Doc_.ORDINAL),
                root.get(DebtHistorySql_.DOC).get(Doc_.NUMBER),
                root.get(DebtHistorySql_.DOC).get(Doc_.ID),
                root.get(DebtHistorySql_.ID).alias("id"));
    }

    public PageResponse<DebtHistoryDto, RepDebtDetail> reportDetail(PageRequest request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate startAccounting = companySettings.getStartAccounting();
        LocalDate dateFrom = DateUtils.max(startAccounting, request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        long counterpartyId = (Long) Validators.checkNotNull(PageRequestUtils.getFieldValue(request.getConditions(),
                CustomSearchType.ORIGIN_ID), "No Counterparty id");
        Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.DEBT_TYPE);
        DebtType type = value != null ? value instanceof DebtType ? (DebtType) value : DebtType.from(value.toString()) : null;
        String currency = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.CURRENCY);

        PageResponse<DebtHistoryDto, RepDebtDetail> response = dbServiceSQL.queryPage(
                request, DebtHistorySql.class, DebtHistorySql.GRAPH_ALL, debtHistorySqlMapper,
                (cb, root) -> whereReportDetail(counterpartyId, type, currency, cb, root),
                this::orderDebts,
                this::selectIdsDebts);

        if (request.isRefresh()) {

            RepDebtDetail detail = new RepDebtDetail();
            detail.setDebt(new HashMap<>());
            detail.setDateFrom(request.getDateFrom());
            detail.setDateTo(request.getDateTo());

            try {
                var q = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("debt", "debt_balance.sql"), Tuple.class);
                q.setParameter("companyId", auth.getCompanyId());
                q.setParameter("startAccounting", startAccounting.minusDays(1));
                q.setParameter("dateFrom", dateFrom);
                q.setParameter("dateTo", dateTo.plusDays(1));
                q.setParameter("counterpartyId", counterpartyId);
                q.setParameter("currency", currency != null ? currency : "");
                q.setParameter("type", type != null ? type.toString() : "");
                q.setParameter("filter", "");

                @SuppressWarnings("unchecked")
                List<Tuple> results = addScalar(q).getResultList();

                if (!results.isEmpty()) {
                    Tuple r = results.get(0);

                    DocCounterparty cp = new DocCounterparty();
                    cp.setId(r.get("counterparty_id", BigInteger.class).longValue());
                    cp.setName(r.get("name", String.class));
                    cp.setShortName(r.get("short_name", String.class));
                    cp.setComCode(r.get("com_code", String.class));
                    cp.setVatCode(r.get("vat_code", String.class));
                    cp.setDb(DBType.POSTGRESQL);
                    detail.setCounterparty(cp);

                    Set<String> usedCurrencies = new HashSet<>();
                    r.get("used_currencies", ArrayNode.class).forEach(c -> usedCurrencies.add(c.textValue()));
                    detail.setUsedCurrencies(usedCurrencies);
                } else {
                    CounterpartySql counterparty = dbServiceSQL.getAndCheck(CounterpartySql.class, counterpartyId);
                    DocCounterparty cp = new DocCounterparty();
                    cp.setId(counterparty.getId());
                    cp.setName(counterparty.getName());
                    cp.setShortName(counterparty.getShortName());
                    cp.setComCode(counterparty.getComCode());
                    cp.setVatCode(counterparty.getVatCode());
                    cp.setDb(DBType.POSTGRESQL);
                    detail.setCounterparty(cp);
                }

                for (Tuple o : results) {
                    String c = o.get("currency", String.class);
                    String bc = o.get("b_currency", String.class);

                    RepDebtDetailCurrency report = detail.getDebt().computeIfAbsent(c, k -> new RepDebtDetailCurrency());

                    GamaMoney debtFrom = GamaMoneyUtils.add(report.getDebtFrom(), GamaMoney.ofNullable(c, o.get("ob", BigDecimal.class)));
                    GamaMoney debit = GamaMoneyUtils.add(report.getDebit(), GamaMoney.ofNullable(c, o.get("debit", BigDecimal.class)));
                    GamaMoney credit = GamaMoneyUtils.add(report.getCredit(), GamaMoney.ofNullable(c, o.get("credit", BigDecimal.class)));
                    GamaMoney baseDebtFrom = GamaMoneyUtils.add(report.getBaseDebtFrom(), GamaMoney.ofNullable(bc, o.get("b_ob", BigDecimal.class)));
                    GamaMoney baseDebit = GamaMoneyUtils.add(report.getBaseDebit(), GamaMoney.ofNullable(bc, o.get("b_debit", BigDecimal.class)));
                    GamaMoney baseCredit = GamaMoneyUtils.add(report.getBaseCredit(), GamaMoney.ofNullable(bc,o.get("b_credit", BigDecimal.class)));

                    report.setDebtFrom(GamaMoneyUtils.isNonZero(debtFrom) ? debtFrom : null);
                    report.setDebit(GamaMoneyUtils.isNonZero(debit) ? debit : null);
                    report.setCredit(GamaMoneyUtils.isNonZero(credit) ? credit : null);

                    GamaMoney debtTo = GamaMoneyUtils.total(report.getDebtFrom(), report.getDebit(), GamaMoneyUtils.negated(report.getCredit()));
                    report.setDebtTo(GamaMoneyUtils.isNonZero(debtTo) ? debtTo : null);

                    report.setBaseDebtFrom(GamaMoneyUtils.isNonZero(baseDebtFrom) ? baseDebtFrom : null);
                    report.setBaseDebit(GamaMoneyUtils.isNonZero(baseDebit) ? baseDebit : null);
                    report.setBaseCredit(GamaMoneyUtils.isNonZero(baseCredit) ? baseCredit : null);

                    GamaMoney baseDebtTo = GamaMoneyUtils.total(report.getBaseDebtFrom(), report.getBaseDebit(), GamaMoneyUtils.negated(report.getBaseCredit()));
                    report.setBaseDebtTo(GamaMoneyUtils.isNonZero(baseDebtTo) ? baseDebtTo : null);
                }
            } catch (Exception e) {
                log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }

            response.setAttachment(detail);
        }
        return response;
    }

    private Query addScalar(Query q) {
        return q.unwrap(org.hibernate.query.NativeQuery.class)
                .addScalar("counterparty_id")
                .addScalar("account_number")
                .addScalar("account")
                .addScalar("currency")
                .addScalar("ob")
                .addScalar("debit")
                .addScalar("credit")
                .addScalar("b_currency")
                .addScalar("b_ob")
                .addScalar("b_debit")
                .addScalar("b_credit")
                .addScalar("type")
                .addScalar("total")
                .addScalar("name")
                .addScalar("short_name")
                .addScalar("com_code")
                .addScalar("vat_code")
                .addScalar("used_currencies") //TODO spring migration , JsonNodeBinaryType.INSTANCE)
                .addScalar("b_ob_total")
                .addScalar("b_debit_total")
                .addScalar("b_credit_total")
                .addScalar("b_debt_total")
                ;
    }

    public DebtOpeningBalanceDto saveDebtOpeningBalance(DebtOpeningBalanceDto request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        Validators.checkOpeningBalanceDate(companySettings, request, auth.getLanguage());

        DebtOpeningBalanceSql result = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                DebtOpeningBalanceSql entity = dbServiceSQL.getAndCheck(DebtOpeningBalanceSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + request.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            } else {
                glUtilsService.assignSortOrder(request.getCounterparties());
            }

            CollectionsHelper.streamOf(request.getCounterparties())
                    .forEach(balance -> currencyService.checkBaseMoneyDocumentExchange(request.getDate(), balance));

            return dbServiceSQL.saveWithCounter(debtOpeningBalanceSqlMapper.toEntity(request));
        });

        return debtOpeningBalanceSqlMapper.toDto(result);
    }

    public DebtCorrectionDto saveDebtCorrection(final DebtCorrectionDto request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkDocumentDate(companySettings, request, auth.getLanguage());

        DocumentDoubleEntry<DebtCorrectionSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {

            if (request.getId() != null) {
                DebtCorrectionSql entity = dbServiceSQL.getAndCheck(DebtCorrectionSql.class, request.getId());
                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (!BooleanUtils.isSame(entity.getFinished(), request.getFinished()) ||
                        !BooleanUtils.isSame(entity.getFinishedDebt(), request.getFinishedDebt())) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentChangeStatus, auth.getLanguage()));
                }

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            currencyService.checkBaseMoneyDocumentExchange(request.getDate(), request, request.getCorrection());

            DebtCorrectionSql entity = dbServiceSQL.saveWithCounter(debtCorrectionSqlMapper.toEntity(request));
            DoubleEntrySql doubleEntrySql = glOperationsService.finishDebtCorrection(debtCorrectionSqlMapper.toDto(entity), request.getDoubleEntry(), false);
            return new DocumentDoubleEntry<>(entity, doubleEntrySql);
        });

        DebtCorrectionDto result = debtCorrectionSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public DebtRateInfluenceDto saveRateInfluence(DebtRateInfluenceDto request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        Validators.checkDocumentDate(companySettings, request, auth.getLanguage());

        DocumentDoubleEntry<DebtRateInfluenceSql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (request.getId() != null) {
                DebtRateInfluenceDto entity = debtRateInfluenceSqlMapper.toDto(dbServiceSQL.getAndCheck(DebtRateInfluenceSql.class, request.getId()));

                Validators.checkDocumentVersion(entity, request, auth.getLanguage());

                if (!BooleanUtils.isSame(entity.getFinished(), request.getFinished()) ||
                        !BooleanUtils.isSame(entity.getFinishedDebt(), request.getFinishedDebt())) {
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentChangeStatus, auth.getLanguage()));
                }

                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.info(entity.getClass().getSimpleName() + ", id = " + entity.getId() + " is finished");
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }

            if (request.getAccounts() != null) {
                var accounts = new ArrayList<>(request.getAccounts());
                accounts.removeIf(balance -> GamaMoneyUtils.isZero(balance.getBaseFixAmount()));
                accounts.forEach(balance -> currencyService.checkBaseMoneyDocumentExchange(request.getDate(), balance, true));
                request.setAccounts(accounts);
            }

            DebtRateInfluenceSql entity = dbServiceSQL.saveWithCounter(debtRateInfluenceSqlMapper.toEntity(request));
            DoubleEntrySql doubleEntrySql = glOperationsService.finishDebtRateInfluence(
                    debtRateInfluenceSqlMapper.toDto(entity), request.getDoubleEntry(), false);
            return new DocumentDoubleEntry<>(entity, doubleEntrySql);
        });

        DebtRateInfluenceDto result = debtRateInfluenceSqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    private DocDebt getDocByIndex(List<DocDebt> docDocs, int index) {
        return docDocs != null && index < docDocs.size() ? docDocs.get(index) : null;
    }

    public DebtCoverageSql saveDebtCoverage(DebtCoverageDto document) {
        Validators.checkArgument(document.getDoc().getId() > 0, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId, auth.getLanguage()));

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        Validators.checkDocumentDate(companySettings, document.getDoc().getDate(), auth.getLanguage());

        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DebtCoverageSql entity = Validators.checkNotNull(getDebt(DebtCoverageSql.class, document.getDoc().getId(), document.getDoc().getDb(),
                    document.getCounterpartyId(), document.getType(), document.getCurrency()), "Debt coverage not found");

            Validators.checkDocumentDate(companySettings, entity.getDoc().getDate(), auth.getLanguage());
            Validators.checkDocumentVersion(entity, document, auth.getLanguage());

            // if document and entity are empty - return without saving
            if (CollectionsHelper.isEmpty(document.getDocs()) && CollectionsHelper.isEmpty(entity.getDocs())) {
                return entity;
            }

            Validators.checkArgument(document.getDocs().size() >= entity.getDocs().size(),
                    "Coverage documents count can only increase or stay the same");

            int docsCount = document.getDocs().size();

            for (int index = 0; index < docsCount; ++index) {
                DocDebt docDoc = getDocByIndex(document.getDocs(), index);
                DocDebt entityDoc = getDocByIndex(entity.getDocs(), index);

                Validators.checkArgument(entityDoc == null ||
                        Objects.equals(docDoc.getDoc().getId(), entityDoc.getDoc().getId()), "Not the same document");

                // if nothing changed - continue
                if (entityDoc != null && GamaMoneyUtils.isEqual(docDoc.getCovered(), entityDoc.getCovered())) continue;

                GamaMoney changes = GamaMoneyUtils.subtract(docDoc.getCovered(), entityDoc == null ? null : entityDoc.getCovered());

                DebtNowSql debtNow = getDebt(DebtNowSql.class, docDoc.getDoc().getId(), docDoc.getDoc().getDb(),
                        entity.getCounterpartyId(), entity.getType(), entity.getCurrency());

                // if amount covered - need to delete
                // if not - need to update or recreate
                if (docDoc.isCovered()) {
                    if (debtNow != null) {
                        entityManager.remove(debtNow);
                    }
                } else {
                    if (debtNow == null) {
                        if (GamaMoneyUtils.isNonZero(changes)) {
                            Doc doc = Doc.of(Validators.checkNotNull(dbServiceSQL.getById(BaseDocumentSql.class, docDoc.getDoc().getId()), "Base document not found"));
                            debtNow = new DebtNowSql(auth.getCompanyId(),
                                    entityManager.getReference(CounterpartySql.class, entity.getCounterpartyId()),
                                    entity.getType(), doc, doc.getDueDate(),
                                    docDoc.getAmount(), GamaMoneyUtils.negated(changes));
                            dbServiceSQL.saveEntityInCompany(debtNow);
                        }
                    } else {
                        debtNow.setRemainder(GamaMoneyUtils.subtract(debtNow.getRemainder(), changes));
                        if (GamaMoneyUtils.isZero(debtNow.getRemainder())) {
                            entityManager.remove(debtNow);
                        }
                    }
                }

                // modify coverage document
                DebtCoverageSql docCoverage = Validators.checkNotNull(getDebt(DebtCoverageSql.class, docDoc.getDoc().getId(),
                                docDoc.getDoc().getDb(), entity.getCounterpartyId(), entity.getType(), entity.getCurrency()),
                        "No coverage document found: " + docDoc.getDoc());

                DocDebt docDebt = docCoverage.getDocs() != null ? docCoverage.getDocs().stream()
                        .filter(it -> Objects.equals(it.getDoc().getId(), entity.getDoc().getId()))
                        .findAny().orElse(null) : null;
                if (docDebt != null) {
                    docDebt.setCovered(GamaMoneyUtils.subtract(docDebt.getCovered(), changes));
                    if (GamaMoneyUtils.isZero(docDebt.getCovered())) {
                        docCoverage.getDocs().removeIf(it -> Objects.equals(it.getDoc().getId(), entity.getDoc().getId()));
                    }
                } else if (GamaMoneyUtils.isNonZero(docDoc.getCovered())) {
                    if (docCoverage.getDocs() == null) docCoverage.setDocs(new ArrayList<>());
                    docCoverage.getDocs().add(new DocDebt(entity.getDoc(), entity.getAmount(), GamaMoneyUtils.negated(changes)));
                }

                docCoverage.setCovered(null);
                docCoverage.getDocs().forEach(doc -> docCoverage.setCovered(GamaMoneyUtils.subtract(docCoverage.getCovered(), doc.getCovered())));
            }

            GamaMoney oldCoverageValue = entity.getCovered();

            entity.setDocs(document.getDocs().stream().filter(doc -> GamaMoneyUtils.isNonZero(doc.getCovered())).toList());
            entity.setAmount(document.getAmount());
            entity.setCovered(null);
            entity.getDocs().forEach(doc -> entity.setCovered(GamaMoneyUtils.subtract(entity.getCovered(), doc.getCovered())));

            GamaMoney changes = GamaMoneyUtils.subtract(entity.getCovered(), oldCoverageValue);

            DebtNowSql debtNow = getDebt(DebtNowSql.class, entity.getDoc().getId(), entity.getDoc().getDb(),
                    entity.getCounterpartyId(), entity.getType(), entity.getCurrency());
            if (GamaMoneyUtils.isZero(entity.getRemainder())) {
                if (debtNow != null) {
                    entityManager.remove(debtNow);
                }
            } else {
                if (debtNow == null) {
                    if (GamaMoneyUtils.isNonZero(changes)) {
                        Doc doc = Doc.of(Validators.checkNotNull(dbServiceSQL.getById(BaseDocumentSql.class, entity.getDoc().getId()), "Base document not found"));
                        debtNow = new DebtNowSql(auth.getCompanyId(),
                                entityManager.getReference(CounterpartySql.class, entity.getCounterpartyId()),
                                entity.getType(), doc, doc.getDueDate(),
                                entity.getAmount(), GamaMoneyUtils.negated(changes));
                        dbServiceSQL.saveEntityInCompany(debtNow);
                    }
                } else {
                    debtNow.setRemainder(GamaMoneyUtils.subtract(debtNow.getRemainder(), changes));
                    if (GamaMoneyUtils.isZero(debtNow.getRemainder())) {
                        entityManager.remove(debtNow);
                    }
                }
            }

            return entity;
        });
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

    public String finishDebtOpeningBalanceTask(long id) {
        return taskQueueService.queueTask(new FinishDebtOpeningBalanceTask(auth.getCompanyId(), id));
    }

    public TaskResponse<Void> runOpeningBalanceTask(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DebtOpeningBalanceSql document = dbServiceSQL.getAndCheck(DebtOpeningBalanceSql.class, id);
        Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());
        finish(document);
        return TaskResponse.success();
    }

    public DebtOpeningBalanceSql finish(DebtOpeningBalanceSql document) {
        // check if finished already - prevent run the same transaction second time
        if (BooleanUtils.isTrue(document.getFinished())) return document;

        // generate debt records
        document = finishDebtOpeningBalance(document);

        return document;
    }

    public DebtCorrectionDto finishDebtCorrection(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DebtCorrectionDto document = debtCorrectionSqlMapper.toDto(dbServiceSQL.getAndCheck(DebtCorrectionSql.class, id));
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return document;

        Validators.checkValid(document.getCounterparty(), "counterparty", document.toString(), auth.getLanguage());

        // check if everything for double-entry registration is ok
        glOperationsService.checkDebtCorrection(document);

        DocumentDoubleEntry<DebtCorrectionDto> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            // generate debt records
            DebtCorrectionDto dto = finishDebtCorrection(document);

            // generate G.L operations
            DoubleEntrySql doubleEntry = glOperationsService.finishDebtCorrection(dto, null, finishGL);
            dto.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

            return new DocumentDoubleEntry<>(
                    debtCorrectionSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(debtCorrectionSqlMapper.toEntity(dto))),
                    doubleEntry);
        });

        DebtCorrectionDto result = pair.getDocument();
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));

        return result;
    }

    public DebtRateInfluenceDto finishRateInfluence(long id, Boolean finishGL) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DebtRateInfluenceDto document = debtRateInfluenceSqlMapper.toDto(dbServiceSQL.getAndCheck(DebtRateInfluenceSql.class, id));
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        // check if finished already - prevent run the same transaction second time
        if (document.isFullyFinished()) return document;

        // check if everything for double-entry registration is ok
        glOperationsService.checkDebtRateInfluence(document);

        if (document.getAccounts() != null) {
            while(findUnfinished(document.getAccounts()) != null) {
                document = debtRateInfluenceSqlMapper.toDto(
                        dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
                            DebtRateInfluenceSql entity = dbServiceSQL.getById(DebtRateInfluenceSql.class, id);
                            DebtRateInfluenceMoneyBalanceSql balance = findUnfinished(entity.getAccounts());
                            if (balance == null) {
                                entity.setFinished(true);
                                return entity;
                            }

                            entity = finishDebtRateInfluence(entity, balance);
                            balance.setFinished(true);

                            return entity;
                        }));
            }
        }
        document.setFinished(true);

        // generate G.L operations
        DoubleEntryDto doubleEntry = doubleEntrySqlMapper.toDto(
                glOperationsService.finishDebtRateInfluence(document, null, finishGL));
        document.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

        document = debtRateInfluenceSqlMapper.toDto(
                dbServiceSQL.saveEntityInCompany(debtRateInfluenceSqlMapper.toEntity(document)));

        document.setDoubleEntry(doubleEntry);
        return document;
    }

    public DebtCorrectionDto recallDebtCorrection(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DebtCorrectionDto document = debtCorrectionSqlMapper.toDto(dbServiceSQL.getAndCheck(DebtCorrectionSql.class, id));
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        // check if not finished already - prevent run the same transaction second time
        if (BooleanUtils.isNotTrue(document.getFinished())) return document;

        Validators.checkValid(document.getCounterparty(), "counterparty", document.toString(), auth.getLanguage());

        DocumentDoubleEntry<DebtCorrectionDto> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            // recall debt records
            DebtCorrectionDto dto = recallDebtCorrection(document);

            // recall G.L operations
            DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false);
            dto.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

            // mark as unfinished
            dto.clearFullyFinished();

            return new DocumentDoubleEntry<>(
                    debtCorrectionSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(debtCorrectionSqlMapper.toEntity(dto))),
                    doubleEntry);
        });

        DebtCorrectionDto result = pair.getDocument();
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));

        return result;
    }

    public DebtRateInfluenceDto recallRateInfluence(long id) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DebtRateInfluenceDto document = debtRateInfluenceSqlMapper.toDto(dbServiceSQL.getAndCheck(DebtRateInfluenceSql.class, id));
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        if (document.getAccounts() != null) {
            while(findFinished(document.getAccounts()) != null) {
                document = debtRateInfluenceSqlMapper.toDto(
                        dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
                            DebtRateInfluenceSql entity = dbServiceSQL.getById(DebtRateInfluenceSql.class, id);
                            DebtRateInfluenceMoneyBalanceSql balance = findFinished(entity.getAccounts());
                            if (balance == null) {
                                entity.setFinished(false);
                                return entity;
                            }

                            entity = recallDebtRateInfluence(entity, balance);
                            balance.setFinished(false);

                            return entity;
                        }));
            }
        }

        // recall G.L operations
        DoubleEntryDto doubleEntry = doubleEntrySqlMapper.toDto(glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, false));
        document.setFinishedGL(doubleEntry != null && BooleanUtils.isTrue(doubleEntry.getFinishedGL()));

        // mark as unfinished
        document.clearFullyFinished();

        document = debtRateInfluenceSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(debtRateInfluenceSqlMapper.toEntity(document)));

        document.setDoubleEntry(doubleEntry);
        return document;
    }

    public List<DebtBalanceDto> genRateInfluence(LocalDate date) {
        Validators.checkNotNull(date, "No date");
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        String baseCurrency = companySettings.getCurrency() != null ? companySettings.getCurrency().getCode() : null;

        List<DebtBalanceDto> balances = new ArrayList<>();
        Map<String, Exchange> exchangeMap = new HashMap<>();

        try {
            List<Tuple> results = entityManager.createQuery(
                    "SELECT H.counterparty.id AS id, CP.name AS name, CP.comCode AS code, H.type AS type, " +
                                "H.exchange.currency AS currency, SUM(H.debt.amount) AS remainder, SUM(H.baseDebt.amount) AS baseRemainder " +
                            " FROM " + DebtHistorySql.class.getName() + " H " +
                            " JOIN " + CounterpartySql.class.getName() + " CP ON CP.id = H.counterparty.id " +
                            " WHERE H.companyId = :companyId " +
                                " AND H.doc.date <= :date " +
                                " AND H.exchange.currency <> H.baseDebt.currency " +
                            " GROUP BY H.counterparty.id, CP.name, CP.comCode, type, H.exchange.currency " +
                            " HAVING (SUM(H.debt.amount) IS NOT NULL AND SUM(H.debt.amount) <> 0) OR " +
                                " (SUM(H.baseDebt.amount) IS NOT NULL AND SUM(H.baseDebt.amount) <> 0) " +
                            " ORDER BY H.counterparty.id, CP.name, CP.comCode, type, H.exchange.currency",
                            Tuple.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("date", date)
                    .getResultList();

            for (Tuple o : results) {
                String currency = o.get("currency", String.class);
                GamaMoney remainder = GamaMoney.ofNullable(currency, o.get("remainder", BigDecimal.class));
                GamaMoney baseRemainder = GamaMoney.ofNullable(baseCurrency, o.get("baseRemainder", BigDecimal.class));

                CounterpartyDto counterparty = new CounterpartyDto();
                counterparty.setId(o.get("id", Long.class));
                counterparty.setName(o.get("name", String.class));
                counterparty.setComCode(o.get("code", String.class));
                counterparty.setDb(DBType.POSTGRESQL);

                Exchange exchange = exchangeMap.get(currency);
                if (exchange == null) {
                    exchange = new Exchange(currency);
                    exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings, exchange, date), "No exchange");
                    exchangeMap.put(currency, exchange);
                }
                GamaMoney fix = GamaMoneyUtils.subtract(exchange.exchange(remainder), baseRemainder);
                if (GamaMoneyUtils.isNonZero(fix)) {
                    DebtBalanceDto debtBalance = new DebtBalanceDto(counterparty, o.get("type", DebtType.class), remainder, baseRemainder, fix);
                    debtBalance.setExchange(exchange);
                    if (debtBalance.getAmount() == null) {
                        debtBalance.setAmount(GamaMoney.zero(exchange.getCurrency()));
                    }
                    balances.add(debtBalance);
                }
            }
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return balances;
    }


    public DebtOpeningBalanceDto importOpeningBalance(long id, String fileName) {
        DebtOpeningBalanceDto entity = debtOpeningBalanceSqlMapper.toDto(
                dbServiceSQL.getAndCheck(DebtOpeningBalanceSql.class, id));
        if (BooleanUtils.isTrue(entity.getFinishedDebt())) return entity;

        return storageService.importDebtOpeningBalance(entity, fileName);
    }

    private void debtCoverageRecallDoc(DebtCoverageSql debtCoverage, long id) {
        Iterator<DocDebt> it = debtCoverage.getDocs().iterator();
        while (it.hasNext()) {
            DocDebt docDebt = it.next();
            if (id == docDebt.getDoc().getId()) {
                debtCoverage.setCovered(GamaMoneyUtils.add(debtCoverage.getCovered(), docDebt.getCovered()));
                Validators.checkDebtCoverage(debtCoverage);

                it.remove();
                break;
            }
        }
    }

    public void recallDebtCoverage(DebtCoverageSql debtCoverage) {
        if (debtCoverage == null) return;

        if (CollectionsHelper.hasValue(debtCoverage.getDocs())) {
            List<Long> ids = new ArrayList<>();
            for (DocDebt doc : debtCoverage.getDocs()) {
                ids.add(doc.getDoc().getId());
            }
            List<DebtCoverageSql> list = entityManager.createQuery(
                    "SELECT a FROM " + DebtCoverageSql.class.getName() + " a "
                            + " WHERE a.doc.id IN :ids "
                            + " AND a.counterparty.id = :counterpartyId "
                            + " AND a.type = :debtType "
                            + " AND a.amount.currency = :currency",
                    DebtCoverageSql.class)
                    .setParameter("ids", ids)
                    .setParameter("counterpartyId", debtCoverage.getCounterpartyId())
                    .setParameter("debtType", debtCoverage.getType())
                    .setParameter("currency", debtCoverage.getAmount().getCurrency())
            .getResultList();

            if (list != null) {
                for (DebtCoverageSql coverage : list) {
                    if (coverage.getCompanyId() != debtCoverage.getCompanyId()) throw new GamaUnauthorizedException("Wrong company Id");
                    debtCoverageRecallDoc(coverage, debtCoverage.getDoc().getId());
                }
            }
        }
        // reset all cover info
        entityManager.remove(debtCoverage);
    }

    //Debts
    public <E extends EntitySql & IDebt>
    List<E> queryDebt(Class<E> type, Long counterpartyId, DebtType debtType, String currency) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cr = cb.createQuery(type);
        Root<E> root = cr.from(type);
        cr.select(root);
        Predicate where = cb.and(cb.and(cb.equal(root.get("companyId"), auth.getCompanyId()),
                        cb.equal(root.get("counterparty").get("id"), counterpartyId)));
        if (debtType != null) where = cb.and(where, cb.equal(root.get("type"), debtType));
        if (currency != null) where = cb.and(where, type.isAssignableFrom(DebtNowSql.class) ?
                        cb.equal(root.get("initial").get("currency"), currency) :
                        type.isAssignableFrom(DebtHistorySql.class) ?
                                cb.equal(root.get("exchange").get("currency"), currency) :
                                cb.equal(root.get("amount").get("currency"), currency));
        cr.where(where);
        cr.orderBy(cb.asc(root.get("doc").get(Doc_.DATE)), cb.asc(root.get("doc").get(Doc_.ORDINAL)), cb.asc(root.get("doc").get(Doc_.SERIES)),
                cb.asc(root.get("doc").get(Doc_.NUMBER)), cb.asc(root.get("doc").get(Doc_.ID)));

        TypedQuery<E> query = entityManager.createQuery(cr);
        return query.getResultList();
    }

    public <E extends EntitySql & IDebt>
    void deleteDebt(Class<E> type, Long documentId, DBType docDb, long counterpartyId, DebtType debtType, String currency) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            int deleted = entityManager.createQuery(
                            " DELETE FROM " + type.getName() + " a" +
                                    " WHERE a.companyId = :companyId" +
                                    " AND a.doc.id = :documentId" +
                                    " AND a.counterparty.id = :counterpartyId" +
                                    " AND a.type = :type" +
                                    (docDb == DBType.POSTGRESQL
                                            ? " AND a.doc.db = 'P'"
                                            : " AND (a.doc.db IS NULL OR a.doc.db = '')") +
                                    (type.isAssignableFrom(DebtNowSql.class)
                                            ? " AND a.initial.currency = :currency"
                                            : type.isAssignableFrom(DebtHistorySql.class)
                                            ? " AND a.exchange.currency = :currency"
                                            : " AND a.amount.currency = :currency"))
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .setParameter("documentId", documentId)
                    .setParameter("type", debtType)
                    .setParameter("currency", currency)
                    .executeUpdate();

            log.info("deleted " + type.getSimpleName() + ": " + deleted);
        });
    }

    public <E extends EntitySql & IDebt>
    E getDebt(Class<E> type, Long documentId, DBType docDb, Long counterpartyId, DebtType debtType, String currency) {
        Validators.checkNotNull(documentId, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId, auth.getLanguage()));
        Validators.checkNotNull(counterpartyId, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyId, auth.getLanguage()));
        Validators.checkNotNull(currency, "No currency");
        try {
            return entityManager.createQuery(
                            "SELECT a FROM " + type.getName() + " a" +
                                    " WHERE a.companyId = :companyId" +
                                    " AND a.doc.id = :documentId" +
                                    " AND a.counterparty.id = :counterpartyId" +
                                    " AND a.type = :type" +
                                    (docDb == DBType.POSTGRESQL
                                            ? " AND a.doc.db = 'P'"
                                            : " AND (a.doc.db IS NULL OR a.doc.db = '')") +
                                    (type.isAssignableFrom(DebtNowSql.class)
                                            ? " AND a.initial.currency = :currency"
                                            : type.isAssignableFrom(DebtHistorySql.class)
                                            ? " AND a.exchange.currency = :currency"
                                            : " AND a.amount.currency = :currency"),
                            type)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .setParameter("documentId", documentId)
                    .setParameter("type", debtType)
                    .setParameter("currency", currency)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public RepDebtBalanceInterval reportBalanceInterval(ReportDebtBalanceIntervalRequest request) {
        CompanySettings settings = dbServiceSQL.getCompanySettings(auth.getCompanyId());

        LocalDate dateFrom = DateUtils.max(settings.getStartAccounting(), request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());
        DebtType type = request.getType();

        try {

            record QueryResult(
                    LocalDate date,
                    BigDecimal baseBalance,
                    BigDecimal baseDebit,
                    BigDecimal baseCredit) {

                QueryResult(Tuple record) {
                    this(record.get("dt", java.sql.Date.class).toLocalDate(),
                            record.get("base_balance_amount", BigDecimal.class),
                            record.get("base_debit_amount", BigDecimal.class),
                            record.get("base_credit_amount", BigDecimal.class));
                }
            }

            @SuppressWarnings("unchecked")
            List<QueryResult> queryData = ((Stream<Tuple>) entityManager.createNativeQuery(
                            StringHelper.loadSQLFromFile("debt", "debt_interval.sql"), Tuple.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("dateFrom", dateFrom)
                    .setParameter("dateTo", dateTo)
                    .setParameter("type", type.toString())
                    .setParameter("timeInterval", (request.getInterval() == null ? ReportInterval.MONTH : request.getInterval()).name().toLowerCase())
                    .getResultStream())
                    .map(QueryResult::new)
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
                    .map(QueryResult::date)
                    .collect(Collectors.toSet());

            if (labelsSet.isEmpty()) {
                return null;
            }

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

            var dataset = new RepDebtBalanceInterval.DatasetData[labels.size()];

            queryData.forEach(record -> {
                int valueIndex = labels.indexOf(record.date());
                dataset[valueIndex] = new RepDebtBalanceInterval.DatasetData(record.date(), record.baseBalance, record.baseDebit, record.baseCredit);
            });

            return new RepDebtBalanceInterval(labels.stream().map(dt -> dt.format(dateTimeFormatter)).toList(), dataset);

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    public void regenerateCounterpartyDebtFromHistory(long counterpartyId) {
        record RegDebtHistory(DebtType type, GamaMoney amount) {

            RegDebtHistory(Tuple record) {
                this(DebtType.from(record.get("type", String.class)),
                        GamaMoney.of(record.get("currency", String.class), record.get("amount", BigDecimal.class)));
            }
        }

        dbServiceSQL.executeInTransaction(entityManager -> {
            // 1) remember present debt values

            // debt history
            @SuppressWarnings("unchecked") final var debtHistorySaved = ((Stream<Tuple>) entityManager.createNativeQuery(
                            "SELECT type AS type, debt_currency AS currency, SUM(debt_amount) AS amount FROM debt_history" +
                                    " WHERE company_id = :companyId" +
                                    " AND counterparty_id = :counterpartyId" +
                                    " GROUP BY type, debt_currency",
                            Tuple.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .getResultStream())
                    .map(RegDebtHistory::new)
                    .filter(x -> GamaMoneyUtils.isNonZero(x.amount()))
                    .collect(groupingBy(RegDebtHistory::type, mapping(RegDebtHistory::amount, toList())));

            // counterparty debt
            CounterpartySql counterparty = dbServiceSQL.getById(CounterpartySql.class, counterpartyId);
            final var counterpartyDebtsSaved = CollectionsHelper.isEmpty(counterparty.getDebts()) ? Collections.EMPTY_MAP
                    : counterparty.getDebts().entrySet().stream().collect(toMap(x -> DebtType.from(x.getKey()), Map.Entry::getValue));

            // debt now
            @SuppressWarnings("unchecked") final var debtNowSaved = ((Stream<Tuple>) entityManager.createNativeQuery(
                            "SELECT type AS type, initial_currency AS currency, SUM(remainder_amount) AS amount FROM debt_now" +
                                    " WHERE company_id = :companyId" +
                                    " AND counterparty_id = :counterpartyId" +
                                    " GROUP BY type, initial_currency",
                            Tuple.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .getResultStream())
                    .map(RegDebtHistory::new)
                    .filter(x -> GamaMoneyUtils.isNonZero(x.amount()))
                    .collect(groupingBy(RegDebtHistory::type, mapping(RegDebtHistory::amount, toList())));

            // 2) check
            Validators.checkArgument(debtHistorySaved.equals(counterpartyDebtsSaved), MessageFormat.format("History records not equal to debt records in counterparty {0}", counterpartyId));
            Validators.checkArgument(debtHistorySaved.equals(debtNowSaved), MessageFormat.format("Counterparty {0} History records not equal to debt-now records", counterpartyId));
            Validators.checkArgument(debtNowSaved.equals(counterpartyDebtsSaved), MessageFormat.format("Counterparty {0} Debt-now records not equal to debt records in counterparty", counterpartyId));

            // 3) delete old and generate new
            counterparty.setDebts(null);

            entityManager.createNativeQuery(
                            "DELETE FROM debt_now" +
                                    " WHERE company_id = :companyId" +
                                    " AND counterparty_id = :counterpartyId")
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .executeUpdate();

            entityManager.createNativeQuery(
                            "DELETE FROM debt_coverage" +
                                    " WHERE company_id = :companyId" +
                                    " AND counterparty_id = :counterpartyId")
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .executeUpdate();

            final MutableInt count = new MutableInt(0);
            final int BUFF_SIZE = 50;

            entityManager.createQuery(
                            "SELECT a FROM " + DebtHistorySql.class.getName() + " a" +
                                    " WHERE " + DebtHistorySql_.COMPANY_ID + " = :companyId" +
                                    " AND " + DebtHistorySql_.COUNTERPARTY + "." + CounterpartySql_.ID + " = :counterpartyId",
                            DebtHistorySql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .setHint(QueryHints.HINT_READONLY, true)
                    .getResultStream()
                    .forEach(history -> {
                        finish(history.getDoc(), counterpartyId, DBType.POSTGRESQL, history.getType(),
                                history.getExchange(), history.getDebt(), history.getBaseDebt(), true);

                        if (count.incrementAndGet() >= BUFF_SIZE) {
                            count.setValue(0);
                            if (entityManager.getTransaction().isActive()) entityManager.flush();
                            entityManager.clear();
                        }
                    });

            // 4) final check
            counterparty = dbServiceSQL.getById(CounterpartySql.class, counterpartyId);
            final var counterpartyDebtsGenerated = CollectionsHelper.isEmpty(counterparty.getDebts())
                    ? Collections.EMPTY_MAP
                    : counterparty.getDebts().entrySet().stream().collect(toMap(x -> DebtType.from(x.getKey()), Map.Entry::getValue));

            @SuppressWarnings("unchecked")
            var debtNowGenerated = ((Stream<Tuple>) entityManager.createNativeQuery(
                            "SELECT type AS type, initial_currency AS currency, SUM(remainder_amount) AS amount FROM debt_now" +
                                    " WHERE company_id = :companyId" +
                                    " AND counterparty_id = :counterpartyId" +
                                    " GROUP BY type, initial_currency",
                            Tuple.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .getResultStream())
                    .map(RegDebtHistory::new)
                    .filter(x -> GamaMoneyUtils.isNonZero(x.amount()))
                    .collect(groupingBy(RegDebtHistory::type, mapping(RegDebtHistory::amount, toList())));

            Validators.checkArgument(debtHistorySaved.equals(debtNowGenerated), MessageFormat.format("Counterparty {0} generated debt-now records not equal to saved records", counterpartyId));
            Validators.checkArgument(counterpartyDebtsSaved.equals(counterpartyDebtsGenerated), MessageFormat.format("Generated counterparty {0} debt records not equal to saved records", counterpartyId));

            log.info(MessageFormat.format("Counterparty {0} debts records regenerated", counterpartyId));
        });
    }

    public DebtNowDto createDebtNowFromDoc(long docId) {
        return debtNowSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            BaseDocumentSql document = dbServiceSQL.getById(BaseDocumentSql.class, docId);
            if (document == null) throw new GamaException("No document with id " + docId);
            if (!document.isFullyFinished()) {
                throw new GamaException("Document " + document.getClass().getSimpleName() + " id=" + docId + " is not finished");
            }
            if (document instanceof BaseDebtDocumentSql debtDocument) {
                DebtNowSql debtNow = new DebtNowSql(auth.getCompanyId(), document.getCounterparty(),
                        debtDocument.getDebtType(), Doc.of(document), debtDocument.getDueDate(), debtDocument.getDebt(), debtDocument.getDebt());
                entityManager.persist(debtNow);
                return debtNow;
            } else {
                throw new GamaException("Wrong document type - debtDocuments are supported only");
            }
        }));
    }

    public Map<String, Integer> rebuildDebtCoveragesForCounterparty(long counterpartyId, DebtType type) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            List<DebtCoverageSql> entities = entityManager.createQuery(
                    "SELECT d FROM " + DebtCoverageSql.class.getName() + " d" +
                            " WHERE " + DebtCoverageSql_.COMPANY_ID + " = :companyId" +
                            " AND " + DebtCoverageSql_.COUNTERPARTY + "." + CounterpartySql_.ID + " = :counterpartyId" +
                            " AND " + DebtCoverageSql_.TYPE + " = :type" +
                            " ORDER BY " + DebtCoverageSql_.DOC + ".date, " + DebtCoverageSql_.DOC + ".id",
                           DebtCoverageSql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("counterpartyId", counterpartyId)
                    .setParameter("type", type)
                    .getResultList();
            // "Invoice": +,  docs: -
            // "AdvanceOperation": -, docs: +
            List<DebtCoverageSql> positives = new ArrayList<>();
            List<DebtCoverageSql> negatives = new ArrayList<>();
            entities.forEach(entity -> {
                entity.setCovered(entity.getAmount() == null ? null : entity.getAmount().withAmount(BigDecimal.ZERO));
                entity.setDocs(new ArrayList<>());
                if (GamaMoneyUtils.isPositive(entity.getAmount())) positives.add(entity);
                else if (GamaMoneyUtils.isNegative(entity.getAmount())) negatives.add(entity);
            });
            int fixed = 0;
            if (CollectionsHelper.hasValue(positives) && CollectionsHelper.hasValue(negatives)) {
                int indexPositive = 0;
                int indexNegative = 0;
                while (indexPositive < positives.size() && indexNegative < negatives.size()) {
                    var positive = positives.get(indexPositive);
                    var negative = negatives.get(indexNegative);

                    var negativeRemainder = negative.getRemainder();
                    var positiveRemainder = positive.getRemainder();

                    if (GamaMoneyUtils.isEqual(negativeRemainder.negated(), positiveRemainder)) {
                        // negative = positive - coverage all positive and negative
                        positive.setCovered(GamaMoneyUtils.add(positive.getCovered(), negativeRemainder.negated()));
                        positive.getDocs().add(new DocDebt(negative.getDoc(), negative.getAmount(), negativeRemainder));
                        indexPositive++;
                        fixed++;

                        negative.setCovered(GamaMoneyUtils.add(negative.getCovered(), positiveRemainder.negated()));
                        negative.getDocs().add(new DocDebt(positive.getDoc(), positive.getAmount(), positiveRemainder));
                        indexNegative++;
                        fixed++;

                    } else if (GamaMoneyUtils.isLessThan(negativeRemainder.negated(), positiveRemainder)) {
                        // -negative <= positive - coverage part of positive and all negative
                        positive.setCovered(GamaMoneyUtils.add(positive.getCovered(), negativeRemainder.negated()));
                        positive.getDocs().add(new DocDebt(negative.getDoc(), negative.getAmount(), negativeRemainder));

                        negative.setCovered(negative.getAmount());
                        negative.getDocs().add(new DocDebt(positive.getDoc(), positive.getAmount(), negativeRemainder.negated()));
                        indexNegative++;
                        fixed++;

                    } else {
                        // negative > positive - coverage part of negative and all positive
                        negative.setCovered(GamaMoneyUtils.add(negative.getCovered(), positiveRemainder.negated()));
                        negative.getDocs().add(new DocDebt(positive.getDoc(), positive.getAmount(), positiveRemainder));

                        positive.getDocs().add(new DocDebt(negative.getDoc(), negative.getAmount(), positiveRemainder.negated()));
                        positive.setCovered(positive.getAmount());
                        indexPositive++;
                        fixed++;
                    }
                }
            }
            return Map.of("entites", entities.size(), "fixed", fixed);
        });
    }
}
