package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.CounterRequest;
import lt.gama.api.response.CompanyResponse;
import lt.gama.api.response.LoginResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.*;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.enums.Permission;
import lt.gama.model.type.inventory.InvoiceNote;
import lt.gama.model.type.sync.SyncSettings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * Gama
 * Created by valdas on 15-08-10.
 */
@RequestMapping(APP_API_3_PATH + "settings")
@RequiresPermissions({Permission.SETTINGS})
public interface SettingsApi extends Api {

    @PostMapping("/getGL")
    APIResult<CompanySettingsGL> getGL() throws GamaApiException;

    @PostMapping("/saveGL")
    APIResult<LoginResponse> saveGL(CompanyDto settings) throws GamaApiException;


    @PostMapping("/getCounter")
    APIResult<Map<String, CounterDesc>> getCounter() throws GamaApiException;

    @PostMapping("/saveCounter")
    APIResult<Void> saveCounter(CompanyDto counter) throws GamaApiException;

    @PostMapping("/updateCounter")
    APIResult<Void> updateCounter(CounterRequest counter) throws GamaApiException;

    @PostMapping("/getCountersValues")
    APIResult<Map<String, Integer>> getCountersValues() throws GamaApiException;


    @PostMapping("/getLocations")
    APIResult<CompanyResponse> getLocations() throws GamaApiException;

    @PostMapping("/saveLocations")
    APIResult<LoginResponse> saveLocations(CompanyDto locations) throws GamaApiException;


    @PostMapping("/getBanks")
    APIResult<List<DocBankAccount>> getBanks() throws GamaApiException;

    @PostMapping("/saveBanks")
    APIResult<LoginResponse> saveBanks(CompanyDto banks) throws GamaApiException;


    @PostMapping("/getCompany")
    APIResult<CompanyDto> getCompany() throws GamaApiException;

    @PostMapping("/saveCompany")
    APIResult<LoginResponse> saveCompany(CompanyDto company) throws GamaApiException;


    @PostMapping("/getTax")
    @RequiresPermissions({Permission.SETTINGS, Permission.SALARY_R, Permission.SALARY_M})
    APIResult<List<CompanyTaxSettings>> getTax() throws GamaApiException;

    @PostMapping("/saveTax")
    APIResult<Void> saveTax(CompanyDto taxSettings) throws GamaApiException;


    @PostMapping("/getSalary")
    APIResult<CompanySettings> getSalary() throws GamaApiException;

    @PostMapping("/saveSalary")
    APIResult<LoginResponse> saveSalary(CompanyDto salary) throws GamaApiException;


    @PostMapping("/getSync")
    APIResult<SyncSettings> getSync() throws GamaApiException;

    @PostMapping("/saveSync")
    APIResult<LoginResponse> saveSync(CompanyDto sync) throws GamaApiException;


    @PostMapping("/getDefaults")
    APIResult<CompanySettings> getDefaults() throws GamaApiException;

    @PostMapping("/saveDefaults")
    APIResult<LoginResponse> saveDefaults(CompanyDto defaults) throws GamaApiException;


    @PostMapping("/getSales")
    APIResult<SalesSettings> getSales() throws GamaApiException;

    @PostMapping("/saveSales")
    APIResult<LoginResponse> saveSales(CompanyDto sales) throws GamaApiException;


    @PostMapping("/getSubscriptionPrice")
    @RequiresPermissions
    APIResult<GamaMoney> getSubscriptionPrice() throws GamaApiException;


    @PostMapping("/getInvoiceNotes")
    APIResult<List<InvoiceNote>> getInvoiceNotes() throws GamaApiException;

    @PostMapping("/saveInvoiceNotes")
    APIResult<List<InvoiceNote>> saveInvoiceNotes(CompanySettings invoiceNotes) throws GamaApiException;
}
