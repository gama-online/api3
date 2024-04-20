package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.EmployeeOpeningBalanceDto;
import lt.gama.model.dto.documents.EmployeeOperationDto;
import lt.gama.model.dto.documents.EmployeeRateInfluenceDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.dto.entities.RoleDto;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyBalanceInterval;
import lt.gama.report.RepMoneyDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.PermitAll;
import java.util.List;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "employee")
@RequiresPermissions
public interface EmployeeApi extends Api {

    @PostMapping("/listEmployee")
    @RequiresPermissions({Permission.EMPLOYEE_R, Permission.EMPLOYEE_M, Permission.GL})
    APIResult<PageResponse<EmployeeDto, Void>> listEmployee(PageRequest request) throws GamaApiException;

    @PostMapping("/saveEmployee")
    @RequiresPermissions({Permission.EMPLOYEE_M, Permission.GL})
    APIResult<EmployeeDto> saveEmployee(EmployeeDto request) throws GamaApiException;

    @PostMapping("/getEmployee")
    @RequiresPermissions({Permission.EMPLOYEE_R, Permission.EMPLOYEE_M, Permission.GL})
    APIResult<EmployeeDto> getEmployee(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteEmployee")
    @RequiresPermissions({Permission.EMPLOYEE_M, Permission.GL})
    APIResult<Void> deleteEmployee(IdRequest request) throws GamaApiException;

    @PostMapping("/undeleteEmployee")
    @RequiresPermissions({Permission.EMPLOYEE_M, Permission.GL, Permission.GL})
    APIResult<EmployeeDto> undeleteEmployee(IdRequest request) throws GamaApiException;

    /*
     * Roles
     */

    @PostMapping("/listRole")
    @RequiresPermissions({Permission.ROLE})
    APIResult<PageResponse<RoleDto, Void>> listRole(PageRequest request) throws GamaApiException;

    @PostMapping("/saveRole")
    @RequiresPermissions({Permission.ROLE})
    APIResult<RoleDto> saveRole(RoleDto request) throws GamaApiException;

    @PostMapping("/getRole")
    @RequiresPermissions({Permission.ROLE})
    APIResult<RoleDto> getRole(IdRequest request) throws GamaApiException;

    /*
     * Accounts
     */

    @PostMapping("/getAccount")
    @RequiresPermissions({Permission.ACCOUNT})
    APIResult<String> getAccount(IdRequest request) throws GamaApiException;

    @PostMapping("/activateAccount")
    @RequiresPermissions({Permission.ACCOUNT})
    APIResult<String> activateAccount(ActivateAccountRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class ActivateAccountRequest {
        public long id;
        public String email;
        public ActivateAccountRequest() {}
        public ActivateAccountRequest(long id, String email) {
            this.id = id;
            this.email = email;
        }
    }

    @PostMapping("/suspendAccount")
    @RequiresPermissions({Permission.ACCOUNT})
    APIResult<Void> suspendAccount(IdRequest request) throws GamaApiException;

    @PostMapping("/resetPassword")
    @PermitAll
    APIResult<Void> resetPassword(IdRequest request) throws GamaApiException;

    /*
     * Advances Opening Balance
     */

    @PostMapping("/listOpeningBalance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<PageResponse<EmployeeOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOpeningBalance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOpeningBalanceDto> saveOpeningBalance(EmployeeOpeningBalanceDto request) throws GamaApiException;

    @PostMapping("/getOpeningBalance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOpeningBalance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/importOpeningBalance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException;

    @PostMapping("/deleteOpeningBalance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException;

    /*
     * Advances
     */

    @PostMapping("/listAdvance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<PageResponse<EmployeeOperationDto, Void>> listAdvance(PageRequest request) throws GamaApiException;

    @PostMapping("/saveAdvance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOperationDto> saveAdvance(EmployeeOperationDto request) throws GamaApiException;

    @PostMapping("/getAdvance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOperationDto> getAdvance(IdRequest request) throws GamaApiException;

    @PostMapping("/finishAdvance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOperationDto> finishAdvance(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteAdvance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<Void> deleteAdvance(IdRequest request) throws GamaApiException;

    @PostMapping("/recallAdvance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeOperationDto> recallAdvance(IdRequest request) throws GamaApiException;

    /*
     *  Advances $$$ Rate Influence
     */

    @PostMapping("/listRateInfluence")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<PageResponse<EmployeeRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException;

    @PostMapping("/saveRateInfluence")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeRateInfluenceDto> saveRateInfluence(EmployeeRateInfluenceDto request) throws GamaApiException;

    @PostMapping("/getRateInfluence")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/finishRateInfluence")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteRateInfluence")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/recallRateInfluence")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<EmployeeRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/genRateInfluence")
    @RequiresPermissions({Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<List<RepMoneyBalance<EmployeeDto>>> genRateInfluence(DateRequest request) throws GamaApiException;

    /*
     * Reports
     */

    @PostMapping("/reportBalance")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<List<RepMoneyBalance<EmployeeDto>>> reportBalance(ReportBalanceRequest request) throws GamaApiException;

    @PostMapping("/reportFlow")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<EmployeeDto>>> reportFlow(PageRequest request) throws GamaApiException;

    @PostMapping("/reportBalanceInterval")
    @RequiresPermissions({Permission.EMPLOYEE_OP_R, Permission.EMPLOYEE_OP_M, Permission.GL})
    APIResult<RepMoneyBalanceInterval> reportBalanceInterval(ReportBalanceIntervalRequest request) throws GamaApiException;
}
