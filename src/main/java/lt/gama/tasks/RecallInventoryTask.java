package lt.gama.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2019-02-11.
 */
public class RecallInventoryTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected TradeService tradeService;


    private final long id;
    private final boolean noTransaction;


    public RecallInventoryTask(long companyId, long id, boolean noTransaction) {
        super(companyId);
        this.id = id;
        this.noTransaction = noTransaction;
    }

    @Override
    public void execute() {
        try {
            finish(tradeService.runRecallInventoryTask(id, noTransaction));
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }


    @Override
    public String toString() {
        return "id=" + id +
                " noTransaction=" + noTransaction +
                ' ' + super.toString();
    }
}
