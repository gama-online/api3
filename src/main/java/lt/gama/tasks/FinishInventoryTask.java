package lt.gama.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.service.TradeService;
import lt.gama.service.ex.rt.GamaNotEnoughQuantityException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2019-02-08.
 */
public class FinishInventoryTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected TradeService tradeService;


    private final long id;
    private final Boolean finishGL;


    public FinishInventoryTask(long companyId, long id, Boolean finishGL) {
        super(companyId);
        this.id = id;
        this.finishGL = finishGL;
    }

    @Override
    public void execute() {
        try {
            finish(tradeService.runFinishInventoryTask(id, finishGL));
        } catch (GamaNotEnoughQuantityException e) {
            finish(TaskResponse.errors(e.getMessages()));
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }


    @Override
    public String toString() {
        return "id=" + id +
                " finishGL=" + finishGL +
                ' ' + super.toString();
    }
}