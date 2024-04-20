package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.salary.WorkTimeCode;

import java.time.LocalDate;

@Entity
@Table(name = "employee_absence")
@NamedEntityGraph(name = EmployeeAbsenceSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(EmployeeAbsenceSql_.EMPLOYEE))
public class EmployeeAbsenceSql extends BaseCompanySql {

    public static final String GRAPH_ALL = "graph.EmployeeAbsenceSql.all";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeSql employee;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    @Embedded
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


    public boolean isWeekends() {
        return weekends != null && weekends;
    }

    public boolean isHolidays() {
        return holidays != null && holidays;
    }

    /**
     * toString except employee
     */
    @Override
    public String toString() {
        return "EmployeeAbsenceSql{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", code=" + code +
                ", document='" + document + '\'' +
                ", weekends=" + weekends +
                ", holidays=" + holidays +
                "} " + super.toString();
    }

    // generated
    // except getHolidays(), getWeekends()

    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
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

    public void setWeekends(Boolean weekends) {
        this.weekends = weekends;
    }

    public void setHolidays(Boolean holidays) {
        this.holidays = holidays;
    }
}
