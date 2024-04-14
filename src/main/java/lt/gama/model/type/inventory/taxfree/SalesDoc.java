package lt.gama.model.type.inventory.taxfree;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class SalesDoc implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private LocalDate date;
    private String invoiceNo;
    private List<Good> goods;

    private BigDecimal acceptedVatAmount;

    // generated

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public List<Good> getGoods() {
        return goods;
    }

    public void setGoods(List<Good> goods) {
        this.goods = goods;
    }

    public BigDecimal getAcceptedVatAmount() {
        return acceptedVatAmount;
    }

    public void setAcceptedVatAmount(BigDecimal acceptedVatAmount) {
        this.acceptedVatAmount = acceptedVatAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesDoc salesDoc = (SalesDoc) o;
        return Objects.equals(date, salesDoc.date) && Objects.equals(invoiceNo, salesDoc.invoiceNo) && Objects.equals(goods, salesDoc.goods) && Objects.equals(acceptedVatAmount, salesDoc.acceptedVatAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, invoiceNo, goods, acceptedVatAmount);
    }

    @Override
    public String toString() {
        return "SalesDoc{" +
                "date=" + date +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", goods=" + goods +
                ", acceptedVatAmount=" + acceptedVatAmount +
                '}';
    }
}
