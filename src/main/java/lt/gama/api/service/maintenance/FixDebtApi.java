package lt.gama.api.service.maintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.auth.annotation.MaintenancePermissions;
import lt.gama.model.dto.entities.DebtNowDto;
import lt.gama.model.type.enums.DebtType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

@RequestMapping(MAINTENANCE_PATH + "debt")
@Tag(name = "inventory")
@MaintenancePermissions
public interface FixDebtApi extends ApiMaintenance {

    @PostMapping("/createDebtNowFromDoc")
    APIResult<DebtNowDto> createDebtNowFromDoc(CreateDebtNowFromDocRequest request) throws GamaApiException;

    class CreateDebtNowFromDocRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("doc") public long docId;
    }

    /**
     * Rebuild DebtCoverage records, but do not change DebtHistory and DebtNow.
     * So need to check these after refresh.
     */
    @PostMapping("/rebuildDebtCoveragesForCounterparty")
    APIResult<Map<String, Integer>> rebuildDebtCoveragesForCounterparty(RebuildDebtCoveragesForCounterpartyRequest request) throws GamaApiException;

    class RebuildDebtCoveragesForCounterpartyRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("counterparty") public long counterpartyId;
        @JsonProperty("type") public DebtType debtType;

    }
}
