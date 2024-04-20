package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.salary.WorkHoursPosition;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2022-04-15.
 * <p>
 * The entity to keep work time codes for one employee for each day of accounting period in each position.
 * Accounting period is a month as usually but can be a week.
 *
 */
@Entity
@Table(name = "work_hours")
@NamedEntityGraph(name = WorkHoursSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(WorkHoursSql_.EMPLOYEE),
        @NamedAttributeNode(WorkHoursSql_.EMPLOYEE_CARD)
})
public class WorkHoursSql extends BaseCompanySql {

    public static final String GRAPH_ALL = "graph.WorkHoursSql.all";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeSql employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private EmployeeCardSql employeeCard;

    /**
     * The starting day of the accounting period (the month)
     */
    private LocalDate date;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<WorkHoursPosition> positions;

    @JdbcTypeCode(SqlTypes.JSON)
    private WorkHoursPosition mainPosition;

    private Boolean fixed;

    private Boolean finished;


    public boolean isFixed() {
        return fixed != null && fixed;
    }

    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        mainPosition = null;
        if (positions != null && positions.size() > 0) {
            for (WorkHoursPosition position : positions) {
                if (position.getPosition() != null && position.getPosition().isMain()) {
                    mainPosition = position;
                    break;
                }
            }
            if (mainPosition == null) mainPosition = positions.get(0);
        }
    }


    /**
     * toString except employee, employeeCard
     */
    @Override
    public String toString() {
        return "WorkHoursSql{" +
                "date=" + date +
                ", positions=" + positions +
                ", mainPosition=" + mainPosition +
                ", fixed=" + fixed +
                ", finished=" + finished +
                "} " + super.toString();
    }

    // generated
    // except getFixed()

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

    public void setFixed(Boolean fixed) {
        this.fixed = fixed;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}
