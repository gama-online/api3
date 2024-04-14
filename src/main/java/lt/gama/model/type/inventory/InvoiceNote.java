package lt.gama.model.type.inventory;

import lt.gama.model.i.ITranslations;
import lt.gama.model.type.l10n.LangInvoiceNote;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class InvoiceNote implements ITranslations<LangInvoiceNote>, Serializable {

    private String note;

    /**
     * Translations
     */
    private Map<String, LangInvoiceNote> translation;

    // generated

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public Map<String, LangInvoiceNote> getTranslation() {
        return translation;
    }

    @Override
    public void setTranslation(Map<String, LangInvoiceNote> translation) {
        this.translation = translation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceNote invoiceNote = (InvoiceNote) o;
        return Objects.equals(note, invoiceNote.note) && Objects.equals(translation, invoiceNote.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, translation);
    }

    @Override
    public String toString() {
        return "InvoiceNote{" +
                "note='" + note + '\'' +
                ", translation=" + translation +
                '}';
    }
}
