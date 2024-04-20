package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.impexp.entity.ISO20022Statements;
import lt.gama.model.dto.documents.BankOpeningBalanceDto;
import lt.gama.model.dto.documents.BankOperationDto;
import lt.gama.model.dto.documents.BankRateInfluenceDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyBalanceInterval;
import lt.gama.report.RepMoneyDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "bank")
@RequiresPermissions
public interface BankApi extends Api {

    /*
     *  Bank accounts
     */

    @PostMapping("/listBankAccount")
    @RequiresPermissions({Permission.BANK_R, Permission.BANK_M, Permission.GL})
    APIResult<PageResponse<BankAccountDto, Void>> listBankAccount(PageRequest request) throws GamaApiException;

    @PostMapping("/saveBankAccount")
    @RequiresPermissions({Permission.BANK_M, Permission.GL})
    APIResult<BankAccountDto> saveBankAccount(BankAccountDto request) throws GamaApiException;

    @PostMapping("/getBankAccount")
    @RequiresPermissions({Permission.BANK_R, Permission.BANK_M, Permission.GL})
    APIResult<BankAccountDto> getBankAccount(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteBankAccount")
    @RequiresPermissions({Permission.BANK_R, Permission.BANK_M, Permission.GL})
    APIResult<Void> deleteBankAccount(IdRequest request) throws GamaApiException;

    @PostMapping("/undeleteBankAccount")
    @RequiresPermissions({Permission.BANK_R, Permission.BANK_M, Permission.GL})
    APIResult<BankAccountDto> undeleteBankAccount(IdRequest request) throws GamaApiException;

    /*
     *  Bank accounts opening balance
     */

    @PostMapping("/listOpeningBalance")
    @RequiresPermissions({Permission.BANK_OP_R, Permission.BANK_OP_M, Permission.GL})
    APIResult<PageResponse<BankOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOpeningBalance")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankOpeningBalanceDto> saveOpeningBalance(BankOpeningBalanceDto request) throws GamaApiException;

    @PostMapping("/getOpeningBalance")
    @RequiresPermissions({Permission.BANK_OP_R, Permission.BANK_OP_M, Permission.GL})
    APIResult<BankOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOpeningBalance")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/importOpeningBalance")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException;

    @PostMapping("/deleteOpeningBalance")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException;

    /*
     *  Bank operation
     */

    @PostMapping("/listOperation")
    @RequiresPermissions({Permission.BANK_OP_R, Permission.BANK_OP_M, Permission.GL})
    APIResult<PageResponse<BankOperationDto, Void>> listOperation(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOperation")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankOperationDto> saveOperation(BankOperationDto request) throws GamaApiException;

    @PostMapping("/getOperation")
    APIResult<BankOperationDto> getOperation(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOperation")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankOperationDto> finishOperation(FinishRequest request) throws GamaApiException;

    @PostMapping("/parseOperations")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<ISO20022Statements> parseOperations(ParseOperationRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class ParseOperationRequest {
        public String fileName;
        public BankFileType type;
        public ParseOperationRequest() {}
        public ParseOperationRequest(String fileName, BankFileType type) {
            this.fileName = fileName;
            this.type = type;
        }
    }

    enum BankFileType {
        ISO20022("0"),
        PAYPAL_CSV_TAB("1"),
        PAYPAL_CSV_COMMA("2"),
        REVOLUT("3"),
        SALARY_TAB("4");

        BankFileType(String value) {
            this.value = value;
        }

        private final String value;

        public static BankFileType from(String value) {
            if (value != null) {
                for (BankFileType t : values()) {
                    if (t.value.equals(value)) {
                        return t;
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @PostMapping("/importOperations")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<ISO20022Statements> importOperations(ISO20022Statements request) throws GamaApiException;

    @PostMapping("/deleteOperation")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<Void> deleteOperation(IdRequest request) throws GamaApiException;

    @PostMapping("/recallOperation")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankOperationDto> recallOperation(IdRequest request) throws GamaApiException;

    /*
     *  Bank $$$ Rate Influence
     */

    @PostMapping("/listRateInfluence")
    @RequiresPermissions({Permission.BANK_OP_R, Permission.BANK_OP_M, Permission.GL})
    APIResult<PageResponse<BankRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException;

    @PostMapping("/saveRateInfluence")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankRateInfluenceDto> saveRateInfluence(BankRateInfluenceDto request) throws GamaApiException;

    @PostMapping("/getRateInfluence")
    @RequiresPermissions({Permission.BANK_OP_R, Permission.BANK_OP_M, Permission.GL})
    APIResult<BankRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/finishRateInfluence")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteRateInfluence")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/recallRateInfluence")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<BankRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException;

    @PostMapping("/genRateInfluence")
    @RequiresPermissions({Permission.BANK_OP_M, Permission.GL})
    APIResult<List<RepMoneyBalance<BankAccountDto>>> genRateInfluence(DateRequest request) throws GamaApiException;

    /*
     * Reports
     */

    @PostMapping("/reportBalance")
    @RequiresPermissions({Permission.BANK_B, Permission.GL})
    APIResult<List<RepMoneyBalance<BankAccountDto>>> reportBalance(ReportBalanceRequest request) throws GamaApiException;

    @PostMapping("/reportFlow")
    @RequiresPermissions({Permission.BANK_B, Permission.GL})
    APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<BankAccountDto>>> reportFlow(PageRequest request) throws GamaApiException;

    @PostMapping("/reportBalanceInterval")
    @RequiresPermissions({Permission.BANK_B, Permission.GL})
    APIResult<RepMoneyBalanceInterval> reportBalanceInterval(ReportBalanceIntervalRequest request) throws GamaApiException;

}
