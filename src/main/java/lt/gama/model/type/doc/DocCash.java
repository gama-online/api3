package lt.gama.model.type.doc;

import lt.gama.model.i.ICash;
import lt.gama.model.i.IName;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.enums.DBType;

import java.io.Serial;
import java.util.Objects;

public class DocCash extends BaseDocEntity implements IName, ICash {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private String cashier;

    public DocCash() {}

    public DocCash(Long id, DBType db, String name, String cashier) {
        setId(id);
        setDb(db);
        this.name = name;
        this.cashier = cashier;
    }

    public DocCash(ICash cash) {
        if (cash == null) return;
        setId(cash.getId());
        this.name = cash.getName();
        this.cashier = cash.getCashier();
        setDb(cash.getDb());
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
    public String getCashier() {
        return cashier;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocCash docCash = (DocCash) o;
        return Objects.equals(name, docCash.name) && Objects.equals(cashier, docCash.cashier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, cashier);
    }

    @Override
    public String toString() {
        return "DocCash{" +
                "name='" + name + '\'' +
                ", cashier='" + cashier + '\'' +
                "} " + super.toString();
    }
}
