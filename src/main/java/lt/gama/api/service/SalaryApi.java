package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.EmployeeVacationResponse;
import lt.gama.api.response.PageResponse;
import lt.gama.api.response.SalaryEmployeeChargeResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.SalaryDto;
import lt.gama.model.dto.entities.*;
import lt.gama.model.type.enums.Permission;
import lt.gama.model.type.salary.WorkHoursPosition;
import lt.gama.model.type.salary.WorkTimeCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * gama-online
 * Created by valdas on 2016-02-09.
 */
@RequestMapping(APP_API_3_PATH + "salary")
@RequiresPermissions
public interface SalaryApi extends Api {

    /*
     *  Charge
     */

    @PostMapping("/listCharge")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<ChargeDto, Void>> listCharge(PageRequest request) throws GamaApiException;

    @PostMapping("/saveCharge")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<ChargeDto> saveCharge(ChargeDto request) throws GamaApiException;

    @PostMapping("/getCharge")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<ChargeDto> getCharge(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteCharge")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deleteCharge(IdRequest request) throws GamaApiException;

    /*
     *  WorkSchedule
     */

    @PostMapping("/listWorkSchedule")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<WorkScheduleDto, Void>> listWorkSchedule(PageRequest request) throws GamaApiException;

    @PostMapping("/saveWorkSchedule")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<WorkScheduleDto> saveWorkSchedule(WorkScheduleDto request) throws GamaApiException;

    @PostMapping("/getWorkSchedule")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<WorkScheduleDto> getWorkSchedule(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteWorkSchedule")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deleteWorkSchedule(IdRequest request) throws GamaApiException;

    /*
     * Work hours
     */

    @PostMapping("/listWorkHours")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<WorkHoursDto, Void>> listWorkHours(PageRequest request) throws GamaApiException;

    @PostMapping("/saveWorkHours")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<WorkHoursDto> saveWorkHours(WorkHoursDto request) throws GamaApiException;

    @PostMapping("/getWorkHours")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<WorkHoursDto> getWorkHours(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteWorkHours")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deleteWorkHours(WorkHoursRequest request) throws GamaApiException;

    @PostMapping("/generateWorkHours")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<WorkHoursDto> generateWorkHours(GenerateWorkHoursRequest request) throws GamaApiException;

    @PostMapping("/generateWorkHoursPosition")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<WorkHoursPosition> generateWorkHoursPosition(GenerateWorkHoursPositionRequest request) throws GamaApiException;

    @PostMapping("/refreshWorkHoursTask")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<String> refreshWorkHoursTask(RefreshWorkHoursRequest request) throws GamaApiException;

    /*
     *  Work Time Codes
     */

    @PostMapping("/listWorkTimeCodes")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<List<WorkTimeCode>> listWorkTimeCodes() throws GamaApiException;

    /*
     *  Position
     */

    @PostMapping("/listPosition")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<PositionDto, Void>> listPosition(PageRequest request) throws GamaApiException;

    @PostMapping("/savePosition")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<PositionDto> savePosition(PositionDto request) throws GamaApiException;

    @PostMapping("/getPosition")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PositionDto> getPosition(IdRequest request) throws GamaApiException;

    @PostMapping("/deletePosition")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deletePosition(IdRequest request) throws GamaApiException;

    /*
     *  Employee Card
     */

    @PostMapping("/saveEmployeeCard")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeCardDto> saveEmployeeCard(EmployeeCardDto request) throws GamaApiException;

    @PostMapping("/getEmployeeCard")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeCardDto> getEmployeeCard(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteEmployeeCard")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deleteEmployeeCard(IdRequest request) throws GamaApiException;

    /*
     *  Employee Salary History
     */

    @PostMapping("/saveEmployeeCardSalaryHistory")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeCardDto> saveEmployeeCardSalaryHistory(EmployeeCardDto request) throws GamaApiException;

    /*
     *  Employee Vacation
     */

    @PostMapping("/listEmployeeVacation")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<EmployeeVacationDto, Void>> listEmployeeVacation(PageRequest request) throws GamaApiException;

    @PostMapping("/saveEmployeeVacation")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeVacationDto> saveEmployeeVacation(EmployeeVacationDto request) throws GamaApiException;

    @PostMapping("/getEmployeeVacation")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeVacationDto> getEmployeeVacation(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteEmployeeVacation")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deleteEmployeeVacation(IdRequest request) throws GamaApiException;

    @PostMapping("/calcEmployeeVacation")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeVacationResponse> calcEmployeeVacation(CalcEmployeeVacationRequest request) throws GamaApiException;

    /*
     *  Salary
     */

    @PostMapping("/listSalary")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<SalaryDto, Void>> listSalary(PageRequest request) throws GamaApiException;

    @PostMapping("/saveSalary")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<SalaryDto> saveSalary(SalaryDto request) throws GamaApiException;

    @PostMapping("/getSalary")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<SalaryDto> getSalary(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteSalary")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deleteSalary(IdRequest request) throws GamaApiException;

    @PostMapping("/finishSalary")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<SalaryDto> finishSalary(FinishSalaryRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class FinishSalaryRequest {
        public long id;
        public Boolean finishGL;
        public FinishSalaryRequest() {}
        public FinishSalaryRequest(long id, Boolean finishGL) {
            this.id = id;
            this.finishGL = finishGL;
        }
    }

    @PostMapping("/recallSalary")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<SalaryDto> recallSalary(IdRequest request) throws GamaApiException;

    @PostMapping("/refreshSalary")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<String> refreshSalary(RefreshSalaryRequest request) throws GamaApiException;

    /*
     *  EmployeeCharge
     */

    @PostMapping("/listEmployeeCharge")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<EmployeeChargeDto, Void>> listEmployeeCharge(PageRequest request) throws GamaApiException;

    @PostMapping("/saveEmployeeCharge")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<SalaryEmployeeChargeResponse> saveEmployeeCharge(EmployeeChargeDto request) throws GamaApiException;

    @PostMapping("/getEmployeeCharge")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeChargeDto> getEmployeeCharge(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteEmployeeCharge")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<SalaryEmployeeChargeResponse> deleteEmployeeCharge(IdRequest request) throws GamaApiException;

    @PostMapping("/finishEmployeeCharge")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<SalaryEmployeeChargeResponse> finishEmployeeCharge(IdRequest request) throws GamaApiException;

    @PostMapping("/recallEmployeeCharge")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<SalaryEmployeeChargeResponse> recallEmployeeCharge(IdRequest request) throws GamaApiException;

    @PostMapping("/generateEmployeeCharge")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeChargeDto> generateEmployeeCharge(GenerateEmployeeChargeRequest request) throws GamaApiException;

    /*
     *  EmployeeAbsence
     */

    @PostMapping("/listAbsence")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<PageResponse<EmployeeAbsenceDto, Void>> listAbsence(PageRequest request) throws GamaApiException;

    @PostMapping("/saveAbsence")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeAbsenceDto> saveAbsence(EmployeeAbsenceDto request) throws GamaApiException;

    @PostMapping("/getAbsence")
    @RequiresPermissions({Permission.SALARY_R, Permission.SALARY_M, Permission.GL})
    APIResult<EmployeeAbsenceDto> getAbsence(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteAbsence")
    @RequiresPermissions({Permission.SALARY_M, Permission.GL})
    APIResult<Void> deleteAbsence(IdRequest request) throws GamaApiException;
}
