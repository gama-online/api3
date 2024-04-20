package lt.gama.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lt.gama.api.ex.GamaApiServerErrorException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.i.*;
import lt.gama.model.mappers.IBaseMapper;
import lt.gama.model.sql.base.*;
import lt.gama.model.sql.documents.BankOperationSql;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.DoubleEntrySql_;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CounterDesc;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.inventory.WarehouseTagged;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaNotFoundException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static lt.gama.api.IApi.APP_API_3_VERSION;
import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_EXISTS;

@Repository
public class DBServiceSQL {

    private static final Logger log = LoggerFactory.getLogger(DBServiceSQL.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${gama.version}") private String appVersion;

    private final Auth auth;
    private final CounterService counterService;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final ObjectMapper objectMapper;

    public DBServiceSQL(Auth auth, CounterService counterService, 
                        AuthSettingsCacheService authSettingsCacheService,
                        ObjectMapper objectMapper) {
        this.auth = auth;
        this.counterService = counterService;
        this.authSettingsCacheService = authSettingsCacheService;
        this.objectMapper = objectMapper;
    }

//    public <E extends BaseEntitySql> E getEntityViewById(long id, Class<E> entityClass) {
//        @SuppressWarnings("unchecked")
//        IView<E> view = (IView<E>) MapView.getView(entityClass.getSimpleName());
//
//        E entity = getById(entityClass, id);
//        return view != null && entity != null ? view.view(entity, auth.getPermissions()) : entity;
//    }

    public <E extends EntitySql> E getById(Class<E> type, Object id) {
        return getById(type, id, null);
    }

    public <E extends EntitySql> E getById(Class<E> entityClass, Object id, String graphName) {
        E entity = StringHelper.isEmpty(graphName) ?
                entityManager.find(entityClass, id) :
                entityManager.find(entityClass, id, Collections.singletonMap(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName)));
        if (entity instanceof BaseCompanySql baseCompany) {
            if (auth.getCompanyId() == null || baseCompany.getCompanyId() != auth.getCompanyId()) {
                log.warn("Wrong company");
                throw new GamaUnauthorizedException("Wrong company, entity id/companyId=" +
                        id + "/" + baseCompany.getCompanyId() +
                        ", auth companyId=" + auth.getCompanyId());
            }
        }
        return entity;
    }

    public <E extends BaseCompanySql> E getByForeignId(Class<E> type, long foreignId) {
        return getByForeignId(type, foreignId, null);
    }

    public <E extends BaseCompanySql> E getByForeignId(Class<E> type, long foreignId, String graphName) {
        TypedQuery<E> q = entityManager
                .createQuery("SELECT e " +
                                " FROM " + type.getName() + " e " +
                                " WHERE foreignId = :foreignId AND companyId = :companyId",
                        type)
                .setParameter("foreignId", foreignId)
                .setParameter("companyId", auth.getCompanyId());
              if (StringHelper.hasValue(graphName)) {
                  q.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
              }
        List<E> list = q.getResultList();
        return list != null && list.size() == 1 ? list.get(0) : null;
    }

    public <E extends BaseCompanySql> TypedQuery<E> makeQueryInCompany(Class<E> type, String graphName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cr = cb.createQuery(type);
        Root<E> root = cr.from(type);
        cr.select(root);
        cr.where(cb.equal(root.get("companyId"), auth.getCompanyId()));
        cr.orderBy(cb.asc(root.get("id")));

        TypedQuery<E> query = entityManager.createQuery(cr);
        if (StringHelper.hasValue(graphName)) {
            query = query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
        }
        return query;
    }

    public <E extends BaseCompanySql> TypedQuery<E> makeQueryInCompany(Class<E> type) {
        return makeQueryInCompany(type, null);
    }

    public <E extends BaseCompanySql> TypedQuery<E> makeQueryInCompany(Class<E> type, String graphName, int skip, int limit) {
        TypedQuery<E> q = makeQueryInCompany(type, graphName);
        q.setFirstResult(skip);
        q.setMaxResults(limit);
        return q;
    }

    public <E extends BaseCompanySql> TypedQuery<E> queryByIds(Class<E> type, String graphName, Collection<Long> ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cr = cb.createQuery(type);
        Root<E> root = cr.from(type);
        cr.select(root);
        cr.where(root.get("id").in(ids));

        TypedQuery<E> query = entityManager.createQuery(cr);
        if (StringHelper.hasValue(graphName)) {
            query = query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
        }
        return query;
    }

    public <E extends BaseEntitySql> TypedQuery<E> makeQuery(Class<E> type, String graphName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cr = cb.createQuery(type);
        Root<E> root = cr.from(type);
        cr.select(root);
        if (IId.class.isAssignableFrom(type)) {
            cr.orderBy(cb.asc(root.get("id")));
        }

        TypedQuery<E> query = entityManager.createQuery(cr);
        if (StringHelper.hasValue(graphName)) {
            query = query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
        }
        return query;
    }

    public <E extends BaseEntitySql> TypedQuery<E> makeQuery(Class<E> type) {
        return makeQuery(type, null);
    }

    public <K, E extends EntitySql & ICompany & IId<K>> E getAndCheck(Class<E> type, K id) {
        return getAndCheck(type, id, null);
    }

    public <K, E extends EntitySql & ICompany & IId<K>> E getAndCheck(Class<E> type, K id, String graphName) {
        E entity = getById(type, id, graphName);
        if (entity == null) {
            log.warn("No " + type.getSimpleName() + ", id=" + id);
            throw new GamaException("No " + type.getSimpleName() + " with id=" + id);
        }
        if (entity.getCompanyId() != auth.getCompanyId()) {
            log.warn("Wrong company");
            throw new GamaUnauthorizedException("Wrong company, entity id=" + id + ", auth companyId=" + auth.getCompanyId());
        }
        return entity;
    }

    public <K, E extends EntitySql & ICompany & IId<K>> E getAndCheckNullable(Class<E> type, K id) {
        return getAndCheckNullable(type, id, null);
    }

    public <K, E extends EntitySql & ICompany & IId<K>> E getAndCheckNullable(Class<E> type, K id, String graphName) {
        E entity = getById(type, id, graphName);
        if (entity != null && entity.getCompanyId() != auth.getCompanyId()) {
            log.warn("Wrong company");
            throw new GamaUnauthorizedException("Wrong company, entity id/companyId=" +
                    id + "/" + entity.getCompanyId() +
                    ", auth companyId=" + auth.getCompanyId());
        }
        return entity;
    }

    public <E extends BaseCompanySql> E getByIdOrForeignId(Class<E> type, long id, DBType db) {
        return getByIdOrForeignId(type, id, db, null);
    }

    public <E extends BaseCompanySql> E getByIdOrForeignId(Class<E> type, long id, DBType db, String graphName) {
        E entity;
        if (db != DBType.POSTGRESQL) {
            entity = getByForeignId(type, id, graphName);
            if (entity == null) {
                log.warn("No " + type.getSimpleName() + ", foreignId=" + id);
                throw new GamaException("No " + type.getSimpleName());
            }
        } else {
            entity = getById(type, id, graphName);
            if (entity == null) {
                log.warn("No " + type.getSimpleName() + ", id=" + id);
                throw new GamaNotFoundException("No " + type.getSimpleName());
            }
        }
        if (entity.getCompanyId() != auth.getCompanyId()) {
            log.warn("Wrong company");
            throw new GamaUnauthorizedException("Wrong company, entity id=" + id + ", auth companyId=" + auth.getCompanyId());
        }
        return entity;
    }

    public <E extends BaseCompanySql> Long getIdByForeignId(Class<E> type, long foreignId) {
        List<Long> list = entityManager
                .createQuery("SELECT " + BaseCompanySql_.ID + " " +
                                " FROM " +  type.getName() + "  a" +
                                " WHERE " + BaseCompanySql_.COMPANY_ID + " = :companyId" +
                                " AND " + BaseCompanySql_.FOREIGN_ID + " = :foreignId" +
                                " AND (a.archive IS null OR a.archive = false)",
                        Long.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("foreignId", foreignId)
                .getResultList();
        return list != null && list.size() == 1 ? list.get(0) : null;
    }

    public <E extends BaseCompanySql> Long getId(Class<E> type, long id, DBType db) {
        if (db != DBType.POSTGRESQL) {
            List<Long> list = entityManager
                    .createQuery("SELECT " + BaseCompanySql_.ID + " " +
                                    " FROM " + type.getName() + " a" +
                                    " WHERE " + BaseCompanySql_.COMPANY_ID + " = :companyId" +
                                    " AND " + BaseCompanySql_.FOREIGN_ID + " = :foreignId" +
                                    " AND (a.archive IS null OR a.archive = false)",
                            Long.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("foreignId", id)
                    .getResultList();
            return list != null && list.size() == 1 ? list.get(0) : null;
        } else {
            return id;
        }
    }

    public <K, E extends BaseEntitySql & IId<K>, D, A>
    PageResponse<D, A> queryPage(PageRequest request, Class<E> resultClass, String graphName, IBaseMapper<D, E> mapper,
                                 BiFunction<CriteriaBuilder, Root<E>, Predicate> where,
                                 BiFunction<CriteriaBuilder, Root<E>, List<Order>> order,
                                 BiFunction<CriteriaBuilder, Root<E>, List<Selection<?>>> selectId) {
        return queryPage(request, resultClass, graphName, mapper, null, null, where, order, selectId);
    }

    public <K, E extends BaseEntitySql & IId<K>, D, A>
    PageResponse<D, A> queryPage(PageRequest request, Class<E> resultClass, String graphName, IBaseMapper<D, E> mapper,
                                 OutputStream out,
                                 Consumer<Root<E>> join,
                                 BiFunction<CriteriaBuilder, Root<E>, Predicate> where,
                                 BiFunction<CriteriaBuilder, Root<E>, List<Order>> order,
                                 BiFunction<CriteriaBuilder, Root<E>, List<Selection<?>>> selectId) {
        try {
            final long companyId = ICompany.class.isAssignableFrom(resultClass) ? auth.getCompanyId() : -1;

            PageResponse<D, A> response = out == null ? new PageResponse<>() : null;
            JsonGenerator jsonGenerator = out != null ? objectMapper.createGenerator(out) : null;
            if (jsonGenerator != null) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("gamaVersion", appVersion);
                jsonGenerator.writeStringField("apiVersion", APP_API_3_VERSION);
                jsonGenerator.writeFieldName("data");
                jsonGenerator.writeStartObject();
            }

            if (request.getPageSize() == PageRequest.MAX_PAGE_SIZE) {

                CriteriaBuilder cb = entityManager.getCriteriaBuilder();

                // Outer query
                CriteriaQuery<E> cr = cb.createQuery(resultClass);
                Root<E> root = cr.from(resultClass);

                // Subquery
                Subquery<Long> scr = cr.subquery(Long.class);
                Root<E> subRoot = scr.from(resultClass);
                scr.select(subRoot.get("id")).distinct(true);

                if (join != null) join.accept(subRoot);

                Predicate predicateWhere = where != null ? where(request, resultClass, cb, subRoot, where) : null;
                if (companyId > 0) {
                    Predicate predicateCompanyId = cb.equal(subRoot.get(BaseCompanySql_.COMPANY_ID), companyId);
                    predicateWhere = predicateWhere != null ? cb.and(predicateCompanyId, predicateWhere) : predicateCompanyId;
                }
                Predicate archiveAndHidden = archiveAndHidden(request, cb, subRoot);
                predicateWhere = predicateWhere != null ? cb.and(predicateWhere, archiveAndHidden) : archiveAndHidden;

                if (predicateWhere != null) scr.where(predicateWhere);

                // Outer query using subquery
                cr.select(root);
                cr.where(root.get("id").in(scr));

                if (order != null) cr.orderBy(order.apply(cb, root));

                TypedQuery<E> query = entityManager.createQuery(cr);
                if (StringHelper.hasValue(graphName)) {
                    query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
                }
                query.setHint(QueryHints.HINT_READONLY, true);
                List<E> list = query.getResultList();

                if (CollectionsHelper.hasValue(list)) {
                    List<D> resultList = new ArrayList<>(list.size());
                    if (response != null) {
                        list.forEach(e -> resultList.add(mapper.toDto(e)));
                        response.setTotal(resultList.size());
                        response.setItems(resultList);
                    } else if (jsonGenerator != null) {
                        jsonGenerator.writeNumberField("total", list.size());
                        jsonGenerator.writeFieldName("items");
                        jsonGenerator.writeStartArray();
                        for (E e : list) {
                            jsonGenerator.writeObject(mapper.toDto(e));
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            } else {

                if (request.getTotal() <= 0) {
                    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                    CriteriaQuery<Long> cr = cb.createQuery(Long.class);
                    Root<E> root = cr.from(resultClass);
                    if (join != null) join.accept(root);
                    cr.select(cb.countDistinct(root));

                    Predicate predicateWhere = where != null ? where(request, resultClass, cb, root, where) : null;
                    if (companyId > 0) {
                        Predicate predicateCompanyId = cb.equal(root.get(BaseCompanySql_.COMPANY_ID), companyId);
                        predicateWhere = predicateWhere != null ? cb.and(predicateCompanyId, predicateWhere) : predicateCompanyId;
                    }
                    if (predicateWhere != null) cr.where(predicateWhere);

                    TypedQuery<Long> query = entityManager.createQuery(cr);
                    long count = query.getSingleResult();

                    if (response != null) {
                        response.setTotal((int) count);
                    } else if (jsonGenerator != null) {
                        jsonGenerator.writeNumberField("total", (int) count);
                    }
                } else {
                    if (response != null) {
                        response.setTotal(request.getTotal());
                    } else if (jsonGenerator != null) {
                        jsonGenerator.writeNumberField("total", request.getTotal());
                    }
                }

                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<Tuple> crId = cb.createQuery(Tuple.class);
                Root<E> rootId = crId.from(resultClass);
                if (join != null) join.accept(rootId);
                if (selectId != null) {
                    crId.multiselect(selectId.apply(cb, rootId)).distinct(true);
                } else {
                    crId.multiselect(rootId.get("id").alias("id")).distinct(true);
                }
                Predicate predicateWhere = where != null ? where(request, resultClass, cb, rootId, where) : null;
                if (companyId > 0) {
                    Predicate predicateCompanyId = cb.equal(rootId.get(BaseCompanySql_.COMPANY_ID), companyId);
                    predicateWhere = predicateWhere != null ? cb.and(predicateCompanyId, predicateWhere) : predicateCompanyId;
                }
                if (predicateWhere != null) crId.where(predicateWhere);
                if (order != null) crId.orderBy(order.apply(cb, rootId));

                int cursor;
                if (request.getCursor() == null) {
                    cursor = 0;
                } else {
                    cursor = request.getCursor();
                }

                if (request.isBackward() && cursor >= request.getPageSize()) {
                    cursor = cursor - request.getPageSize();
                }

                List<Tuple> selectTuples = entityManager.createQuery(crId)
                        .setFirstResult(cursor)
                        .setMaxResults(request.getPageSize() + 1)
                        .getResultList();

                if (selectTuples.size() > request.getPageSize()) {
                    if (response != null) {
                        response.setMore(true);
                    } else if (jsonGenerator != null) {
                        jsonGenerator.writeBooleanField("more", true);
                    }
                    selectTuples.remove(selectTuples.size() - 1);
                }
                List<?> ids = selectTuples.stream().map(t -> t.get("id")).collect(Collectors.toList());
                List<E> listEntity = null;
                if (!ids.isEmpty()) {
                    CriteriaQuery<E> cr = cb.createQuery(resultClass);
                    Root<E> root = cr.from(resultClass);
                    if (join != null) join.accept(root);
                    else cr.select(root);
                    cr.where(root.get("id").in(ids));
                    if (order != null) cr.orderBy(order.apply(cb, root));

                    TypedQuery<E> query = entityManager.createQuery(cr);
                    if (StringHelper.hasValue(graphName)) {
                        query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
                    }
                    query.setHint(QueryHints.HINT_READONLY, true);
                    listEntity = query.getResultList();
                }

                if (CollectionsHelper.hasValue(listEntity)) {
                    if (response != null) {
                        response.setItems(listEntity.stream()
                                .sorted(Comparator.comparingInt(v -> ids.indexOf(v.getId())))
                                .map(mapper::toDto).collect(Collectors.toList()));
                    } else if (jsonGenerator != null) {
                        jsonGenerator.writeFieldName("items");
                        jsonGenerator.writeStartArray();

                        listEntity.sort(Comparator.comparingInt(v -> ids.indexOf(v.getId())));
                        for (E e : listEntity) {
                            jsonGenerator.writeObject(mapper.toDto(e));
                        }
                        jsonGenerator.writeEndArray();
                    }
                }

                if (request.isBackward() && cursor == 0) {
                    if (response != null) {
                        response.setMore(false);
                    } else if (jsonGenerator != null) {
                        jsonGenerator.writeBooleanField("more", false);
                    }
                }

                if (!request.isBackward()) {
                    cursor = cursor + request.getPageSize();
                }

                if (response != null) {
                    response.setCursor(cursor);
                } else if (jsonGenerator != null) {
                    jsonGenerator.writeStringField("cursor", String.valueOf(cursor));
                }
            }

            if (jsonGenerator != null) {
                jsonGenerator.writeEndObject(); // close "data"
                jsonGenerator.writeEndObject(); // close root object
                jsonGenerator.close();
            }

            return response;

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            throw new GamaApiServerErrorException(e.getMessage(), e);
        }
    }

    public <K, E extends BaseEntitySql & IId<K>, D, A>
    PageResponse<D, A> queryPage(PageRequest request, Class<E> resultClass, String graphName, IBaseMapper<D, E> mapper,
                                 Supplier<Query> allQuery,
                                 Supplier<Integer> countQuery,
                                 Function<PageResponse<D, A>, Query> dataQuery) {
        PageResponse<D, A> response = new PageResponse<>();
        if (request.getPageSize() == PageRequest.MAX_PAGE_SIZE) {
            Query query = allQuery.get();
            if (StringHelper.hasValue(graphName)) {
                query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
            }
            query.setHint(QueryHints.HINT_READONLY, true);
            @SuppressWarnings("unchecked")
            List<E> listEntity = query.getResultList();
            if (CollectionsHelper.hasValue(listEntity)) {
                response.setItems(listEntity.stream().map(mapper::toDto).collect(Collectors.toList()));
                response.setTotal(response.getItems().size());
            }
        } else {
            if (request.getTotal() <= 0) {
                response.setTotal(countQuery.get());
            } else {
                response.setTotal(request.getTotal());
            }
            Query query = dataQuery.apply(response);
            if (query != null) {
                if (StringHelper.hasValue(graphName)) {
                    query.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
                }
                query.setHint(QueryHints.HINT_READONLY, true);
                @SuppressWarnings("unchecked")
                List<E> listEntity = query.getResultList();

                response.setResponseValues(request, request.getCursor(), listEntity.size());

                if (CollectionsHelper.hasValue(listEntity)) {
                    response.setItems(listEntity.stream()
                            .limit(request.getPageSize())
                            .map(mapper::toDto)
                            .collect(Collectors.toList()));
                }
            }
        }
        return response;
    }

    public <K, E extends BaseEntitySql & IId<K>, D, A>
    PageResponse<D, A> list(PageRequest request, Class<E> resultClass, String graphName, IBaseMapper<D, E> mapper,
                            BiFunction<CriteriaBuilder, Root<E>, Predicate> where,
                            BiFunction<CriteriaBuilder, Root<E>, List<Order>> order,
                            BiFunction<CriteriaBuilder, Root<E>, List<Selection<?>>> selectId) {
        return queryPage(request, resultClass, graphName, mapper, null, null, where, order, selectId);
    }

    public <K, E extends BaseEntitySql & IId<K>, D, A>
    PageResponse<D, A> list(PageRequest request, Class<E> resultClass, String graphName, IBaseMapper<D, E> mapper,
                            Consumer<Root<E>> join,
                            BiFunction<CriteriaBuilder, Root<E>, Predicate> where,
                            BiFunction<CriteriaBuilder, Root<E>, List<Order>> order,
                            BiFunction<CriteriaBuilder, Root<E>, List<Selection<?>>> selectId) {
        return queryPage(request, resultClass, graphName, mapper, null, join, where, order, selectId);
    }

    public <K, E extends BaseEntitySql & IId<K>, D, A>
    PageResponse<D, A> list(PageRequest request, Class<E> resultClass, String graphName, IBaseMapper<D, E> mapper,
                            OutputStream out,
                            BiFunction<CriteriaBuilder, Root<E>, Predicate> where,
                            BiFunction<CriteriaBuilder, Root<E>, List<Order>> order,
                            BiFunction<CriteriaBuilder, Root<E>, List<Selection<?>>> selectId) {
        return queryPage(request, resultClass, graphName, mapper, out, null, where, order, selectId);
    }

    public <K, E extends BaseEntitySql & IId<K>, D, A>
    PageResponse<D, A> list(PageRequest request, Class<E> resultClass, String graphName, IBaseMapper<D, E> mapper,
                            OutputStream out,
                            Consumer<Root<E>> join,
                            BiFunction<CriteriaBuilder, Root<E>, Predicate> where,
                            BiFunction<CriteriaBuilder, Root<E>, List<Order>> order,
                            BiFunction<CriteriaBuilder, Root<E>, List<Selection<?>>> selectId) {
        return queryPage(request, resultClass, graphName, mapper, out, join, where, order, selectId);
    }

    private <E extends BaseEntitySql>
    Predicate archiveAndHidden(PageRequest request, CriteriaBuilder cb, Root<E> root) {
        Predicate archive = null;
        Predicate hidden = null;
        if (CollectionsHelper.hasValue(request.getConditions())) {
            Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.ARCHIVE);
            if (value instanceof Boolean b && b) {
                archive = cb.equal(root.get(BaseEntitySql_.ARCHIVE), true);
            }
            value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.HIDDEN);
            if (!(value instanceof Boolean b && b)) {
                // if not hidden
                hidden = cb.or(cb.equal(root.get(BaseEntitySql_.HIDDEN), false), cb.isNull(root.get(BaseEntitySql_.HIDDEN)));
            }
        }
        if (archive == null) archive = cb.or(cb.equal(root.get(BaseEntitySql_.ARCHIVE), false), cb.isNull(root.get(BaseEntitySql_.ARCHIVE)));
        return hidden == null ? archive : cb.and(archive, hidden);
    }

    private <E extends BaseEntitySql>
    Predicate where(PageRequest request, Class<E> resultClass, CriteriaBuilder cb, Root<E> root,
                    BiFunction<CriteriaBuilder, Root<E>, Predicate> whereBuilder) {

        Predicate where = archiveAndHidden(request, cb, root);

        if (DoubleEntrySql.class.isAssignableFrom(resultClass)) {
            Boolean finished = (Boolean) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.FINISHED);
            Predicate finishedFilter = finished == null ? null : finished
                    ? cb.equal(root.get(DoubleEntrySql_.FINISHED_GL), true)
                    : cb.or(cb.equal(root.get(DoubleEntrySql_.FINISHED_GL), false), cb.isNull(root.get(DoubleEntrySql_.FINISHED_GL)));
            if (finishedFilter != null) where = cb.and(where, finishedFilter);

        } else if (BaseDocumentSql.class.isAssignableFrom(resultClass)) {
            Boolean finished = (Boolean) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.FINISHED);
            Predicate finishedFilter = finished == null ? null : finished
                    ? cb.equal(root.get(BaseDocumentSql_.FINISHED), true)
                    : cb.or(cb.equal(root.get(BaseDocumentSql_.FINISHED), false), cb.isNull(root.get(BaseDocumentSql_.FINISHED)));
            if (finishedFilter != null) where = cb.and(where, finishedFilter);

            Boolean finishedGL = (Boolean) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.FINISHED_GL);
            Predicate finishedGlFilter = finishedGL == null ? null : finishedGL
                    ? cb.equal(root.get(BaseDocumentSql_.FINISHED_GL), true)
                    : cb.or(cb.equal(root.get(BaseDocumentSql_.FINISHED_GL), false), cb.isNull(root.get(BaseDocumentSql_.FINISHED_GL)));
            if (finishedGlFilter != null) where = cb.and(where, finishedGlFilter);
        }

        if (request.isDateRange() && !AssetSql.class.isAssignableFrom(resultClass)) {
            if (request.getDateFrom() != null) {
                where = IDoc.class.isAssignableFrom(resultClass) ?
                        cb.and(where, cb.greaterThanOrEqualTo(root.get("doc").get("date"), request.getDateFrom())) :
                        cb.and(where, cb.greaterThanOrEqualTo(root.get("date"), request.getDateFrom()));
            }
            if (request.getDateTo() != null) {
                where = IDoc.class.isAssignableFrom(resultClass) ?
                        cb.and(where, cb.lessThanOrEqualTo(root.get("doc").get("date"), request.getDateTo())) :
                        cb.and(where, cb.lessThanOrEqualTo(root.get("date"), request.getDateTo()));
            }
        }

        if (StringHelper.hasValue(request.getLabel())) {
            where = cb.and(where, cb.isTrue(
                    cb.function(JSONB_EXISTS, Boolean.class, root.get(BaseDocumentSql_.LABELS), cb.literal(request.getLabel()))));
        }

        if (whereBuilder != null) {
            Predicate pre = whereBuilder.apply(cb, root);
            if (pre != null) where = cb.and(where, pre);
        }
        return where;
    }

    @Transactional
    public <E extends BaseEntitySql> E saveEntity(final E entity) {
        if (entity instanceof BaseCompanySql) {
            throw new GamaException("Need to use 'saveEntityInCompany' method");
        }
        if (!(entity instanceof IId)) {
            throw new GamaException("Wrong entity " + entity.getClass().getSimpleName());
        }
        E saved;
        if (((IId<?>) entity).getId() == null) {
            entityManager.persist(entity);
            saved = entity;
        } else {
            saved = entityManager.merge(entity);
        }
        if (saved instanceof CompanySql company) {
            authSettingsCacheService.put(company.getId(), company.getSettings());
        }
        return saved;
    }

    @Transactional
    public <E extends BaseCompanySql> E saveEntityInCompany(E entity) {
        if (entity.getCompanyId() > 0 && entity.getCompanyId() != auth.getCompanyId()) {
            throw new GamaUnauthorizedException();
        }
        return executeAndReturnInTransaction(entityManager -> {
            entity.setCompanyId(auth.getCompanyId());
            if (entity.getId() == null || entity.getId() == 0) {
                entityManager.persist(entity);
                return entity;
            } else {
                return entityManager.merge(entity);
            }
        });
    }

    public <E extends BaseNumberDocumentSql> E saveWithCounter(E entity) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        CounterDesc desc = companySettings.getCounterByClass(entity.getClass());
        return saveWithCounter(entity, desc);
    }

    public <E extends BaseNumberDocumentSql> E saveWithCounter(E entity, CounterDesc desc) {
        return executeAndReturnInTransaction(entityManager -> {
            if (BooleanUtils.isTrue(entity.getAutoNumber())) {
                if (entity instanceof BankOperationSql) {
                    ISeriesWithOrdinal seriesWithOrdinal = counterService.next(desc);
                    entity.setOrdinal(seriesWithOrdinal.getOrdinal());
                    entity.setSeries(seriesWithOrdinal.getSeries());
                    entity.setNumber(seriesWithOrdinal.getNumber());
                } else {
                    String series = desc != null ? StringHelper.trim2null(desc.getPrefix()) : null;
                    LocalDate dateFrom = entity.getDate().minusMonths(3); // look 3 month back only
                    Long id = entity.getId();
                    TypedQuery<Long> lastNumberQuery = entityManager.createQuery(
                                    "SELECT MAX(a.ordinal) FROM " + entity.getClass().getName() + " a" +
                                            " WHERE a.companyId = :companyId AND a.date > :dateFrom" +
                                            " AND " +
                                            (StringHelper.isEmpty(series)
                                                    ? " (a.series IS NULL OR a.series = '')"
                                                    : " (a.series = :series)") +
                                            (id != null ? " AND a.id <> :id" : ""),
                                    Long.class)
                            .setParameter("companyId", auth.getCompanyId())
                            .setParameter("dateFrom", dateFrom);
                    if (StringHelper.hasValue(series)) lastNumberQuery.setParameter("series", series);
                    if (id != null) lastNumberQuery.setParameter("id", id);
                    Long lastNumber = lastNumberQuery.getSingleResult();
                    entity.setOrdinal(lastNumber == null
                            ? (desc != null && desc.getStart() != null ? desc.getStart() : 1)
                            : lastNumber + 1);
                    entity.setSeries(series);
                    entity.setNumber(counterService.format(entity.getOrdinal(), desc));
                }
                entity.setAutoNumber(null);
            } else {
                counterService.decodeDocNumber(entity);
            }
            return saveEntityInCompany(entity);
        });
    }

    public <K, E extends EntitySql & IId<K>> int removeById(Class<E> type, K id) {
        return executeAndReturnInTransaction(entityManager -> {
            int deleted = entityManager.createQuery("DELETE FROM " + type.getName() + " a WHERE a.id = :id")
                    .setParameter("id", id).executeUpdate();
            log.info("deleted " + type.getSimpleName() + ": " + deleted);
            return deleted;
        });
    }

    public <E extends BaseCompanySql> void deleteById(Class<E> type, long id) {
        executeInTransaction(entityManager -> {
            E entity = entityManager.find(type, id);
            if (entity == null) {
                throw new GamaException("Not found");
            }
            if (entity.getCompanyId() != auth.getCompanyId()) {
                throw new GamaUnauthorizedException();
            }
            if (entity instanceof BaseDocumentSql && BooleanUtils.isTrue(((BaseDocumentSql) entity).getFinished())) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
            }
            entity.setArchive(true);
        });
    }

    public <E extends EntitySql & ICompany> void deleteAll(Class<E> type) {
        executeInTransaction(entityManager -> {
            int deleted = entityManager.createQuery("DELETE FROM " + type.getName() + " a WHERE a.companyId = :companyId")
                    .setParameter("companyId", auth.getCompanyId()).executeUpdate();
            log.info("deleted " + type.getSimpleName() + ": " + deleted);
        });
    }

    public <E extends BaseCompanySql> E undeleteById(Class<E> type, long id) {
        return executeAndReturnInTransaction(entityManager -> {
            E entity = Validators.checkNotNull(entityManager.find(type, id), "Not found");
            if (entity.getCompanyId() != auth.getCompanyId()) {
                log.warn("Wrong company");
                throw new GamaUnauthorizedException("Wrong company, entity id/companyId=" +
                        id + "/" + entity.getCompanyId() + ", auth companyId=" + auth.getCompanyId());
            }
            entity.setArchive(false);
            return entity;
        });
    }

    @Transactional
    public void executeInTransaction(ConsumerWithExeption<EntityManager> executor) {
        executeAndReturnInTransaction(entityManager -> {
            executor.accept(entityManager);
            return null;
        });
    }

//    public <T> T executeAndReturnInTransaction(FunctionWithException<EntityManager, T> executor) {
//        var entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
//        final EntityTransaction transaction = entityManager.getTransaction();
//        boolean inTransactionAlready = transaction.isActive();
//        if (!inTransactionAlready) transaction.begin();
//        try {
//            T result = executor.apply(entityManager);
//            if (!inTransactionAlready) transaction.commit();
//            return result;
//        } catch (Exception e) {
//            if (!inTransactionAlready) transaction.rollback();
//
//            if (e instanceof GamaException gamaException) throw gamaException;
//            throw new GamaException(e.getMessage(), e);
//        }
//    }

    @Transactional
    public <T> T executeAndReturnInTransaction(FunctionWithException<EntityManager, T> executor) {
        try {
            return executor.apply(entityManager);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GamaException(e);
        }
    }

    public CompanySettings getAndCheckCompanySettings(long companyId) {
        return Validators.checkNotNull(getCompanySettings(companyId),
                MessageFormat.format(TranslationService.getInstance().translate(TranslationService.DB.NoCompanySettings, auth.getLanguage()), companyId));
    }

    public CompanySettings getCompanySettings(long companyId) {
        return authSettingsCacheService.get(companyId);
    }

    public VATRate getMaxVATRate(String country, LocalDate date) {
        VATRatesDate vatRatesDate = getVATRateDate(country, date);
        return vatRatesDate != null ? vatRatesDate.getRates().stream().max(Comparator.comparing(VATRate::getRate)).orElse(null) : null;
    }

    public VATRatesDate getVATRateDate(String country, LocalDate date) {
        CountryVatRateSql countryVatRate = getById(CountryVatRateSql.class, country);
        return countryVatRate != null ? countryVatRate.getRatesMap(date) : null;
    }

    public <P extends IDocPart & IPartSN>
    InventoryNowSql getInventoryNow(WarehouseTagged warehouse, P part, Long docId) {
        return getInventoryNow(warehouse, part, docId, null);
    }

    public <P extends IDocPart & IPartSN>
    InventoryNowSql getInventoryNow(WarehouseTagged warehouse, P part, Long docId, String graphName) {
        List<InventoryNowSql> inventoriesNow = getInventoriesNow(warehouse, part, docId, graphName);
        if (inventoriesNow.size() > 1) throw new GamaException("To many InventoryNow records");
        return inventoriesNow.size() == 1 ? inventoriesNow.get(0) : null;
    }

    public <P extends IDocPart & IPartSN>
    List<InventoryNowSql> getInventoriesNow(WarehouseTagged warehouse, P part) {
        return getInventoriesNow(warehouse, part, null);
    }

    public <P extends IDocPart & IPartSN>
    List<InventoryNowSql> getInventoriesNow(WarehouseTagged warehouse, P part, String graphName) {
        return getInventoriesNow(warehouse, part, null, graphName);
    }

    public <P extends IDocPart & IPartSN>
    List<InventoryNowSql> getInventoriesNow(WarehouseTagged warehouse, P part, Long docId, String graphName) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT i FROM " + InventoryNowSql.class.getName() + " i");
        sj.add("WHERE i." + InventoryNowSql_.COMPANY_ID + " = :companyId");
        sj.add("AND i." + InventoryNowSql_.PART + "." + PartSql_.ID + " = :partId");
        sj.add("AND i." + InventoryNowSql_.WAREHOUSE + "." + WarehouseSql_.ID + " = :warehouseId");
        if (docId != null) sj.add("AND i." + InventoryNowSql_.DOC + ".id = :docId");
        if (warehouse.getTag() != null) sj.add("AND i." + InventoryNowSql_.TAG + " = :tag");
        if (part.getType() == PartType.PRODUCT_SN) {
            sj.add("AND i." + InventoryNowSql_.SN + "." + InventoryNowSql_.SN +
                    ((part.getSn() != null && StringHelper.hasValue(part.getSn().getSn()) ? " = :sn" : " IS NULL")));
        }
        sj.add("ORDER BY i." + InventoryNowSql_.DOC + ".date, i." + InventoryNowSql_.ID);

        TypedQuery<InventoryNowSql> q = entityManager.createQuery(sj.toString(), InventoryNowSql.class);
        q.setParameter("companyId", auth.getCompanyId());
        q.setParameter("partId", part.getPartId());
        q.setParameter("warehouseId", warehouse.getId());
        if (docId != null) q.setParameter("docId", docId);
        if (warehouse.getTag() != null) q.setParameter("tag", warehouse.getTag());
        if (part.getType() == PartType.PRODUCT_SN && part.getSn() != null && StringHelper.hasValue(part.getSn().getSn())) {
            q.setParameter("sn", part.getSn().getSn());
        }
        if (StringHelper.hasValue(graphName)) {
            q.setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(graphName));
        }
        return q.getResultList();
    }
}
