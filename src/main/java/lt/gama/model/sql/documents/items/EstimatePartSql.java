package lt.gama.model.sql.documents.items;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lt.gama.model.i.ILinkUuid;
import lt.gama.model.i.IVatRate;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.part.VATRate;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@DiscriminatorValue(EstimatePartSql.DISCRIMINATOR_VALUE_PART)
public class EstimatePartSql extends EstimateBasePartSql implements IVatRate, ILinkUuid {

    public static final String DISCRIMINATOR_VALUE_PART = "P";

    private BigDecimal estimate;

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
    @Embedded
    private GamaBigMoney discountedPrice;

    /**
     * Links Estimate part and subpart
     */
    private UUID linkUuid;

    public EstimatePartSql() {
    }

    public EstimatePartSql(UUID linkUuid) {
        this.linkUuid = linkUuid;
    }

    // customized getters/setters

    @Override
    public void setVat(VATRate vat) {
        this.vat = vat;
        this.vatRate = vat != null ? vat.getRate() : null;
        this.taxable = vat != null && vat.getRate() != null && vat.getRate() > 0;
    }

    // generated

    public BigDecimal getEstimate() {
        return estimate;
    }

    public void setEstimate(BigDecimal estimate) {
        this.estimate = estimate;
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

    @Override
    public UUID getLinkUuid() {
        return linkUuid;
    }

    @Override
    public void setLinkUuid(UUID linkUuid) {
        this.linkUuid = linkUuid;
    }

    @Override
    public String toString() {
        return "EstimatePartSql{" +
                "estimate=" + estimate +
                ", taxable=" + taxable +
                ", vatRate=" + vatRate +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", vat=" + vat +
                ", discount=" + discount +
                ", discountDoc=" + discountDoc +
                ", discountedPrice=" + discountedPrice +
                ", linkUuid=" + linkUuid +
                "} " + super.toString();
    }
}
