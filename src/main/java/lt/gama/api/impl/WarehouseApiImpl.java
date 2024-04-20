package lt.gama.api.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.WarehouseApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.LocationUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.mappers.WarehouseSqlMapper;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.service.DBServiceSQL;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Gama
 * Created by valdas on 15-04-07.
 */
@RestController
public class WarehouseApiImpl implements WarehouseApi {

    @PersistenceContext
    private EntityManager entityManager;

    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final WarehouseSqlMapper warehouseSqlMapper;

    public WarehouseApiImpl(Auth auth, DBServiceSQL dbServiceSQL, WarehouseSqlMapper warehouseSqlMapper) {
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.warehouseSqlMapper = warehouseSqlMapper;
    }

    @Override
    public APIResult<PageResponse<WarehouseDto, Void>> listWarehouse(PageRequest request) throws GamaApiException {
        return APIResult.result(() -> dbServiceSQL.queryPage(request, WarehouseSql.class, null, warehouseSqlMapper,
                () -> allQueryWarehouse(request),
                () -> countQueryWarehouse(request),
                (resp) -> dataQueryWarehouse(request, resp)));
    }

    private Query allQueryWarehouse(PageRequest ignoredRequest) {
        Query query = entityManager.createQuery(
                "SELECT w FROM " + WarehouseSql.class.getName() + " w" +
                        " WHERE companyId = :companyId" +
                        " AND (w.archive IS null OR w.archive = false)" +
                        " AND (w.hidden IS null OR w.hidden = false)" +
                        " ORDER BY w.name, w.id");
        query.setParameter("companyId", auth.getCompanyId());
        return query;
    }

    private Integer countQueryWarehouse(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT w.id) FROM warehouse w");
        makeQueryWarehouse(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void makeQueryWarehouse(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        sj.add("WHERE w.company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        sj.add("AND (w.archive IS null OR w.archive = false)");
        sj.add("AND (w.hidden IS null OR w.hidden = false)");
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("trim(unaccent(w.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(w.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter()).toLowerCase() + "%");
        }
    }

    private Query idsPageQueryWarehouse(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT w.id AS id, w.name FROM warehouse w");
        makeQueryWarehouse(request, sj, params);
        sj.add("ORDER BY w.name, w.id");

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryWarehouse(PageRequest request, PageResponse<WarehouseDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryWarehouse(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                                "SELECT w FROM " + WarehouseSql.class.getName() + " w" +
                                        " WHERE w.id IN :ids" +
                                        " ORDER BY w.name, w.id",
                                WarehouseSql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", Number.class).longValue())
                                        .collect(Collectors.toList()));
    }

    @Override
    public APIResult<WarehouseDto> saveWarehouse(WarehouseDto request) throws GamaApiException {
        if (!LocationUtils.isValid(request.getLocation())) request.setLocation(null);
        return APIResult.result(() -> warehouseSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(warehouseSqlMapper.toEntity(request))));
    }

    @Override
    public APIResult<WarehouseDto> getWarehouse(IdRequest request) throws GamaApiException {
        return APIResult.result(() -> warehouseSqlMapper.toDto(dbServiceSQL.getById(WarehouseSql.class, request.getId())));
    }
}
