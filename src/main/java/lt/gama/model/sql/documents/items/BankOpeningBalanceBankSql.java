package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.BankOpeningBalanceSql;
import lt.gama.model.sql.entities.BankAccountSql;


@Entity
@Table(name = "bank_ob_balances")
public class BankOpeningBalanceBankSql extends BaseMoneyBalanceSql implements ISortOrder {

    private Double sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("bankAccounts")
    private BankOpeningBalanceSql parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_account_id")
    private BankAccountSql bankAccount;

    @Override
    public Long getAccountId() {
        return bankAccount != null ? bankAccount.getId() : null;
    }

    @Override
    public String getAccountName() {
        return bankAccount != null ? bankAccount.getAccount() : null;
    }

    /**
     * toString except parent
     */
    @Override
    public String toString() {
        return "BankOpeningBalanceBankSql{" +
                "sortOrder=" + sortOrder +
                ", bankAccount=" + bankAccount +
                "} " + super.toString();
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

    public BankOpeningBalanceSql getParent() {
        return parent;
    }

    public void setParent(BankOpeningBalanceSql parent) {
        this.parent = parent;
    }

    @Override
    public BankAccountSql getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountSql bankAccount) {
        this.bankAccount = bankAccount;
    }
}
