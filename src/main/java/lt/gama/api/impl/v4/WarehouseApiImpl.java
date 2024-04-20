package lt.gama.api.impl.v4;

import com.google.common.base.Objects;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.v4.WarehouseApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.mappers.WarehouseSqlMapper;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.service.APIResultService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RestController("WarehouseApiImplv4")
public class WarehouseApiImpl implements WarehouseApi {

    @PersistenceContext
    private EntityManager entityManager;

    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final WarehouseSqlMapper warehouseSqlMapper;
    private final APIResultService apiResultService;

    public WarehouseApiImpl(Auth auth, DBServiceSQL dbServiceSQL, WarehouseSqlMapper warehouseSqlMapper, APIResultService apiResultService) {
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.warehouseSqlMapper = warehouseSqlMapper;
        this.apiResultService = apiResultService;
    }


    @Override
    public PageResponse<WarehouseDto, Void> list(Integer cursor, int pageSize) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(pageSize > 0 && pageSize <= PageRequest.MAX_PAGE_SIZE,
                    MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.InvalidPageSize, auth.getLanguage()),
                            pageSize, PageRequest.MAX_PAGE_SIZE));

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPageSize(pageSize);
            pageRequest.setCursor(cursor);
            pageRequest.setOrder("mainIndex");

            return dbServiceSQL.queryPage(pageRequest, WarehouseSql.class, null, warehouseSqlMapper,
                    () -> allQueryWarehouse(pageRequest),
                    () -> countQueryWarehouse(pageRequest),
                    (resp) -> dataQueryWarehouse(pageRequest, resp));
        });
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
    public WarehouseDto get(long id) throws GamaApiException {
        return apiResultService.execute(() -> {
            WarehouseDto entity = warehouseSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(WarehouseSql.class, id));
            if (entity == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoWarehouseWithId, auth.getLanguage()),
                        id));
            }
            return entity;
        });
    }

    @Override
    public WarehouseDto create(WarehouseDto dto) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(dto.getId() == null, "id specified");
            Validators.checkArgument(StringHelper.hasValue(dto.getName()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoName, auth.getLanguage()));

            dto.setId(null);

            return warehouseSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(warehouseSqlMapper.toEntity(dto)));
        });
    }

    @Override
    public WarehouseDto update(WarehouseDto dto) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(StringHelper.hasValue(dto.getName()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoName, auth.getLanguage()));

            WarehouseDto entity = warehouseSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(WarehouseSql.class, dto.getId()));
            if (entity == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoWarehouseWithId, auth.getLanguage()),
                        dto.getId()));
            }

            boolean updated = false;
            if (!StringHelper.isEquals(entity.getName(), dto.getName())) {
                entity.setName(dto.getName());
                updated = true;
            }
            if (!Objects.equal(entity.getLocation(), dto.getLocation())) {
                entity.setLocation(dto.getLocation());
                updated = true;
            }
            if (!Objects.equal(entity.getStorekeeper(), dto.getStorekeeper())) {
                entity.setStorekeeper(dto.getStorekeeper());
                updated = true;
            }
            if (!BooleanUtils.isSame(entity.isClosed(), dto.isClosed())) {
                entity.setClosed(dto.isClosed());
                updated = true;
            }

            if (updated) entity = warehouseSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(warehouseSqlMapper.toEntity(entity)));

            return entity;
        });
    }
}
