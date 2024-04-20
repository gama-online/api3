package lt.gama.api.response;

import lt.gama.model.dto.documents.SalaryDto;
import lt.gama.model.dto.entities.EmployeeChargeDto;

/**
 * gama-online
 * Created by valdas on 2016-06-16.
 */
public class SalaryEmployeeChargeResponse {

    private SalaryDto salary;

    private EmployeeChargeDto employeeCharge;

    @SuppressWarnings("unused")
    protected SalaryEmployeeChargeResponse() {}

    public SalaryEmployeeChargeResponse(EmployeeChargeDto employeeCharge) {
        this.employeeCharge = employeeCharge;
    }

    public SalaryEmployeeChargeResponse(SalaryDto salary, EmployeeChargeDto employeeCharge) {
        this.salary = salary;
        this.employeeCharge = employeeCharge;
    }

    // generated

    public SalaryDto getSalary() {
        return salary;
    }

    public void setSalary(SalaryDto salary) {
        this.salary = salary;
    }

    public EmployeeChargeDto getEmployeeCharge() {
        return employeeCharge;
    }

    public void setEmployeeCharge(EmployeeChargeDto employeeCharge) {
        this.employeeCharge = employeeCharge;
    }

    @Override
    public String toString() {
        return "SalaryEmployeeChargeResponse{" +
                "salary=" + salary +
                ", employeeCharge=" + employeeCharge +
                '}';
    }
}
