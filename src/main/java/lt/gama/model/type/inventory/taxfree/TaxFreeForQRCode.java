package lt.gama.model.type.inventory.taxfree;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class TaxFreeForQRCode implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private DocHeader docHeader;
    private Customer customer;
    private List<Good> goods;

    // generated

    public DocHeader getDocHeader() {
        return docHeader;
    }

    public void setDocHeader(DocHeader docHeader) {
        this.docHeader = docHeader;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Good> getGoods() {
        return goods;
    }

    public void setGoods(List<Good> goods) {
        this.goods = goods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxFreeForQRCode that = (TaxFreeForQRCode) o;
        return Objects.equals(docHeader, that.docHeader) && Objects.equals(customer, that.customer) && Objects.equals(goods, that.goods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docHeader, customer, goods);
    }

    @Override
    public String toString() {
        return "TaxFreeForQRCode{" +
                "docHeader=" + docHeader +
                ", customer=" + customer +
                ", goods=" + goods +
                '}';
    }
}
