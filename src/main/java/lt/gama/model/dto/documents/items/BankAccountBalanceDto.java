package lt.gama.model.dto.documents.items;

import lt.gama.model.dto.base.BaseMoneyBalanceDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.type.GamaMoney;

public class BankAccountBalanceDto extends BaseMoneyBalanceDto implements ISortOrder {

    private Double sortOrder;

    private BankAccountDto bankAccount;

    public BankAccountBalanceDto() {
    }

    public BankAccountBalanceDto(BankAccountDto account, GamaMoney amount) {
        this.bankAccount = account;
        this.setAmount(amount);
    }

    public BankAccountBalanceDto(BankAccountDto account, GamaMoney amount, GamaMoney baseAmount, GamaMoney baseFixAmount) {
        super(amount, baseAmount, baseFixAmount);
        this.bankAccount = account;
    }

    @Override
    public Long getAccountId() {
        return bankAccount != null ? bankAccount.getId() : null;
    }

    @Override
    public String getAccountName() {
        return bankAccount != null ? bankAccount.getAccount() : null;
    }


    // for import compatibility

    public void setAccount(BankAccountDto account) {
        setBankAccount(account);
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
    public BankAccountDto getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDto bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public String toString() {
        return "BankAccountBalanceDto{" +
                "sortOrder=" + sortOrder +
                ", bankAccount=" + bankAccount +
                "} " + super.toString();
    }
}
