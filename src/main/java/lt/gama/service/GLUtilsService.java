package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.i.IGLOperation;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.DoubleEntrySqlMapper;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.DoubleEntrySql_;
import lt.gama.model.sql.documents.items.GLOperationSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.ex.rt.GamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GLUtilsService {

    private static final Logger log = LoggerFactory.getLogger(GLUtilsService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;

    public GLUtilsService(Auth auth, DBServiceSQL dbServiceSQL, DoubleEntrySqlMapper doubleEntrySqlMapper) {
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
    }

    public DoubleEntrySql createDoubleEntry(IBaseDocument document) {
        Validators.checkNotNull(document, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
        DoubleEntrySql entity = new DoubleEntrySql();
        entity.setParentId(document.getId());
        entity.setParentType(document.getDocumentType());
        entity.setCompanyId(document.getCompanyId());
        entity.setDate(document.getDate());
        entity.setAutoNumber(true);

        entity.setParentNumber(document.getNumber());
        if (Validators.isValid(document.getCounterparty())) {
            entity.setParentCounterparty(entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()));
        }

        return entity;
    }

    public DoubleEntrySql updateFinishedGL(DoubleEntrySql entity, boolean finish) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            if (BooleanUtils.isTrue(entity.getFinishedGL()) != finish) {
                // if document is archived - "restore" it, i.e. clear archive flag
                if (BooleanUtils.isTrue(entity.getArchive())) entity.setArchive(false);
                entity.setFinishedGL(finish);
            }
            return dbServiceSQL.saveWithCounter(entity);
        });
    }

    public DoubleEntrySql getDoubleEntryByParentId(long parentId) {
        List<DoubleEntrySql> list = entityManager
                .createQuery("SELECT DISTINCT de " +
                                " FROM " + DoubleEntrySql.class.getName() + " de " +
                                " LEFT JOIN FETCH de." + DoubleEntrySql_.OPERATIONS + " op " +
                                " WHERE de." + DoubleEntrySql_.PARENT_ID + " = :parentId " +
                                " AND de.companyId = :companyId",
                        DoubleEntrySql.class)
                .setParameter("parentId", parentId)
                .setParameter("companyId", auth.getCompanyId())
                .getResultList();
        if (list != null && list.size() > 1) {
            log.error(list.size() + " DoubleEntry records are linked to parent: " + parentId + " in company: " + auth.getCompanyId());
            // unlink all of them
            List<Long> ids = list.stream().map(DoubleEntrySql::getId).collect(Collectors.toList());
            try {
                int updated = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                        entityManager.createQuery("UPDATE " + DoubleEntrySql.class.getName() +
                                        " SET " + DoubleEntrySql_.PARENT_ID + " = NULL" +
                                        ", " + DoubleEntrySql_.PARENT_TYPE + " = NULL" +
                                        ", " + DoubleEntrySql_.PARENT_DB + " = NULL" +
                                        " WHERE id IN :ids")
                                .setParameter("ids", ids)
                                .executeUpdate());
                log.info(updated + " DoubleEntry records are unlinked from parent: " + parentId + " in company: " + auth.getCompanyId());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return list != null && list.size() == 1 ? list.get(0) : null;
    }

    public DoubleEntrySql saveDoubleEntry(DoubleEntrySql doubleEntry) {
        if (doubleEntry.getId() == null) {
            if (doubleEntry.getOperations() != null) {
                doubleEntry.getOperations().forEach(op -> {
                    op.setId(null);
                    op.setDoubleEntry(doubleEntry);
                });
                assignSortOrder(doubleEntry.getOperations());
            }
            return dbServiceSQL.saveWithCounter(doubleEntry);
        }
        return updateDoubleEntry(doubleEntry);
    }

    public DoubleEntrySql updateState(long id, DBType parentDbType, Boolean parent, boolean finish, boolean updateParent) {
        DoubleEntrySql entity = BooleanUtils.isTrue(parent) ?
                getDoubleEntryByParentId(id) :
                dbServiceSQL.getById(DoubleEntrySql.class, id, DoubleEntrySql.GRAPH_ALL);

        if (entity == null) {
            log.error("No double entry record found - trying to recreate - id=" + id + ", companyId=" + auth.getCompanyId());
            BaseDocumentSql parentDocument = dbServiceSQL.getAndCheck(BaseDocumentSql.class, id);
            DoubleEntrySql doubleEntry = createDoubleEntry(parentDocument);
            doubleEntry.setFinishedGL(finish);
            if (BooleanUtils.isTrue(parentDocument.getFinishedGL()) != finish) {
                parentDocument.setFinishedGL(finish);
            }
            return dbServiceSQL.saveWithCounter(doubleEntry);
        }

        if (BooleanUtils.isTrue(entity.getFinishedGL()) == finish) return entity;

        entity.setFinishedGL(finish);

        // if document is archived - "restore" it, i.e. clear archive flag
        if (BooleanUtils.isTrue(entity.getArchive())) entity.setArchive(false);

        // update master document in DB (if updateParent = true)
        if (updateParent && entity.getParentId() != null && entity.getParentId() > 0) {
            BaseDocumentSql document = dbServiceSQL.getAndCheck(BaseDocumentSql.class, entity.getParentId());
            if (document != null && !BooleanUtils.isSame(document.getFinishedGL(), finish)) {
                document.setFinishedGL(finish);
                dbServiceSQL.saveEntityInCompany(document);
            }
        }

        return dbServiceSQL.saveEntityInCompany(entity);
    }

    public void assignSortOrder(List<? extends ISortOrder> items) {
        if (items != null && items.stream().anyMatch(e -> e.getSortOrder() == null)) {
            double sortOrder = 128.0;
            for (ISortOrder item : items) {
                item.setSortOrder(sortOrder);
                sortOrder += 128.0;
            }
        }
    }

    private DoubleEntrySql updateDoubleEntry(DoubleEntrySql doubleEntry) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            DoubleEntrySql entity = dbServiceSQL.getById(DoubleEntrySql.class, doubleEntry.getId(), DoubleEntrySql.GRAPH_ALL);
            if (entity == null) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
            }
            Validators.checkDocumentVersion(entity, doubleEntry, auth.getLanguage());
            if (entity != doubleEntry && BooleanUtils.isTrue(entity.getFinishedGL())) {
                log.error(MessageFormat.format("Operation {0} {1} is finished already",
                        DoubleEntrySql.class.getSimpleName(), doubleEntry.getId()));
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
            }

            entity.setCompanyId(auth.getCompanyId());
            entity.setArchive(false);   // always unarchived on save
            entity.setNumber(doubleEntry.getNumber());
            entity.setAutoNumber(doubleEntry.getAutoNumber());
            entity.setDate(doubleEntry.getDate());
            entity.setContent(doubleEntry.getContent());
            entity.setFrozen(doubleEntry.getFrozen());
            entity.setTotal(doubleEntry.getTotal());

            mergeGLOperation(entity, doubleEntrySqlMapper.toOperationDtoList(doubleEntry.getOperations()));

            setDataFromParentDocument(entity);
            return dbServiceSQL.saveWithCounter(entity);
        });
    }

    public void setDataFromParentDocument(DoubleEntrySql doubleEntry, IBaseDocument parentDocument) {
        if (doubleEntry == null || parentDocument == null) return;
        doubleEntry.setParentType(parentDocument.getDocumentType());
        if (doubleEntry.getDate() == null) doubleEntry.setDate(parentDocument.getDate());
        doubleEntry.setParentNumber(parentDocument.getNumber());
        if (Validators.isValid(parentDocument.getCounterparty())) {
            doubleEntry.setParentCounterparty(entityManager.getReference(CounterpartySql.class, parentDocument.getCounterparty().getId()));
        }
        doubleEntry.setParentDb(parentDocument.getDb());
    }

    public void setDataFromParentDocument(DoubleEntrySql doubleEntry) {
        if (doubleEntry == null || doubleEntry.getParentId() == null) return;
        IBaseDocument parentDocument = dbServiceSQL.getById(BaseDocumentSql.class, doubleEntry.getParentId());
        setDataFromParentDocument(doubleEntry, parentDocument);
    }

    public void mergeGLOperation(DoubleEntrySql db, List<GLOperationDto> changes) {
        if (changes == null || changes.size() == 0) {
            db.getOperations().clear();
        } else {
            final Map<Long, GLOperationDto> changesIds = changes.stream()
                    .filter(op -> op.getId() != null)
                    .collect(Collectors.toMap(GLOperationDto::getId, Function.identity()));
            // 0. find changes operations which are the same as in db but without id and assign id to them,
            // so they will not be recreated
            db.getOperations().stream()
                    .filter(dbOp -> !changesIds.containsKey(dbOp.getId()))
                    .forEach(dbOp ->
                            changes.stream()
                                    .filter(changesOp -> changesOp.getId() == null && isTheSameAccountsAndRCs(dbOp, changesOp))
                                    .findAny()
                                    .ifPresent(changesOp -> {
                                        changesOp.setId(dbOp.getId());
                                        changesIds.put(changesOp.getId(), changesOp);
                                    })
                    );

            // 1. delete from db if not exists in ids (changes)
            db.getOperations().removeIf(op -> !changesIds.containsKey(op.getId()));
            // 2. update info in db from ids (changes)
            db.getOperations().forEach(op -> {
                GLOperationDto opChanged = changesIds.get(op.getId());
                op.setDebit(opChanged.getDebit());
                op.setDebitRC(opChanged.getDebitRC());
                op.setCredit(opChanged.getCredit());
                op.setCreditRC(opChanged.getCreditRC());
                op.setAmount(opChanged.getAmount());
                op.setSortOrder(opChanged.getSortOrder());
            });
            // 3. insert in db new records from ids (changes)
            changes.stream()
                    .filter(op -> op.getId() == null)
                    .forEach(op ->
                            db.addOperation(doubleEntrySqlMapper.toOperationEntity(op)));
            //.forEach(db::addOperation);

            // x. sort
            db.getOperations().sort(Comparator.comparing(GLOperationSql::getSortOrder, Comparator.nullsFirst(Comparator.naturalOrder())));
        }
    }

    private boolean isTheSameAccountsAndRCs(IGLOperation op1, IGLOperation op2) {
        return op1 == op2 ||
                (op1.getDebit().equals(op2.getDebit()) &&
                        op1.getCredit().equals(op2.getCredit()) &&
                        CollectionsHelper.isEqual(op1.getDebitRC(), op2.getDebitRC()) &&
                        CollectionsHelper.isEqual(op1.getCreditRC(), op2.getCreditRC()));
    }
}
