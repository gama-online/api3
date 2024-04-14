package lt.gama.model.type.doc;

import lt.gama.model.i.IManufacturer;
import lt.gama.model.i.IName;
import lt.gama.model.type.base.BaseDocEntity;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2018-02-15.
 */
public class DocManufacturer extends BaseDocEntity implements IName, IManufacturer {

    private String name;

    public DocManufacturer() {
    }

    public DocManufacturer(IManufacturer m) {
        if (m == null) return;
        setId(m.getId());
        setName(m.getName());
    }

    // generated

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocManufacturer that = (DocManufacturer) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DocManufacturer{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
