package lt.gama.model.sql.system.id;

import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ExchangeRateId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Exchange type, can be 'LT' only at this moment
     */
    private String type;

    /**
     * ISO 4217 Currency code
     */
    private String currency;

    /**
     * Exchange rate date
     */
    private LocalDate date;


    protected ExchangeRateId() {
    }

    public ExchangeRateId(String type, String currency, LocalDate date) {
        this.type = type;
        this.currency = currency;
        this.date = date;
    }

    // generated

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRateId that = (ExchangeRateId) o;
        return Objects.equals(type, that.type) && Objects.equals(currency, that.currency) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, currency, date);
    }

    @Override
    public String toString() {
        return "ExchangeRateId{" +
                "type='" + type + '\'' +
                ", currency='" + currency + '\'' +
                ", date=" + date +
                '}';
    }
}
