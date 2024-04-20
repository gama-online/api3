package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.DebtCorrectionDto;
import lt.gama.model.dto.documents.DebtOpeningBalanceDto;
import lt.gama.model.dto.documents.DebtRateInfluenceDto;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.DebtCoverageDto;
import lt.gama.model.dto.entities.DebtHistoryDto;
import lt.gama.model.dto.entities.DebtNowDto;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepDebtBalance;
import lt.gama.report.RepDebtBalanceInterval;
import lt.gama.report.RepDebtDetail;
import lt.gama.service.DebtService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "debt")
@RequiresPermissions
public interface DebtApi extends Api {

    @PostMapping("/listCounterparty")
    @RequiresPermissions({Permission.COUNTERPARTY_R, Permission.COUNTERPARTY_M, Permission.GL})
    APIResult<PageResponse<CounterpartyDto, Void>> listCounterparty(PageRequest request) throws GamaApiException;

    @PostMapping("/saveCounterparty")
    @RequiresPermissions({Permission.COUNTERPARTY_M, Permission.GL})
    APIResult<CounterpartyDto> saveCounterparty(CounterpartyDto request) throws GamaApiException;

    @PostMapping("/getCounterparty")
    @RequiresPermissions({Permission.COUNTERPARTY_R, Permission.COUNTERPARTY_M, Permission.GL})
    APIResult<CounterpartyDto> getCounterparty(IdRequest request) throws GamaApiException;

    /**
     * Check if counterparty with the same code or name exists in db already
     * @param request counterparty to check
     * @return 0 or counterparty id
     */
    @PostMapping("/checkCounterparty")
    @RequiresPermissions({Permission.COUNTERPARTY_R, Permission.COUNTERPARTY_M, Permission.GL})
    APIResult<List<CounterpartyDto>> checkCounterparty(CounterpartyDto request) throws GamaApiException;

    @PostMapping("/deleteCounterparty")
    @RequiresPermissions({Permission.COUNTERPARTY_M, Permission.GL})
    APIResult<Void> deleteCounterparty(IdRequest request) throws GamaApiException;

    @PostMapping("/undeleteCounterparty")
    @RequiresPermissions({Permission.COUNTERPARTY_M, Permission.GL})
    APIResult<CounterpartyDto> undeleteCounterparty(IdRequest request) throws GamaApiException;

    /*
     *  Debt opening balance
     */

    @PostMapping("/listOpeningBalance")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<PageResponse<DebtOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOpeningBalance")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtOpeningBalanceDto> saveOpeningBalance(DebtOpeningBalanceDto request) throws GamaApiException;

    @PostMapping("/getOpeningBalance")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOpeningBalanceTask")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<String> finishOpeningBalanceTask(IdRequest request) throws GamaApiException;

    @PostMapping("/importOpeningBalance")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException;

    @PostMapping("/deleteOpeningBalance")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException;

    /*
     * Debt Correction
     */

    @PostMapping("/listDebtCorrection")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<PageResponse<DebtCorrectionDto, Void>> listDebtCorrection(PageRequest request) throws GamaApiException;

    @PostMapping("/saveDebtCorrection")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtCorrectionDto> saveDebtCorrection(DebtCorrectionDto request) throws GamaApiException;

    @PostMapping("/getDebtCorrection")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtCorrectionDto> getDebtCorrection(IdRequest request) throws GamaApiException;

    @PostMapping("/finishDebtCorrection")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtCorrectionDto> finishDebtCorrection(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteDebtCorrection")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<Void> deleteDebtCorrection(IdRequest request) throws GamaApiException;

    @PostMapping("/recallDebtCorrection")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtCorrectionDto> recallDebtCorrection(IdRequest request) throws GamaApiException;

    /*
     *  Debt $$$ Rate Influence
     */

    @PostMapping("/listRateInfluence")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<PageResponse<DebtRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException;

    @PostMapping("/saveRateInfluence")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtRateInfluenceDto> saveRateInfluence(DebtRateInfluenceDto request) throws GamaApiException;

    @PostMapping("/getRateInfluence")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/finishRateInfluence")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteRateInfluence")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/recallRateInfluence")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/genRateInfluence")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<List<DebtBalanceDto>> genRateInfluence(DateRequest request) throws GamaApiException;

    /*
     * Debt Coverage
     */

    @PostMapping("/listDebtCoverage")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<PageResponse<DebtCoverageDto, Void>> listDebtCoverage(PageRequest request) throws GamaApiException;

    @PostMapping("/saveDebtCoverage")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtCoverageDto> saveDebtCoverage(DebtCoverageDto request) throws GamaApiException;

    @PostMapping("/getDebtCoverage")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<DebtCoverageDto> getDebtCoverage(GetDebtCoverageRequest request) throws GamaApiException;

    /*
     * Reports
     */

    @PostMapping("/reportBalance")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<PageResponse<RepDebtBalance, DebtService.RepDebtBalanceAttachment>> reportBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/reportDetail")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<PageResponse<DebtHistoryDto, RepDebtDetail>> reportDetail(PageRequest request) throws GamaApiException;

    @PostMapping("/debtHistory")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<PageResponse<DebtHistoryDto, CounterpartyDto>> debtHistory(PageRequest request) throws GamaApiException;

    @PostMapping("/debtNow")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<List<DebtNowDto>> debtNow(DebtNowRequest request) throws GamaApiException;

    @PostMapping("/reportBalanceInterval")
    @RequiresPermissions({Permission.COUNTERPARTY_B, Permission.GL})
    APIResult<RepDebtBalanceInterval> reportBalanceInterval(ReportDebtBalanceIntervalRequest request) throws GamaApiException;
}
