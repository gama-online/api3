package lt.gama.model.type.part;

import lt.gama.model.i.IDb;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.i.IVatRate;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * Gama
 * Created by valdas on 15-06-20.
 */
public class DocPartEstimate extends BaseDocPart implements IVatRate {

    @Serial
    private static final long serialVersionUID = -1L;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private DocWarehouse warehouse;

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

    private List<DocPartEstimateSubpart> parts;


    public DocPartEstimate() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartEstimate(P part) {
        super(part);
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartEstimate(P part, VATRate vat) {
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

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
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

    public List<DocPartEstimateSubpart> getParts() {
        return parts;
    }

    public void setParts(List<DocPartEstimateSubpart> parts) {
        this.parts = parts;
    }

    @Override
    public String toString() {
        return "DocPartEstimate{" +
                "quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
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
                "} " + super.toString();
    }
}
