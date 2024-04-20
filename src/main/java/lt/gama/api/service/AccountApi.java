package lt.gama.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PasswordRequest;
import lt.gama.api.response.CompanyResponse;
import lt.gama.api.response.LoginResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.PermitAll;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "auth")
@RequiresPermissions
public interface AccountApi extends Api {

	/**
	 * Refresh access token
	 * @param request - refresh token and company index
	 * @return login info (LoginResponse) with access token
	 */
	@PostMapping("/refreshAccess")
	@PermitAll
	APIResult<LoginResponse> refreshAccess(RefreshAccessRequest request) throws GamaApiException;

	@SuppressWarnings("unused")
	class RefreshAccessRequest {
		@JsonProperty("token") public String refreshToken;
		@JsonProperty("company") public Integer companyIndex;
		public RefreshAccessRequest() {}
		public RefreshAccessRequest(String refreshToken, Integer companyIndex) {
			this.refreshToken = refreshToken;
			this.companyIndex = companyIndex;
		}
	}

    /**
     * Send email with password refresh info
     * @return none
     */
	@PostMapping("/forgotPassword")
	@PermitAll
	APIResult<Void> forgotPassword(ForgotPasswordRequest request) throws GamaApiException;

	@SuppressWarnings("unused")
	class ForgotPasswordRequest {
		@JsonProperty("name") public String login;
		public ForgotPasswordRequest() {}
		public ForgotPasswordRequest(String login) {
			this.login = login;
		}
	}

	/**
	 * Check if reset password token is valid
	 * @param request - password reset token
	 * @return login info (LoginResponse) with new access token
	 */
	@PostMapping("/resetPassword")
	@PermitAll
	APIResult<LoginResponse> resetPassword(ResetPasswordRequest request) throws GamaApiException;

	@SuppressWarnings("unused")
	class ResetPasswordRequest {
		@JsonProperty("reset") public String resetToken;
		public ResetPasswordRequest() {}
		public ResetPasswordRequest(String resetToken) {
			this.resetToken = resetToken;
		}
	}

	/**
	 * Create a new password and generate new refresh token
	 * @param loginParam - login name (email) and password
	 * @return none or error
	 */
	@PostMapping("/createPassword")
	APIResult<String> createPassword(PasswordRequest loginParam) throws GamaApiException;

	/**
	 * Switch to company by companyIndex
	 * @param request - company index in login token
	 * @return LoginResponse object with new access token
	 */
	@PostMapping("/changeCompany")
	APIResult<LoginResponse> changeCompany(ChangeCompanyRequest request) throws GamaApiException;

	@SuppressWarnings("unused")
	class ChangeCompanyRequest {
		@JsonProperty("company") public int companyIndex;
		public ChangeCompanyRequest() {}
		public ChangeCompanyRequest(int companyIndex) {
			this.companyIndex = companyIndex;
		}
	}

	/**
	 * Set default company by companyIndex
	 * @param request - company index in login token
	 * @return new profile or error
	 */
	@PostMapping("/setDefaultCompany")
	APIResult<LoginResponse> setDefaultCompany(SetDefaultCompanyRequest request) throws GamaApiException;

	@SuppressWarnings("unused")
	class SetDefaultCompanyRequest {
		@JsonProperty("company") public int companyIndex;
		public SetDefaultCompanyRequest() {}
		public SetDefaultCompanyRequest(int companyIndex) {
			this.companyIndex = companyIndex;
		}
	}

	/**
	 * Get company data
	 * @param request - company index in login token
	 * @return company data like logo, settings, locations ....
	 */
	@PostMapping("/getCompany")
	APIResult<CompanyResponse> getCompany(GetCompanyRequest request) throws GamaApiException;

	@SuppressWarnings("unused")
	class GetCompanyRequest {
		@JsonProperty("company") public int companyIndex;
		public GetCompanyRequest() {}
		public GetCompanyRequest(int companyIndex) {
			this.companyIndex = companyIndex;
		}
	}

	/**
	 * Sign params
	 * @return http error if not valid
	 */
	@PostMapping("/signParams")
	APIResult<String> signParams(SignParamsRequest request) throws GamaApiException;

	@SuppressWarnings("unused")
	class SignParamsRequest {
		public String params;
		public SignParamsRequest() {}
		public SignParamsRequest(String params) {
			this.params = params;
		}
	}
}
