package lt.gama.helpers;

import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.auth.CompanyAccount;

import java.util.HashMap;

/**
 * gama-online
 * Created by valdas on 2016-07-08.
 */
public final class AccountingUtils {

    private AccountingUtils() {}

    public static void modifyConnection(CompanySql company, CompanyAccount connection) {
        if (company == null || connection == null) return;
        if (company.getOtherAccounts() == null) {
            company.setOtherAccounts(new HashMap<>());
            company.getOtherAccounts().put(connection.getId(), connection);
        } else {
            Long key = connection.getId();
            CompanyAccount conn = company.getOtherAccounts().get(key);
            if (conn == null) {
                company.getOtherAccounts().put(key, connection);
            } else {
                conn.setAccounts(conn.getAccounts() + connection.getAccounts());
                if (conn.getAccounts() == 0) company.getOtherAccounts().remove(key);
            }
        }
        company.setPayerAccounts(company.getPayerAccounts() + connection.getAccounts());
    }

}
