package lt.gama.model.sql.documents;

import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.sql.base.BaseMoneyDocumentSql;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.type.enums.AccountType;


@Entity
@Table(name = "bank_operation")
@NamedEntityGraph(name = BankOperationSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(BankOperationSql_.BANK_ACCOUNT),
        @NamedAttributeNode(BankOperationSql_.BANK_ACCOUNT2),
        @NamedAttributeNode(BankOperationSql_.COUNTERPARTY),
        @NamedAttributeNode(BankOperationSql_.EMPLOYEE),
})
public class BankOperationSql extends BaseMoneyDocumentSql {

    public static final String GRAPH_ALL = "graph.BankOperationSql.all";

    /**
     * Bank account
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_account_id")
    private BankAccountSql bankAccount;

    /**
     * Secondary bank account
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_account2_id")
    private BankAccountSql bankAccount2;

    /**
     * Counterparty or employee account number
     */
    private String otherAccount;

    private Boolean cashOperation;

    private Boolean paymentCode;


    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() && isFinishedMoneyAll();
    }

    public boolean isFinishedMoneyAll() {
        return isFinishedMoneyType(AccountType.BANK) &&
                (BooleanUtils.isTrue(getNoDebt()) || getEmployee() == null || isFinishedMoneyType(AccountType.EMPLOYEE));
    }

    // generated

    @Override
    public BankAccountSql getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountSql bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public BankAccountSql getBankAccount2() {
        return bankAccount2;
    }

    public void setBankAccount2(BankAccountSql bankAccount2) {
        this.bankAccount2 = bankAccount2;
    }

    public String getOtherAccount() {
        return otherAccount;
    }

    public void setOtherAccount(String otherAccount) {
        this.otherAccount = otherAccount;
    }

    public Boolean getCashOperation() {
        return cashOperation;
    }

    public void setCashOperation(Boolean cashOperation) {
        this.cashOperation = cashOperation;
    }

    public Boolean getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(Boolean paymentCode) {
        this.paymentCode = paymentCode;
    }

    @Override
    public String toString() {
        return "BankOperationSql{" +
                "bankAccount=" + bankAccount +
                ", bankAccount2=" + bankAccount2 +
                ", otherAccount='" + otherAccount + '\'' +
                ", cashOperation=" + cashOperation +
                ", paymentCode=" + paymentCode +
                "} " + super.toString();
    }
}
