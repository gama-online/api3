package lt.gama.service.sync.openCart.model;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 12/11/2018.
 */
public class OCImportStep {

    private int created;

    private int skipped;

    private int updated;

    private int imported;

    private int exported;

    private int errors;

    public void created() {
        ++created;
    }

    public void updated() {
        ++updated;
    }

    public void skipped() {
        ++skipped;
    }

    public void imported() {
        ++imported;
    }

    public void exported() {
        ++exported;
    }

    public void errors() {
        ++errors;
    }

    // generated

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getExported() {
        return exported;
    }

    public void setExported(int exported) {
        this.exported = exported;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OCImportStep that = (OCImportStep) o;
        return created == that.created && skipped == that.skipped && updated == that.updated && imported == that.imported && exported == that.exported && errors == that.errors;
    }

    @Override
    public int hashCode() {
        return Objects.hash(created, skipped, updated, imported, exported, errors);
    }

    @Override
    public String toString() {
        return "OCImportStep{" +
                "created=" + created +
                ", skipped=" + skipped +
                ", updated=" + updated +
                ", imported=" + imported +
                ", exported=" + exported +
                ", errors=" + errors +
                '}';
    }
}
