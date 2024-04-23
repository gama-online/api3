package lt.gama.test.jpa.experimental.entities;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;

@MappedSuperclass
public class EntityMasterBase {

    @Id
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> labels;

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }


    @Override
    public String toString() {
        return "EntityMasterBase{" +
                "id=" + id +
                ", labels=" + labels +
                '}';
    }
}
