package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.PartInventoryDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.i.IDocPartsDto;
import lt.gama.model.type.l10n.LangInventory;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryDto extends BaseDocumentDto implements IDocPartsDto<PartInventoryDto> {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<PartInventoryDto> parts = new ArrayList<>();

    private WarehouseDto warehouse;

    private String tag;

    private Boolean finishedParts;

    /**
     * Note visible to customer.
     * Can be translated
     */
    private String inventoryNote;

    /**
     * Translations
     */
    private Map<String, LangInventory> translation;

    // generated

    public List<PartInventoryDto> getParts() {
        return parts;
    }

    public void setParts(List<PartInventoryDto> parts) {
        this.parts = parts;
    }

    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDto warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, LangInventory> getTranslation() {
        return translation;
    }

    public void setTranslation(Map<String, LangInventory> translation) {
        this.translation = translation;
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
    public String toString() {
        return "InventoryDto{" +
                "parts=" + parts +
                ", warehouse=" + warehouse +
                ", finishedParts=" + finishedParts +
                ", inventoryNote='" + inventoryNote + '\'' +
                "} " + super.toString();
    }
}
