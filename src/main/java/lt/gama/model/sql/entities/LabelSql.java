package lt.gama.model.sql.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.enums.LabelType;


@Entity
@Table(name = "label")
public class LabelSql extends BaseCompanySql {

    private String name;

    private LabelType type;


    public LabelSql() {
    }

    public LabelSql(String name) {
        this.name = name;
    }

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LabelType getType() {
        return type;
    }

    public void setType(LabelType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LabelSql{" +
                "name='" + name + '\'' +
                ", type=" + type +
                "} " + super.toString();
    }
}
