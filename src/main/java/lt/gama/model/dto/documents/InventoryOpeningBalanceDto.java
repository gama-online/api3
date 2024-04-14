package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.PartOpeningBalanceDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.i.IDocPartsDto;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryOpeningBalanceDto extends BaseDocumentDto implements IDocPartsDto<PartOpeningBalanceDto> {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<PartOpeningBalanceDto> parts = new ArrayList<>();

    private WarehouseDto warehouse;

    private String tag;

    private Boolean finishedParts;

    // generated

    @Override
    public List<PartOpeningBalanceDto> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<PartOpeningBalanceDto> parts) {
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

    @Override
    public Boolean getFinishedParts() {
        return finishedParts;
    }

    @Override
    public void setFinishedParts(Boolean finishedParts) {
        this.finishedParts = finishedParts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InventoryOpeningBalanceDto that = (InventoryOpeningBalanceDto) o;
        return Objects.equals(parts, that.parts) && Objects.equals(warehouse, that.warehouse) && Objects.equals(tag, that.tag) && Objects.equals(finishedParts, that.finishedParts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parts, warehouse, tag, finishedParts);
    }

    @Override
    public String toString() {
        return "InventoryOpeningBalanceDto{" +
                "parts=" + parts +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finishedParts=" + finishedParts +
                "} " + super.toString();
    }
}
