package lt.gama.model.type.doc;

import lt.gama.model.i.IName;
import lt.gama.model.type.base.BaseDocEntity;

import java.util.Objects;

public class DocPl extends BaseDocEntity implements IName {

    private String name;

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
        DocPl docPl = (DocPl) o;
        return Objects.equals(name, docPl.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "DocPl{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
