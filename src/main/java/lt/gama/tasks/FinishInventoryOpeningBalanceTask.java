package lt.gama.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2018-12-12.
 */
public class FinishInventoryOpeningBalanceTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected TradeService tradeService;


    private final long id;


    public FinishInventoryOpeningBalanceTask(long companyId, long id) {
        super(companyId);
        this.id = id;
    }

    @Override
    public void execute() {
        try {
            finish(tradeService.runOpeningBalanceTask(id));
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }


    @Override
    public String toString() {
        return "id=" + id +
                ' ' + super.toString();
    }
}
