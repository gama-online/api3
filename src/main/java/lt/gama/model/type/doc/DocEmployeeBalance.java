package lt.gama.model.type.doc;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseMoneyBalance;
import lt.gama.model.type.enums.DBType;

/**
 * Gama
 * Created by valdas on 15-05-11.
 */
public class DocEmployeeBalance extends BaseMoneyBalance {

    private DocEmployee employee;


    public DocEmployeeBalance() {
    }

    public DocEmployeeBalance(DocEmployee employee, GamaMoney sum) {
        this.employee = employee;
        this.setSum(sum);
    }

    public DocEmployeeBalance(DocEmployee employee, GamaMoney sum, GamaMoney baseSum, GamaMoney baseFixSum) {
        super(sum, baseSum, baseFixSum);
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

    @Override
    public DBType getAccountDb() {
        return employee != null ? employee.getDb() : null;
    }

    // generated

    @Override
    public DocEmployee getEmployee() {
        return employee;
    }

    public void setEmployee(DocEmployee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "DocEmployeeBalance{" +
                "employee=" + employee +
                "} " + super.toString();
    }
}
