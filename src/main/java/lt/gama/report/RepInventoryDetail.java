package lt.gama.report;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.part.DocPartBalance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Gama
 * Created by valdas on 15-08-21.
 */
public class RepInventoryDetail {

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private GamaMoney costFrom;

    private GamaMoney costTo;

    private BigDecimal quantityFrom;

    private BigDecimal quantityTo;

    private DocPartBalance part;

    private Set<DocWarehouse> usedWarehouses = new HashSet<>();

    // generated

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

    public GamaMoney getCostFrom() {
        return costFrom;
    }

    public void setCostFrom(GamaMoney costFrom) {
        this.costFrom = costFrom;
    }

    public GamaMoney getCostTo() {
        return costTo;
    }

    public void setCostTo(GamaMoney costTo) {
        this.costTo = costTo;
    }

    public BigDecimal getQuantityFrom() {
        return quantityFrom;
    }

    public void setQuantityFrom(BigDecimal quantityFrom) {
        this.quantityFrom = quantityFrom;
    }

    public BigDecimal getQuantityTo() {
        return quantityTo;
    }

    public void setQuantityTo(BigDecimal quantityTo) {
        this.quantityTo = quantityTo;
    }

    public DocPartBalance getPart() {
        return part;
    }

    public void setPart(DocPartBalance part) {
        this.part = part;
    }

    public Set<DocWarehouse> getUsedWarehouses() {
        return usedWarehouses;
    }

    public void setUsedWarehouses(Set<DocWarehouse> usedWarehouses) {
        this.usedWarehouses = usedWarehouses;
    }

    @Override
    public String toString() {
        return "RepInventoryDetail{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", costFrom=" + costFrom +
                ", costTo=" + costTo +
                ", quantityFrom=" + quantityFrom +
                ", quantityTo=" + quantityTo +
                ", part=" + part +
                ", usedWarehouses=" + usedWarehouses +
                '}';
    }
}
