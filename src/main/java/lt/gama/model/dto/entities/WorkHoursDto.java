package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.salary.WorkHoursPosition;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class WorkHoursDto extends BaseCompanyDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private EmployeeDto employee;

    private EmployeeCardDto employeeCard;

    /**
     * The starting day of accounting period
     */
    private LocalDate date;

    private List<WorkHoursPosition> positions;

    private WorkHoursPosition mainPosition;

    private Boolean fixed;

    private Boolean finished;


    public WorkHoursDto() {
    }

    public WorkHoursDto(LocalDate date, EmployeeDto employee, EmployeeCardDto employeeCard) {
        this.date = date;
        this.employee = employee;
        this.employeeCard = employeeCard;
    }

    // generated

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public EmployeeCardDto getEmployeeCard() {
        return employeeCard;
    }

    public void setEmployeeCard(EmployeeCardDto employeeCard) {
        this.employeeCard = employeeCard;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<WorkHoursPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<WorkHoursPosition> positions) {
        this.positions = positions;
    }

    public WorkHoursPosition getMainPosition() {
        return mainPosition;
    }

    public void setMainPosition(WorkHoursPosition mainPosition) {
        this.mainPosition = mainPosition;
    }

    public Boolean getFixed() {
        return fixed;
    }

    public void setFixed(Boolean fixed) {
        this.fixed = fixed;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "WorkHoursDto{" +
                "employee=" + employee +
                ", employeeCard=" + employeeCard +
                ", date=" + date +
                ", positions=" + positions +
                ", mainPosition=" + mainPosition +
                ", fixed=" + fixed +
                ", finished=" + finished +
                "} " + super.toString();
    }
}
