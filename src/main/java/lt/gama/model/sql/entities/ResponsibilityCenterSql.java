package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.helpers.StringHelper;
import lt.gama.model.sql.base.BaseCompanySql;


@Entity
@Table(name = "resp_centers")
@NamedEntityGraph(name = ResponsibilityCenterSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(ResponsibilityCenterSql_.PARENT))
public class ResponsibilityCenterSql extends BaseCompanySql {

    public static final String GRAPH_ALL = "graph.ResponsibilityCenterSql.all";

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    private ResponsibilityCenterSql parent;

    private int depth;

    public void setName(String name) {
        this.name = StringHelper.trimNormalize2null(name);
    }

    /**
     * toString except parent
     */
    @Override
    public String toString() {
        return "ResponsibilityCenterSql{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", depth=" + depth +
                "} " + super.toString();
    }

    // generated

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResponsibilityCenterSql getParent() {
        return parent;
    }

    public void setParent(ResponsibilityCenterSql parent) {
        this.parent = parent;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
