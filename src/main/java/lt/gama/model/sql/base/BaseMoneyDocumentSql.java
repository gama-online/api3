package lt.gama.model.sql.base;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.i.*;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.AccountType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
public abstract class BaseMoneyDocumentSql extends BaseDebtDocumentSql implements IMoneyDocument, IDebtNoDebt, IExchangeAmount {

    @Embedded
    private GamaMoney amount;

    @Embedded
    private GamaMoney baseAmount;

    /**
     * is finished by {@link AccountType}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Boolean> finishedMoney;

    private Boolean noDebt;

    @Override
    public ICash getCash() {
        return null;
    }

    @Override
    public IBankAccount getBankAccount() {
        return null;
    }

    @Override
    public IBankAccount getBankAccount2() {
        return null;
    }

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
        return "BaseMoneyDocumentSql{" +
                "amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", finishedMoney=" + finishedMoney +
                ", noDebt=" + noDebt +
                "} " + super.toString();
    }
}
