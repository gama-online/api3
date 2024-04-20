package lt.gama.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.model.type.auth.CompanySettings;

import java.util.Set;

public class AccountInfoResponse {

	/**
	 * not real id but index
	 */
	@JsonProperty("companyId") private int companyIndex;

	private String companyName;

	private String employeeName;

	private Set<String> permissions;

	private CompanySettings settings;


	@SuppressWarnings("unused")
	protected AccountInfoResponse() {}

	public AccountInfoResponse(int companyIndex, String companyName, CompanySettings settings, String employeeName, Set<String> permissions) {
		this.companyIndex = companyIndex;
		this.companyName = companyName;
        this.settings = settings;
		this.employeeName = employeeName;
		this.permissions = permissions;
	}

	// generated


	public int getCompanyIndex() {
		return companyIndex;
	}

	public void setCompanyIndex(int companyIndex) {
		this.companyIndex = companyIndex;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	public CompanySettings getSettings() {
		return settings;
	}

	public void setSettings(CompanySettings settings) {
		this.settings = settings;
	}

	@Override
	public String toString() {
		return "AccountInfoResponse{" +
				"companyIndex=" + companyIndex +
				", companyName='" + companyName + '\'' +
				", employeeName='" + employeeName + '\'' +
				", permissions=" + permissions +
				", settings=" + settings +
				'}';
	}
}
