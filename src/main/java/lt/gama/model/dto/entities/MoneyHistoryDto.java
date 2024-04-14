package lt.gama.model.dto.entities;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IDoc;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.AccountType;

import java.io.Serial;

public class MoneyHistoryDto extends BaseCompanyDto implements IDoc {

    @Serial
    private static final long serialVersionUID = -1L;

    private AccountType accountType;

    private long accountId;

    private Doc doc;

    private CounterpartyDto counterparty;

    private EmployeeDto employee;

    private CashDto cash;

    private BankAccountDto bankAccount;

    private BankAccountDto bankAccount2;

    private Exchange exchange;

    private GamaMoney amount;

    private GamaMoney baseAmount;

    /**
     * Used in frontend only
     */
    private GamaMoney remainder;

    public String getCurrency() {
        return amount != null ? amount.getCurrency() : exchange != null ? exchange.getCurrency() : null;
    }

    @SuppressWarnings("unused") // used in advance-stmt.ftl report
    public GamaMoney getSumDebit() {
        return GamaMoneyUtils.isPositive(amount) ? amount : null;
    }

    @SuppressWarnings("unused") // used in advance-stmt.ftl report
    public GamaMoney getSumCredit() {
        return GamaMoneyUtils.isNegative(amount) ? amount.negated() : null;
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

    public CounterpartyDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyDto counterparty) {
        this.counterparty = counterparty;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public CashDto getCash() {
        return cash;
    }

    public void setCash(CashDto cash) {
        this.cash = cash;
    }

    public BankAccountDto getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDto bankAccount) {
        this.bankAccount = bankAccount;
    }

    public BankAccountDto getBankAccount2() {
        return bankAccount2;
    }

    public void setBankAccount2(BankAccountDto bankAccount2) {
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

    @Override
    public String toString() {
        return "MoneyHistoryDto{" +
                "accountType=" + accountType +
                ", accountId=" + accountId +
                ", doc=" + doc +
                ", counterparty=" + counterparty +
                ", employee=" + employee +
                ", cash=" + cash +
                ", bankAccount=" + bankAccount +
                ", bankAccount2=" + bankAccount2 +
                ", exchange=" + exchange +
                ", amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", remainder=" + remainder +
                "} " + super.toString();
    }
}

