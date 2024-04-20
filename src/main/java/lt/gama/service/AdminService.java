package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.i.ICompany;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.auth.AccountInfo;
import lt.gama.model.type.auth.CompanyAccount;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.tasks.DeleteEntityInCompanyTask;
import lt.gama.tasks.maintenance.FinishDocumentsTask;
import lt.gama.tasks.maintenance.FixCompanyAccountsCountTask;
import lt.gama.tasks.maintenance.FixRefreshAccountsTask;
import lt.gama.tasks.maintenance.RecallDocumentsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gama-online
 * Created by valdas on 2015-11-12.
 */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);


    @PersistenceContext
    private EntityManager entityManager;

    private final AccountService accountService;
    private final DBServiceSQL dbServiceSQL;
    private final Auth auth;
    private final TaskQueueService taskQueueService;

    public AdminService(AccountService accountService, 
                        DBServiceSQL dbServiceSQL,
                        Auth auth,
                        TaskQueueService taskQueueService) {
        this.accountService = accountService;
        this.dbServiceSQL = dbServiceSQL;
        this.auth = auth;
        this.taskQueueService = taskQueueService;
    }

    public void recallDocuments(long companyId) {
        Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company {0}", companyId);
        taskQueueService.queueTask(new RecallDocumentsTask(companyId));
    }

    public void finishDocuments(long companyId, LocalDate dateFrom, LocalDate dateTo) {
        Validators.checkNotNull(dateFrom, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateFrom, auth.getLanguage()));
        Validators.checkNotNull(dateTo, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateTo, auth.getLanguage()));

        Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company {0}", companyId);

        taskQueueService.queueTask(new FinishDocumentsTask(companyId, true, dateFrom, dateTo));
    }

    public void recalculateAllCompaniesAccounts() {
        List<Long> keys = entityManager.createQuery(
                "SELECT id FROM " + CompanySql.class.getName() + " a" +
                        " WHERE (a.archive IS null OR a.archive = false)",
                        Long.class)
                .getResultList();

        if (CollectionsHelper.isEmpty(keys)) throw new GamaException("No companies");

        keys.forEach(companyId -> taskQueueService.queueTask(new FixCompanyAccountsCountTask(companyId)));

        log.info("Companies found: " + keys.size());
    }

    public void recalculateCompanyAccounts(long companyId) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company " + companyId);

            int activeAccounts = 0;
            int payerAccounts = 0;
            Map<Long, CompanyAccount> otherAccounts = new HashMap<>();

            for (AccountSql account : dbServiceSQL.makeQuery(AccountSql.class).getResultList()) {
                if (account.getCompanies() == null) continue;

                // check account has payer
                boolean isCompanyPayer = (account.getPayer() != null && account.getPayer().getId() != null && account.getPayer().getId() == companyId);
                boolean isOtherPayer = (account.getPayer() != null && account.getPayer().getId() != null && account.getPayer().getId() != companyId);

                for (AccountInfo accountInfo : account.getCompanies()) {
                    if (accountInfo.getCompanyId() == companyId) {
                        ++activeAccounts;

                        if (isOtherPayer) {
                            CompanyAccount companyAccount = otherAccounts.get(account.getPayer().getId());
                            if (companyAccount == null) {
                                companyAccount = new CompanyAccount(account.getPayer().getId(), account.getPayer().getName(), -1);
                                otherAccounts.put(account.getPayer().getId(), companyAccount);
                            } else {
                                companyAccount.setAccounts(companyAccount.getAccounts() - 1);
                            }
                            --payerAccounts;
                        }

                    } else if (isCompanyPayer) {
                        CompanyAccount companyAccount = otherAccounts.get(accountInfo.getCompanyId());
                        if (companyAccount == null) {
                            companyAccount = new CompanyAccount(accountInfo.getCompanyId(), accountInfo.getCompanyName(), 1);
                            otherAccounts.put(accountInfo.getCompanyId(), companyAccount);
                        } else {
                            companyAccount.setAccounts(companyAccount.getAccounts() + 1);
                        }
                        ++payerAccounts;
                    }
                }
            }

            company.setActiveAccounts(activeAccounts);
            company.setPayerAccounts(payerAccounts);
            company.setOtherAccounts(!otherAccounts.isEmpty() ? otherAccounts : null);

            log.info("Company: " + companyId + " recalculated: " +
                    "active " + company.getActiveAccounts() + ", payer " + company.getPayerAccounts());
        });
    }

    public void deleteAll(Class<? extends ICompany> className, long companyId) {
        taskQueueService.queueTask(new DeleteEntityInCompanyTask(companyId, className.getName()));
    }

    public String fixRefreshAccounts() {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            List<Long> keys = entityManager.createQuery(
                            "SELECT id FROM " + CompanySql.class.getName() + " a" +
                                    " WHERE (a.archive IS null OR a.archive = false)",
                            Long.class)
                    .getResultList();

            if (CollectionsHelper.isEmpty(keys)) return "No companies";

            keys.forEach(companyId -> taskQueueService.queueTask(new FixRefreshAccountsTask(companyId)));

            return "Companies found: " + keys.size();
        });
    }

    public void refreshCompanyAccounts(long companyId) {
        accountService.updateCompanyInfo(companyId);
    }
}
