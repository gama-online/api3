package lt.gama.tasks.sync;

import lt.gama.api.response.TaskResponse;
import lt.gama.service.TradeService;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class SyncTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected TradeService tradeService;


    public SyncTask(long companyId) {
        super(companyId);
    }

    @Override
    public void execute() {
        try {
            finish(tradeService.runSyncTask());
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }
}
