package lt.gama.model.type.gl;

import jakarta.persistence.Embeddable;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-05-11.*
 */
@Embeddable
public class GLMoneyAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<GLCurrencyAccount> accounts;


    public GLMoneyAccount() {
    }

    public GLMoneyAccount(List<GLCurrencyAccount> accounts) {
        this.accounts = accounts;
    }

    public GLCurrencyAccount getGLAccount(String currency) {
        if (CollectionsHelper.isEmpty(accounts)) return null;
        GLCurrencyAccount noCurrency = null;
        for (GLCurrencyAccount account : accounts) {
            if (StringHelper.isEmpty(currency) && StringHelper.isEmpty(account.getCurrency())) return account;
            if (StringHelper.hasValue(currency) && currency.equalsIgnoreCase(account.getCurrency())) return account;
            if (StringHelper.isEmpty(account.getCurrency())) noCurrency = account;
        }
        return noCurrency;
    }

    // generated

    public List<GLCurrencyAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<GLCurrencyAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLMoneyAccount that = (GLMoneyAccount) o;
        return Objects.equals(accounts, that.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accounts);
    }

    @Override
    public String toString() {
        return "GLMoneyAccount{" +
                "accounts=" + accounts +
                '}';
    }
}
