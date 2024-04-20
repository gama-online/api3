package lt.gama.tasks.maintenance;

import lt.gama.api.response.BatchFixResponse;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.RequestTimeoutChecker;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.service.CheckRequestTimeoutService;
import lt.gama.service.DepreciationService;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.util.List;

import static lt.gama.Constants.DB_BATCH_SIZE;

public class FixAssetTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    private static final int BUFFER_SIZE = 500;

    @Autowired
    transient protected DepreciationService depreciationService;

    @Autowired
    transient protected CheckRequestTimeoutService checkRequestTimeoutService;


    private final int processed;
    private final int fixed;


    public FixAssetTask() {
        super(-1);
        this.processed = 0;
        this.fixed = 0;
    }

    public FixAssetTask(int processed, int fixed) {
        super(-1);
        this.processed = processed;
        this.fixed = fixed;
    }

    @Override
    public void execute() {
        try {
            final BatchFixResponse batchFixResponse = new BatchFixResponse(processed, fixed);

            // proceed
            RequestTimeoutChecker requestTimeoutChecker = checkRequestTimeoutService.init();

            List<AssetSql> assets = dbServiceSQL.makeQuery(AssetSql.class)
                    .setFirstResult(processed)
                    .setMaxResults(BUFFER_SIZE)
                    .getResultList();

            dbServiceSQL.executeInTransaction(entityManager -> {
                int count = BUFFER_SIZE;
                for (AssetSql asset : assets) {
                    count--;

                    batchFixResponse.add(depreciationService.fixAsset(asset, DateUtils.date()));

                    if (batchFixResponse.getProcessed() % DB_BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }

                    if (requestTimeoutChecker.isTimeout()) {
                        // if timeout - start a new task
                        count = 0;
                        break;
                    }
                }

                entityManager.flush();
                entityManager.clear();

                if (count == 0) {
                    log.info(className + ": Start a Asset fix task at " + batchFixResponse.getProcessed());
                    taskQueueService.queueTask(new FixAssetTask(batchFixResponse.getProcessed(), batchFixResponse.getProcessed()));
                } else {
                    log.info(className + ": Asset records fixed " + batchFixResponse.getFixed() + " of " + batchFixResponse.getProcessed());
                }
            });

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }
}
