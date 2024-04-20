package lt.gama.model.sql.base;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.i.IMoneyAccount;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.gl.GLCurrencyAccount;
import lt.gama.model.type.gl.GLMoneyAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

/**
 * Gama
 * Created by valdas on 15-08-14.
 */
@MappedSuperclass
public abstract class BaseMoneyAccountSql<E> extends BaseCompanySql implements IMoneyAccount<E> {

    @Embedded
    private GLMoneyAccount moneyAccount;

    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> usedCurrencies = new HashSet<>();

    /**
     *  remainders map by currency
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, GamaMoney> remainder = new HashMap<>();

    @Override
    public void setMoneyAccount(GLMoneyAccount moneyAccount) {
        this.moneyAccount = moneyAccount;
        if (moneyAccount != null && moneyAccount.getAccounts() != null) {
            for (GLCurrencyAccount currencyGLAccount : moneyAccount.getAccounts()) {
                if (currencyGLAccount.getCurrency() != null)
                    currencyGLAccount.setCurrency((currencyGLAccount.getCurrency().toUpperCase()));
            }
        }
    }

    /**
     * Return as array to frontend
     * @return map as array
     */
    public Collection<GamaMoney> getRemainders() {
        return remainder == null ? null : remainder.values();
    }

    /**
     * Do nothing - need for endpoint serialization
     * @param remainders - none
     */
    private void setRemainders(Collection<GamaMoney> remainders) {}

    public boolean isEmpty() {
        if (remainder != null) {
            for (GamaMoney money : getRemainders()) {
                if (GamaMoneyUtils.isNonZero(money)) return false;
            }
        }
        return true;
    }

    /**
     * Do nothing - need for endpoint serialization
     * @param empty - none
     */
    private void setEmpty(boolean empty) {}

    @Override
    public void updateRemainder(GamaMoney amount) {
        if (GamaMoneyUtils.isZero(amount)) return;
        String currency = amount.getCurrency();
        // remember currency
        if (getUsedCurrencies() == null) setUsedCurrencies(new HashSet<>());
        getUsedCurrencies().add(currency);
        // update remainder
        if (getRemainder() == null) {
            setRemainder(new HashMap<>());
            getRemainder().put(currency, amount);
        } else {
            amount = GamaMoneyUtils.add(getRemainder().get(currency), amount);
            getRemainder().remove(currency);
            if (GamaMoneyUtils.isNonZero(amount)) {
                getRemainder().put(currency, amount);
            }
        }
    }

    public void reset() {
        remainder = null;
        usedCurrencies = null;
    }

    // generated

    @Override
    public GLMoneyAccount getMoneyAccount() {
        return moneyAccount;
    }

    @Override
    public Set<String> getUsedCurrencies() {
        return usedCurrencies;
    }

    @Override
    public void setUsedCurrencies(Set<String> usedCurrencies) {
        this.usedCurrencies = usedCurrencies;
    }

    @Override
    public Map<String, GamaMoney> getRemainder() {
        return remainder;
    }

    public void setRemainder(Map<String, GamaMoney> remainder) {
        this.remainder = remainder;
    }

    @Override
    public String toString() {
        return "BaseMoneyAccountSql{" +
                "moneyAccount=" + moneyAccount +
                ", usedCurrencies=" + usedCurrencies +
                ", remainder=" + remainder +
                "} " + super.toString();
    }
}
