package lt.gama.model.i;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.gl.GLMoneyAccount;

import java.util.Map;
import java.util.Set;

/**
 * Gama
 * Created by valdas on 15-05-11.
 */
public interface IMoneyAccount<T> {

    GLMoneyAccount getMoneyAccount();

    void setMoneyAccount(GLMoneyAccount moneyAccount);

    void updateRemainder(GamaMoney amount);

    Map<String, GamaMoney> getRemainder();

    Set<String> getUsedCurrencies();

    void setUsedCurrencies(Set<String> usedCurrencies);

    T doc();
}
