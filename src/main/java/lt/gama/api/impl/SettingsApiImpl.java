package lt.gama.api.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.CounterRequest;
import lt.gama.api.response.CompanyResponse;
import lt.gama.api.response.LoginResponse;
import lt.gama.api.service.SettingsApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.SyncSql;
import lt.gama.model.sql.system.SystemSettingsSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.*;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.inventory.InvoiceNote;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.APIResultService;
import lt.gama.service.AccountService;
import lt.gama.service.CounterService;
import lt.gama.service.DBServiceSQL;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-08-10.
 */
@RestController
public class SettingsApiImpl implements SettingsApi {

    @PersistenceContext
    private EntityManager entityManager;

    private final AccountService accountService;
    private final CounterService counterService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final APIResultService apiResultService;

    public SettingsApiImpl(AccountService accountService, CounterService counterService, Auth auth, DBServiceSQL dbServiceSQL, APIResultService apiResultService) {
        this.accountService = accountService;
        this.counterService = counterService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.apiResultService = apiResultService;
    }

    @Override
    public APIResult<CompanySettingsGL> getGL() throws GamaApiException {
        return apiResultService.result(() -> {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getGl();
        });
    }

    @Transactional
    @Override
    public APIResult<LoginResponse> saveGL(CompanyDto settings) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            boolean updated = company.getSettings().isDisableGL() != settings.getSettings().isDisableGL();

            company.getSettings().setDisableGL(settings.getSettings().isDisableGL());
            company.getSettings().setGl(settings.getSettings().getGl());

            company = dbServiceSQL.saveEntity(company);

            return updateAccountInfo(updated, company.getId(), auth.getId());
        });
    }

    @Override
    public APIResult<Map<String, CounterDesc>> getCounter() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getCounter();
        });
    }

    @Transactional
    @Override
    public APIResult<Void> saveCounter(CompanyDto counter) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            Map<String, CounterDesc> countersOld = company.getSettings().getCounter();
            company.getSettings().setCounter(counter.getSettings() == null ? null : counter.getSettings().getCounter());

            if (CollectionsHelper.hasValue(countersOld) && CollectionsHelper.hasValue(company.getSettings().getCounter())) {
                countersOld.forEach((k, v) -> {
                    CounterDesc updated = company.getSettings().getCounter().get(k);
                    if (v != null && updated != null &&
                            (!IntegerUtils.isEqual(v.getStart(), updated.getStart()) ||
                                    !Objects.equals(v.getPrefix(), updated.getPrefix()))) {
                        updated.setStart(updated.getStart());
                        counterService.setCount(updated);
                    }
                });
            }
            dbServiceSQL.saveEntity(company);
        });
    }

    @Override
    public APIResult<Void> updateCounter(CounterRequest counter) throws GamaApiException {
        return apiResultService.result(() -> {
            counter.getDesc().setStart(IntegerUtils.subtract(counter.getDesc().getStart(), 1));
            counterService.setCount(counter.getDesc());
        });
    }

    @Override
    public APIResult<Map<String, Integer>> getCountersValues() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            Map<String, CounterDesc> counter = companySettings.getCounter();
            Map<String, Integer> currentValue = new HashMap<>();

            if (counter != null) {
                for (CounterDesc counterDesc : counter.values()) {
                    currentValue.put(counterDesc.getLabel(), counterService.getCount(counterDesc));
                }
            }

            return currentValue;
        });
    }

    @Override
    public APIResult<CompanyResponse> getLocations() throws GamaApiException {
        return apiResultService.result(() -> {
            final long companyId = auth.getCompanyId();

            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company");

            CompanyResponse companyResponse = new CompanyResponse();
            companyResponse.setRegistrationAddress(company.getRegistrationAddress());
            companyResponse.setBusinessAddress(company.getBusinessAddress());
            companyResponse.setLocations(company.getLocations());

            companyResponse.setContactsInfo(company.getContactsInfo());

            return companyResponse;
        });
    }

    @Transactional
    @Override
    public APIResult<LoginResponse> saveLocations(CompanyDto companyLocations) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            company.setRegistrationAddress(companyLocations.getRegistrationAddress());
            company.setBusinessAddress(companyLocations.getBusinessAddress());
            company.setLocations(companyLocations.getLocations());
            company.setContactsInfo(companyLocations.getContactsInfo());
            dbServiceSQL.saveEntity(company);

            return updateAccountInfo(true, company.getId(), auth.getId());
        });
    }

    @Override
    public APIResult<List<DocBankAccount>> getBanks() throws GamaApiException {
        return apiResultService.result(() -> {
            final long companyId = auth.getCompanyId();
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company");
            return company.getBanks();
        });
    }

    @Override
    public APIResult<LoginResponse> saveBanks(CompanyDto banks) throws GamaApiException {
        return APIResult.Data( dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            company.setBanks(banks.getBanks());
            dbServiceSQL.saveEntity(company);

            return updateAccountInfo(true, company.getId(), auth.getId());
        }));
    }

    @Override
    public APIResult<CompanyDto> getCompany() throws GamaApiException {
        return apiResultService.result(() -> {
            final long companyId = auth.getCompanyId();

            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company");
            CompanyDto result = new CompanyDto();
            result.setName(company.getName());
            result.setBusinessName(company.getBusinessName());
            result.setCode(company.getCode());
            result.setVatCode(company.getVatCode());
            result.setSsCode(company.getSsCode());
            result.setId(company.getId());
            result.setLogo(company.getLogo());
            result.setEmail(company.getEmail());
            result.setCcEmail(company.getCcEmail());

            result.setActiveAccounts(company.getActiveAccounts());
            result.setPayerAccounts(company.getPayerAccounts());
            result.setOtherAccounts(company.getOtherAccounts());
            result.setTotalPrice(company.getTotalPrice());

            result.setSettings(new CompanySettings());
            if (company.getSettings() != null) {
                result.getSettings().setAccYear(company.getSettings().getAccYear());
                result.getSettings().setAccMonth(company.getSettings().getAccMonth());
            }

            if (GamaMoneyUtils.isZero(company.getAccountPrice())) {
                SystemSettingsSql systemSettings = Validators.checkNotNull(dbServiceSQL.getById(SystemSettingsSql.class, SystemSettingsSql.ID), "No SystemSettings");
                result.setAccountPrice(systemSettings.getAccountPrice());
            } else {
                result.setAccountPrice(company.getAccountPrice());
            }

            return result;
        });
    }

    @Override
    public APIResult<LoginResponse> saveCompany(CompanyDto comp) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            company.setBusinessName(comp.getBusinessName());
            company.setCode(comp.getCode());
            company.setVatCode(comp.getVatCode());
            company.setSsCode(comp.getSsCode());
            company.setLogo(comp.getLogo());
            company.setEmail(comp.getEmail());
            company.setCcEmail(comp.getCcEmail());

            if (company.getSettings() == null) company.setSettings(new CompanySettings());
            company.getSettings().setVatPayer(comp.getVatCode() != null && !comp.getVatCode().isEmpty());

            if (comp.getSettings() != null) {
                company.getSettings().setAccYear(comp.getSettings().getAccYear());
            }
            dbServiceSQL.saveEntity(company);

            return updateAccountInfo(true, company.getId(), auth.getId());
        });
    }

    @Override
    public APIResult<List<CompanyTaxSettings>> getTax() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getTaxes();
        });
    }

    @Override
    public APIResult<Void> saveTax(CompanyDto taxSettings) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            List<CompanyTaxSettings> taxes = taxSettings.getSettings() == null ? null : taxSettings.getSettings().getTaxes();
            if (taxes != null && taxes.size() > 1) {
                /*
                 * nulls goes to the end of the list
                 */
                taxes.sort((o1, o2) -> o1.getDate() == null && o2.getDate() == null ? 0 :
                        o1.getDate() == null ? 1 : o2.getDate() == null ? -1 : o1.getDate().compareTo(o2.getDate()));

            }
            company.getSettings().setTaxes(taxes);
            dbServiceSQL.saveEntity(company);
        });
    }

    @Override
    public APIResult<CompanySettings> getSalary() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            CompanySettings companySettingsRet = new CompanySettings();
            companySettingsRet.setChargeAdvance(companySettings.getChargeAdvance());
            companySettingsRet.setChargeWork(companySettings.getChargeWork());
            companySettingsRet.setChargeIllness(companySettings.getChargeIllness());
            companySettingsRet.setChargeVacation(companySettings.getChargeVacation());
            companySettingsRet.setChargeChildDays(companySettings.getChargeChildDays());
            companySettingsRet.setSalary(companySettings.getSalary());

            return companySettingsRet;
        });
    }

    @Override
    public APIResult<LoginResponse> saveSalary(CompanyDto salary) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            company.getSettings().setChargeAdvance(salary.getSettings() == null ? null : salary.getSettings().getChargeAdvance());
            company.getSettings().setChargeWork(salary.getSettings() == null ? null : salary.getSettings().getChargeWork());
            company.getSettings().setChargeIllness(salary.getSettings() == null ? null : salary.getSettings().getChargeIllness());
            company.getSettings().setChargeVacation(salary.getSettings() == null ? null : salary.getSettings().getChargeVacation());
            company.getSettings().setChargeChildDays(salary.getSettings() == null ? null : salary.getSettings().getChargeChildDays());

            List<CompanySalarySettings> salarySettings = salary.getSettings() == null ? null : salary.getSettings().getSalary();
            if (salarySettings != null && salarySettings.size() > 1) {
                /*
                 * nulls goes to the end of the list
                 */
                salarySettings.sort((o1, o2) -> o1.getDate() == null && o2.getDate() == null ? 0 :
                        o1.getDate() == null ? 1 : o2.getDate() == null ? -1 : o1.getDate().compareTo(o2.getDate()));

            }
            company.getSettings().setSalary(salarySettings);
            dbServiceSQL.saveEntity(company);

            return updateAccountInfo(true, company.getId(), auth.getId());
        });
    }

    LoginResponse updateAccountInfo(boolean update, long companyId, String login) {
        // update company info in accounts
        if (update) accountService.updateCompanyInfo(companyId);

        AccountSql account = dbServiceSQL.getById(AccountSql.class, login);
        int companyIndex = accountService.findCompanyIndexById(account, companyId);
        return accountService.login(account, companyIndex);
    }


    @Override
    public APIResult<SyncSettings> getSync() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getSync();
        });
    }

    @Override
    public APIResult<LoginResponse> saveSync(CompanyDto syncSettings) throws GamaApiException {
        if (syncSettings.getSettings() == null) syncSettings.setSettings(new CompanySettings());
        if (syncSettings.getSettings().getSync() == null) syncSettings.getSettings().setSync(new SyncSettings());

        return apiResultService.result(() -> {
            final long companyId = auth.getCompanyId();
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company");
            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            company.getSettings().setSync(syncSettings.getSettings().getSync());
            company.getSettings().getSync().setTimeZone(company.getSettings().getTimeZone());

            if (BooleanUtils.isNotTrue(company.getSettings().getSync().getSyncActive())) {
                entityManager.createQuery("DELETE FROM " + SyncSql.class.getName() + " c WHERE id = :id")
                        .setParameter("id", companyId)
                        .executeUpdate();
            } else {
                SyncSql sync = dbServiceSQL.getById(SyncSql.class, companyId);
                if (sync == null) {
                    sync = new SyncSql();
                    sync.setId(companyId);
                }
                sync.setSettings(company.getSettings().getSync());
                if (sync.getDate() == null) {
                    sync.setDate(company.getSettings().getSync().getDate() != null ?
                            company.getSettings().getSync().getDate().atStartOfDay() : null);
                }
                dbServiceSQL.saveEntity(sync);
            }
            dbServiceSQL.saveEntity(company);

            return updateAccountInfo(true, company.getId(), auth.getId());
        });
    }

    @Override
    public APIResult<CompanySettings> getDefaults() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            CompanySettings companySettingsRet = new CompanySettings();
            companySettingsRet.setWarehouse(companySettings.getWarehouse());
            companySettingsRet.setAccount(companySettings.getAccount());
            companySettingsRet.setCash(companySettings.getCash());

            return companySettingsRet;
        });
    }

    @Override
    public APIResult<LoginResponse> saveDefaults(CompanyDto defaults) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            company.getSettings().setWarehouse(defaults.getSettings() == null ? null : defaults.getSettings().getWarehouse());
            company.getSettings().setAccount(defaults.getSettings() == null ? null : defaults.getSettings().getAccount());
            company.getSettings().setCash(defaults.getSettings() == null ? null : defaults.getSettings().getCash());
            dbServiceSQL.saveEntity(company);

            return updateAccountInfo(true, company.getId(), auth.getId());
        });
    }

    @Override
    public APIResult<SalesSettings> getSales() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getSales();
        });
    }

    @Override
    public APIResult<LoginResponse> saveSales(CompanyDto sales) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");

            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            company.getSettings().setSales(sales.getSettings() == null ? null : sales.getSettings().getSales());
            dbServiceSQL.saveEntity(company);

            return updateAccountInfo(true, company.getId(), auth.getId());
        });
    }

    @Override
    public APIResult<GamaMoney> getSubscriptionPrice() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No Company");
            if (Validators.isValid(company.getPayer())) return null;

            AccountSql account = Validators.checkNotNull(dbServiceSQL.getById(AccountSql.class, auth.getId()), "No account");
            if (Validators.isValid(account.getPayer()) && !Objects.equals(account.getPayer().getId(), company.getId())) return null;

            if (company.getTotalPrice() != null && GamaMoneyUtils.isPositiveOrZero(company.getTotalPrice())) return company.getTotalPrice();
            if (company.getAccountPrice() != null && GamaMoneyUtils.isPositiveOrZero(company.getAccountPrice())) return company.getAccountPrice();

            SystemSettingsSql systemSettings = Validators.checkNotNull(dbServiceSQL.getById(SystemSettingsSql.class, SystemSettingsSql.ID), "No SystemSettings");
            return systemSettings.getAccountPrice();
        });
    }

    @Override
    public APIResult<List<InvoiceNote>> getInvoiceNotes() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return companySettings.getInvoiceNotes();
        });
    }

    @Override
    public APIResult<List<InvoiceNote>> saveInvoiceNotes(CompanySettings invoiceNotes) throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");

            if (company.getSettings() == null) company.setSettings(new CompanySettings());

            company.getSettings().setInvoiceNotes(invoiceNotes.getInvoiceNotes());
            dbServiceSQL.saveEntity(company);

            return company.getSettings().getInvoiceNotes();
        });
    }
}
