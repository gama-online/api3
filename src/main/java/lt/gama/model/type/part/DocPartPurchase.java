package lt.gama.model.type.part;

import jakarta.persistence.Transient;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IFinished;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.ibase.IBaseDocPart;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-19.
 */
public class DocPartPurchase extends BaseDocPart implements IBaseDocPartCost, IBaseDocPart, IFinished {

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
     * If 'service' price must be included in cost calculations
     */
    private Boolean inCost;

    /**
     * If 'service', not included in cost calculations (inCost = false), cost can be increased by other expenses
     */
    private Boolean addExp;


    /**
     * 3'rd parties expenses amount in the cost
     */
    private GamaMoney expense;

    /**
     *  taxTotal = discountedTotal * vatRate / 100
     */
    private GamaMoney taxTotal;

    private String vendorCode;

	/**
	 * Returning doc info
	 */
	private Doc docReturn;

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


    public DocPartPurchase() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartPurchase(P part) {
        super(part);
    }

    public void reset() {
        finished = false;
    }


    // customized getters/setters

    public boolean isInCost() {
        return inCost != null && inCost;
    }

    public boolean isAddExp() {
        return addExp != null && addExp;
    }

    // generated
    // except getInCost(), getAddExp()

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

    public void setInCost(Boolean inCost) {
        this.inCost = inCost;
    }

    public void setAddExp(Boolean addExp) {
        this.addExp = addExp;
    }

    public GamaMoney getExpense() {
        return expense;
    }

    public void setExpense(GamaMoney expense) {
        this.expense = expense;
    }

    public GamaMoney getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(GamaMoney taxTotal) {
        this.taxTotal = taxTotal;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public Doc getDocReturn() {
        return docReturn;
    }

    public void setDocReturn(Doc docReturn) {
        this.docReturn = docReturn;
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
        DocPartPurchase that = (DocPartPurchase) o;
        return Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(finished, that.finished) && Objects.equals(costTotal, that.costTotal) && Objects.equals(inCost, that.inCost) && Objects.equals(addExp, that.addExp) && Objects.equals(expense, that.expense) && Objects.equals(taxTotal, that.taxTotal) && Objects.equals(vendorCode, that.vendorCode) && Objects.equals(docReturn, that.docReturn) && Objects.equals(costInfo, that.costInfo) && Objects.equals(notEnough, that.notEnough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quantity, total, baseTotal, warehouse, finished, costTotal, inCost, addExp, expense, taxTotal, vendorCode, docReturn, costInfo, notEnough);
    }

    @Override
    public String toString() {
        return "DocPartPurchase{" +
                "quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", finished=" + finished +
                ", costTotal=" + costTotal +
                ", inCost=" + inCost +
                ", addExp=" + addExp +
                ", expense=" + expense +
                ", taxTotal=" + taxTotal +
                ", vendorCode='" + vendorCode + '\'' +
                ", docReturn=" + docReturn +
                ", costInfo=" + costInfo +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}
