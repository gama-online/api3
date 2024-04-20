package lt.gama.model.type;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-10-17.
 */
public class VATCode {

    private String code;

    private Boolean purchase;

    private Boolean invoice;

    private Double rate;

    private String description;

    private String note;

    /**
     * Order by
     */
    private Double order;

    // generated

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getPurchase() {
        return purchase;
    }

    public void setPurchase(Boolean purchase) {
        this.purchase = purchase;
    }

    public Boolean getInvoice() {
        return invoice;
    }

    public void setInvoice(Boolean invoice) {
        this.invoice = invoice;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Double getOrder() {
        return order;
    }

    public void setOrder(Double order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VATCode vatCode = (VATCode) o;
        return Objects.equals(code, vatCode.code) && Objects.equals(purchase, vatCode.purchase) && Objects.equals(invoice, vatCode.invoice) && Objects.equals(rate, vatCode.rate) && Objects.equals(description, vatCode.description) && Objects.equals(note, vatCode.note) && Objects.equals(order, vatCode.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, purchase, invoice, rate, description, note, order);
    }

    @Override
    public String toString() {
        return "VATCode{" +
                "code='" + code + '\'' +
                ", purchase=" + purchase +
                ", invoice=" + invoice +
                ", rate=" + rate +
                ", description='" + description + '\'' +
                ", note='" + note + '\'' +
                ", order=" + order +
                '}';
    }
}
