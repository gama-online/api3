package lt.gama.tasks.maintenance;

import lt.gama.service.TradeService;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.time.LocalDate;

public class CreateInvoicesTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected TradeService tradeService;


    private final int count;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;


    public CreateInvoicesTask(long companyId, int count, LocalDate dateFrom, LocalDate dateTo) {
        super(companyId);
        this.count = count;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    @Override
    public void execute() {
        try {
            tradeService.runCreateInvoicesTask(count, dateFrom, dateTo);
            log.info("Created " + count + " invoices");
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }
}
