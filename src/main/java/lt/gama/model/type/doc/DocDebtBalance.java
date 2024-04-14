package lt.gama.model.type.doc;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseMoneyBalance;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;

/**
 * Gama
 * Created by valdas on 15-05-14.
 */
public class DocDebtBalance extends BaseMoneyBalance {

    private DocCounterparty counterparty;

    private DebtType type;


    public DocDebtBalance() {
    }

    public DocDebtBalance(DocCounterparty counterparty, DebtType type, GamaMoney sum) {
        this.counterparty = counterparty;
        this.type = type;
        this.setSum(sum);
    }

    public DocDebtBalance(DocCounterparty counterparty, DebtType type, GamaMoney sum, GamaMoney baseSum, GamaMoney baseFixSum) {
        this.counterparty = counterparty;
        this.type = type;
        this.setSum(sum);
        this.setBaseSum(baseSum);
        this.setBaseFixSum(baseFixSum);
    }

    @Override
    public Long getAccountId() {
        return counterparty != null ? counterparty.getId() : null;
    }

    @Override
    public String getAccountName() {
        return counterparty != null ? counterparty.getName() : null;
    }

    @Override
    public DBType getAccountDb() {
        return counterparty != null ? counterparty.getDb() : null;
    }

    // generated

    @Override
    public DocCounterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(DocCounterparty counterparty) {
        this.counterparty = counterparty;
    }

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DocDebtBalance{" +
                "counterparty=" + counterparty +
                ", type=" + type +
                "} " + super.toString();
    }
}
