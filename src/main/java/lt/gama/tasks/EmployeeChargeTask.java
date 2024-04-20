package lt.gama.tasks;

import lt.gama.api.request.SalaryType;
import lt.gama.api.response.TaskResponse;
import lt.gama.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 12/12/2018.
 */
public class EmployeeChargeTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected SalaryService salaryService;


    private final long salaryId;
    private final SalaryType salaryType;
    private final boolean fresh;


    public EmployeeChargeTask(long companyId, long salaryId, SalaryType salaryType, boolean fresh) {
        super(companyId);
        this.salaryId = salaryId;
        this.salaryType = salaryType;
        this.fresh = fresh;
    }

    @Override
    public void execute() {
        try {
            finish(salaryService.refreshSalaryTask(salaryId, salaryType, fresh));
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }


    @Override
    public String toString() {
        return "salaryId=" + salaryId +
                " salaryType=" + salaryType +
                " fresh=" + fresh +
                ' ' + super.toString();
    }
}
