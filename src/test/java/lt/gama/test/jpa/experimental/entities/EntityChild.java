package lt.gama.test.jpa.experimental.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.type.GamaMoney;
import lt.gama.test.jpa.experimental.types.TestCustomer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "entity_child")
public class EntityChild {

    @Id
    private Long id;

    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    private TestCustomer customer;

    @JdbcTypeCode(SqlTypes.JSON)
    private GamaMoney money;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("children")
    private EntityMaster master;

    @Override
    public String toString() {
        return "EntityChild{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", customer=" + customer +
                ", money=" + money +
                '}';
    }

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(TestCustomer customer) {
        this.customer = customer;
    }

    public GamaMoney getMoney() {
        return money;
    }

    public void setMoney(GamaMoney money) {
        this.money = money;
    }

    public EntityMaster getMaster() {
        return master;
    }

    public void setMaster(EntityMaster master) {
        this.master = master;
    }
}
