package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.model.i.IDoc;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.AccountType;


@Entity
@Table(name = "money_history")
@NamedEntityGraph(name = MoneyHistorySql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(MoneyHistorySql_.COUNTERPARTY),
        @NamedAttributeNode(MoneyHistorySql_.CASH),
        @NamedAttributeNode(MoneyHistorySql_.BANK_ACCOUNT),
        @NamedAttributeNode(MoneyHistorySql_.BANK_ACCOUNT2),
})
public class MoneyHistorySql extends BaseCompanySql implements IDoc {

    public static final String GRAPH_ALL = "graph.MoneyHistorySql.all";

    private AccountType accountType;

    private long accountId;

    @Embedded
    private Doc doc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeSql employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_id")
    private CashSql cash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccountSql bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account2_id")
    private BankAccountSql bankAccount2;

    @Embedded
    private Exchange exchange;

    @Embedded
    private GamaMoney amount;

    @Embedded
    private GamaMoney baseAmount;

    /**
     * Used in frontend only
     */
    @Transient
    private GamaMoney remainder;


    public String getCurrency() {
        return amount != null ? amount.getCurrency() : exchange != null ? exchange.getCurrency() : null;
    }

    /**
     * toString except counterparty, employee, cash, bankAccount, bankAccount2
     */
    @Override
    public String toString() {
        return "MoneyHistorySql{" +
                "accountType=" + accountType +
                ", accountId=" + accountId +
                ", doc=" + doc +
                ", exchange=" + exchange +
                ", amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", remainder=" + remainder +
                "} " + super.toString();
    }

    // generated

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public Doc getDoc() {
        return doc;
    }

    @Override
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public CounterpartySql getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartySql counterparty) {
        this.counterparty = counterparty;
    }

    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
        this.employee = employee;
    }

    public CashSql getCash() {
        return cash;
    }

    public void setCash(CashSql cash) {
        this.cash = cash;
    }

    public BankAccountSql getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountSql bankAccount) {
        this.bankAccount = bankAccount;
    }

    public BankAccountSql getBankAccount2() {
        return bankAccount2;
    }

    public void setBankAccount2(BankAccountSql bankAccount2) {
        this.bankAccount2 = bankAccount2;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public GamaMoney getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(GamaMoney baseAmount) {
        this.baseAmount = baseAmount;
    }

    public GamaMoney getRemainder() {
        return remainder;
    }

    public void setRemainder(GamaMoney remainder) {
        this.remainder = remainder;
    }
}
