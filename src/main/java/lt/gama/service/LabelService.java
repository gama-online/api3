package lt.gama.service;

import lt.gama.api.request.UpdateLabelsRequest;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.StringHelper;
import lt.gama.model.i.base.IBaseCompany;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.tasks.DeleteLabelTask;
import lt.gama.tasks.UpdateLabelTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * gama-online
 * Created by valdas on 2016-03-18.
 */
@Service
public class LabelService {

    private static final Logger log = LoggerFactory.getLogger(LabelService.class);


    private final DBServiceSQL dbServiceSQL;
    private final Auth auth;
    private final TaskQueueService taskQueueService;


    public LabelService(DBServiceSQL dbServiceSQL, Auth auth, TaskQueueService taskQueueService) {
        this.dbServiceSQL = dbServiceSQL;
        this.auth = auth;
        this.taskQueueService = taskQueueService;
    }

    public <E extends IBaseCompany> E updateLabels(UpdateLabelsRequest request, final Class<E> entityClass) {
        final long id = request.getId();
        final Set<String> labels = request.getLabels();

        if (BaseCompanySql.class.isAssignableFrom(entityClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends BaseCompanySql> entitySqlClass = (Class<BaseCompanySql>) entityClass;
            @SuppressWarnings("unchecked")
            E document = (E) dbServiceSQL.getAndCheck(entitySqlClass, id);
            if (document == null) {
                log.error(entityClass.getSimpleName() + " not found " + request.getId() + " in company " + auth.getCompanyId());
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument, auth.getLanguage()));
            }

           return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                @SuppressWarnings("unchecked")
                E entity = (E) dbServiceSQL.getById(entitySqlClass, id);
                entity.setLabels(labels);
                return entity;
            });

        } else {
            throw new GamaException("Wrong entity: " + entityClass.getName());
        }
    }

    public <E extends IBaseCompany> void deleteLabel(final String label, final Class<E> entityClass) {
        if (entityClass != null && StringHelper.hasValue(label)) {
            taskQueueService.queueTask(new DeleteLabelTask<>(auth.getCompanyId(), label, entityClass));
        }
    }

    public <E extends IBaseCompany> void deleteLabelAction(String label, Class<E> entityClass) {
        if (BaseCompanySql.class.isAssignableFrom(entityClass)) {
            try {
                int updated = dbServiceSQL.executeAndReturnInTransaction(entityManager -> entityManager.createQuery(
                        "UPDATE " + entityClass.getName() + " e" +
                                " SET e.labels = gama_jsonb_remove_key(e.labels, :label)" +
                                " WHERE companyId = :companyId" +
                                " AND gama_jsonb_exists(labels, :label) = true")
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("label", label)
                        .executeUpdate());
                log.info( updated + " " + entityClass.getSimpleName() + " records updated");
            } catch (Exception e) {
                throw new GamaException(e.getMessage(), e);
            }

        } else {
            throw new GamaException("Wrong entity: " + entityClass.getName());
        }
    }

    public <E extends IBaseCompany> void updateLabel(String labelOld, String labelNew, Class<E> entityClass) {
        if (entityClass != null && StringHelper.hasValue(labelOld) && StringHelper.hasValue(labelNew) && !labelOld.equals(labelNew)) {
            taskQueueService.queueTask(new UpdateLabelTask<>(auth.getCompanyId(), labelOld, labelNew, entityClass));
        }
    }

    public <E extends IBaseCompany> void updateLabelAction(String labelOld, String labelNew, Class<E> entityClass) {
        if (BaseCompanySql.class.isAssignableFrom(entityClass)) {
            try {
                int updated = dbServiceSQL.executeAndReturnInTransaction(entityManager -> entityManager.createQuery(
                        "UPDATE " + entityClass.getName() + " e" +
                                " SET e.labels = gama_jsonb_add_text(gama_jsonb_remove_key(e.labels, :labelOld), :labelNew)" +
                                " WHERE companyId = :companyId" +
                                " AND gama_jsonb_exists(labels, :labelOld) = true")
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("labelOld", labelOld)
                        .setParameter("labelNew", labelNew)
                        .executeUpdate());
                log.info( updated + " " + entityClass.getSimpleName() + " records updated");
            } catch (Exception e) {
                throw new GamaException(e.getMessage(), e);
            }

        } else {
            throw new GamaException("Wrong entity: " + entityClass.getName());
        }
    }
}
