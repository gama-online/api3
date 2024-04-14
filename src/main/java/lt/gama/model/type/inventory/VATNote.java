package lt.gama.model.type.inventory;

import lt.gama.model.i.ITranslations;
import lt.gama.model.type.l10n.LangVatNote;

import java.util.Map;
import java.util.Objects;

public class VATNote implements ITranslations<LangVatNote> {

    private String note;

    /**
     * Translations
     */
    private Map<String, LangVatNote> translation;

    // generated

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public Map<String, LangVatNote> getTranslation() {
        return translation;
    }

    @Override
    public void setTranslation(Map<String, LangVatNote> translation) {
        this.translation = translation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VATNote vatNote = (VATNote) o;
        return Objects.equals(note, vatNote.note) && Objects.equals(translation, vatNote.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, translation);
    }

    @Override
    public String toString() {
        return "VATNote{" +
                "note='" + note + '\'' +
                ", translation=" + translation +
                '}';
    }
}
