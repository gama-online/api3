package lt.gama.test.jpa.experimental.types;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lt.gama.model.type.GamaMoney;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class TestCostMoney {

    private BigDecimal qty;

    @Embedded
    private GamaMoney amount;


    public TestCostMoney() {
    }

    public TestCostMoney(BigDecimal qty, GamaMoney amount) {
        this.qty = qty;
        this.amount = amount;
    }

    // generated

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCostMoney that = (TestCostMoney) o;
        return Objects.equals(qty, that.qty) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qty, amount);
    }

    @Override
    public String toString() {
        return "TestCostMoney{" +
                "qty=" + qty +
                ", amount=" + amount +
                '}';
    }
}
