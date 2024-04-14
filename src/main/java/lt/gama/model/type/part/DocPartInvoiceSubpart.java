package lt.gama.model.type.part;

import jakarta.persistence.Transient;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.dto.entities.PartPartDto;
import lt.gama.model.i.IFinished;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.ibase.IBaseDocPart;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartOutRemainder;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DocPartInvoiceSubpart extends BaseDocPart implements IBaseDocPartOutRemainder, IBaseDocPartCost,
        IBaseDocPart, IFinished {

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

    /**
     * if forward sell is allowed this is remainder without cost. Must be resolved in future.
     */
    private BigDecimal remainder;

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
     *  Not enough quantity to finish operation - used in frontend only and is filled in invoice finishing procedure
     */
    @Transient
    private BigDecimal notEnough;


    public DocPartInvoiceSubpart() {
    }

    public DocPartInvoiceSubpart(PartDto part) {
        super(part);
        setForwardSell(part.isForwardSell());
    }

    public DocPartInvoiceSubpart(PartPartDto part, BigDecimal quantity) {
        super(part);
        this.quantity = quantity;
        setForwardSell(part.isForwardSell());
    }

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
    public BigDecimal getRemainder() {
        return remainder;
    }

    @Override
    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
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
        DocPartInvoiceSubpart that = (DocPartInvoiceSubpart) o;
        return Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(finished, that.finished) && Objects.equals(remainder, that.remainder) && Objects.equals(costTotal, that.costTotal) && Objects.equals(costInfo, that.costInfo) && Objects.equals(notEnough, that.notEnough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quantity, total, baseTotal, warehouse, finished, remainder, costTotal, costInfo, notEnough);
    }

    @Override
    public String toString() {
        return "DocPartInvoiceSubpart{" +
                "quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", finished=" + finished +
                ", remainder=" + remainder +
                ", costTotal=" + costTotal +
                ", costInfo=" + costInfo +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}
