package lt.gama.api.impl;

import jakarta.persistence.criteria.*;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.DebtApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.PageRequestUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.DebtCorrectionDto;
import lt.gama.model.dto.documents.DebtOpeningBalanceDto;
import lt.gama.model.dto.documents.DebtRateInfluenceDto;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.DebtCoverageDto;
import lt.gama.model.dto.entities.DebtHistoryDto;
import lt.gama.model.dto.entities.DebtNowDto;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDocumentSql_;
import lt.gama.model.sql.documents.DebtCorrectionSql;
import lt.gama.model.sql.documents.DebtOpeningBalanceSql;
import lt.gama.model.sql.documents.DebtRateInfluenceSql;
import lt.gama.model.sql.documents.items.DebtRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.Doc_;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.report.RepDebtBalance;
import lt.gama.report.RepDebtBalanceInterval;
import lt.gama.report.RepDebtDetail;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DebtApiImpl implements DebtApi {

    private final DBServiceSQL dbServiceSQL;
    private final DebtService debtService;
    private final DocumentService documentService;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final DebtOpeningBalanceSqlMapper debtOpeningBalanceSqlMapper;
    private final DebtHistorySqlMapper debtHistorySqlMapper;
    private final DebtCoverageSqlMapper debtCoverageSqlMapper;
    private final DebtNowSqlMapper debtNowSqlMapper;
    private final Auth auth;
    private final DebtCorrectionSqlMapper debtCorrectionSqlMapper;
    private final DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper;
    private final APIResultService apiResultService;

    public DebtApiImpl(DBServiceSQL dbServiceSQL, DebtService debtService, DocumentService documentService, CounterpartySqlMapper counterpartySqlMapper, DebtOpeningBalanceSqlMapper debtOpeningBalanceSqlMapper, DebtHistorySqlMapper debtHistorySqlMapper, DebtCoverageSqlMapper debtCoverageSqlMapper, DebtNowSqlMapper debtNowSqlMapper, Auth auth, DebtCorrectionSqlMapper debtCorrectionSqlMapper, DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.debtService = debtService;
        this.documentService = documentService;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.debtOpeningBalanceSqlMapper = debtOpeningBalanceSqlMapper;
        this.debtHistorySqlMapper = debtHistorySqlMapper;
        this.debtCoverageSqlMapper = debtCoverageSqlMapper;
        this.debtNowSqlMapper = debtNowSqlMapper;
        this.auth = auth;
        this.debtCorrectionSqlMapper = debtCorrectionSqlMapper;
        this.debtRateInfluenceSqlMapper = debtRateInfluenceSqlMapper;
        this.apiResultService = apiResultService;
    }

    @Override
    public APIResult<PageResponse<CounterpartyDto, Void>> listCounterparty(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.listCounterparty(request));
    }

    @Override
    public APIResult<CounterpartyDto> saveCounterparty(CounterpartyDto request) throws GamaApiException {
        return apiResultService.result(() -> debtService.saveCounterpartySql(request, () -> auth.getPermissions()));
    }

    @Override
    public APIResult<CounterpartyDto> getCounterparty(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                counterpartySqlMapper.toDto(dbServiceSQL.getById(CounterpartySql.class, request.getId())));
    }

    @Override
    public APIResult<List<CounterpartyDto>> checkCounterparty(CounterpartyDto request) throws GamaApiException {
        return apiResultService.result(() -> debtService.checkCounterparty(request));
    }

    @Override
    public APIResult<Void> deleteCounterparty(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(CounterpartySql.class, request.getId()));
    }

    @Override
    public APIResult<CounterpartyDto> undeleteCounterparty(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                counterpartySqlMapper.toDto(dbServiceSQL.undeleteById(CounterpartySql.class, request.getId())));
    }

    /*
     *  Debt opening balance
     */

    private Predicate whereOpeningBalance(PageRequest request, CriteriaBuilder cb, Root<?> root) {
        if (!StringHelper.hasValue(request.getFilter())) return null;
        String pattern = "%" + StringUtils.stripAccents(request.getFilter().trim()).toLowerCase() + "%";
        var counterparty = root.join("counterparties", JoinType.LEFT);
        return cb.or(
                cb.like(cb.lower(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.NOTE))), pattern),
                cb.like(cb.lower(cb.function("unaccent", String.class,
                        counterparty.get(BaseDocumentSql_.COUNTERPARTY).get(CounterpartySql_.NAME))), pattern)
        );

    }

    @Override
    public APIResult<PageResponse<DebtOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, DebtOpeningBalanceSql.class,
                        DebtOpeningBalanceSql.GRAPH_ALL, debtOpeningBalanceSqlMapper,
                        (cb, root) -> whereOpeningBalance(request, cb, root),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<DebtOpeningBalanceDto> saveOpeningBalance(DebtOpeningBalanceDto request) throws GamaApiException {
        return apiResultService.result(() -> debtService.saveDebtOpeningBalance(request));
    }

    @Override
    public APIResult<DebtOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                debtOpeningBalanceSqlMapper.toDto(
                        dbServiceSQL.getById(DebtOpeningBalanceSql.class, request.getId(), DebtOpeningBalanceSql.GRAPH_ALL)));
    }

    @Override
    public APIResult<String> finishOpeningBalanceTask(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.finishDebtOpeningBalanceTask(request.getId()));
    }

    @Override
    public APIResult<DebtOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.importOpeningBalance(request.getId(), request.getFileName()));
    }

    @Override
    public APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(DebtOpeningBalanceSql.class, request.getId()));
   }

    /*
     * Debt Correction
     */

    private Predicate whereDebtCorrection(PageRequest request, CriteriaBuilder cb, Root<DebtCorrectionSql> root) {
        if (!StringHelper.hasValue(request.getFilter())) return null;
        String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
        Join<DebtCorrectionSql, CounterpartySql> join = root.join(BaseDocumentSql_.COUNTERPARTY, JoinType.LEFT);
        return cb.or(
                cb.like(cb.lower(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.NOTE))), "%" + filter + "%"),
                cb.like(cb.lower(cb.function("unaccent", String.class, join.get(CounterpartySql_.NAME))), "%" + filter + "%")
        );
    }

    @Override
    public APIResult<PageResponse<DebtCorrectionDto, Void>> listDebtCorrection(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, DebtCorrectionSql.class,
                        DebtCorrectionSql.GRAPH_ALL, debtCorrectionSqlMapper,
                        (cb, root) -> whereDebtCorrection(request, cb, root),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<DebtCorrectionDto> saveDebtCorrection(DebtCorrectionDto request) throws GamaApiException {
        return apiResultService.result(() -> debtService.saveDebtCorrection(request));
    }

    @Override
    public APIResult<DebtCorrectionDto> getDebtCorrection(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                documentService.getDocumentSql(DebtCorrectionSql.class, request.getId(), DebtCorrectionSql.GRAPH_ALL));
    }

    @Override
    public APIResult<DebtCorrectionDto> finishDebtCorrection(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.finishDebtCorrection(request.id, request.finishGL));
     }

    @Override
	public APIResult<Void> deleteDebtCorrection(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
	}

    @Override
    public APIResult<DebtCorrectionDto> recallDebtCorrection(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.recallDebtCorrection(request.getId()));
    }

    /*
     *  Debt $$$ Rate Influence
     */

    private Predicate whereDebtRateInfluence(PageRequest request, CriteriaBuilder cb, Root<DebtRateInfluenceSql> root) {
        if (!StringHelper.hasValue(request.getFilter())) return null;
        String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
        Join<DebtRateInfluenceSql, DebtRateInfluenceMoneyBalanceSql> accountsJoin = root.join("accounts", JoinType.LEFT);
        Join<DebtRateInfluenceMoneyBalanceSql, CounterpartySql> counterpartyJoin = accountsJoin.join("counterparty", JoinType.LEFT);
        return cb.or(
                cb.like(cb.lower(cb.function("unaccent", String.class, root.get("note"))), "%" + filter + "%"),
                cb.like(cb.lower(cb.function("unaccent", String.class, counterpartyJoin.get("name"))), "%" + filter + "%"));
    }

    @Override
    public APIResult<PageResponse<DebtRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, DebtRateInfluenceSql.class,
                        DebtRateInfluenceSql.GRAPH_ALL, debtRateInfluenceSqlMapper,
                        (cb, root) -> whereDebtRateInfluence(request, cb, root),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<DebtRateInfluenceDto> saveRateInfluence(DebtRateInfluenceDto request) throws GamaApiException {
        return apiResultService.result(() -> debtService.saveRateInfluence(request));
    }

    @Override
    public APIResult<DebtRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                documentService.getDocumentSql(DebtRateInfluenceSql.class, request.getId(), DebtRateInfluenceSql.GRAPH_ALL));
    }

    @Override
    public APIResult<DebtRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.finishRateInfluence(request.id, request.finishGL));
    }

    @Override
    public APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<DebtRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.recallRateInfluence(request.getId()));
    }

    @Override
    public APIResult<List<DebtBalanceDto>> genRateInfluence(DateRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.genRateInfluence(request.getDate()));
    }

    /*
     * Debt Coverage
     */

    private Predicate whereDebtCoverage(long counterpartyId, DebtType type, String currency, CriteriaBuilder cb, Root<?> root) {
        return cb.and(
                cb.equal(root.get(DebtCoverageSql_.COUNTERPARTY).get("id"), counterpartyId),
                cb.equal(root.get(DebtCoverageSql_.TYPE), type),
                cb.equal(root.get(DebtCoverageSql_.AMOUNT).get("currency"), currency));
    }

    private List<Order> orderDebts(String orderBy, CriteriaBuilder cb, Root<?> root) {
        return EntityUtils.orderList(cb, orderBy, expressions(orderBy, cb, root, false).toArray(Expression[]::new));
    }

    private List<Selection<?>> expressions(String orderBy, CriteriaBuilder cb, Root<?> root, boolean id) {
        if ("number".equalsIgnoreCase(orderBy) || "-number".equalsIgnoreCase(orderBy)) {
            return List.of(
                    root.get(DebtCoverageSql_.DOC).get(Doc_.SERIES),
                    root.get(DebtCoverageSql_.DOC).get(Doc_.ORDINAL),
                    root.get(DebtCoverageSql_.DOC).get(Doc_.NUMBER),
                    root.get(DebtCoverageSql_.DOC).get(Doc_.DATE),
                    root.get(DebtCoverageSql_.DOC).get(Doc_.ID),
                    id ? root.get(DebtCoverageSql_.ID).alias("id") : root.get(DebtCoverageSql_.ID));
        }

        return List.of(
                root.get(DebtCoverageSql_.DOC).get(Doc_.DATE),
                root.get(DebtCoverageSql_.DOC).get(Doc_.SERIES),
                root.get(DebtCoverageSql_.DOC).get(Doc_.ORDINAL),
                root.get(DebtCoverageSql_.DOC).get(Doc_.NUMBER),
                root.get(DebtCoverageSql_.DOC).get(Doc_.ID),
                id ? root.get(DebtCoverageSql_.ID).alias("id") : root.get(DebtCoverageSql_.ID));
    }

    @Override
    public APIResult<PageResponse<DebtCoverageDto, Void>> listDebtCoverage(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (request.getParentObj().get("counterpartyId") == null)
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyId, auth.getLanguage()));

            long counterpartyId;

            try {
                counterpartyId = Long.parseLong(request.getParentObj().get("counterpartyId"));
            } catch (NumberFormatException e) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyId, auth.getLanguage()));
            }

            if (request.getParentObj().get("debtType") == null) throw new GamaException("No debt type");
            DebtType type = DebtType.from(request.getParentObj().get("debtType"));
            if (type == null) throw new GamaException("No debt type");

            if (request.getParentObj().get("currency") == null) throw new GamaException("No currency");
            String currency = request.getParentObj().get("currency");

            return dbServiceSQL.list(request, DebtCoverageSql.class, null, debtCoverageSqlMapper,
                    (cb, root) -> whereDebtCoverage(counterpartyId, type, currency, cb, root),
                    (cb, root) -> orderDebts(request.getOrder(), cb, root),
                    (cb, root) -> expressions(request.getOrder(), cb, root, true));
        });
    }

    @Override
    public APIResult<DebtCoverageDto> saveDebtCoverage(DebtCoverageDto request) throws GamaApiException {
        return apiResultService.result(() -> debtCoverageSqlMapper.toDto(debtService.saveDebtCoverage(request)));
    }

    @Override
    public APIResult<DebtCoverageDto> getDebtCoverage(GetDebtCoverageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            debtCoverageSqlMapper.toDto(debtService.getDebt(
                    DebtCoverageSql.class,
                    request.getId(),
                    request.getDb(),
                    request.getParentObj().getCounterpartyId(),
                    request.getParentObj().getDebtType(),
                    request.getParentObj().getCurrency())));
    }

    /*
     * Reports
     */

    @Override
    public APIResult<PageResponse<RepDebtBalance, DebtService.RepDebtBalanceAttachment>> reportBalance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.reportBalance(request));
    }

    @Override
    public APIResult<PageResponse<DebtHistoryDto, RepDebtDetail>> reportDetail(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.reportDetail(request));
    }

    private Predicate whereDebtHistory(PageRequest request, CriteriaBuilder cb, Root<?> root) {
        Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.ORIGIN_ID);
        long cpId = value instanceof Number ? ((Number) value).longValue() : Long.parseLong((String) value);
        Predicate where = cb.equal(root.get("counterparty").get("id"), cpId);

        value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.DEBT_TYPE);
        DebtType type = value != null ? value instanceof DebtType ? (DebtType) value : DebtType.from(value.toString()) : null;
        if (type != null) {
            Predicate debtTypePredicate = cb.equal(root.get("type"), type);
            where = cb.and(where, debtTypePredicate);
        }

        String currency = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.CURRENCY);
        if (currency != null) {
            Predicate currencyPredicate = cb.equal(root.get("exchange").get("currency"), currency);
            where = cb.and(where, currencyPredicate);
        }

        Predicate docType = cb.notEqual(root.get("doc").get("type"), EntityUtils.normalizeEntityClassName(DebtOpeningBalanceSql.class));
        where = cb.and(where, docType);

        return where;
    }

    @Override
    public APIResult<PageResponse<DebtHistoryDto, CounterpartyDto>> debtHistory(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            Number counterpartyId = (Number) Validators.checkNotNull(PageRequestUtils.getFieldValue(request.getConditions(),
                    CustomSearchType.ORIGIN_ID), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyId, auth.getLanguage()));
            PageResponse<DebtHistoryDto, CounterpartyDto> response = dbServiceSQL.queryPage(request, DebtHistorySql.class,
                    null, debtHistorySqlMapper,
                    (cb, root) -> whereDebtHistory(request, cb, root),
                    (cb, root) -> orderDebts(request.getOrder(), cb, root),
                    (cb, root) -> expressions(request.getOrder(), cb, root, true));

            if (request.isRefresh()) {
                response.setAttachment(counterpartySqlMapper.toDto(dbServiceSQL.getById(CounterpartySql.class, counterpartyId.longValue())));
            }
            return response;
        });
    }

    @Override
    public APIResult<List<DebtNowDto>> debtNow(DebtNowRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            debtService.queryDebt(DebtNowSql.class, request.getId(), request.getType(), request.getCurrency())
                    .stream()
                    .map(debtNowSqlMapper::toDto)
                    .collect(Collectors.toList()));
    }

    @Override
    public APIResult<RepDebtBalanceInterval> reportBalanceInterval(ReportDebtBalanceIntervalRequest request) throws GamaApiException {
        return apiResultService.result(() -> debtService.reportBalanceInterval(request));
    }
}
