package lt.gama.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.api.APIResult;
import lt.gama.api.IApi;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.CalendarAdminRequest;
import lt.gama.api.request.CreateCompanyRequest;
import lt.gama.api.request.GLAccountRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.LoginResponse;
import lt.gama.api.response.PageResponse;
import lt.gama.api.response.UploadResponse;
import lt.gama.auth.annotation.MaintenancePermissions;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.dto.entities.AccountDto;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.GLAccountDto;
import lt.gama.model.dto.system.CalendarSettingsDto;
import lt.gama.model.sql.system.*;
import lt.gama.model.type.CalendarMonth;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DataFormatType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.PermitAll;
import java.time.LocalDate;
import java.util.List;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "admin")
@MaintenancePermissions
public interface AdminApi extends Api {

    @PostMapping("/getVersion")
    @PermitAll
    APIResult<String> getVersion() throws GamaApiException;


    @PostMapping("/getSystemSettings")
    APIResult<SystemSettingsSql> getSystemSettings() throws GamaApiException;

    @PostMapping("/saveSystemSettings")
    APIResult<SystemSettingsSql> saveSystemSettings(SystemSettingsSql systemSettings) throws GamaApiException;


    @PostMapping("/companyList")
    APIResult<PageResponse<CompanyDto, Void>> companyList(PageRequest request) throws GamaApiException;

    @PostMapping("/companySave")
    APIResult<CompanyDto> companySave(CompanyDto company) throws GamaApiException;

    @PostMapping("/companyGet")
    APIResult<CompanyDto> companyGet(CompanyIdRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class CompanyIdRequest {
        @JsonProperty("id") public long companyId;
        public CompanyIdRequest() {}
        public CompanyIdRequest(long companyId) {
            this.companyId = companyId;
        }
    }

    @PostMapping("/companyDelete")
    APIResult<Void> companyDelete(CompanyIdRequest request) throws GamaApiException;

    @PostMapping("/companyMonthlyPayment")
    APIResult<GamaMoney> companyMonthlyPayment(CompanyIdRequest request) throws GamaApiException;

    @PostMapping("/companyCreate")
    APIResult<CompanyDto> companyCreate(CreateCompanyRequest request) throws GamaApiException;

    @PostMapping("/employeeList")
    APIResult<PageResponse<EmployeeDto, Void>> employeeList(EmployeeListRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class EmployeeListRequest extends PageRequest {
        @JsonProperty("company") public Long companyId;
        public EmployeeListRequest() {}
        public EmployeeListRequest(Long companyId) {
            this.companyId = companyId;
        }
    }

    @PostMapping("/accountList")
    APIResult<PageResponse<AccountDto, Void>> accountList(PageRequest request) throws GamaApiException;

    @PostMapping("/accountGet")
    APIResult<AccountDto> accountGet(AccountGetRequest request) throws GamaApiException;

    class AccountGetRequest {
        @JsonProperty("id") public String accountId;
    }

    @PostMapping("/accountAssignPayer")
    APIResult<AccountDto> accountAssignPayer(AccountAssignPayerRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class AccountAssignPayerRequest {
        @JsonProperty("id") public String accountId;
        @JsonProperty("payer") public Long payerId;
        public AccountAssignPayerRequest() {}
        public AccountAssignPayerRequest(String accountId, Long payerId) {
            this.accountId = accountId;
            this.payerId = payerId;
        }
    }

    @PostMapping("/employeeImpersonate")
    APIResult<LoginResponse> employeeImpersonate(EmployeeImpersonateRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class EmployeeImpersonateRequest {
        @JsonProperty("company") public Long companyId;
        @JsonProperty("employee") public Long employeeId;
        public EmployeeImpersonateRequest() {}
        public EmployeeImpersonateRequest(Long companyId, Long employeeId) {
            this.companyId = companyId;
            this.employeeId = employeeId;
        }
    }

    @PostMapping("/accountImpersonate")
    APIResult<LoginResponse> accountImpersonate(AccountImpersonateRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class AccountImpersonateRequest {
        @JsonProperty("id") public String accountId;
        public AccountImpersonateRequest() {}
        public AccountImpersonateRequest(String accountId) {
            this.accountId = accountId;
        }
    }

    @PostMapping("/storageUrl")
    APIResult<UploadResponse> storageUrl(StorageUrlRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class StorageUrlRequest {
        public String contentType;
        @JsonProperty("company") public Long companyId;
        public String folder;
        public String fileName;
        public boolean isPublic;
        public String sourceFileName;
        public StorageUrlRequest() {}
        public StorageUrlRequest(String contentType, Long companyId, String folder, String fileName, boolean isPublic, String sourceFileName) {
            this.contentType = contentType;
            this.companyId = companyId;
            this.folder = folder;
            this.fileName = fileName;
            this.isPublic = isPublic;
            this.sourceFileName = sourceFileName;
        }
    }

    @PostMapping("/storageImport")
    APIResult<String> storageImport(StorageImportRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class StorageImportRequest {
        @JsonProperty("company") public Long companyId;
        public String fileName;
        public String entity;
        public boolean delete;
        public DataFormatType format;
        public StorageImportRequest() {}
        public StorageImportRequest(Long companyId, String fileName, String entity, boolean delete, DataFormatType format) {
            this.companyId = companyId;
            this.fileName = fileName;
            this.entity = entity;
            this.delete = delete;
            this.format = format;
        }
    }

    @PostMapping("/storageUpload")
    APIResult<String> storageUpload(UploadDataRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
	class UploadDataRequest {
		public String data;
		public Long companyId;
		public String folder;
		public String fileName;
		public String contentType;
        public UploadDataRequest() {}
        public UploadDataRequest(String data, Long companyId, String folder, String fileName, String contentType) {
            this.data = data;
            this.companyId = companyId;
            this.folder = folder;
            this.fileName = fileName;
            this.contentType = contentType;
        }
    }

    @PostMapping("/gLAccountList")
    APIResult<PageResponse<GLAccountDto, Void>> gLAccountList(GLAccountListRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class GLAccountListRequest extends PageRequest {
        @JsonProperty("company") public Long companyId;
        public GLAccountListRequest() {}
        public GLAccountListRequest(Long companyId) {
            this.companyId = companyId;
        }
    }

    @PostMapping("/gLAccountSave")
    APIResult<GLAccountDto> gLAccountSave(GLAccountRequest request) throws GamaApiException;

    @PostMapping("/gLAccountDelete")
    APIResult<Void> gLAccountDelete(GLAccountRequest request) throws GamaApiException;



	/*
	 * Calendar
	 */

    @PostMapping("/calendarGetYear")
    APIResult<CalendarSql> calendarGetYear(CalendarAdminRequest request) throws GamaApiException;

    @PostMapping("/calendarGetMonth")
    APIResult<CalendarMonth> calendarGetMonth(CalendarAdminRequest request) throws GamaApiException;

    @PostMapping("/calendarSaveMonth")
    APIResult<Void> calendarSaveMonth(CalendarAdminRequest request) throws GamaApiException;


    @PostMapping("/calendarSettingsList")
    APIResult<List<CalendarSettingsDto>> calendarSettingsList(CalendarSettingsListRequest request) throws GamaApiException;

    class CalendarSettingsListRequest {
        public String country;
    }

    @PostMapping("/calendarSettingsGet")
    APIResult<CalendarSettingsDto> calendarSettingsGet(CalendarSettingsGetRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class CalendarSettingsGetRequest {
        public String country;
        public int year;
        public CalendarSettingsGetRequest() {}
        public CalendarSettingsGetRequest(String country, int year) {
            this.country = country;
            this.year = year;
        }
    }

    @PostMapping("/calendarSettingsSave")
    APIResult<CalendarSettingsDto> calendarSettingsSave(CalendarSettingsDto request) throws GamaApiException;

    @PostMapping("/calendarSettingsDelete")
    APIResult<Void> calendarSettingsDelete(CalendarSettingsDto request) throws GamaApiException;


    @PostMapping("/workTimeCodesList")
    APIResult<PageResponse<CountryWorkTimeCodeSql, Void>> workTimeCodesList() throws GamaApiException;

    @PostMapping("/workTimeCodesGet")
    APIResult<CountryWorkTimeCodeSql> workTimeCodesGet(WorkTimeCodesGetRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class WorkTimeCodesGetRequest {
        public String id;
        public WorkTimeCodesGetRequest() {}
        public WorkTimeCodesGetRequest(String id) {
            this.id = id;
        }
    }

    @PostMapping("/workTimeCodesSave")
    APIResult<CountryWorkTimeCodeSql> workTimeCodesSave(CountryWorkTimeCodeSql request) throws GamaApiException;


    @PostMapping("/vatCodeList")
    APIResult<PageResponse<CountryVatCodeSql, Void>> vatCodeList() throws GamaApiException;

    @PostMapping("/vatCodeGet")
    APIResult<CountryVatCodeSql> vatCodeGet(VatCodeGetGetRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class VatCodeGetGetRequest {
        public String id;
        public VatCodeGetGetRequest() {}
        public VatCodeGetGetRequest(String id) {
            this.id = id;
        }
    }

    @PostMapping("/vatCodeSave")
    APIResult<CountryVatCodeSql> vatCodeSave(CountryVatCodeSql request) throws GamaApiException;


    @PostMapping("/vatNoteList")
    APIResult<List<CountryVatNoteSql>> vatNoteList() throws GamaApiException;

    @PostMapping("/vatNoteGet")
    APIResult<CountryVatNoteSql> vatNoteGet(VatNoteGetRequest request) throws GamaApiException;

    class VatNoteGetRequest {
        public String id;
    }

    @PostMapping("/vatNoteSave")
    APIResult<CountryVatNoteSql> vatNoteSave(CountryVatNoteSql request) throws GamaApiException;


    @PostMapping("/vatRateList")
    APIResult<List<CountryVatRateSql>> vatRateList() throws GamaApiException;

    @PostMapping("/vatRateGet")
    APIResult<CountryVatRateSql> vatRateGet(VatRateGetRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class VatRateGetRequest {
        public String id;
        public VatRateGetRequest() {}
        public VatRateGetRequest(String id) {
            this.id = id;
        }
    }

    @PostMapping("/vatRateSave")
    APIResult<CountryVatRateSql> vatRateSave(CountryVatRateSql request) throws GamaApiException;

    @PostMapping("/generateSubscriptionInvoice")
    APIResult<InvoiceDto> generateSubscriptionInvoice(GenerateSubscriptionInvoiceRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class GenerateSubscriptionInvoiceRequest {
        @JsonProperty("company") public long companyId;
        public LocalDate date;
        public Boolean debug;
        public GenerateSubscriptionInvoiceRequest() {}
        public GenerateSubscriptionInvoiceRequest(long companyId) {
            this.companyId = companyId;
        }
    }

    @PostMapping("/getAvailableTimeZoneIDs")
    APIResult<List<String>> getAvailableTimeZoneIDs() throws GamaApiException;
}
