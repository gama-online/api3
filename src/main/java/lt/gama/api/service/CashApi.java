package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.CashOpeningBalanceDto;
import lt.gama.model.dto.documents.CashOperationDto;
import lt.gama.model.dto.documents.CashRateInfluenceDto;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyBalanceInterval;
import lt.gama.report.RepMoneyDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "cash")
@RequiresPermissions
public interface CashApi extends Api {

    /*
     *  Cash
     */

    @PostMapping("/listCash")
    @RequiresPermissions({Permission.CASH_R, Permission.CASH_M, Permission.GL})
    APIResult<PageResponse<CashDto, Void>> listCash(PageRequest request) throws GamaApiException;

    @PostMapping("/saveCash")
    @RequiresPermissions({Permission.CASH_M, Permission.GL})
    APIResult<CashDto> saveCash(CashDto request) throws GamaApiException;

    @PostMapping("/getCash")
    @RequiresPermissions({Permission.CASH_R, Permission.CASH_M, Permission.GL})
    APIResult<CashDto> getCash(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteCash")
    @RequiresPermissions({Permission.CASH_M, Permission.GL})
    APIResult<Void> deleteCash(IdRequest request) throws GamaApiException;

    @PostMapping("/undeleteCash")
    @RequiresPermissions({Permission.CASH_M, Permission.GL})
    APIResult<CashDto> undeleteCash(IdRequest request) throws GamaApiException;

    /*
     *  Cash order
     */

    @PostMapping("/listCashOrder")
    @RequiresPermissions({Permission.CASH_OP_R, Permission.CASH_OP_M, Permission.GL})
    APIResult<PageResponse<CashOperationDto, Void>> listCashOrder(PageRequest request) throws GamaApiException;

    @PostMapping("/saveCashOrder")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOperationDto> saveCashOrder(CashOperationDto request) throws GamaApiException;

    @PostMapping("/getCashOrder")
    @RequiresPermissions({Permission.CASH_OP_R, Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOperationDto> getCashOrder(IdRequest request) throws GamaApiException;

    @PostMapping("/finishCashOrder")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOperationDto> finishCashOrder(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteCashOrder")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<Void> deleteCashOrder(IdRequest request) throws GamaApiException;

    @PostMapping("/recallCashOrder")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOperationDto> recallCashOrder(IdRequest request) throws GamaApiException;

    /*
     *  Cash opening balance
     */

    @PostMapping("/listOpeningBalance")
    @RequiresPermissions({Permission.CASH_OP_R, Permission.CASH_OP_M, Permission.GL})
    APIResult<PageResponse<CashOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOpeningBalance")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOpeningBalanceDto> saveOpeningBalance(CashOpeningBalanceDto request) throws GamaApiException;

    @PostMapping("/getOpeningBalance")
    @RequiresPermissions({Permission.CASH_OP_R, Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOpeningBalance")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/importOpeningBalance")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException;

    @PostMapping("/deleteOpeningBalance")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException;

    /*
     *  Cash $$$ Rate Influence
     */

    @PostMapping("/listRateInfluence")
    @RequiresPermissions({Permission.CASH_OP_R, Permission.CASH_OP_M, Permission.GL})
    APIResult<PageResponse<CashRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException;

    @PostMapping("/saveRateInfluence")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashRateInfluenceDto> saveRateInfluence(CashRateInfluenceDto request) throws GamaApiException;

    @PostMapping("/getRateInfluence")
    @RequiresPermissions({Permission.CASH_OP_R, Permission.CASH_OP_M, Permission.GL})
    APIResult<CashRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/finishRateInfluence")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteRateInfluence")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/recallRateInfluence")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<CashRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/genRateInfluence")
    @RequiresPermissions({Permission.CASH_OP_M, Permission.GL})
    APIResult<List<RepMoneyBalance<CashDto>>> genRateInfluence(DateRequest request) throws GamaApiException;

    /*
     * Reports
     */

    @PostMapping("/reportBalance")
    @RequiresPermissions({Permission.CASH_B, Permission.GL})
    APIResult<List<RepMoneyBalance<CashDto>>> reportBalance(ReportBalanceRequest request) throws GamaApiException;

    @PostMapping("/reportFlow")
    @RequiresPermissions({Permission.CASH_B, Permission.GL})
    APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<CashDto>>> reportFlow(PageRequest request) throws GamaApiException;

    @PostMapping("/reportBalanceInterval")
    @RequiresPermissions({Permission.CASH_B, Permission.GL})
    APIResult<RepMoneyBalanceInterval> reportBalanceInterval(ReportBalanceIntervalRequest request) throws GamaApiException;
}