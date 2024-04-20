package lt.gama.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2018-12-12.
 */
public class WorkHoursTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected SalaryService salaryService;


    private final int year;
    private final int month;
    /**
     * true - old records will be deleted and new created
     * false - only missed records will be added
     */
    private final boolean fresh;


    public WorkHoursTask(long companyId, int year, int month, boolean fresh) {
        super(companyId);
        this.year = year;
        this.month = month;
        this.fresh = fresh;
    }

    @Override
    public void execute() {
        try {
            finish(salaryService.refreshWorkHoursTask(year, month, fresh, null));
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }


    @Override
    public String toString() {
        return "year=" + year +
                " month=" + month +
                " fresh=" + fresh +
                ' ' + super.toString();
    }
}
