package lt.gama.api.impl;

import jakarta.persistence.criteria.*;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.GenerateDERequest;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.AssetApi;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.PageRequestUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.type.AssetTotal;
import lt.gama.model.mappers.AssetSqlMapper;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.model.sql.entities.AssetSql_;
import lt.gama.model.sql.entities.EmployeeSql_;
import lt.gama.model.type.enums.AssetStatusType;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.service.APIResultService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DepreciationService;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * gama-online
 * Created by valdas on 2015-10-30.
 */
@RestController
public class AssetApiImpl implements AssetApi {

    private final DBServiceSQL dbServiceSQL;
    private final DepreciationService depreciationService;
    private final AssetSqlMapper assetSqlMapper;
    private final APIResultService apiResultService;


    public AssetApiImpl(DBServiceSQL dbServiceSQL, DepreciationService depreciationService, AssetSqlMapper assetSqlMapper, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.depreciationService = depreciationService;
        this.assetSqlMapper = assetSqlMapper;
        this.apiResultService = apiResultService;
    }


    @Override
    public APIResult<PageResponse<AssetDto, AssetTotal>> listAsset(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            PageResponse<AssetDto, AssetTotal> response = dbServiceSQL.list(request, AssetSql.class, null, assetSqlMapper,
                    root -> Map.of(AssetSql_.RESPONSIBLE, root.join(AssetSql_.RESPONSIBLE, JoinType.LEFT)),
                    (cb, root, joins) -> where(request, cb, root, joins),
                    (cb, root, joins) -> order(request.getOrder(), cb, root),
                    (cb, root, joins) -> expresionsList(request.getOrder(), cb, root, true));

            Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.DATE);
            LocalDate dt = value instanceof LocalDate ? (LocalDate) value :
                    value instanceof String ? DateUtils.parseLocalDate((String) value) : null;

            response.setAttachment(depreciationService.calcAssetTotal(response.getItems(), dt));
            return response;
        });
    }

    private Predicate where(PageRequest request, CriteriaBuilder cb, Root<?> root, Map<String, Join<?, ?>> joins) {
        Predicate where = null;
        if (request.getConditions() != null) {
            // CustomSearchType.DATE
            Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.DATE);
            LocalDate dt = value instanceof LocalDate ? (LocalDate) value :
                    value instanceof String ? DateUtils.parseLocalDate((String) value) : null;
            if (dt == null) {
                throw new GamaException("No date");
            }
            // dt >= acquisitionDate AND (dt <= lastDate OR status = OPERATING OR status = CONSERVED)
            where = cb.and(
                    cb.greaterThanOrEqualTo(cb.literal(dt.with(lastDayOfMonth())), root.get(AssetSql_.ACQUISITION_DATE)),
                    cb.or(
                            cb.lessThanOrEqualTo(cb.literal(dt), root.get(AssetSql_.LAST_DATE)),
                            cb.equal(root.get(AssetSql_.STATUS), cb.literal(AssetStatusType.OPERATING)),
                            cb.equal(root.get(AssetSql_.STATUS), cb.literal(AssetStatusType.CONSERVED)),
                            cb.isNull(root.get(AssetSql_.STATUS))
                    )
            );

            // CustomSearchType.ASSET_STATUS
            value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.ASSET_STATUS);
            AssetStatusType assetStatusType = value instanceof AssetStatusType ? (AssetStatusType) value :
                    value instanceof String ? AssetStatusType.from((String) value) : null;
            if (assetStatusType != null) {
                Predicate predicateHistory = cb.isTrue(cb.function("jsonb_path_exists", Boolean.class,
                        root.get(AssetSql_.HISTORY),
                        cb.literal("$[*] ? (@.status == $status && @.date <= $date && ($date < @.endDate || !exists(@.endDate)))"),
                        cb.function("jsonb_build_object", String.class,
                                cb.literal("status"), cb.literal(assetStatusType.toString()),
                                cb.literal("date"), cb.literal(dt.toString()))));

                Predicate predicate;
                if (assetStatusType == AssetStatusType.OPERATING || assetStatusType == AssetStatusType.CONSERVED) {
                    predicate = cb.or(
                            predicateHistory,
                            cb.equal(cb.literal(assetStatusType), root.get(AssetSql_.STATUS))
                    );
                } else {
                    predicate = predicateHistory;
                }
                where = where == null ? predicate : cb.and(where, predicate);
            }
        }
        if (StringHelper.hasValue(request.getFilter())) {
            String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
            Predicate contentFilter = cb.or(
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.NAME))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.CODE))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.CIPHER))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.NOTE))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, joins.get(AssetSql_.RESPONSIBLE).get(EmployeeSql_.NAME))), "%" + filter + "%")
            );
            where = where == null ? contentFilter : cb.and(where, contentFilter);
        }
        return where;
    }

    private List<Order> order(String orderBy, CriteriaBuilder cb, Root<?> root) {
        return EntityUtils.orderList(cb, orderBy, expresionsList(orderBy, cb, root, false).toArray(Expression[]::new));
    }

    private List<Selection<?>> expresionsList(String orderBy, CriteriaBuilder cb, Root<?> root, boolean id) {
        List<Selection<?>> exp = new ArrayList<>();

        if ("cipher".equalsIgnoreCase(orderBy) || "-cipher".equalsIgnoreCase(orderBy)) {
            exp.add(cb.lower(cb.trim(cb.function("unaccent", String.class, root.get(AssetSql_.CIPHER)))));
        }
        if ("date".equalsIgnoreCase(orderBy) || "-date".equalsIgnoreCase(orderBy)) {
            exp.add(root.get(AssetSql_.DATE));
        }
        if ("acquisitionDate".equalsIgnoreCase(orderBy) || "-acquisitionDate".equalsIgnoreCase(orderBy)) {
            exp.add(root.get(AssetSql_.ACQUISITION_DATE));
        }
        if ("cost".equalsIgnoreCase(orderBy) || "-cost".equalsIgnoreCase(orderBy)) {
            exp.add(root.get(AssetSql_.COST).get("amount"));
        }
        if ("accountCost".equalsIgnoreCase(orderBy) || "-accountCost".equalsIgnoreCase(orderBy)) {
            exp.add(root.get(AssetSql_.ACCOUNT_COST).get("number"));
        }
        if ("accountRevaluation".equalsIgnoreCase(orderBy) || "-accountRevaluation".equalsIgnoreCase(orderBy)) {
            exp.add(root.get(AssetSql_.ACCOUNT_REVALUATION).get("number"));
        }
        if ("accountDepreciation".equalsIgnoreCase(orderBy) || "-accountDepreciation".equalsIgnoreCase(orderBy)) {
            exp.add(root.get(AssetSql_.ACCOUNT_DEPRECIATION).get("number"));
        }
        if ("accountExpense".equalsIgnoreCase(orderBy) || "-accountExpense".equalsIgnoreCase(orderBy)) {
            exp.add(root.get(AssetSql_.ACCOUNT_EXPENSE).get("number"));
        }

        exp.addAll(List.of(codeExpression(cb, root), nameExpression(cb, root),
                id ? root.get(AssetSql_.ID).alias("id") : root.get(AssetSql_.ID)));
        return exp;
    }

    private Expression<?> codeExpression(CriteriaBuilder cb, Root<?> root) {
        return cb.lower(cb.trim(cb.function("unaccent", String.class, root.get(AssetSql_.CODE))));
    }

    private Expression<?> nameExpression(CriteriaBuilder cb, Root<?> root) {
        return cb.lower(cb.trim(cb.function("unaccent", String.class, root.get(AssetSql_.NAME))));
    }

    @Override
    public APIResult<AssetDto> saveAsset(AssetDto request) throws GamaApiException {
        return apiResultService.result(() -> depreciationService.save(request));
    }

    @Override
    public APIResult<Void> deleteAsset(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(AssetSql.class, request.getId()));
    }

    @Override
    public APIResult<AssetDto> getAsset(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                assetSqlMapper.toDto(dbServiceSQL.getById(AssetSql.class, request.getId(), AssetSql.GRAPH_ALL)));
    }

    @Override
    public APIResult<PageResponse<AssetDto, AssetTotal>> periodListAsset(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            PageResponse<AssetDto, AssetTotal> response = dbServiceSQL.list(request, AssetSql.class, null, assetSqlMapper,
                    root -> Map.of(AssetSql_.RESPONSIBLE, root.join(AssetSql_.RESPONSIBLE, JoinType.LEFT)),
                    (cb, root, joins) -> wherePeriod(request, cb, root, joins),
                    (cb, root, joins) -> order(request.getOrder(), cb, root),
                    (cb, root, joins) -> expresionsList(request.getOrder(), cb, root, true));
            response.setAttachment(depreciationService.calcAssetTotal(response.getItems(), request.getDateFrom(), request.getDateTo()));
            return response;
        });
    }

    private Predicate wherePeriod(PageRequest request, CriteriaBuilder cb, Root<?> root, Map<String, Join<?, ?>> joins) {
        Predicate where = null;
        if (request.getConditions() != null) {
            // dateFrom - dateTo
            if (request.getDateFrom() == null) {
                throw new GamaException("No starting date");
            }
            if (request.getDateTo() == null) {
                throw new GamaException("No ending date");
            }
            LocalDate dateFrom = request.getDateFrom().with(firstDayOfMonth());
            LocalDate dateTo = request.getDateTo().with(lastDayOfMonth());

            // dateTo >= acquisitionDate AND (dateFrom <= lastDate OR status = OPERATING OR status = CONSERVED)
            where = cb.and(
                    cb.greaterThanOrEqualTo(cb.literal(dateTo), root.get(AssetSql_.ACQUISITION_DATE)),
                    cb.or(
                            cb.lessThanOrEqualTo(cb.literal(dateFrom), root.get(AssetSql_.LAST_DATE)),
                            cb.equal(root.get(AssetSql_.STATUS), cb.literal(AssetStatusType.OPERATING)),
                            cb.equal(root.get(AssetSql_.STATUS), cb.literal(AssetStatusType.CONSERVED)),
                            cb.isNull(root.get(AssetSql_.STATUS))
                    )
            );

            // CustomSearchType.ASSET_STATUS at the end of period, i.e. at dateTo
            Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.ASSET_STATUS);
            AssetStatusType assetStatusType = value instanceof AssetStatusType ? (AssetStatusType) value :
                    value instanceof String ? AssetStatusType.from((String) value) : null;
            if (assetStatusType != null) {
                Predicate predicateHistory = cb.isTrue(cb.function("jsonb_path_exists", Boolean.class,
                        root.get(AssetSql_.HISTORY),
                        cb.literal("$[*] ? (@.status == $status && @.date <= $date && ($date < @.endDate || !exists(@.endDate)))"),
                        cb.function("jsonb_build_object", String.class,
                                cb.literal("status"), cb.literal(assetStatusType.toString()),
                                cb.literal("date"), cb.literal(dateTo.toString()))));

                Predicate predicate;
                if (assetStatusType == AssetStatusType.OPERATING || assetStatusType == AssetStatusType.CONSERVED) {
                    predicate = cb.or(
                            predicateHistory,
                            cb.equal(cb.literal(assetStatusType), root.get(AssetSql_.STATUS))
                    );
                } else {
                    predicate = predicateHistory;
                }
                where = where == null ? predicate : cb.and(where, predicate);
            }
        }
        if (StringHelper.hasValue(request.getFilter())) {
            String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
            Predicate contentFilter = cb.or(
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.NAME))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.CODE))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.CIPHER))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(AssetSql_.NOTE))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, joins.get(AssetSql_.RESPONSIBLE).get(EmployeeSql_.NAME))), "%" + filter + "%")
            );
            where = where == null ? contentFilter : cb.and(where, contentFilter);
        }
        return where;
    }

    @Override
    public APIResult<AssetDto> reset(AssetDto request) throws GamaApiException {
        return apiResultService.result(() -> {
            AssetSql asset = assetSqlMapper.toEntity(request);
            depreciationService.reset(asset);
            return assetSqlMapper.toDto(asset);
        });
    }

    @Override
    public APIResult<DoubleEntryDto> generateDE(GenerateDERequest request) throws GamaApiException {
        return apiResultService.result(() -> depreciationService.generateDE(request.getDate()));
    }
}
