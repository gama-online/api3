package lt.gama.model.dto.documents.items;

import lt.gama.model.dto.base.BaseMoneyBalanceDto;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.type.GamaMoney;

public class CashBalanceDto extends BaseMoneyBalanceDto implements ISortOrder {

    private Double sortOrder;

    private CashDto cash;


    public CashBalanceDto() {
    }

    public CashBalanceDto(CashDto cash, GamaMoney amount) {
        this.cash = cash;
        this.setAmount(amount);
    }

    public CashBalanceDto(CashDto cash, GamaMoney amount, GamaMoney baseAmount, GamaMoney baseFixAmount) {
        super(amount, baseAmount, baseFixAmount);
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
    public CashDto getCash() {
        return cash;
    }

    public void setCash(CashDto cash) {
        this.cash = cash;
    }

    @Override
    public String toString() {
        return "CashBalanceDto{" +
                "sortOrder=" + sortOrder +
                ", cash=" + cash +
                "} " + super.toString();
    }
}
