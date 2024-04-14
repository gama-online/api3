package lt.gama.model.type.auth;

import java.io.Serial;
import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-09-13.
 */
public class CompanyInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private long companyId;

    private String companyName;

    private long employeeId;

    private String employeeName;


    protected CompanyInfo() {
    }

    public CompanyInfo(AccountInfo accountInfo) {
        this.companyId = accountInfo.getCompanyId();
        this.companyName = accountInfo.getCompanyName();
        this.employeeId = accountInfo.getEmployeeId();
        this.employeeName = accountInfo.getEmployeeName();
    }

    // generated

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    @Override
    public String toString() {
        return "CompanyInfo{" +
                "companyId=" + companyId +
                ", companyName='" + companyName + '\'' +
                ", employeeId=" + employeeId +
                ", employeeName='" + employeeName + '\'' +
                '}';
    }
}
