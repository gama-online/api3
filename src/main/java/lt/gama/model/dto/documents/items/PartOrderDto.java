package lt.gama.model.dto.documents.items;

import lt.gama.model.type.part.VATRate;
import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.i.IQuantity;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.i.IVatRate;
import lt.gama.model.type.GamaMoney;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;

public class PartOrderDto extends BaseDocPartDto implements IVatRate, ISortOrder, IQuantity {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    private Double sortOrder;

    private BigDecimal quantity;

    private BigDecimal estimate;

    /**
     * remainder to order
     */
    private BigDecimal remainder;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private String vendorCode;

    private boolean taxable;

    @Deprecated
    private Double vatRate;

    /**
     * Lookup to VAT rate info
     */
    private String vatRateCode;

    private VATRate vat;

    private GamaMoney currentTotal;

    private GamaMoney currentTaxTotal;

    // customized getters/setters

    @Override
    public void setVat(VATRate vat) {
        this.vat = vat;
        this.vatRate = vat != null ? vat.getRate() : null;
        this.taxable = vat != null && vat.getRate() != null && vat.getRate() > 0;
    }

    // generated


    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartOrderDto that = (PartOrderDto) o;
        return taxable == that.taxable && Objects.equals(recordId, that.recordId) && Objects.equals(sortOrder, that.sortOrder) && Objects.equals(quantity, that.quantity) && Objects.equals(estimate, that.estimate) && Objects.equals(remainder, that.remainder) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(vendorCode, that.vendorCode) && Objects.equals(vatRate, that.vatRate) && Objects.equals(vatRateCode, that.vatRateCode) && Objects.equals(vat, that.vat) && Objects.equals(currentTotal, that.currentTotal) && Objects.equals(currentTaxTotal, that.currentTaxTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), recordId, sortOrder, quantity, estimate, remainder, total, baseTotal, vendorCode, taxable, vatRate, vatRateCode, vat, currentTotal, currentTaxTotal);
    }

    @Override
    public String toString() {
        return "PartOrderDto{" +
                "recordId=" + recordId +
                ", sortOrder=" + sortOrder +
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
                "} " + super.toString();
    }
}
