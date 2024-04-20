package lt.gama.report;

import lt.gama.model.type.enums.AccountType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * gama-online
 * Created by valdas on 2016-04-30.
 */
public class RepMoneyDetail<T> {

    private AccountType type;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Map<String, RepMoneyDetailCurrency> amount;

    private T account;

    private Set<String> usedCurrencies = new HashSet<>();

    // generated

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public Map<String, RepMoneyDetailCurrency> getAmount() {
        return amount;
    }

    public void setAmount(Map<String, RepMoneyDetailCurrency> amount) {
        this.amount = amount;
    }

    public T getAccount() {
        return account;
    }

    public void setAccount(T account) {
        this.account = account;
    }

    public Set<String> getUsedCurrencies() {
        return usedCurrencies;
    }

    public void setUsedCurrencies(Set<String> usedCurrencies) {
        this.usedCurrencies = usedCurrencies;
    }

    @Override
    public String toString() {
        return "RepMoneyDetail{" +
                "type=" + type +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", amount=" + amount +
                ", account=" + account +
                ", usedCurrencies=" + usedCurrencies +
                '}';
    }
}
