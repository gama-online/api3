package lt.gama.model.type.part;

import jakarta.persistence.Transient;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.NumberUtils;
import lt.gama.model.i.*;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.ibase.IBaseDocPart;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartOutRemainder;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-20.
 */
public class DocPartInvoice extends BaseDocPart implements IBaseDocPartOutRemainder, IBaseDocPartCost,
        IBaseDocPart, IFinished, IVatRate, IInvoicePartPrice {

    @Serial
    private static final long serialVersionUID = -1L;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private DocWarehouse warehouse;

    private GamaBigMoney price;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    /**
     * if forward sell is allowed this is remainder without cost. Must be resolved in the future.
     */
    private BigDecimal remainder;

    private boolean taxable;

    /**
     * Fix total amount, i.e. recalculate unit price from total and quantity
     */
    private Boolean fixTotal;

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

    /**
     * Part total with discount: discountedPrice * quantity
     */
    private GamaMoney discountedTotal;

    /**
     * Document level discount amount.
     * Sum of them can be shown as document discount
     * discountDocTotal = total * discountDoc / 100
     * i.e. total = discountTotal - discountDocTotal
     */
    private GamaMoney discountDocTotal;


    private List<DocPartInvoiceSubpart> parts;

    /**
     * don't print sub-parts of this part on invoice
     */
    private Boolean noPrint;

    /**
     * Returning doc info
     */
    private Doc docReturn;

    /**
     * Total cost in base currency.
     * p.s. information can be filled later if part can be allowed to sell without knowing cost at the moment of sell.
     * In this case the remainder will be negative and must be compensated later.
     */
    private GamaMoney costTotal;

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

    public DocPartInvoice() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartInvoice(P part) {
        super(part);
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartInvoice(P part, VATRate vat) {
        super(part);
        this.vat = vat;
        this.vatRateCode = vat != null ? vat.getCode() : null;
        this.taxable = vat != null && vat.getRate() != null && vat.getRate() > 0;
    }

    public void reset() {
        finished = null;
        costInfo = null;
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

    // generated
    // except getNoPrint(), getFixTotal()

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
    public GamaBigMoney getPrice() {
        return price;
    }

    @Override
    public void setPrice(GamaBigMoney price) {
        this.price = price;
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
    public BigDecimal getRemainder() {
        return remainder;
    }

    @Override
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

    public List<DocPartInvoiceSubpart> getParts() {
        return parts;
    }

    public void setParts(List<DocPartInvoiceSubpart> parts) {
        this.parts = parts;
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

    @Override
    public GamaMoney getCostTotal() {
        return costTotal;
    }

    @Override
    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
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
        DocPartInvoice that = (DocPartInvoice) o;
        return taxable == that.taxable && Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(price, that.price) && Objects.equals(finished, that.finished) && Objects.equals(remainder, that.remainder) && Objects.equals(fixTotal, that.fixTotal) && Objects.equals(vatRate, that.vatRate) && Objects.equals(vatRateCode, that.vatRateCode) && Objects.equals(vat, that.vat) && Objects.equals(discount, that.discount) && Objects.equals(discountDoc, that.discountDoc) && Objects.equals(discountedPrice, that.discountedPrice) && Objects.equals(discountedTotal, that.discountedTotal) && Objects.equals(discountDocTotal, that.discountDocTotal) && Objects.equals(parts, that.parts) && Objects.equals(noPrint, that.noPrint) && Objects.equals(docReturn, that.docReturn) && Objects.equals(costTotal, that.costTotal) && Objects.equals(costInfo, that.costInfo) && Objects.equals(notEnough, that.notEnough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quantity, total, baseTotal, warehouse, price, finished, remainder, taxable, fixTotal, vatRate, vatRateCode, vat, discount, discountDoc, discountedPrice, discountedTotal, discountDocTotal, parts, noPrint, docReturn, costTotal, costInfo, notEnough);
    }

    @Override
    public String toString() {
        return "DocPartInvoice{" +
                "quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", price=" + price +
                ", finished=" + finished +
                ", remainder=" + remainder +
                ", taxable=" + taxable +
                ", fixTotal=" + fixTotal +
                ", vatRate=" + vatRate +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", vat=" + vat +
                ", discount=" + discount +
                ", discountDoc=" + discountDoc +
                ", discountedPrice=" + discountedPrice +
                ", discountedTotal=" + discountedTotal +
                ", discountDocTotal=" + discountDocTotal +
                ", parts=" + parts +
                ", noPrint=" + noPrint +
                ", docReturn=" + docReturn +
                ", costTotal=" + costTotal +
                ", costInfo=" + costInfo +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}
