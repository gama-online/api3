package lt.gama.tasks;

import lt.gama.model.sql.base.BaseCompanySql;

import java.io.Serial;

public class DeleteEntityInCompanyTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    private final String entityName;


    public DeleteEntityInCompanyTask(long companyId, String entityName) {
        super(companyId);
        this.entityName = entityName;
    }

    @Override
    public void execute() {
        try {
            if (entityName == null) {
                log.error("No entity");
                return;
            }
            Class<?> entityClass;
            try {
                entityClass = Class.forName(entityName);
            } catch (ClassNotFoundException e) {
                log.error("Class not found: '" + entityName + "'");
                return;
            }

            // proceed
            if (BaseCompanySql.class.isAssignableFrom(entityClass)) {
                //noinspection unchecked
                dbServiceSQL.deleteAll((Class<? extends BaseCompanySql>) entityClass);
            } else {
                log.error(entityClass.getSimpleName() + " not ICompany & EntityDS");
            }
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }
}
