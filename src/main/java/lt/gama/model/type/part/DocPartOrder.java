package lt.gama.model.type.part;

import lt.gama.model.i.IDb;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.i.IVatRate;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-02-01.
 */
public class DocPartOrder extends BaseDocPart implements IVatRate {

    @Serial
    private static final long serialVersionUID = -1L;

    private BigDecimal quantity;

    private GamaBigMoney price;

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


    public DocPartOrder() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartOrder(P part) {
        super(part);
    }

    // customized getters/setters

    @Override
    public void setVat(VATRate vat) {
        this.vat = vat;
        this.vatRate = vat != null ? vat.getRate() : null;
        this.taxable = vat != null && vat.getRate() != null && vat.getRate() > 0;
    }

    // generated

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public GamaBigMoney getPrice() {
        return price;
    }

    @Override
    public void setPrice(GamaBigMoney price) {
        this.price = price;
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
        DocPartOrder that = (DocPartOrder) o;
        return taxable == that.taxable && Objects.equals(quantity, that.quantity) && Objects.equals(price, that.price) && Objects.equals(estimate, that.estimate) && Objects.equals(remainder, that.remainder) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(vendorCode, that.vendorCode) && Objects.equals(vatRate, that.vatRate) && Objects.equals(vatRateCode, that.vatRateCode) && Objects.equals(vat, that.vat) && Objects.equals(currentTotal, that.currentTotal) && Objects.equals(currentTaxTotal, that.currentTaxTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quantity, price, estimate, remainder, total, baseTotal, vendorCode, taxable, vatRate, vatRateCode, vat, currentTotal, currentTaxTotal);
    }

    @Override
    public String toString() {
        return "DocPartOrder{" +
                "quantity=" + quantity +
                ", price=" + price +
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
