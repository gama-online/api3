package lt.gama.model.dto.system;

import lt.gama.model.dto.base.BaseEntityDto;
import lt.gama.model.type.Exchange;

import java.time.LocalDate;
import java.util.Objects;

public class ExchangeRateDto extends BaseEntityDto {

    /**
     * Exchange type, can be 'LT' only at this moment
     */
    private String type;

    /**
     * ISO 4217 Currency code
     */
    private String currency; // currency code

    /**
     * Exchange rate date
     */
    private LocalDate date;

    private Exchange exchange;

    private ExchangeRateDto() {
    }

    public ExchangeRateDto(String type, String currency, LocalDate date, Exchange exchange) {
        this.type = type;
        this.currency = currency;
        this.date = date;
        this.exchange = exchange;
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

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExchangeRateDto that = (ExchangeRateDto) o;
        return Objects.equals(type, that.type) && Objects.equals(currency, that.currency) && Objects.equals(date, that.date) && Objects.equals(exchange, that.exchange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, currency, date, exchange);
    }

    @Override
    public String toString() {
        return "ExchangeRateDto{" +
                "type='" + type + '\'' +
                ", currency='" + currency + '\'' +
                ", date=" + date +
                ", exchange=" + exchange +
                "} " + super.toString();
    }
}
