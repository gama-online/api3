package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.EmployeeOpeningBalanceSql;
import lt.gama.model.sql.entities.EmployeeSql;


@Entity
@Table(name = "employee_ob_balances")
public class EmployeeOpeningBalanceEmployeeSql extends BaseMoneyBalanceSql implements ISortOrder {

    private Double sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("employees")
    private EmployeeOpeningBalanceSql parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private EmployeeSql employee;

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
        return "EmployeeOpeningBalanceEmployeeSql{" +
                "sortOrder=" + sortOrder +
                ", parent=" + parent +
                ", employee=" + employee +
                "} " + super.toString();
    }

    // generated

    @Override
    public Double getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    public EmployeeOpeningBalanceSql getParent() {
        return parent;
    }

    public void setParent(EmployeeOpeningBalanceSql parent) {
        this.parent = parent;
    }

    @Override
    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
        this.employee = employee;
    }
}
