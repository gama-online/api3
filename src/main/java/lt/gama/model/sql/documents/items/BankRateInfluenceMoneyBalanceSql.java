package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.BankRateInfluenceSql;
import lt.gama.model.sql.entities.BankAccountSql;


@Entity
@Table(name = "bank_rate_influences_bank")
public class BankRateInfluenceMoneyBalanceSql extends BaseMoneyBalanceSql {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("accounts")
    private BankRateInfluenceSql parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_account_id")
    private BankAccountSql bankAccount;

    private Double sortOrder;

    @Override
    public Long getAccountId() {
        return bankAccount != null ? bankAccount.getId() : null;
    }

    @Override
    public String getAccountName() {
        return bankAccount != null ? bankAccount.getName() : null;
    }

    /**
     * toString except parent
     */
    @Override
    public String toString() {
        return "BankRateInfluenceMoneyBalanceSql{" +
                ", bankAccount=" + bankAccount +
                ", sortOrder=" + sortOrder +
                "} " + super.toString();
    }

    // generated

    public BankRateInfluenceSql getParent() {
        return parent;
    }

    public void setParent(BankRateInfluenceSql parent) {
        this.parent = parent;
    }

    @Override
    public BankAccountSql getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountSql bankAccount) {
        this.bankAccount = bankAccount;
    }

    public Double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }
}
