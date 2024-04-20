package lt.gama.impexp.entity;

import lt.gama.helpers.BooleanUtils;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.part.DocPartInvoice;
import lt.gama.model.type.part.DocPartInvoiceSubpart;
import lt.gama.model.type.part.PartCostSource;

import java.util.ArrayList;

/**
 * gama-online
 * Created by valdas on 2016-04-03.
 */
public class DocPartInvoiceExport extends PartPartExport {

    private boolean taxable;

    private Double vatRate;

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
     * discountedTotal = discountedPrice * quantity
     */
    private GamaMoney discountedTotal;


    @SuppressWarnings("unused")
    protected DocPartInvoiceExport() {}

    public DocPartInvoiceExport(DocPartInvoiceSubpart src) {
        super(src);

        setWarehouse(src.getWarehouse());
        setQuantity(src.getQuantity());
        setCf(src.getCf());
        if (src.getCostInfo() != null && src.getCostInfo().size() > 0) {
            setCostInfo(new ArrayList<>());
            for (PartCostSource partCostSource : src.getCostInfo()) {
                getCostInfo().add(new PartCostSourceExport(partCostSource));
            }
        }
    }

    public DocPartInvoiceExport(DocPartInvoice src) {
        super(src);
        setWarehouse(src.getWarehouse());
        setQuantity(src.getQuantity());
        setCf(src.getCf());
        if (src.getCostInfo() != null && src.getCostInfo().size() > 0) {
            setCostInfo(new ArrayList<>());
            for (PartCostSource partCostSource : src.getCostInfo()) {
                getCostInfo().add(new PartCostSourceExport(partCostSource));
            }
        }

        setFinished(BooleanUtils.isTrue(src.getFinished()));
        setPrice(src.getPrice());
        setTotal(src.getTotal());
        setBaseTotal(src.getBaseTotal());
        setCostTotal(src.getCostTotal());
        setForwardSell(src.isForwardSell());

        taxable = src.isTaxable();
        vatRate = src.getVat() != null ? src.getVat().getRate() : src.getVatRate();

        discount = src.getDiscount();
        discountDoc = src.getDiscountDoc();
        discountedPrice = src.getDiscountedPrice();
        discountedTotal = src.getDiscountedTotal();
    }

    // generated

    public boolean isTaxable() {
        return taxable;
    }

    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public Double getVatRate() {
        return vatRate;
    }

    public void setVatRate(Double vatRate) {
        this.vatRate = vatRate;
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

    @Override
    public String toString() {
        return "DocPartInvoiceExport{" +
                "taxable=" + taxable +
                ", vatRate=" + vatRate +
                ", discount=" + discount +
                ", discountDoc=" + discountDoc +
                ", discountedPrice=" + discountedPrice +
                ", discountedTotal=" + discountedTotal +
                "} " + super.toString();
    }
}
