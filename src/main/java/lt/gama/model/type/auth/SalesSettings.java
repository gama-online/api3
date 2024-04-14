package lt.gama.model.type.auth;

import lt.gama.model.type.enums.PartSortOrderType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2018-06-30.
 */
public class SalesSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Apply last sale price?
     */
    private Boolean lastSalePrice;

    /**
     * Electronic Cash Registers (ECR) numbers list.
     * Used in Invoice
     */
    private List<String> ecrNo;

    private PartSortOrderType defaultPartSortOrder;

    /**
     * Do not print Barcode on Invoice
     */
    private Boolean invoiceNoBarcode;

    /**
     * Do not print Sku on Invoice
     */
    private Boolean invoiceNoSku;

    // generated

    public Boolean getLastSalePrice() {
        return lastSalePrice;
    }

    public void setLastSalePrice(Boolean lastSalePrice) {
        this.lastSalePrice = lastSalePrice;
    }

    public List<String> getEcrNo() {
        return ecrNo;
    }

    public void setEcrNo(List<String> ecrNo) {
        this.ecrNo = ecrNo;
    }

    public PartSortOrderType getDefaultPartSortOrder() {
        return defaultPartSortOrder;
    }

    public void setDefaultPartSortOrder(PartSortOrderType defaultPartSortOrder) {
        this.defaultPartSortOrder = defaultPartSortOrder;
    }

    public Boolean getInvoiceNoBarcode() {
        return invoiceNoBarcode;
    }

    public void setInvoiceNoBarcode(Boolean invoiceNoBarcode) {
        this.invoiceNoBarcode = invoiceNoBarcode;
    }

    public Boolean getInvoiceNoSku() {
        return invoiceNoSku;
    }

    public void setInvoiceNoSku(Boolean invoiceNoSku) {
        this.invoiceNoSku = invoiceNoSku;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesSettings that = (SalesSettings) o;
        return Objects.equals(lastSalePrice, that.lastSalePrice) && Objects.equals(ecrNo, that.ecrNo) && defaultPartSortOrder == that.defaultPartSortOrder && Objects.equals(invoiceNoBarcode, that.invoiceNoBarcode) && Objects.equals(invoiceNoSku, that.invoiceNoSku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastSalePrice, ecrNo, defaultPartSortOrder, invoiceNoBarcode, invoiceNoSku);
    }

    @Override
    public String toString() {
        return "SalesSettings{" +
                "lastSalePrice=" + lastSalePrice +
                ", ecrNo=" + ecrNo +
                ", defaultPartSortOrder=" + defaultPartSortOrder +
                ", invoiceNoBarcode=" + invoiceNoBarcode +
                ", invoiceNoSku=" + invoiceNoSku +
                '}';
    }
}
