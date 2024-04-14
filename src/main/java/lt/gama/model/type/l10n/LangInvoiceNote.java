package lt.gama.model.type.l10n;

import java.util.Objects;

public class LangInvoiceNote extends LangBase {

    private String note;

    // generated

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LangInvoiceNote that = (LangInvoiceNote) o;
        return Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), note);
    }

    @Override
    public String toString() {
        return "LangInvoiceNote{" +
                "note='" + note + '\'' +
                "} " + super.toString();
    }
}
