package lt.gama.model.dto.documents.items;

import lt.gama.model.dto.base.BaseMoneyBalanceDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.type.GamaMoney;

public class EmployeeBalanceDto extends BaseMoneyBalanceDto implements ISortOrder {

    private Double sortOrder;

    private EmployeeDto employee;


    public EmployeeBalanceDto() {
    }

    public EmployeeBalanceDto(EmployeeDto employee, GamaMoney amount) {
        this.employee = employee;
        this.setAmount(amount);
    }

    public EmployeeBalanceDto(EmployeeDto employee, GamaMoney amount, GamaMoney baseAmount, GamaMoney baseFixAmount) {
        super(amount, baseAmount, baseFixAmount);
        this.employee = employee;
    }

    @Override
    public Long getAccountId() {
        return employee != null ? employee.getId() : null;
    }

    @Override
    public String getAccountName() {
        return employee != null ? employee.getName() : null;
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

    @Override
    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "EmployeeBalanceDto{" +
                "sortOrder=" + sortOrder +
                ", employee=" + employee +
                "} " + super.toString();
    }
}
