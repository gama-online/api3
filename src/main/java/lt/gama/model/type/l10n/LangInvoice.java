package lt.gama.model.type.l10n;

import java.util.Objects;

public class LangInvoice extends LangBase {

    private String invoiceNote;

    // generated

    public String getInvoiceNote() {
        return invoiceNote;
    }

    public void setInvoiceNote(String invoiceNote) {
        this.invoiceNote = invoiceNote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LangInvoice that = (LangInvoice) o;
        return Objects.equals(invoiceNote, that.invoiceNote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), invoiceNote);
    }

    @Override
    public String toString() {
        return "LangInvoice{" +
                "invoiceNote='" + invoiceNote + '\'' +
                "} " + super.toString();
    }
}
