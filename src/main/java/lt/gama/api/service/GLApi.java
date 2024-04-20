package lt.gama.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.documents.GLOpeningBalanceDto;
import lt.gama.model.dto.entities.GLAccountDto;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.dto.entities.GLSaftAccountDto;
import lt.gama.model.dto.entities.ResponsibilityCenterDto;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepTrialBalance;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "gl")
@RequiresPermissions({Permission.GL})
public interface GLApi extends Api {

    @PostMapping("/listRC")
    APIResult<PageResponse<ResponsibilityCenterDto, Void>> listRC(PageRequest request) throws GamaApiException;

    @PostMapping("/getRC")
    APIResult<ResponsibilityCenterDto> getRC(IdRequest request) throws GamaApiException;

    @PostMapping("/saveRC")
    APIResult<ResponsibilityCenterDto> saveRC(ResponsibilityCenterDto request) throws GamaApiException;

    @PostMapping("/deleteRC")
    APIResult<Map<String, Integer>> deleteRC(IdRequest request) throws GamaApiException;

    /**
     * Hide RCs
     * @param request list of id's to hide
     * @return the first RC from the list of id's
     */
    @PostMapping("/hideRC")
    APIResult<Map<String, Integer>> hideRC(IdRequest request) throws GamaApiException;

    /**
     * Make hidden RCs visible again
     * @param request list of id's to show
     * @return the first RC from the list of id's
     */
    @PostMapping("/showRC")
    APIResult<Map<String, Integer>> showRC(IdRequest request) throws GamaApiException;

    @PostMapping("/filterRC")
    APIResult<List<ResponsibilityCenterDto>> filterRC(FilterRCRequest request) throws GamaApiException;

    @PostMapping("/countRC")
    APIResult<Integer> countRC() throws GamaApiException;


    @PostMapping("/listAccount")
    APIResult<PageResponse<GLAccountDto, Void>> listAccount(PageRequest request) throws GamaApiException;

    @PostMapping("/getAccount")
    APIResult<GLAccountDto> getAccount(IdRequest request) throws GamaApiException;

    @PostMapping("/saveAccount")
    APIResult<GLAccountDto> saveAccount(GLAccountRequest request) throws GamaApiException;

    @PostMapping("/deleteAccount")
    APIResult<Void> deleteAccount(GLAccountRequest request) throws GamaApiException;

    @PostMapping("/templateAccount")
    APIResult<Void> templateAccount() throws GamaApiException;

    @PostMapping("/listSaftAccount")
    APIResult<PageResponse<GLSaftAccountDto, Void>> listSaftAccount(PageRequest request) throws GamaApiException;

    @PostMapping("/assignSaft")
    APIResult<Void> assignSaft() throws GamaApiException;


    @PostMapping("/listDoubleEntry")
    APIResult<PageResponse<DoubleEntryDto, Void>> listDoubleEntry(PageRequest request) throws GamaApiException;

    @PostMapping("/saveDoubleEntry")
    APIResult<DoubleEntryDto> saveDoubleEntry(DoubleEntryDto request) throws GamaApiException;

    @PostMapping("/getDoubleEntry")
    APIResult<DoubleEntryDto> getDoubleEntry(IdRequest request) throws GamaApiException;

    @PostMapping("/finishDoubleEntry")
    APIResult<DoubleEntryDto> finishDoubleEntry(IdRequest request) throws GamaApiException;

    @PostMapping("/recallDoubleEntry")
    APIResult<DoubleEntryDto> recallDoubleEntry(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteDoubleEntry")
    APIResult<Void> deleteDoubleEntry(IdRequest request) throws GamaApiException;

    @PostMapping("/closeProfitLoss")
    APIResult<List<GLOperationDto>> closeProfitLoss(DateRequest request) throws GamaApiException;


    @PostMapping("/listOpeningBalance")
    APIResult<PageResponse<GLOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOpeningBalance")
    APIResult<GLOpeningBalanceDto> saveOpeningBalance(GLOpeningBalanceDto request) throws GamaApiException;

    @PostMapping("/getOpeningBalance")
    APIResult<GLOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOpeningBalance")
    APIResult<GLOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/countIntermediateBalance")
    APIResult<GLOpeningBalanceDto> countIntermediateBalance(IntermediateBalanceRequest request) throws GamaApiException;

    @PostMapping("/recallOpeningBalance")
    APIResult<GLOpeningBalanceDto> recallOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/importOpeningBalance")
    APIResult<GLOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException;

    @PostMapping("/deleteOpeningBalance")
    APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException;


    @PostMapping("/reportTrialBalance")
    APIResult<List<RepTrialBalance>> reportTrialBalance(GLReportBalanceRequest request) throws GamaApiException;

    @PostMapping("/reportFlow")
    APIResult<PageResponse<DoubleEntryDto, RepTrialBalance>> reportFlow(PageRequest request) throws GamaApiException;

    @PostMapping("/getProfitLossJson")
    APIResult<JsonNode> getProfitLossJson(ProfitLossJsonRequest request) throws GamaApiException;
}
