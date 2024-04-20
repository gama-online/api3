package lt.gama.model.sql.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.model.sql.base.BaseCompanySql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipe")
@NamedEntityGraph(name = RecipeSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(RecipeSql_.PARTS))
public class RecipeSql extends BaseCompanySql {

    public static final String GRAPH_ALL = "graph.RecipeSql.all";

    private String name;

    private String description;

    private BigDecimal quantity;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<RecipePartSql> parts = new ArrayList<>();

    @Transient
    private List<RecipePartFromSql> partsFrom;

    @Transient
    private List<RecipePartToSql> partsTo;

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public List<RecipePartSql> getParts() {
        return parts;
    }

    public void setParts(List<RecipePartSql> parts) {
        this.parts = parts;
    }

    public List<RecipePartFromSql> getPartsFrom() {
        if (partsFrom == null) {
            partsFrom = CollectionsHelper.streamOf(parts)
                    .filter(p -> p instanceof RecipePartFromSql).map(p -> (RecipePartFromSql) p).toList();
        }
        return partsFrom;
    }

    public void setPartsFrom(List<RecipePartFromSql> partsFrom) {
        this.partsFrom = partsFrom;
    }

    public List<RecipePartToSql> getPartsTo() {
        if (partsTo == null) {
            partsTo = CollectionsHelper.streamOf(parts)
                    .filter(p -> p instanceof RecipePartToSql).map(p -> (RecipePartToSql) p).toList();
        }
        return partsTo;
    }

    public void setPartsTo(List<RecipePartToSql> partsTo) {
        this.partsTo = partsTo;
    }

    @Override
    public String toString() {
        return "RecipeSql{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                "} " + super.toString();
    }
}
