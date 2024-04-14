package lt.gama.model.dto.base;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.i.IBankAccount;
import lt.gama.model.i.ICash;
import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.i.IMoneyDocument;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.AccountType;

import java.util.HashMap;
import java.util.Map;

/**
 * Gama
 * Created by valdas on 15-08-14.
 */
public abstract class BaseMoneyDocumentDto extends BaseDebtDocumentDto implements IMoneyDocument, IExchangeAmount {

    private GamaMoney amount;

    private GamaMoney baseAmount;

    /**
     * is finished by {@link AccountType}
     */
    private Map<String, Boolean> finishedMoney;

    private Boolean noDebt;


    @Override
    public ICash getCash() { return null; }

    @Override
    public IBankAccount getBankAccount() { return null; }

    @Override
    public IBankAccount getBankAccount2() { return null; }

    @Override
    public GamaMoney getDebt() {
        return GamaMoneyUtils.negated(amount);
    }

    @Override
    public GamaMoney getBaseDebt() {
        return GamaMoneyUtils.negated(baseAmount);
    }

    public String getCurrency() {
        return amount == null ? null : amount.getCurrency();
    }

    @Override
    public void setFinishedMoneyType(AccountType accountType, boolean finished) {
        if (!finished) {
            if (getFinishedMoney() == null) return;
            getFinishedMoney().remove(accountType.toString());
        } else {
            if (getFinishedMoney() == null) setFinishedMoney(new HashMap<>());
            getFinishedMoney().put(accountType.toString(), true);
        }
    }

    @Override
    public boolean isFinishedMoneyType(AccountType accountType) {
        if (getFinishedMoney() == null) return false;
        Boolean finished = getFinishedMoney().get(accountType.toString());
        return finished != null && finished;
    }

    @Override
    public void reset() {
        super.reset();
        finishedMoney = null;
    }

    // for import compatibility

    public void setSum(GamaMoney amount) {
        this.amount = amount;
    }

    public void setBaseSum(GamaMoney baseAmount) {
        this.baseAmount = baseAmount;
    }

    // generated

    @Override
    public GamaMoney getAmount() {
        return amount;
    }

    @Override
    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    @Override
    public GamaMoney getBaseAmount() {
        return baseAmount;
    }

    @Override
    public void setBaseAmount(GamaMoney baseAmount) {
        this.baseAmount = baseAmount;
    }

    public Map<String, Boolean> getFinishedMoney() {
        return finishedMoney;
    }

    public void setFinishedMoney(Map<String, Boolean> finishedMoney) {
        this.finishedMoney = finishedMoney;
    }

    @Override
    public Boolean getNoDebt() {
        return noDebt;
    }

    public void setNoDebt(Boolean noDebt) {
        this.noDebt = noDebt;
    }

    @Override
    public String toString() {
        return "BaseMoneyDocumentDto{" +
                "amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", finishedMoney=" + finishedMoney +
                ", noDebt=" + noDebt +
                "} " + super.toString();
    }
}
