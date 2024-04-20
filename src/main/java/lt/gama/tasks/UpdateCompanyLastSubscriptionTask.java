package lt.gama.tasks;

import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.GamaMoney;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2018-06-18.
 */
public class UpdateCompanyLastSubscriptionTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    private final GamaMoney lastTotal;


    public UpdateCompanyLastSubscriptionTask(long companyId, GamaMoney lastTotal) {
        super(companyId);
        this.lastTotal = lastTotal;
    }

    @Override
    public void execute() {
        try {
            dbServiceSQL.executeInTransaction(entityManager -> {
                CompanySql company = dbServiceSQL.getById(CompanySql.class, getCompanyId());
                if (company != null) {
                    company.setLastTotal(this.lastTotal);
                    dbServiceSQL.saveEntity(company);
                }
            });
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }

}
