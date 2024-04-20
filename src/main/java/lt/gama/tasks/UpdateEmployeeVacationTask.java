package lt.gama.tasks;

import lt.gama.model.dto.entities.WorkHoursDto;
import lt.gama.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2017-10-27.
 */
public class UpdateEmployeeVacationTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected SalaryService salaryService;


    private final long employeeId;
    private final int year;
    private final int month;


    public UpdateEmployeeVacationTask(long companyId, int year, int month, long employeeId) {
        super(companyId);
        this.employeeId = employeeId;
        this.year = year;
        this.month = month;
    }

    @Override
    public void execute() {
        try {
            WorkHoursDto workHours = salaryService.getWorkHours(employeeId, year, month);
            salaryService.updateVacations(workHours);
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }


    @Override
    public String toString() {
        return "employeeId=" + employeeId +
                " year=" + year +
                " month=" + month +
                ' ' + super.toString();
    }
}
