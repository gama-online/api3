package lt.gama.model.dto.documents;

import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.entities.PartInventoryApiDto;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

public class InventoryApiDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<PartInventoryApiDto> parts;

    private DocWarehouse warehouse;

    private Boolean finishedParts;

    /**
     * Note visible to customer.
     * Can be translated
     */
    private String inventoryNote;

    // generated

    public List<PartInventoryApiDto> getParts() {
        return parts;
    }

    public void setParts(List<PartInventoryApiDto> parts) {
        this.parts = parts;
    }

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Boolean getFinishedParts() {
        return finishedParts;
    }

    public void setFinishedParts(Boolean finishedParts) {
        this.finishedParts = finishedParts;
    }

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
        InventoryApiDto that = (InventoryApiDto) o;
        return Objects.equals(parts, that.parts) && Objects.equals(warehouse, that.warehouse) && Objects.equals(finishedParts, that.finishedParts) && Objects.equals(inventoryNote, that.inventoryNote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parts, warehouse, finishedParts, inventoryNote);
    }

    @Override
    public String toString() {
        return "InventoryApiDto{" +
                "parts=" + parts +
                ", warehouse=" + warehouse +
                ", finishedParts=" + finishedParts +
                ", inventoryNote='" + inventoryNote + '\'' +
                "} " + super.toString();
    }
}
