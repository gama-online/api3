package lt.gama.model.type.doc;

import lt.gama.model.i.IName;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.base.BaseDocEntity;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2015-11-28.
 */
public class DocCompany extends BaseDocEntity implements IName {

    private String name;

    public DocCompany(long id, String name) {
        setId(id);
        this.name = name;
    }

    public DocCompany() {
    }

    public DocCompany(CompanySql company) {
        setId(company.getId());
        this.name = company.getName();
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
        DocCompany that = (DocCompany) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "DocCompany{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
