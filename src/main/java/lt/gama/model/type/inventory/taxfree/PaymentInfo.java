package lt.gama.model.type.inventory.taxfree;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class PaymentInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private String paymentType;
    private BigDecimal amount;
    private LocalDate date;

    // generated

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentInfo that = (PaymentInfo) o;
        return Objects.equals(paymentType, that.paymentType) && Objects.equals(amount, that.amount) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentType, amount, date);
    }

    @Override
    public String toString() {
        return "PaymentInfo{" +
                "paymentType='" + paymentType + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}
