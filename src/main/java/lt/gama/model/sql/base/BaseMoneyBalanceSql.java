package lt.gama.model.sql.base;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.i.IFinished;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.CashSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;


@MappedSuperclass
public abstract class BaseMoneyBalanceSql extends BaseCompanySql implements IFinished, IExchangeAmount {

    @Embedded
    private Exchange exchange;

    @Embedded
    private GamaMoney amount;

    @Embedded
    private GamaMoney baseAmount;

    @Embedded
    private GamaMoney baseFixAmount;

    private Boolean finished;


    public abstract Long getAccountId();

    public abstract String getAccountName();


    public EmployeeSql getEmployee() { return null; }

    public CounterpartySql getCounterparty() { return null; }

    public CashSql getCash() { return null; }

    public BankAccountSql getBankAccount() { return null; }

    // generated

    @Override
    public Exchange getExchange() {
        return exchange;
    }

    @Override
    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

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

    public GamaMoney getBaseFixAmount() {
        return baseFixAmount;
    }

    public void setBaseFixAmount(GamaMoney baseFixAmount) {
        this.baseFixAmount = baseFixAmount;
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
        return "BaseMoneyBalanceSql{" +
                "exchange=" + exchange +
                ", amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", baseFixAmount=" + baseFixAmount +
                ", finished=" + finished +
                "} " + super.toString();
    }
}
