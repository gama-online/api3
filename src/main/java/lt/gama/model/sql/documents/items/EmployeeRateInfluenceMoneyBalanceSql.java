package lt.gama.model.sql.documents.items;

import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.EmployeeRateInfluenceSql;
import lt.gama.model.sql.entities.EmployeeSql;


@Entity
@Table(name = "employee_rate_influences_employee")
public class EmployeeRateInfluenceMoneyBalanceSql extends BaseMoneyBalanceSql {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private EmployeeRateInfluenceSql parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private EmployeeSql employee;

    private Double sortOrder;

    @Override
    public Long getAccountId() {
        return employee != null ? employee.getId() : null;
    }

    @Override
    public String getAccountName() {
        return employee != null ? employee.getName() : null;
    }

    /**
     * toString except parent
     */
    @Override
    public String toString() {
        return "EmployeeRateInfluenceMoneyBalanceSql{" +
                "parent=" + parent +
                ", employee=" + employee +
                ", sortOrder=" + sortOrder +
                "} " + super.toString();
    }

    // generated

    public EmployeeRateInfluenceSql getParent() {
        return parent;
    }

    public void setParent(EmployeeRateInfluenceSql parent) {
        this.parent = parent;
    }

    @Override
    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
        this.employee = employee;
    }

    public Double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }
}
