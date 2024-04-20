package lt.gama.api.response;

import java.util.List;

public class LoginResponse {

	private Boolean impersonated;

	private AccountInfoResponse defaultCompany;

	private int companyIndex;

	private List<CompanyInfoResponse> companies;

	private String refreshToken;

	private String accessToken;

	// generated

	public Boolean getImpersonated() {
		return impersonated;
	}

	public void setImpersonated(Boolean impersonated) {
		this.impersonated = impersonated;
	}

	public AccountInfoResponse getDefaultCompany() {
		return defaultCompany;
	}

	public void setDefaultCompany(AccountInfoResponse defaultCompany) {
		this.defaultCompany = defaultCompany;
	}

	public int getCompanyIndex() {
		return companyIndex;
	}

	public void setCompanyIndex(int companyIndex) {
		this.companyIndex = companyIndex;
	}

	public List<CompanyInfoResponse> getCompanies() {
		return companies;
	}

	public void setCompanies(List<CompanyInfoResponse> companies) {
		this.companies = companies;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public String toString() {
		return "LoginResponse{" +
				"impersonated=" + impersonated +
				", defaultCompany=" + defaultCompany +
				", companyIndex=" + companyIndex +
				", companies=" + companies +
				", refreshToken='" + refreshToken + '\'' +
				", accessToken='" + accessToken + '\'' +
				'}';
	}
}
