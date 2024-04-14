package lt.gama.model.dto.base;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.i.IMoneyAccount;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.gl.GLCurrencyAccount;
import lt.gama.model.type.gl.GLMoneyAccount;

import java.util.*;

/**
 * Gama
 * Created by valdas on 15-08-14.
 */
public abstract class BaseMoneyAccountDocumentDto<E> extends BaseCompanyDto implements IMoneyAccount<E> {

    private GLMoneyAccount moneyAccount;

    private Set<String> usedCurrencies = new HashSet<>();

    /**
     *  remainders map by currency
     */
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
            if (GamaMoneyUtils.isZero(amount)) {
                getRemainder().remove(currency);
            } else {
                getRemainder().put(currency, amount);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
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
        return "BaseMoneyAccountDocumentDto{" +
                "moneyAccount=" + moneyAccount +
                ", usedCurrencies=" + usedCurrencies +
                ", remainder=" + remainder +
                "} " + super.toString();
    }
}
