package lt.gama.model.dto.documents.items;

import lt.gama.model.dto.base.BaseMoneyBalanceDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;

public class DebtBalanceDto extends BaseMoneyBalanceDto implements ISortOrder {

    private Double sortOrder;

    private CounterpartyDto counterparty;

    private DebtType type;

    public DebtBalanceDto() {
    }

    public DebtBalanceDto(CounterpartyDto counterparty, DebtType type, GamaMoney amount) {
        this.counterparty = counterparty;
        this.type = type;
        this.setAmount(amount);
    }

    public DebtBalanceDto(CounterpartyDto counterparty, DebtType type, GamaMoney amount, GamaMoney baseFixAmount) {
        super(amount, baseFixAmount);
        this.counterparty = counterparty;
        this.type = type;
    }

    public DebtBalanceDto(CounterpartyDto counterparty, DebtType type, GamaMoney amount, GamaMoney baseAmount, GamaMoney baseFixAmount) {
        super(amount, baseAmount, baseFixAmount);
        this.counterparty = counterparty;
        this.type = type;
    }

    @Override
    public Long getAccountId() {
        return counterparty != null ? counterparty.getId() : null;
    }

    @Override
    public String getAccountName() {
        return counterparty != null ? counterparty.getName() : null;
    }

    // generated

    @Override
    public Double getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public CounterpartyDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyDto counterparty) {
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
        return "DebtBalanceDto{" +
                "sortOrder=" + sortOrder +
                ", counterparty=" + counterparty +
                ", type=" + type +
                "} " + super.toString();
    }
}
