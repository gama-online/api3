package lt.gama.model.type.doc;

import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-15.
 */
public class DocExpense implements Serializable {

    private String name;

    private GamaBigMoney price;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private String note;

    private Exchange exchange;


    public DocExpense() {
    }

    public DocExpense(String name, GamaBigMoney price, BigDecimal quantity, GamaMoney total) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
    }

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GamaBigMoney getPrice() {
        return price;
    }

    public void setPrice(GamaBigMoney price) {
        this.price = price;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocExpense that = (DocExpense) o;
        return Objects.equals(name, that.name) && Objects.equals(price, that.price) && Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(note, that.note) && Objects.equals(exchange, that.exchange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, quantity, total, baseTotal, note, exchange);
    }

    @Override
    public String toString() {
        return "DocExpense{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", note='" + note + '\'' +
                ", exchange=" + exchange +
                '}';
    }
}
