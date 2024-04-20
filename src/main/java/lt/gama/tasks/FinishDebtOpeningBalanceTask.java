package lt.gama.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class FinishDebtOpeningBalanceTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected DebtService debtService;

    private final long id;

    public FinishDebtOpeningBalanceTask(long companyId, long id) {
        super(companyId);
        this.id = id;
    }

    @Override
    public void execute() {
        try {
            finish(debtService.runOpeningBalanceTask(id));
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
