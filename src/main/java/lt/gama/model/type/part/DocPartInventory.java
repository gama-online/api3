package lt.gama.model.type.part;

import jakarta.persistence.Transient;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.model.i.IFinished;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.ibase.IBaseDocPart;
import lt.gama.model.type.ibase.IBaseDocPartCost;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DocPartInventory extends BaseDocPart implements IBaseDocPartCost, IBaseDocPart, IFinished {

    @Serial
    private static final long serialVersionUID = -1L;

    private Double sortOrder;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private DocWarehouse warehouse;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    /**
     * Total cost in base currency.
     * p.s. information can be filled later if part can be allowed to sell without knowing cost at the moment of sell.
     * In this case the remainder will be negative and must be compensated later.
     */
    private GamaMoney costTotal;

    /**
     * Cost source info list, i.e. list of purchase/production docs info: id, document date and number, quantity and cost.
     * Information filled in the time of sale, i.e. at finishing invoice document.
     * The information here can be used for the reconstruction of inventory records (purchase, production or others)
     * at finishing returning invoice document (i.e. invoice with negative quantities)
     * which mus be linked through {@link DocPartInvoice#getDocReturn docReturn} to this document.
     */
    private List<PartCostSource> costInfo;

    /**
     * if change = true then
     *  1) quantityRemainder = quantityInitial + quantity
     *  2) costTotal is set only if quantity > 0, i.e. parts are added
     * <p>
     * if change = false then
     *  1) quantityTotal is set
     *  2) quantity = quantityInitial - quantityRemainder
     */
    private Boolean change;

    private BigDecimal quantityInitial;

    private GamaMoney costInitial;

    private BigDecimal quantityRemainder;

    private GamaMoney costRemainder;

    /**
     *  Not enough quantity to finish operation - used in frontend only and is filled in invoice finishing procedure
     */
    @Transient
    private BigDecimal notEnough;


    public void reset() {
        finished = null;
        quantityInitial = null;
        costInitial = null;
        costRemainder = null;
        if (isChange()) {
            quantityRemainder = null;
            if (BigDecimalUtils.isNegative(getQuantity())) {
                setCostTotal(null);
            }
        } else {
            setQuantity(null);
        }
    }

    // customized getters/setters

    public boolean isChange() {
        return change != null && change;
    }

    // generated
    // except getChange()

    public Double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

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

    public void setChange(Boolean change) {
        this.change = change;
    }

    public BigDecimal getQuantityInitial() {
        return quantityInitial;
    }

    public void setQuantityInitial(BigDecimal quantityInitial) {
        this.quantityInitial = quantityInitial;
    }

    public GamaMoney getCostInitial() {
        return costInitial;
    }

    public void setCostInitial(GamaMoney costInitial) {
        this.costInitial = costInitial;
    }

    public BigDecimal getQuantityRemainder() {
        return quantityRemainder;
    }

    public void setQuantityRemainder(BigDecimal quantityRemainder) {
        this.quantityRemainder = quantityRemainder;
    }

    public GamaMoney getCostRemainder() {
        return costRemainder;
    }

    public void setCostRemainder(GamaMoney costRemainder) {
        this.costRemainder = costRemainder;
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
        DocPartInventory that = (DocPartInventory) o;
        return Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(finished, that.finished) && Objects.equals(costTotal, that.costTotal) && Objects.equals(costInfo, that.costInfo) && Objects.equals(change, that.change) && Objects.equals(quantityInitial, that.quantityInitial) && Objects.equals(costInitial, that.costInitial) && Objects.equals(quantityRemainder, that.quantityRemainder) && Objects.equals(costRemainder, that.costRemainder) && Objects.equals(notEnough, that.notEnough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quantity, total, baseTotal, warehouse, finished, costTotal, costInfo, change, quantityInitial, costInitial, quantityRemainder, costRemainder, notEnough);
    }

    @Override
    public String toString() {
        return "DocPartInventory{" +
                "quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", finished=" + finished +
                ", costTotal=" + costTotal +
                ", costInfo=" + costInfo +
                ", change=" + change +
                ", quantityInitial=" + quantityInitial +
                ", costInitial=" + costInitial +
                ", quantityRemainder=" + quantityRemainder +
                ", costRemainder=" + costRemainder +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}
