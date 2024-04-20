package lt.gama.api.impl.maintenance;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.ex.GamaApiUnauthorizedException;
import lt.gama.api.request.LoginRequest;
import lt.gama.api.response.LoginResponse;
import lt.gama.api.service.maintenance.FixApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.service.*;
import lt.gama.tasks.maintenance.RegenerateCounterpartyDebtFromHistoryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static lt.gama.Constants.DB_BATCH_SIZE;

/**
 * gama-online
 * Created by valdas on 2016-01-29.
 * Administrator's Data Fixing Methods by hand.
 */
@RestController
public class FixApiImpl implements FixApi {

    private static final Logger log = LoggerFactory.getLogger(FixApiImpl.class);

    private final AdminService adminService;
    private final AccountService accountService;
    private final AccountingService accountingService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final APIResultService apiResultService;
    private final TaskQueueService taskQueueService;

    public FixApiImpl(AdminService adminService, AccountService accountService, AccountingService accountingService, Auth auth, DBServiceSQL dbServiceSQL, AuthSettingsCacheService authSettingsCacheService, APIResultService apiResultService, TaskQueueService taskQueueService) {
        this.adminService = adminService;
        this.accountService = accountService;
        this.accountingService = accountingService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.authSettingsCacheService = authSettingsCacheService;
        this.apiResultService = apiResultService;
        this.taskQueueService = taskQueueService;
    }

    @Override
    public APIResult<LoginResponse> login(LoginRequest loginRequest) throws GamaApiException {
        return apiResultService.result(() -> {
            try {
               return accountService.loginAdmin(loginRequest.getName(), loginRequest.getPassword());

            } catch (Exception e) {
                log.warn(loginRequest.getName() + ": " + e);
                throw new GamaApiUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.WrongLogin, auth.getLanguage()));
            }
        });
    }

    @Override
    public APIResult<String> setPassword(LoginRequest loginRequest) throws GamaApiException {
        return apiResultService.result(() -> {
            accountService.createPassword(loginRequest.getName(), loginRequest.getPassword());
            return "OK";
        });
    }

    @Override
    public APIResult<String> start() throws GamaApiException {
        return apiResultService.result(() ->
            dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                long count = ((BigInteger) entityManager
                        .createNativeQuery("SELECT COUNT(*) FROM companies")
                        .getSingleResult()).longValue();
                if (count > 0) return "Data already exists";
                accountService.initData();
                return "Data initialized";
            }));
    }

    @Override
    public APIResult<Void> recallDocuments(RecallDocumentsRequest request) throws GamaApiException {
        return apiResultService.result(() -> adminService.recallDocuments(request.companyId));
    }

    @Override
    public APIResult<Void> finishDocuments(FinishDocumentsRequest request) throws GamaApiException {
        return apiResultService.result(() -> adminService.finishDocuments(request.companyId, request.dateFrom, request.dateTo));
    }

    @Override
    public APIResult<Void> fixCompanyAccounts(FixCompanyAccountsRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (request.companyId == 0) {
                adminService.recalculateAllCompaniesAccounts();
            } else {
                adminService.recalculateCompanyAccounts(request.companyId);
            }
        });
    }

    @Override
    public APIResult<String> refreshAccounts() throws GamaApiException {
        return apiResultService.result(() -> adminService.fixRefreshAccounts());
    }

    @Override
    public APIResult<String> calcCompaniesMonthlyPayment() throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                    List<CompanySql> companies = dbServiceSQL.makeQuery(CompanySql.class).getResultList();
                    int refreshed = 0;
                    for (CompanySql c : companies) {
                        if (BooleanUtils.isTrue(c.getArchive())) continue;
                        if (!CompanyStatusType.SUBSCRIBER.equals(c.getStatus())) continue;

                        c.setCurrentTotal(accountingService.companyAmountToPay(c));

                        refreshed++;

                        if (refreshed % DB_BATCH_SIZE == 0) {
                            entityManager.flush();
                            entityManager.clear();
                        }
                    }
                    return "refreshed: " + refreshed;
                })
        );
    }

    @Override
    public APIResult<String> changeAccountEmail(ChangeAccountEmailRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            int count = accountService.changeAccountEmail(request.oldMail.trim(), request.newMail.toLowerCase().trim());
            return "Changed from " + request.oldMail + " to " + request.newMail + ", employees changed: " + count;
        });
    }

    @Override
    public APIResult<String> refreshCompanyConnections(RefreshCompanyConnectionsRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            Set<Long> ids = accountingService.refreshCompaniesConnections(request.companyId, request.date);
            return "Refresh tasks activated for companies: " + ids.toString();
        });
    }

    @Override
    public APIResult<Void> companyDeleteDocuments(CompanyDeleteDocumentsRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            accountService.deleteCompanyDocuments(request.companyId);
        });
    }

    @Override
    public APIResult<Void> deleteCompany(DeleteCompanyRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            accountService.deleteCompany(request.companyId);
        });
    }

    @Override
    public APIResult<Void> reloadCompanySettings(ReloadCompanySettingsRequest request) throws GamaApiException {
        return apiResultService.result(() -> authSettingsCacheService.remove(request.companyId));
    }

    @Override
    public APIResult<String> regenerateCounterpartyDebtFromHistory(RegenerateCounterpartyDebtFromHistoryRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            CounterpartySql counterparty = Validators.checkNotNull(dbServiceSQL.getById(CounterpartySql.class, request.counterpartyId),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty, auth.getLanguage()));
            return taskQueueService.queueTask(new RegenerateCounterpartyDebtFromHistoryTask(counterparty.getCompanyId(), request.counterpartyId));
        });
    }
}
