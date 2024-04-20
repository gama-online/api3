package lt.gama.report;

import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.enums.DebtType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gama
 * Created by valdas on 15-07-30.
 */
public class RepDebtDetail {

    private String currency;

    private DebtType type;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Map<String, RepDebtDetailCurrency> debt;

    private DocCounterparty counterparty;

    private Set<String> usedCurrencies = new HashSet<>();

    // generated

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
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

    public Map<String, RepDebtDetailCurrency> getDebt() {
        return debt;
    }

    public void setDebt(Map<String, RepDebtDetailCurrency> debt) {
        this.debt = debt;
    }

    public DocCounterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(DocCounterparty counterparty) {
        this.counterparty = counterparty;
    }

    public Set<String> getUsedCurrencies() {
        return usedCurrencies;
    }

    public void setUsedCurrencies(Set<String> usedCurrencies) {
        this.usedCurrencies = usedCurrencies;
    }

    @Override
    public String toString() {
        return "RepDebtDetail{" +
                "currency='" + currency + '\'' +
                ", type=" + type +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", debt=" + debt +
                ", counterparty=" + counterparty +
                ", usedCurrencies=" + usedCurrencies +
                '}';
    }
}
