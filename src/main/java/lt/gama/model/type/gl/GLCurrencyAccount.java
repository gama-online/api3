package lt.gama.model.type.gl;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-04-24.
 */
public class GLCurrencyAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String currency;

    private GLOperationAccount account;

    public GLCurrencyAccount() {
    }

    public GLCurrencyAccount(String currency, GLOperationAccount account) {
        this.currency = currency;
        this.account = account;
    }

    // generated

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public GLOperationAccount getAccount() {
        return account;
    }

    public void setAccount(GLOperationAccount account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLCurrencyAccount that = (GLCurrencyAccount) o;
        return Objects.equals(currency, that.currency) && Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, account);
    }

    @Override
    public String toString() {
        return "GLCurrencyAccount{" +
                "currency='" + currency + '\'' +
                ", account=" + account +
                '}';
    }
}
