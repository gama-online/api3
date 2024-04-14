package lt.gama.model.type.base;

import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.i.IFinished;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.doc.DocCash;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.type.enums.DBType;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-05-11.
 */
public abstract class BaseMoneyBalance implements IFinished, IExchangeAmount, Serializable {

    private Exchange exchange;

    private GamaMoney sum;

    private GamaMoney baseSum;

    private GamaMoney baseFixSum;

    private Boolean finished;

    protected BaseMoneyBalance() {
    }

    public BaseMoneyBalance(GamaMoney sum, GamaMoney baseSum, GamaMoney baseFixSum) {
        this.sum = sum;
        this.baseSum = baseSum;
        this.baseFixSum = baseFixSum;
    }

    public abstract Long getAccountId();

    public abstract DBType getAccountDb();

    public abstract String getAccountName();


    public DocEmployee getEmployee() { return null; }

    public DocCounterparty getCounterparty() { return null; }

    public DocCash getCash() { return null; }

    public DocBankAccount getAccount() { return null; }


    // for compatibility with Dto and Sql:

    public GamaMoney getAmount() {
        return getSum();
    }

    public void setAmount(GamaMoney amount) {
        setSum(amount);
    }

    public GamaMoney getBaseAmount() {
        return getBaseSum();
    }

    public void setBaseAmount(GamaMoney amount) {
        setBaseSum(amount);
    }

    public GamaMoney getBaseFixAmount() {
        return getBaseFixSum();
    }

    public void setBaseFixAmount(GamaMoney amount) {
        setBaseFixSum(amount);
    }

    // generated

    @Override
    public Exchange getExchange() {
        return exchange;
    }

    @Override
    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public GamaMoney getSum() {
        return sum;
    }

    public void setSum(GamaMoney sum) {
        this.sum = sum;
    }

    public GamaMoney getBaseSum() {
        return baseSum;
    }

    public void setBaseSum(GamaMoney baseSum) {
        this.baseSum = baseSum;
    }

    public GamaMoney getBaseFixSum() {
        return baseFixSum;
    }

    public void setBaseFixSum(GamaMoney baseFixSum) {
        this.baseFixSum = baseFixSum;
    }

    @Override
    public Boolean getFinished() {
        return finished;
    }

    @Override
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "BaseMoneyBalance{" +
                "exchange=" + exchange +
                ", sum=" + sum +
                ", baseSum=" + baseSum +
                ", baseFixSum=" + baseFixSum +
                ", finished=" + finished +
                '}';
    }
}
