package lt.gama.model.dto.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lt.gama.model.type.Location;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocRecipe;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.PartFromDto;
import lt.gama.model.dto.documents.items.PartToDto;
import lt.gama.model.dto.entities.WarehouseDto;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransProdDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<PartFromDto> partsFrom = new ArrayList<>();

    private List<PartToDto> partsTo = new ArrayList<>();

    private WarehouseDto warehouseFrom;

    private String tagFrom;

    private WarehouseDto warehouseReserved;

    private String tagReserved;

    private WarehouseDto warehouseTo;

    private String tagTo;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean finishedPartsFrom;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean finishedPartsTo;

    /**
     * Is the document reserved? - i.e. the reserving procedure started.
     * Some attributes of document (date, number, finished parts) cannot be edited
     * but others can (like unreserved parts)
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean reserved;

    /**
     * Are all parts (partsFrom) reserved, i.e. moved to special warehouse with tag?
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean reservedParts;

    private DocRecipe recipe;

    /*
     * Transportation info
     */

    private String driver;

    private Location loadAddress;

    private Location unloadAddress;

    private String transportId;

    private String transportMarque;

    private List<Doc> docs;

    public TransProdDto() {
    }

    // generated

    public List<PartFromDto> getPartsFrom() {
        return partsFrom;
    }

    public void setPartsFrom(List<PartFromDto> partsFrom) {
        this.partsFrom = partsFrom;
    }

    public List<PartToDto> getPartsTo() {
        return partsTo;
    }

    public void setPartsTo(List<PartToDto> partsTo) {
        this.partsTo = partsTo;
    }

    public WarehouseDto getWarehouseFrom() {
        return warehouseFrom;
    }

    public void setWarehouseFrom(WarehouseDto warehouseFrom) {
        this.warehouseFrom = warehouseFrom;
    }

    public String getTagFrom() {
        return tagFrom;
    }

    public void setTagFrom(String tagFrom) {
        this.tagFrom = tagFrom;
    }

    public WarehouseDto getWarehouseReserved() {
        return warehouseReserved;
    }

    public void setWarehouseReserved(WarehouseDto warehouseReserved) {
        this.warehouseReserved = warehouseReserved;
    }

    public String getTagReserved() {
        return tagReserved;
    }

    public void setTagReserved(String tagReserved) {
        this.tagReserved = tagReserved;
    }

    public WarehouseDto getWarehouseTo() {
        return warehouseTo;
    }

    public void setWarehouseTo(WarehouseDto warehouseTo) {
        this.warehouseTo = warehouseTo;
    }

    public String getTagTo() {
        return tagTo;
    }

    public void setTagTo(String tagTo) {
        this.tagTo = tagTo;
    }

    public Boolean getFinishedPartsFrom() {
        return finishedPartsFrom;
    }

    public void setFinishedPartsFrom(Boolean finishedPartsFrom) {
        this.finishedPartsFrom = finishedPartsFrom;
    }

    public Boolean getFinishedPartsTo() {
        return finishedPartsTo;
    }

    public void setFinishedPartsTo(Boolean finishedPartsTo) {
        this.finishedPartsTo = finishedPartsTo;
    }

    public Boolean getReserved() {
        return reserved;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    public Boolean getReservedParts() {
        return reservedParts;
    }

    public void setReservedParts(Boolean reservedParts) {
        this.reservedParts = reservedParts;
    }

    public DocRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(DocRecipe recipe) {
        this.recipe = recipe;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Location getLoadAddress() {
        return loadAddress;
    }

    public void setLoadAddress(Location loadAddress) {
        this.loadAddress = loadAddress;
    }

    public Location getUnloadAddress() {
        return unloadAddress;
    }

    public void setUnloadAddress(Location unloadAddress) {
        this.unloadAddress = unloadAddress;
    }

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    public String getTransportMarque() {
        return transportMarque;
    }

    public void setTransportMarque(String transportMarque) {
        this.transportMarque = transportMarque;
    }

    public List<Doc> getDocs() {
        return docs;
    }

    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TransProdDto that = (TransProdDto) o;
        return Objects.equals(partsFrom, that.partsFrom) && Objects.equals(partsTo, that.partsTo) && Objects.equals(warehouseFrom, that.warehouseFrom) && Objects.equals(tagFrom, that.tagFrom) && Objects.equals(warehouseReserved, that.warehouseReserved) && Objects.equals(tagReserved, that.tagReserved) && Objects.equals(warehouseTo, that.warehouseTo) && Objects.equals(tagTo, that.tagTo) && Objects.equals(finishedPartsFrom, that.finishedPartsFrom) && Objects.equals(finishedPartsTo, that.finishedPartsTo) && Objects.equals(reserved, that.reserved) && Objects.equals(reservedParts, that.reservedParts) && Objects.equals(recipe, that.recipe) && Objects.equals(driver, that.driver) && Objects.equals(loadAddress, that.loadAddress) && Objects.equals(unloadAddress, that.unloadAddress) && Objects.equals(transportId, that.transportId) && Objects.equals(transportMarque, that.transportMarque) && Objects.equals(docs, that.docs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), partsFrom, partsTo, warehouseFrom, tagFrom, warehouseReserved, tagReserved, warehouseTo, tagTo, finishedPartsFrom, finishedPartsTo, reserved, reservedParts, recipe, driver, loadAddress, unloadAddress, transportId, transportMarque, docs);
    }

    @Override
    public String toString() {
        return "TransProdDto{" +
                "partsFrom=" + partsFrom +
                ", partsTo=" + partsTo +
                ", warehouseFrom=" + warehouseFrom +
                ", tagFrom='" + tagFrom + '\'' +
                ", warehouseReserved=" + warehouseReserved +
                ", tagReserved='" + tagReserved + '\'' +
                ", warehouseTo=" + warehouseTo +
                ", tagTo='" + tagTo + '\'' +
                ", finishedPartsFrom=" + finishedPartsFrom +
                ", finishedPartsTo=" + finishedPartsTo +
                ", reserved=" + reserved +
                ", reservedParts=" + reservedParts +
                ", recipe=" + recipe +
                ", driver='" + driver + '\'' +
                ", loadAddress=" + loadAddress +
                ", unloadAddress=" + unloadAddress +
                ", transportId='" + transportId + '\'' +
                ", transportMarque='" + transportMarque + '\'' +
                ", docs=" + docs +
                "} " + super.toString();
    }
}
