package lt.gama.model.type.doc;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseMoneyBalance;
import lt.gama.model.type.enums.DBType;

/**
 * Gama
 * Created by valdas on 15-05-11.
 */
public class DocCashBalance extends BaseMoneyBalance {

    private DocCash cash;


    public DocCashBalance() {
    }

    public DocCashBalance(DocCash cash, GamaMoney sum) {
        this.cash = cash;
        this.setSum(sum);
    }

    public DocCashBalance(DocCash cash, GamaMoney sum, GamaMoney baseSum, GamaMoney baseFixSum) {
        super(sum, baseSum, baseFixSum);
        this.cash = cash;
    }

    @Override
    public Long getAccountId() {
        return cash != null ? cash.getId() : null;
    }

    @Override
    public String getAccountName() {
        return cash != null ? cash.getName() : null;
    }

    @Override
    public DBType getAccountDb() {
        return cash != null ? cash.getDb() : null;
    }

    // generated

    @Override
    public DocCash getCash() {
        return cash;
    }

    public void setCash(DocCash cash) {
        this.cash = cash;
    }

    @Override
    public String toString() {
        return "DocCashBalance{" +
                "cash=" + cash +
                "} " + super.toString();
    }
}
