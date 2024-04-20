package lt.gama.tasks;

import jakarta.persistence.EntityManager;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.EmployeeSql_;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.AccountSql_;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CompanySql_;
import lt.gama.model.type.auth.AccountInfo;
import lt.gama.model.type.auth.CompanyAccount;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.ExCompanyType;
import lt.gama.service.AccountingService;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.time.LocalDate;
import java.util.*;


/**
 * gama-online
 * Created by valdas on 2018-06-21.
 * <p>
 * Refresh company connections info, i.e. recalculate all inner and outer connections
 */
public class RefreshCompanyConnectionsTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected AccountingService accountingService;


    private final LocalDate date;


    public RefreshCompanyConnectionsTask(long companyId, LocalDate date) {
        super(companyId);
        this.date = date;
    }

    @Override
    public void execute() {
        dbServiceSQL.executeInTransaction(this::executeInTransaction);
    }

    private void executeInTransaction(EntityManager entityManager) {
        /*
         * Employee <<--> Account
         * Account <-->> Company
         */

        CompanySql company = dbServiceSQL.getById(CompanySql.class, getCompanyId());
        if (company == null || BooleanUtils.isTrue(company.getArchive())) return;

        Map<Long, CompanyAccount> otherAccounts = new HashMap<>();
        Set<String> logins = new HashSet<>();

        List<String> emails = getEmployeeEmails(entityManager, Collections.singletonList(getCompanyId()));
        int activeAccounts = emails == null ? 0 : emails.size();
        if (CollectionsHelper.hasValue(emails)) {
            logins.addAll(emails);
        }

        // add accounts where payer is current company
        logins.addAll(entityManager.createQuery(
                "SELECT id FROM " + AccountSql.class.getName() + " a" +
                        " WHERE (a.archive IS null OR a.archive = false)" +
                        " AND a." + AccountSql_.PAYER + ".id = :payer", String.class)
                .setParameter("payer", getCompanyId()).getResultList());

        // add other companies accounts where company payer is current company
        List<Long> compKeys = entityManager.createQuery(
                "SELECT id FROM " + CompanySql.class.getName() + " a" +
                        " WHERE " + CompanySql_.STATUS + " = :status" +
                        " AND (a.archive IS null OR a.archive = false)" +
                        " AND " + CompanySql_.PAYER + ".id = :payerId",
                        Long.class)
                .setParameter("status", CompanyStatusType.SUBSCRIBER)
                .setParameter("payerId", getCompanyId())
                .getResultList();

        if (CollectionsHelper.hasValue(compKeys)) {
            List<String> employeeEmails = getEmployeeEmails(entityManager, compKeys);
            if (employeeEmails != null) {
                logins.addAll(employeeEmails);
            }
        }

        if (!logins.isEmpty()) {
            List<AccountSql> accounts = entityManager.createQuery(
                    "SELECT a FROM " + AccountSql.class.getName() + " a" +
                            " WHERE a.id IN :ids" +
                            " AND (a.archive IS null OR a.archive = false)",
                            AccountSql.class)
                    .setParameter("ids", logins)
                    .getResultList();
            /*
             * company (companyId):
             *   employees in company
             *
             * account in accounts:
             *   accountInfo in account.companies:
             *
             *   payerAccount -1 if account is in company and other company is payer of account
             *   payerAccount +1 if account is not in company and company is payer of account
             */
            Map<Long, CompanySql> companiesMap = new HashMap<>();
            for (AccountSql account : accounts) {
                if (BooleanUtils.isTrue(account.getArchive())) continue;
                for (AccountInfo accountInfo : account.getCompanies()) {

                    long otherCompanyId = accountInfo.getCompanyId();

                    CompanySql otherCompany = companiesMap.get(otherCompanyId);
                    if (otherCompany == null) {
                        otherCompany = dbServiceSQL.getById(CompanySql.class, otherCompanyId);
                        companiesMap.put(otherCompanyId, otherCompany);
                    }

                    if (otherCompany.getStatus() != CompanyStatusType.SUBSCRIBER) continue;
                    // skip if no account and no company payer
                    if (!Validators.isValid(account.getPayer()) && !Validators.isValid(otherCompany.getPayer()))
                        continue;

                    // company payer has precedence over account payer
                    long payerId = Validators.isValid(otherCompany.getPayer()) ? otherCompany.getPayer().getId() :
                            account.getPayer().getId();
                    CompanySql payerCompany = companiesMap.get(payerId);
                    if (payerCompany == null) {
                        payerCompany = dbServiceSQL.getById(CompanySql.class, payerId);
                        companiesMap.put(payerId, payerCompany);
                    }

                    int delta = 0;
                    if (otherCompanyId == getCompanyId() && payerId != getCompanyId()) {
                        delta = -1;
                    } else if (otherCompanyId != getCompanyId() && payerId == getCompanyId()) {
                        delta = 1;
                        payerId = otherCompanyId;
                        payerCompany = otherCompany;
                    }

                    if (delta != 0) {
                        CompanyAccount companyAccount = otherAccounts.get(payerId);
                        if (companyAccount == null) {
                            companyAccount = new CompanyAccount(payerId, payerCompany.getName(), delta);
                            otherAccounts.put(payerId, companyAccount);
                        } else {
                            if (delta < 0 || company.getExCompanies() != ExCompanyType.COMPANY) {
                                companyAccount.setAccounts(companyAccount.getAccounts() + delta);
                            }
                        }
                    }
                }
            }
        }

        int payerAccounts = 0;
        for (CompanyAccount companyAccount : otherAccounts.values()) {
            payerAccounts += companyAccount.getAccounts();
        }
        company.setActiveAccounts(activeAccounts);
        company.setPayerAccounts(payerAccounts);
        company.setOtherAccounts(otherAccounts);

        company.setCurrentTotal(accountingService.companyAmountToPay(company));

        accountingService.setConnections(company, date);
    }

    private List<String> getEmployeeEmails(EntityManager entityManager, List<Long> compIds) {
        return entityManager.createQuery(
                "SELECT " + EmployeeSql_.EMAIL +
                        " FROM " + EmployeeSql.class.getName() + " a" +
                        " WHERE " + EmployeeSql_.ACTIVE + " IS TRUE" +
                        " AND (a.archive IS null OR a.archive = false)" +
                        " AND companyId IN :companyIds",
                        String.class)
                .setParameter("companyIds", compIds)
                .getResultList();
    }


    @Override
    public String toString() {
        return "date=" + date +
                ' ' + super.toString();
    }
}
