package lt.gama.model.dto.documents.items;

import lt.gama.model.type.part.VATRate;
import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.i.*;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PartEstimateDto extends BaseDocPartDto implements IVatRate, ISortOrder,
        ILinkUuidParts<PartEstimateSubpartDto>, IUuidParts<PartEstimateSubpartDto>, IQuantity {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    private Double sortOrder;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private WarehouseDto warehouse;

    private String tag;

    private BigDecimal estimate;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    /**
     * remainder to sell
     */
    private BigDecimal remainder;

    private boolean taxable;

    @Deprecated
    private Double vatRate;

    /**
     * Lookup to VAT rate info
     */
    private String vatRateCode;

    private VATRate vat;

    /**
     * Part's discount in percent
     */
    private Double discount;

    /**
     * Document's discount in percent
     */
    private Double discountDoc;

    /**
     * discountedPrice = price * (1 - discount / 100)
     */
    private GamaBigMoney discountedPrice;

    private List<PartEstimateSubpartDto> parts;

    /**
     * Links Invoice part and subpart
     */
    private UUID linkUuid;


    public PartEstimateDto() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> PartEstimateDto(P part) {
        super(part);
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> PartEstimateDto(P part, VATRate vat) {
        super(part);
        this.vat = vat;
        this.vatRateCode = vat != null ? vat.getCode() : null;
        this.taxable = vat != null && vat.getRate() != null && vat.getRate() > 0;
    }


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

    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDto warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public BigDecimal getEstimate() {
        return estimate;
    }

    public void setEstimate(BigDecimal estimate) {
        this.estimate = estimate;
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

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getDiscountDoc() {
        return discountDoc;
    }

    public void setDiscountDoc(Double discountDoc) {
        this.discountDoc = discountDoc;
    }

    public GamaBigMoney getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(GamaBigMoney discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public List<PartEstimateSubpartDto> getParts() {
        return parts;
    }

    public void setParts(List<PartEstimateSubpartDto> parts) {
        this.parts = parts;
    }

    @Override
    public UUID getLinkUuid() {
        return linkUuid;
    }

    @Override
    public void setLinkUuid(UUID linkUuid) {
        this.linkUuid = linkUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartEstimateDto that = (PartEstimateDto) o;
        return taxable == that.taxable && Objects.equals(recordId, that.recordId) && Objects.equals(sortOrder, that.sortOrder) && Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(tag, that.tag) && Objects.equals(estimate, that.estimate) && Objects.equals(finished, that.finished) && Objects.equals(remainder, that.remainder) && Objects.equals(vatRate, that.vatRate) && Objects.equals(vatRateCode, that.vatRateCode) && Objects.equals(vat, that.vat) && Objects.equals(discount, that.discount) && Objects.equals(discountDoc, that.discountDoc) && Objects.equals(discountedPrice, that.discountedPrice) && Objects.equals(parts, that.parts) && Objects.equals(linkUuid, that.linkUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), recordId, sortOrder, quantity, total, baseTotal, warehouse, tag, estimate, finished, remainder, taxable, vatRate, vatRateCode, vat, discount, discountDoc, discountedPrice, parts, linkUuid);
    }

    @Override
    public String toString() {
        return "PartEstimateDto{" +
                "recordId=" + recordId +
                ", sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", estimate=" + estimate +
                ", finished=" + finished +
                ", remainder=" + remainder +
                ", taxable=" + taxable +
                ", vatRate=" + vatRate +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", vat=" + vat +
                ", discount=" + discount +
                ", discountDoc=" + discountDoc +
                ", discountedPrice=" + discountedPrice +
                ", parts=" + parts +
                ", linkUuid=" + linkUuid +
                "} " + super.toString();
    }
}
