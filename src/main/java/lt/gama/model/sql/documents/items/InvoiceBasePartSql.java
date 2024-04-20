package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.helpers.Validators;
import lt.gama.model.i.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartOutRemainder;
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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "disc")
@Table(name="invoice_parts")
public abstract class InvoiceBasePartSql extends BaseCompanySql implements IBaseDocPartOutRemainder,
        IBaseDocPartCost, IBaseDocPartSql, IDocPart, IPartSN, IUuid, IFinished, IPartMessage, ISortOrder, IQuantity {

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

    /**
     * if forward sell is allowed this is remainder without cost. Must be resolved in future.
     */
    private BigDecimal remainder;

    /**
     * Total cost in base currency.
     * p.s. information can be filled later if part can be allowed to sell without knowing cost at the moment of sell.
     * In this case the remainder will be negative and must be compensated later.
     */
    @Embedded
    private GamaMoney costTotal;

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
    private InvoiceSql parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private PartSql part;

    /**
     *  Not enough quantity to finish operation - used in frontend only and is filled in invoice finishing procedure
     */
    @Transient
    private BigDecimal notEnough;

    public String toMessage() {
        return ((docPart.getSku() == null ? "" : docPart.getSku()) + ' ' + (docPart.getName() == null ? "" : docPart.getName()) +
                (!PartType.PRODUCT_SN.equals(docPart.getType()) ? "" : sn == null ? "" : (", S/N: " + sn))).trim();
    }

    // customized getters/setters

    public Long getPartId() {
        return Validators.isValid(getDocPart()) ? getDocPart().getId() : Validators.isValid(getPart()) ? getPart().getId() : null;
    }

    // generated

    public Double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    public WarehouseSql getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseSql warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public BigDecimal getRemainder() {
        return remainder;
    }

    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
    }

    public GamaMoney getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    public List<PartCostSource> getCostInfo() {
        return costInfo;
    }

    public void setCostInfo(List<PartCostSource> costInfo) {
        this.costInfo = costInfo;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public DocPart getDocPart() {
        return docPart;
    }

    public void setDocPart(DocPart docPart) {
        this.docPart = docPart;
    }

    public InvoiceSql getParent() {
        return parent;
    }

    public void setParent(InvoiceSql parent) {
        this.parent = parent;
    }

    public PartSql getPart() {
        return part;
    }

    public void setPart(PartSql part) {
        this.part = part;
    }

    public BigDecimal getNotEnough() {
        return notEnough;
    }

    public void setNotEnough(BigDecimal notEnough) {
        this.notEnough = notEnough;
    }

    @Override
    public String toString() {
        return "InvoiceBasePartSql{" +
                ", sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finished=" + finished +
                ", remainder=" + remainder +
                ", costTotal=" + costTotal +
                ", costInfo=" + costInfo +
                ", sn=" + sn +
                ", uuid=" + uuid +
                ", docPart=" + docPart +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}
