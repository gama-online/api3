package lt.gama.model.type.auth;

import lt.gama.model.i.IEmployee;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.enums.EmployeeType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public class AccountInfo implements Serializable {

	@Serial
    private static final long serialVersionUID = -1L;

	private String companyName;

	private Long companyId;

	private String code;

	private String vatCode;

	private String employeeName;

	private String employeeOffice;

	private Long employeeId;

	private Set<String> permissions;

	private LocalDateTime lastLogin;

	private Boolean api;

	public AccountInfo() {
	}

	public AccountInfo(CompanySql company, IEmployee employee) {
		this.companyName = company.getBusinessName();
		if (this.companyName == null) this.companyName = company.getName();
		this.companyId = company.getId();
        this.code = company.getCode();
        this.vatCode = company.getVatCode();
        if (employee != null) {
			this.employeeName = employee.getName();
			this.employeeOffice = employee.getOffice();
			this.employeeId = employee.getId();
			this.permissions = employee.getUnionPermissions();
			this.api = employee.getType() == EmployeeType.API;
		}
	}

	// generated

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getVatCode() {
		return vatCode;
	}

	public void setVatCode(String vatCode) {
		this.vatCode = vatCode;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getEmployeeOffice() {
		return employeeOffice;
	}

	public void setEmployeeOffice(String employeeOffice) {
		this.employeeOffice = employeeOffice;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Boolean getApi() {
		return api;
	}

	public void setApi(Boolean api) {
		this.api = api;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountInfo that = (AccountInfo) o;
		return Objects.equals(companyName, that.companyName) && Objects.equals(companyId, that.companyId) && Objects.equals(code, that.code) && Objects.equals(vatCode, that.vatCode) && Objects.equals(employeeName, that.employeeName) && Objects.equals(employeeOffice, that.employeeOffice) && Objects.equals(employeeId, that.employeeId) && Objects.equals(permissions, that.permissions) && Objects.equals(lastLogin, that.lastLogin) && Objects.equals(api, that.api);
	}

	@Override
	public int hashCode() {
		return Objects.hash(companyName, companyId, code, vatCode, employeeName, employeeOffice, employeeId, permissions, lastLogin, api);
	}

	@Override
	public String toString() {
		return "AccountInfo{" +
				"companyName='" + companyName + '\'' +
				", companyId=" + companyId +
				", code='" + code + '\'' +
				", vatCode='" + vatCode + '\'' +
				", employeeName='" + employeeName + '\'' +
				", employeeOffice='" + employeeOffice + '\'' +
				", employeeId=" + employeeId +
				", permissions=" + permissions +
				", lastLogin=" + lastLogin +
				", api=" + api +
				'}';
	}
}
