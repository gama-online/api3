package lt.gama.model.sql.documents.items;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.NumberUtils;
import lt.gama.model.i.IInvoicePartPrice;
import lt.gama.model.i.ILinkUuid;
import lt.gama.model.i.IVatRate;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.part.VATRate;

import java.util.UUID;

@Entity
@DiscriminatorValue(InvoicePartSql.DISCRIMINATOR_VALUE_PART)
public class InvoicePartSql extends InvoiceBasePartSql implements IVatRate, ILinkUuid, IInvoicePartPrice {

    public static final String DISCRIMINATOR_VALUE_PART = "P";

    private boolean taxable;

    /**
     * Fix total amount, i.e. recalculate unit price from total and quantity
     */
    private Boolean fixTotal;

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
     * Part total with discount: discountedPrice * quantity
     */
    @Embedded
    private GamaMoney discountedTotal;

    /**
     * Document level discount amount.
     * Sum of them can be shown as document discount
     * discountDocTotal = total * discountDoc / 100
     * i.e. total = discountTotal - discountDocTotal
     */
    @Embedded
    private GamaMoney discountDocTotal;

    /**
     * don't print sub-parts of this part on invoice
     */
    private Boolean noPrint;

    /**
     * Returning doc info
     */
    @Embedded
    private Doc docReturn;

    /**
     * Links Invoice part and subpart
     */
    private UUID linkUuid;

    public InvoicePartSql() {
    }

    public InvoicePartSql(UUID linkUuid) {
        this.linkUuid = linkUuid;
    }

    // customized getters/setters

    @Deprecated
    public Double getVatRate() {
        if (!isTaxable()) return null;
        return getVat() != null ? getVat().getRate() : vatRate;
    }

    @Deprecated
    protected void setVatRate(Double vatRate) {
        this.vatRate = vatRate;
    }

    @Override
    public void setVat(VATRate vat) {
        this.vat = vat;
        this.vatRate = vat != null ? vat.getRate() : null;
        this.taxable = this.vatRate != null && this.vatRate > 0;
    }

    public boolean isFixTotal() {
        return fixTotal != null && fixTotal;
    }

    public boolean isNoPrint() {
        return noPrint != null && noPrint;
    }

    public GamaBigMoney getDiscountedPriceWithVAT() {
        if (!isTaxable() || GamaMoneyUtils.isZero(getDiscountedPrice())) return getDiscountedPrice();
        Double vat = getVat() != null ? getVat().getRate() : null;
        if (NumberUtils.isZero(vat, 2)) return getDiscountedPrice();
        return GamaMoneyUtils.taxBy(getDiscountedPrice(), vat);
    }

    public GamaMoney getDiscountedTotalWithVAT() {
        if (!isTaxable() || GamaMoneyUtils.isZero(getDiscountedTotal())) return getDiscountedTotal();
        Double vat = getVat() != null ? getVat().getRate() : null;
        if (NumberUtils.isZero(vat, 2)) return getDiscountedTotal();
        return GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(getDiscountedPriceWithVAT(), getQuantity()));
    }

    @Override
    public GamaBigMoney getPrice() {
        return super.getPrice();
    }

    // generated
    // except getNoPrint(), getFixTotal()

    @Override
    public boolean isTaxable() {
        return taxable;
    }

    @Override
    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public void setFixTotal(Boolean fixTotal) {
        this.fixTotal = fixTotal;
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

    public GamaMoney getDiscountedTotal() {
        return discountedTotal;
    }

    public void setDiscountedTotal(GamaMoney discountedTotal) {
        this.discountedTotal = discountedTotal;
    }

    public GamaMoney getDiscountDocTotal() {
        return discountDocTotal;
    }

    public void setDiscountDocTotal(GamaMoney discountDocTotal) {
        this.discountDocTotal = discountDocTotal;
    }

    public void setNoPrint(Boolean noPrint) {
        this.noPrint = noPrint;
    }

    public Doc getDocReturn() {
        return docReturn;
    }

    public void setDocReturn(Doc docReturn) {
        this.docReturn = docReturn;
    }

    public UUID getLinkUuid() {
        return linkUuid;
    }

    public void setLinkUuid(UUID linkUuid) {
        this.linkUuid = linkUuid;
    }

    @Override
    public String toString() {
        return "InvoicePartSql{" +
                "taxable=" + taxable +
                ", fixTotal=" + fixTotal +
                ", vatRate=" + vatRate +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", vat=" + vat +
                ", discount=" + discount +
                ", discountDoc=" + discountDoc +
                ", discountedPrice=" + discountedPrice +
                ", discountedTotal=" + discountedTotal +
                ", discountDocTotal=" + discountDocTotal +
                ", noPrint=" + noPrint +
                ", docReturn=" + docReturn +
                ", linkUuid=" + linkUuid +
                "} " + super.toString();
    }
}
