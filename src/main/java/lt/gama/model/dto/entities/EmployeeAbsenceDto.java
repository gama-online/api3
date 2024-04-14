package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.salary.WorkTimeCode;

import java.time.LocalDate;

public class EmployeeAbsenceDto extends BaseCompanyDto {

    private EmployeeDto employee;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private WorkTimeCode code;

    private String document;

    /**
     * If the period applies to weekends
     */
    private Boolean weekends;

    /**
     * If the period applies to state holidays
     */
    private Boolean holidays;

    // generated

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public WorkTimeCode getCode() {
        return code;
    }

    public void setCode(WorkTimeCode code) {
        this.code = code;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public Boolean getWeekends() {
        return weekends;
    }

    public void setWeekends(Boolean weekends) {
        this.weekends = weekends;
    }

    public Boolean getHolidays() {
        return holidays;
    }

    public void setHolidays(Boolean holidays) {
        this.holidays = holidays;
    }

    @Override
    public String toString() {
        return "EmployeeAbsenceDto{" +
                "employee=" + employee +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", code=" + code +
                ", document='" + document + '\'' +
                ", weekends=" + weekends +
                ", holidays=" + holidays +
                "} " + super.toString();
    }
}
