package lt.gama.model.type.doc;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import lt.gama.model.dto.entities.ResponsibilityCenterDto;
import lt.gama.model.i.IName;
import lt.gama.model.type.base.BaseDocEntity;

import java.io.Serial;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 2015-10-16.
 */
public class DocRC extends BaseDocEntity implements IName, Comparable<DocRC> {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    protected DocRC() {
    }

    public DocRC(Long id, String name) {
        setId(id);
        this.name = name;
    }

    public DocRC(ResponsibilityCenterDto rc) {
        if (rc == null) return;
        setId(rc.getId());
        this.name = rc.getName();
    }

    @Override
    public int compareTo(DocRC o) {
        return ComparisonChain.start()
                .compare(name, o.name, Ordering.natural().nullsFirst())
                .compare(getId(), o.getId(), Ordering.natural().nullsFirst())
                .result();
    }

    // generated

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocRC docRC = (DocRC) o;
        return Objects.equals(name, docRC.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "DocRC{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
