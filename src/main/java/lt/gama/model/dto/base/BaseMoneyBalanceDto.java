package lt.gama.model.dto.base;

import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.i.IFinished;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;

public abstract class BaseMoneyBalanceDto extends BaseCompanyDto implements IFinished, IExchangeAmount {

    private Long id;

    private Exchange exchange;

    private GamaMoney amount;

    private GamaMoney baseAmount;

    private GamaMoney baseFixAmount;

    private Boolean finished;

    protected BaseMoneyBalanceDto() {
    }

    public BaseMoneyBalanceDto(GamaMoney amount, GamaMoney baseFixAmount) {
        this.amount = amount;
        this.baseFixAmount = baseFixAmount;
    }

    public BaseMoneyBalanceDto(GamaMoney amount, GamaMoney baseAmount, GamaMoney baseFixAmount) {
        this.amount = amount;
        this.baseAmount = baseAmount;
        this.baseFixAmount = baseFixAmount;
    }

    public abstract Long getAccountId();

    public abstract String getAccountName();


    public EmployeeDto getEmployee() { return null; }

    public CounterpartyDto getCounterparty() { return null; }

    public CashDto getCash() { return null; }

    public BankAccountDto getBankAccount() { return null; }

    // for import compatibility

    public void setSum(GamaMoney sum) {
        setAmount(sum);
    }

    public void setBaseSum(GamaMoney baseSum) {
        setBaseAmount(baseSum);
    }

    public void setBaseFixSum(GamaMoney baseFixSum) {
        setBaseFixAmount(baseFixSum);
    }

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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
        return "BaseMoneyBalanceDto{" +
                "id=" + id +
                ", exchange=" + exchange +
                ", amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", baseFixAmount=" + baseFixAmount +
                ", finished=" + finished +
                "} " + super.toString();
    }
}
