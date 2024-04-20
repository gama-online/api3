package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.helpers.Validators;
import lt.gama.model.i.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartSql;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.DocPartInvoice;
import lt.gama.model.type.part.PartCostSource;
import lt.gama.model.type.part.PartSN;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_parts")
public class PurchasePartSql extends BaseCompanySql implements IBaseDocPartCost, IBaseDocPartSql, IDocPart, IPartSN,
        IUuid, IFinished, IPartMessage, ISortOrder, IQuantity {

    private Double sortOrder;

    private BigDecimal quantity;

    @Embedded
    private GamaMoney total;

    @Embedded
    private GamaMoney baseTotal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    @Embedded
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
    @Embedded
    private GamaMoney expense;

    /**
     *  taxTotal = discountedTotal * vatRate / 100
     */
    @Embedded
    private GamaMoney taxTotal;

    private String vendorCode;

	/**
	 * Returning doc info
	 */
    @Embedded
	private Doc docReturn;

    /**
     * Cost source info list, i.e. list of purchase/production docs info: id, document date and number, quantity and cost.
     * Information filled in the time of sale, i.e. at finishing invoice document.
     * The information here can be used for the reconstruction of inventory records (purchase, production or others)
     * at finishing returning invoice document (i.e. invoice with negative quantities)
     * which mus be linked through {@link DocPartInvoice#getDocReturn docReturn} to this document.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<PartCostSource> costInfo;

    @Embedded
    private PartSN sn;

    /**
     * Unique id in part's list in the document
     */
    private UUID uuid;

    @JdbcTypeCode(SqlTypes.JSON)
    private DocPart docPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("parts")
    private PurchaseSql parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private PartSql part;

    /**
     *  Not enough quantity to finish operation - used in frontend only and is filled in invoice finishing procedure
     */
    @Transient
    private BigDecimal notEnough;

    public void reset() {
        finished = false;
    }

    @Override
    public String toMessage() {
        return ((docPart.getSku() == null ? "" : docPart.getSku()) + ' ' + (docPart.getName() == null ? "" : docPart.getName()) +
                (!PartType.PRODUCT_SN.equals(docPart.getType()) ? "" : sn == null ? "" : (", S/N: " + sn))).trim();
    }

    // customized getters/setters

    public boolean isInCost() {
        return inCost != null && inCost;
    }

    public boolean isAddExp() {
        return addExp != null && addExp;
    }

    @Override
    public Long getPartId() {
        return Validators.isValid(docPart) ? docPart.getId() : Validators.isValid(part) ? part.getId() : null;
    }

    // generated
    // except getInCost(), getAddExp()


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
    public WarehouseSql getWarehouse() {
        return warehouse;
    }

    @Override
    public void setWarehouse(WarehouseSql warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
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
    public PartSN getSn() {
        return sn;
    }

    @Override
    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public DocPart getDocPart() {
        return docPart;
    }

    @Override
    public void setDocPart(DocPart docPart) {
        this.docPart = docPart;
    }

    public PurchaseSql getParent() {
        return parent;
    }

    public void setParent(PurchaseSql parent) {
        this.parent = parent;
    }

    @Override
    public PartSql getPart() {
        return part;
    }

    @Override
    public void setPart(PartSql part) {
        this.part = part;
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
    public String toString() {
        return "PurchasePartSql{" +
                "sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finished=" + finished +
                ", costTotal=" + costTotal +
                ", inCost=" + inCost +
                ", addExp=" + addExp +
                ", expense=" + expense +
                ", taxTotal=" + taxTotal +
                ", vendorCode='" + vendorCode + '\'' +
                ", docReturn=" + docReturn +
                ", costInfo=" + costInfo +
                ", sn=" + sn +
                ", uuid=" + uuid +
                ", docPart=" + docPart +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}