package lt.gama.model.type.auth;

import jakarta.persistence.Transient;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import lt.gama.model.type.part.VATRate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-04-20.
 */
public class VATRatesDate implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Valid date from (until next valid date)
     */
    private LocalDate date;

    private List<VATRate> rates;

    @Transient
    private Map<String, VATRate> ratesMap = null;

    public Map<String, VATRate> getRatesMap() {
        if (ratesMap == null && CollectionsHelper.hasValue(getRates())) {
            ratesMap = new HashMap<>();
            for (VATRate rate : getRates()) {
                if (StringHelper.hasValue(rate.getCode())) ratesMap.put(rate.getCode(), rate);
            }
        }
        return ratesMap;
    }

    // generated

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<VATRate> getRates() {
        return rates;
    }

    public void setRates(List<VATRate> rates) {
        this.rates = rates;
    }

    public void setRatesMap(Map<String, VATRate> ratesMap) {
        this.ratesMap = ratesMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VATRatesDate that = (VATRatesDate) o;
        return Objects.equals(date, that.date) && Objects.equals(rates, that.rates) && Objects.equals(ratesMap, that.ratesMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, rates, ratesMap);
    }

    @Override
    public String toString() {
        return "VATRatesDate{" +
                "date=" + date +
                ", rates=" + rates +
                ", ratesMap=" + ratesMap +
                '}';
    }
}
