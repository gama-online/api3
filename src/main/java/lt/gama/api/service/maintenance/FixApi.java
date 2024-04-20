package lt.gama.api.service.maintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.LoginRequest;
import lt.gama.api.response.LoginResponse;
import lt.gama.auth.annotation.MaintenancePermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.PermitAll;
import java.time.LocalDate;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

/**
 * gama-online
 * Created by valdas on 2016-01-29.
 * Administrator's Data Fixing Methods
 */
@Tag(name = "admin")
@RequestMapping(MAINTENANCE_PATH + "admin")
@MaintenancePermissions
public interface FixApi extends ApiMaintenance {

    /**
     *
     * @param loginRequest username, password and companyId
     * @return login token
     */
    @PostMapping("login")
    @PermitAll
    APIResult<LoginResponse> login(LoginRequest loginRequest) throws GamaApiException;

    /**
     * Do database initialization, i.e. create test companies, personnel and
     * account
     */
    @PostMapping("start")
    APIResult<String> start() throws GamaApiException;

    /**
     * Set new password
     * @param loginRequest login request
     * @return OK
     */
    @PostMapping("setPassword")
    APIResult<String> setPassword(LoginRequest loginRequest) throws GamaApiException;

    /**
     * Recall all company documents.
     */
    @PostMapping("/recallDocuments")
    APIResult<Void> recallDocuments(RecallDocumentsRequest request) throws GamaApiException;

    class RecallDocumentsRequest {
        @JsonProperty("company") public long companyId;
    }

    /**
     * Finish all company documents.
     * @param request request
     * @return none
     */
    @PostMapping("/finishDocuments")
    APIResult<Void> finishDocuments(FinishDocumentsRequest request) throws GamaApiException;

    class FinishDocumentsRequest {
        @JsonProperty("company") public long companyId;
        public LocalDate dateFrom;
        public LocalDate dateTo;
    }

    /**
     * Recalculate company activeAccounts, payerAccounts and fill company otherAccounts with correct data
     */
    @PostMapping("/fixCompanyAccounts")
    APIResult<Void> fixCompanyAccounts(FixCompanyAccountsRequest request) throws GamaApiException;

    class FixCompanyAccountsRequest {
        @JsonProperty("company") public long companyId;
    }

    /**
     * Refresh accounts info with new company data
     * @return none
     */
    @PostMapping("/refreshAccounts")
    APIResult<String> refreshAccounts() throws GamaApiException;

    @PostMapping("/calcCompaniesMonthlyPayment")
    APIResult<String> calcCompaniesMonthlyPayment() throws GamaApiException;

    @PostMapping("/changeAccountEmail")
    APIResult<String> changeAccountEmail(ChangeAccountEmailRequest request) throws GamaApiException;

    class ChangeAccountEmailRequest {
        public String oldMail;
        public String newMail;
    }

    @PostMapping("/refreshCompanyConnections")
    APIResult<String> refreshCompanyConnections(RefreshCompanyConnectionsRequest request) throws GamaApiException;

    class RefreshCompanyConnectionsRequest {
        @JsonProperty("company") public long companyId;
        public LocalDate date;
    }

    @PostMapping("/companyDeleteDocuments")
    APIResult<Void> companyDeleteDocuments(CompanyDeleteDocumentsRequest request) throws GamaApiException;

    class CompanyDeleteDocumentsRequest {
        @JsonProperty("company") public long companyId;
    }

    @PostMapping("/deleteCompany")
    APIResult<Void> deleteCompany(DeleteCompanyRequest request) throws GamaApiException;

    class DeleteCompanyRequest {
        @JsonProperty("company") public long companyId;
    }

    @PostMapping("/reloadCompanySettings")
    APIResult<Void> reloadCompanySettings(ReloadCompanySettingsRequest request) throws GamaApiException;

    class ReloadCompanySettingsRequest {
        @JsonProperty("company") public long companyId;
    }

    @PostMapping("/regenerateCounterpartyDebtFromHistory")
    APIResult<String> regenerateCounterpartyDebtFromHistory(RegenerateCounterpartyDebtFromHistoryRequest request) throws GamaApiException;

    class RegenerateCounterpartyDebtFromHistoryRequest {
        @JsonProperty("counterparty") public long counterpartyId;
    }
}
