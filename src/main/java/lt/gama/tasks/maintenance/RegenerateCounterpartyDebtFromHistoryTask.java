package lt.gama.tasks.maintenance;

import lt.gama.service.DebtService;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class RegenerateCounterpartyDebtFromHistoryTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected DebtService debtService;


    private final long counterpartyId;


    public RegenerateCounterpartyDebtFromHistoryTask(long companyId, long counterpartyId) {
        super(companyId);
        this.counterpartyId = counterpartyId;
    }

    @Override
    public void execute() {
        try {
            debtService.regenerateCounterpartyDebtFromHistory(counterpartyId);
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }


    @Override
    public String toString() {
        return "counterpartyId=" + counterpartyId +
                ' ' + super.toString();
    }
}
