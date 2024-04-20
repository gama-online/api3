package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.helpers.Validators;
import lt.gama.model.i.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.documents.OrderSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.PartSN;
import lt.gama.model.type.part.VATRate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_parts")
public class OrderPartSql extends BaseCompanySql implements IDocPart, IPartSN, IUuid, IVatRate, ISortOrder, IQuantity {

    private Double sortOrder;

    private BigDecimal quantity;

    private BigDecimal estimate;

    /**
     * remainder to order
     */
    private BigDecimal remainder;

    @Embedded
    private GamaMoney total;

    @Embedded
    private GamaMoney baseTotal;

    private String vendorCode;

    private boolean taxable;

    @Deprecated
    @Transient
    private Double vatRate;

    /**
     * Lookup to VAT rate info
     */
    private String vatRateCode;

    @Embedded
    private VATRate vat;

    @Embedded
    private GamaMoney currentTotal;

    @Embedded
    private GamaMoney currentTaxTotal;

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
    private OrderSql parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private PartSql part;

    // customized getters/setters

    @Override
    public void setVat(VATRate vat) {
        this.vat = vat;
        this.vatRate = vat != null ? vat.getRate() : null;
        this.taxable = vat != null && vat.getRate() != null && vat.getRate() > 0;
    }

    @Override
    public Long getPartId() {
        return Validators.isValid(docPart) ? docPart.getId() : Validators.isValid(part) ? part.getId() : null;
    }

    // generated

    @Override
    public Double getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getEstimate() {
        return estimate;
    }

    public void setEstimate(BigDecimal estimate) {
        this.estimate = estimate;
    }

    public BigDecimal getRemainder() {
        return remainder;
    }

    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
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

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    @Override
    public boolean isTaxable() {
        return taxable;
    }

    @Override
    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public Double getVatRate() {
        return vatRate;
    }

    public void setVatRate(Double vatRate) {
        this.vatRate = vatRate;
    }

    @Override
    public String getVatRateCode() {
        return vatRateCode;
    }

    @Override
    public void setVatRateCode(String vatRateCode) {
        this.vatRateCode = vatRateCode;
    }

    @Override
    public VATRate getVat() {
        return vat;
    }

    public GamaMoney getCurrentTotal() {
        return currentTotal;
    }

    public void setCurrentTotal(GamaMoney currentTotal) {
        this.currentTotal = currentTotal;
    }

    public GamaMoney getCurrentTaxTotal() {
        return currentTaxTotal;
    }

    public void setCurrentTaxTotal(GamaMoney currentTaxTotal) {
        this.currentTaxTotal = currentTaxTotal;
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

    public OrderSql getParent() {
        return parent;
    }

    public void setParent(OrderSql parent) {
        this.parent = parent;
    }

    public PartSql getPart() {
        return part;
    }

    public void setPart(PartSql part) {
        this.part = part;
    }

    @Override
    public String toString() {
        return "OrderPartSql{" +
                "sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", estimate=" + estimate +
                ", remainder=" + remainder +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", vendorCode='" + vendorCode + '\'' +
                ", taxable=" + taxable +
                ", vatRate=" + vatRate +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", vat=" + vat +
                ", currentTotal=" + currentTotal +
                ", currentTaxTotal=" + currentTaxTotal +
                ", sn=" + sn +
                ", uuid=" + uuid +
                ", docPart=" + docPart +
                "} " + super.toString();
    }
}
