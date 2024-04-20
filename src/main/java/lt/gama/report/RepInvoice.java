package lt.gama.report;

import java.math.BigDecimal;
import java.util.Map;

public class RepInvoice {
    Long id;

    String name;

    String currency;

    BigDecimal total;

    Map<String, BigDecimal> totalsByMonth;

    // generated


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Map<String, BigDecimal> getTotalsByMonth() {
        return totalsByMonth;
    }

    public void setTotalsByMonth(Map<String, BigDecimal> totalsByMonth) {
        this.totalsByMonth = totalsByMonth;
    }

    @Override
    public String toString() {
        return "RepInvoice{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", currency='" + currency + '\'' +
                ", total=" + total +
                ", totalsByMonth=" + totalsByMonth +
                '}';
    }
}
