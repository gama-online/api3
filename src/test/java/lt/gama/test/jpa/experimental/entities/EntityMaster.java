package lt.gama.test.jpa.experimental.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.type.GamaMoney;
import lt.gama.test.jpa.experimental.types.TestCustomer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "entity_master")
@NamedEntityGraph(name = EntityMaster.GRAPH_ALL, attributeNodes = @NamedAttributeNode("children") )
public class EntityMaster extends EntityMasterBase {

    public static final String GRAPH_ALL = "graph.EntityMaster.all";

    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    private TestCustomer customer;

    @JdbcTypeCode(SqlTypes.JSON)
    private GamaMoney money;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("master")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<EntityChild> children;

    @Override
    public String toString() {
        return "EntityMaster{" +
                "name='" + name + '\'' +
                ", customer=" + customer +
                ", money=" + money +
                "} " + super.toString();
    }

    // generated

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

    public List<EntityChild> getChildren() {
        return children;
    }

    public void setChildren(List<EntityChild> children) {
        this.children = children;
    }
}
