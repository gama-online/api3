package lt.gama.model.dto.entities;

import lt.gama.model.type.doc.DocPosition;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IEmployeeCard;
import lt.gama.model.type.enums.SexType;
import lt.gama.model.type.salary.EmployeeTaxSettings;
import lt.gama.model.type.salary.SalaryPerMonth;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

public class EmployeeCardDto extends BaseCompanyDto implements IEmployeeCard {

    @Serial
    private static final long serialVersionUID = -1L;

    private EmployeeDto employee;

    /**
     * Social Security Number
     */
    private String ssn;

    /**
     * National identification number
     */
    private String nin;

    /**
     * List of employee taxes by dates
     */
    private List<EmployeeTaxSettings> taxes;

    private LocalDate hired;

    private String hireNote;

    private LocalDate fired;

    private String fireNote;

    private List<DocPosition> positions;

    private SexType sex;

    /**
     * Salary history, i.e. salary records before start of accounting
     */
    private List<SalaryPerMonth> salaryHistory;

    // generated

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    @Override
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    @Override
    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
    }

    @Override
    public List<EmployeeTaxSettings> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<EmployeeTaxSettings> taxes) {
        this.taxes = taxes;
    }

    @Override
    public LocalDate getHired() {
        return hired;
    }

    public void setHired(LocalDate hired) {
        this.hired = hired;
    }

    public String getHireNote() {
        return hireNote;
    }

    public void setHireNote(String hireNote) {
        this.hireNote = hireNote;
    }

    @Override
    public LocalDate getFired() {
        return fired;
    }

    public void setFired(LocalDate fired) {
        this.fired = fired;
    }

    public String getFireNote() {
        return fireNote;
    }

    public void setFireNote(String fireNote) {
        this.fireNote = fireNote;
    }

    @Override
    public List<DocPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<DocPosition> positions) {
        this.positions = positions;
    }

    @Override
    public SexType getSex() {
        return sex;
    }

    public void setSex(SexType sex) {
        this.sex = sex;
    }

    public List<SalaryPerMonth> getSalaryHistory() {
        return salaryHistory;
    }

    public void setSalaryHistory(List<SalaryPerMonth> salaryHistory) {
        this.salaryHistory = salaryHistory;
    }

    @Override
    public String toString() {
        return "EmployeeCardDto{" +
                "employee=" + employee +
                ", ssn='" + ssn + '\'' +
                ", nin='" + nin + '\'' +
                ", taxes=" + taxes +
                ", hired=" + hired +
                ", hireNote='" + hireNote + '\'' +
                ", fired=" + fired +
                ", fireNote='" + fireNote + '\'' +
                ", positions=" + positions +
                ", sex=" + sex +
                ", salaryHistory=" + salaryHistory +
                "} " + super.toString();
    }
}
