package lt.gama.model.sql.system;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.sql.system.id.ExchangeRateId;
import lt.gama.model.type.Exchange;

import java.time.LocalDate;

@Entity
@Table(name = "exchange_rate")
public class ExchangeRateSql extends BaseEntitySql implements IId<ExchangeRateId>  {

    /**
     * Synthetic key: type + currency + date
     */
    @EmbeddedId
    private ExchangeRateId id;

    @Embedded
    private Exchange exchange;


    @SuppressWarnings("unused")
    protected ExchangeRateSql() {}

    public ExchangeRateSql(String type, String currency, LocalDate date, Exchange exchange) {
        this.id = new ExchangeRateId(type, currency, date);
        this.exchange = exchange;
    }

    // generated

    @Override
    public ExchangeRateId getId() {
        return id;
    }

    @Override
    public void setId(ExchangeRateId id) {
        this.id = id;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String toString() {
        return "ExchangeRateSql{" +
                "id=" + id +
                ", exchange=" + exchange +
                "} " + super.toString();
    }
}
