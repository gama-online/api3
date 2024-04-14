package lt.gama.model.dto.entities;

import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.salary.EmployeeCardInfo;
import lt.gama.model.type.salary.VacationBalance;

import java.util.List;

public class EmployeeVacationDto extends BaseCompanyDto {

    private DocEmployee employee;

    private EmployeeCardInfo employeeCard;

    /**
     * List of employee vacations with balances
     */
    private List<VacationBalance> vacations;

    // generated

    public DocEmployee getEmployee() {
        return employee;
    }

    public void setEmployee(DocEmployee employee) {
        this.employee = employee;
    }

    public EmployeeCardInfo getEmployeeCard() {
        return employeeCard;
    }

    public void setEmployeeCard(EmployeeCardInfo employeeCard) {
        this.employeeCard = employeeCard;
    }

    public List<VacationBalance> getVacations() {
        return vacations;
    }

    public void setVacations(List<VacationBalance> vacations) {
        this.vacations = vacations;
    }

    @Override
    public String toString() {
        return "EmployeeVacationDto{" +
                "employee=" + employee +
                ", employeeCard=" + employeeCard +
                ", vacations=" + vacations +
                "} " + super.toString();
    }
}
