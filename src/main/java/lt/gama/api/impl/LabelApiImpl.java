package lt.gama.api.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.LabelsRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.UpdateLabelsRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.LabelApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.PageRequestUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.LabelDto;
import lt.gama.model.i.base.IBaseCompany;
import lt.gama.model.mappers.LabelSqlMapper;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.LabelType;
import lt.gama.service.APIResultService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.LabelService;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;


/**
 * gama-online
 * Created by valdas on 2016-03-10.
 */
@RestController
public class LabelApiImpl implements LabelApi {

    /**
     * Add here all label entities maps
     */
    static private final Map<String, Class<? extends IBaseCompany>> labelTypes = Map.ofEntries(
            entry(LabelType.ASSET.toString(), AssetSql.class),
            entry(LabelType.PART.toString(), PartSql.class),
            entry(LabelType.INVOICE.toString(), InvoiceSql.class),
            entry(LabelType.PURCHASE.toString(), PurchaseSql.class),
            entry(LabelType.EMPLOYEE.toString(), EmployeeSql.class),
            entry(LabelType.COUNTERPARTY.toString(), CounterpartySql.class));

    @PersistenceContext
    private EntityManager entityManager;

    private final LabelService labelService;
    private final DBServiceSQL dbServiceSQL;
    private final LabelSqlMapper labelSqlMapper;
    private final Auth auth;
    private final APIResultService apiResultService;

    public LabelApiImpl(LabelService labelService, DBServiceSQL dbServiceSQL, LabelSqlMapper labelSqlMapper, Auth auth, APIResultService apiResultService) {
        this.labelService = labelService;
        this.dbServiceSQL = dbServiceSQL;
        this.labelSqlMapper = labelSqlMapper;
        this.auth = auth;
        this.apiResultService = apiResultService;
    }

    @Override
    public APIResult<PageResponse<LabelDto, Void>> listLabel(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            dbServiceSQL.queryPage(request, LabelSql.class, null,
                    labelSqlMapper,
                    () -> allQueryLabel(request),
                    () -> countQueryLabel(request),
                    resp -> dataQueryLabel(request, resp)));
    }

    private Query allQueryLabel(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT a.* FROM label a");
        makeQueryLabel(request, sj, params);
        sj.add("ORDER BY LOWER(UNACCENT(a.name)), a.id");

        Query query = entityManager.createNativeQuery(sj.toString(), LabelSql.class);
        params.forEach(query::setParameter);

        return query;
    }

    private Integer countQueryLabel(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT a.id) FROM label a");
        makeQueryLabel(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void makeQueryLabel(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        sj.add("WHERE a.company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        sj.add("AND (a.archive IS null OR a.archive = false)");
        sj.add("AND (a.hidden IS null OR a.hidden = false)");
        LabelType labelType = LabelType.from((String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.LABEL_TYPE));
        if (labelType != null) {
            sj.add("AND a.type = :labelType");
            params.put("labelType", labelType.toString());
        }
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("trim(unaccent(a.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(a.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter().trim()).toLowerCase() + "%");
        }
    }

    private Query idsPageQueryLabel(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT a.id AS id, LOWER(UNACCENT(a.name)) FROM label a");
        makeQueryLabel(request, sj, params);
        sj.add("ORDER BY LOWER(UNACCENT(a.name)), a.id");

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryLabel(PageRequest request, PageResponse<LabelDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryLabel(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                                "SELECT a FROM " + LabelSql.class.getName() + " a" +
                                        " WHERE a.id IN :ids" +
                                        " ORDER BY LOWER(UNACCENT(a.name)), a.id",
                                LabelSql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", Number.class).longValue())
                                        .collect(Collectors.toList()));
    }

    @Override
    public APIResult<LabelDto> saveLabel(LabelDto request) throws GamaApiException {
        return apiResultService.result(() -> {
            Validators.checkArgument(StringHelper.hasValue(request.getName()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoLabelName, auth.getLanguage()));
            return labelSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                LabelSql label;
                if (request.getId() != null && request.getId() != 0) {
                    label = Validators.checkNotNull(dbServiceSQL.getById(LabelSql.class, request.getId()),
                            TranslationService.getInstance().translate(TranslationService.DB.NoEntityToUpdate, auth.getLanguage()));
                    Class<? extends IBaseCompany> classType = labelTypes.get(label.getType().toString());
                    if (classType != null) {
                        labelService.updateLabel(label.getName(), request.getName(), classType);
                    }
                    label.setName(request.getName());
                } else {
                    label = labelSqlMapper.toEntity(request);
                }
                return dbServiceSQL.saveEntityInCompany(label);
            }));
        });
    }

    @Override
    public APIResult<List<LabelDto>> saveLabelsList(LabelsRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            List<LabelSql> labels = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                List<LabelSql> entities = new ArrayList<>();
                if (request.getLabels() != null) {
                    for (LabelDto label : request.getLabels()) {
                        label.setType(request.getType());
                        entities.add(dbServiceSQL.saveEntityInCompany(labelSqlMapper.toEntity(label)));
                    }
                }
                return entities;
            });
            return labels.stream().map(labelSqlMapper::toDto).collect(Collectors.toList());
        });
    }

    @Override
    public APIResult<LabelDto> getLabel(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            labelSqlMapper.toDto(dbServiceSQL.getById(LabelSql.class, request.getId())));
    }

    @Transactional
    @Override
    public APIResult<Void> deleteLabel(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            LabelSql label = Validators.checkNotNull(dbServiceSQL.getById(LabelSql.class, request.getId()),
                    TranslationService.getInstance().translate(TranslationService.DB.NoEntityToDelete, auth.getLanguage()));

            label.setArchive(true);
            dbServiceSQL.saveEntityInCompany(label);

            Class<? extends IBaseCompany> classType = labelTypes.get(label.getType().toString());
            if (classType != null) {
                labelService.deleteLabel(label.getName(), classType);
            }
        });
    }

    @Override
    public APIResult<IBaseCompany> updateLabels(UpdateLabelsRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            Class<? extends IBaseCompany> classType = labelTypes.get(request.getType().toString());
            if (classType == null) throw new GamaException("Invalid label type");
            return labelService.updateLabels(request, classType);
        });
    }
}
