package lt.gama.test.jpa.experimental.entities;

import jakarta.persistence.*;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.test.jpa.experimental.types.TestCostMoney;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "entity_money")
public class EntityMoney {

    @Id
    private Long id;

    @Embedded
    private GamaMoney amount;

    @Embedded
    private GamaBigMoney big;

    @Embedded
    private TestCostMoney cost;

    @Embedded
    private GamaMoney money;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<GamaMoney> remainders;

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public GamaBigMoney getBig() {
        return big;
    }

    public void setBig(GamaBigMoney big) {
        this.big = big;
    }

    public TestCostMoney getCost() {
        return cost;
    }

    public void setCost(TestCostMoney cost) {
        this.cost = cost;
    }

    public GamaMoney getMoney() {
        return money;
    }

    public void setMoney(GamaMoney money) {
        this.money = money;
    }

    public List<GamaMoney> getRemainders() {
        return remainders;
    }

    public void setRemainders(List<GamaMoney> remainders) {
        this.remainders = remainders;
    }

    @Override
    public String toString() {
        return "EntityMoney{" +
                "id=" + id +
                ", amount=" + amount +
                ", big=" + big +
                ", cost=" + cost +
                ", money=" + money +
                ", remainders=" + remainders +
                '}';
    }
}
