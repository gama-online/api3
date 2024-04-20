package lt.gama.api.service.maintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.MaintenancePermissions;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.type.enums.AccountType;
import lt.gama.report.RepMoneyDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

/**
 * gama-online
 * Created by valdas on 2016-03-21.
 */
@RequestMapping(MAINTENANCE_PATH + "money")
@Tag(name = "money")
@MaintenancePermissions
public interface FixMoneyApi extends ApiMaintenance {

    /**
     * Retrieve MoneyHistory records from dateFrom to dateTo
     * @return array of MoneyHistory records
     */
    @PostMapping("/retrieveMoneyHistory")
    APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<Object>>> retrieveMoneyHistory(RetrieveMoneyHistoryRequest request) throws GamaApiException;

    class RetrieveMoneyHistoryRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("type") public AccountType type;
        @JsonProperty("account") public long accountId;
        public String currency;
        public LocalDate dateFrom;
        public LocalDate dateTo;
        @JsonProperty("limit") public int pageSize = 10;
    }

    /**
     * Fix money account (Employee/Bank/Cash), money balance records, i.e. regenerate all money records from start.
     * if currency code not specified then fix all
     * @return none
     */
    @PostMapping("/fixMoneyAccount")
    APIResult<Void> fixMoneyAccount(FixMoneyAccountRequest request) throws GamaApiException;

    class FixMoneyAccountRequest {
        @JsonProperty("type") public AccountType type;
        @JsonProperty("account") public long accountId;
        @JsonProperty("currency") public String currency;
    }
}
