package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.salary.VacationBalance;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/**
 * gama-online
 * <p>
 * one-to-one relationship (the same id) with related Employee entity
 */
@Entity
@Table(name = "employee_vacation")
@NamedEntityGraph(name = EmployeeVacationSql.GRAPH_ALL,
        attributeNodes = {
                @NamedAttributeNode(EmployeeVacationSql_.EMPLOYEE),
                @NamedAttributeNode(EmployeeVacationSql_.EMPLOYEE_CARD)
        })
public class EmployeeVacationSql extends BaseCompanySql {

    public static final String GRAPH_ALL = "graph.EmployeeVacationSql.all";

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private EmployeeSql employee;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private EmployeeCardSql employeeCard;

    /**
     * List of employee vacations with balances
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<VacationBalance> vacations;

    /**
     * toString exclude employee, employeeCard
     */
    @Override
    public String toString() {
        return "EmployeeVacationSql{" +
                ", vacations=" + vacations +
                "} " + super.toString();
    }

    // generated

    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
        this.employee = employee;
    }

    public EmployeeCardSql getEmployeeCard() {
        return employeeCard;
    }

    public void setEmployeeCard(EmployeeCardSql employeeCard) {
        this.employeeCard = employeeCard;
    }

    public List<VacationBalance> getVacations() {
        return vacations;
    }

    public void setVacations(List<VacationBalance> vacations) {
        this.vacations = vacations;
    }
}
