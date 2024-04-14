package lt.gama.model.type.part;

import jakarta.persistence.Transient;
import lt.gama.model.i.IFinished;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.ibase.IBaseDocPart;
import lt.gama.model.type.ibase.IBaseDocPartCost;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-20.
 */
public class DocPartOB extends BaseDocPart implements IBaseDocPartCost, IBaseDocPart, IFinished {

    @Serial
    private static final long serialVersionUID = -1L;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private DocWarehouse warehouse;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    private GamaMoney costTotal;

    /**
     * Cost source info list, i.e. list of purchase/production docs info: id, document date and number, quantity and cost.
     * Information filled in the time of sale, i.e. at finishing invoice document.
     * The information here can be used for the reconstruction of inventory records (purchase, production or others)
     * at finishing returning invoice document (i.e. invoice with negative quantities)
     * which mus be linked through {@link DocPartInvoice#getDocReturn docReturn} to this document.
     */
    private List<PartCostSource> costInfo;

    private Exchange exchange;

    /**
     *  Not enough quantity to finish operation - used in frontend only and is filled in invoice finishing procedure
     */
    @Transient
    private BigDecimal notEnough;


    public void reset() {
        finished = null;
        costInfo = null;
    }

    // generated

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public GamaMoney getTotal() {
        return total;
    }

    @Override
    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    @Override
    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    @Override
    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    @Override
    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    @Override
    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public Boolean getFinished() {
        return finished;
    }

    @Override
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public GamaMoney getCostTotal() {
        return costTotal;
    }

    @Override
    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    @Override
    public List<PartCostSource> getCostInfo() {
        return costInfo;
    }

    @Override
    public void setCostInfo(List<PartCostSource> costInfo) {
        this.costInfo = costInfo;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public BigDecimal getNotEnough() {
        return notEnough;
    }

    @Override
    public void setNotEnough(BigDecimal notEnough) {
        this.notEnough = notEnough;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocPartOB docPartOB = (DocPartOB) o;
        return Objects.equals(quantity, docPartOB.quantity) && Objects.equals(total, docPartOB.total) && Objects.equals(baseTotal, docPartOB.baseTotal) && Objects.equals(warehouse, docPartOB.warehouse) && Objects.equals(finished, docPartOB.finished) && Objects.equals(costTotal, docPartOB.costTotal) && Objects.equals(costInfo, docPartOB.costInfo) && Objects.equals(exchange, docPartOB.exchange) && Objects.equals(notEnough, docPartOB.notEnough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quantity, total, baseTotal, warehouse, finished, costTotal, costInfo, exchange, notEnough);
    }

    @Override
    public String toString() {
        return "DocPartOB{" +
                "quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", finished=" + finished +
                ", costTotal=" + costTotal +
                ", costInfo=" + costInfo +
                ", exchange=" + exchange +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}
