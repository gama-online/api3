package lt.gama.model.type.l10n;

import java.util.Objects;

public class LangInventory extends LangBase {

    private String inventoryNote;

    // generated

    public String getInventoryNote() {
        return inventoryNote;
    }

    public void setInventoryNote(String inventoryNote) {
        this.inventoryNote = inventoryNote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LangInventory that = (LangInventory) o;
        return Objects.equals(inventoryNote, that.inventoryNote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inventoryNote);
    }

    @Override
    public String toString() {
        return "LangInventory{" +
                "inventoryNote='" + inventoryNote + '\'' +
                "} " + super.toString();
    }
}
