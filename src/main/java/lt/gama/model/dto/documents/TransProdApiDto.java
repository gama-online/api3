package lt.gama.model.dto.documents;

import io.swagger.v3.oas.annotations.media.*;
import lt.gama.model.type.Location;
import lt.gama.model.dto.base.*;
import lt.gama.model.dto.entities.*;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocRecipe;
import lt.gama.model.type.doc.DocWarehouse;

import java.util.*;

public class TransProdApiDto extends BaseDocumentDto {

    private List<PartFromApiDto> partsFrom;

    private List<PartToApiDto> partsTo;

    private DocWarehouse warehouseFrom;

    private DocWarehouse warehouseReserved;

    private DocWarehouse warehouseTo;

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

    // generated

    public List<PartFromApiDto> getPartsFrom() {
        return partsFrom;
    }

    public void setPartsFrom(List<PartFromApiDto> partsFrom) {
        this.partsFrom = partsFrom;
    }

    public List<PartToApiDto> getPartsTo() {
        return partsTo;
    }

    public void setPartsTo(List<PartToApiDto> partsTo) {
        this.partsTo = partsTo;
    }

    public DocWarehouse getWarehouseFrom() {
        return warehouseFrom;
    }

    public void setWarehouseFrom(DocWarehouse warehouseFrom) {
        this.warehouseFrom = warehouseFrom;
    }

    public DocWarehouse getWarehouseReserved() {
        return warehouseReserved;
    }

    public void setWarehouseReserved(DocWarehouse warehouseReserved) {
        this.warehouseReserved = warehouseReserved;
    }

    public DocWarehouse getWarehouseTo() {
        return warehouseTo;
    }

    public void setWarehouseTo(DocWarehouse warehouseTo) {
        this.warehouseTo = warehouseTo;
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
        TransProdApiDto that = (TransProdApiDto) o;
        return Objects.equals(partsFrom, that.partsFrom) && Objects.equals(partsTo, that.partsTo) && Objects.equals(warehouseFrom, that.warehouseFrom) && Objects.equals(warehouseReserved, that.warehouseReserved) && Objects.equals(warehouseTo, that.warehouseTo) && Objects.equals(finishedPartsFrom, that.finishedPartsFrom) && Objects.equals(finishedPartsTo, that.finishedPartsTo) && Objects.equals(reserved, that.reserved) && Objects.equals(reservedParts, that.reservedParts) && Objects.equals(recipe, that.recipe) && Objects.equals(driver, that.driver) && Objects.equals(loadAddress, that.loadAddress) && Objects.equals(unloadAddress, that.unloadAddress) && Objects.equals(transportId, that.transportId) && Objects.equals(transportMarque, that.transportMarque) && Objects.equals(docs, that.docs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), partsFrom, partsTo, warehouseFrom, warehouseReserved, warehouseTo, finishedPartsFrom, finishedPartsTo, reserved, reservedParts, recipe, driver, loadAddress, unloadAddress, transportId, transportMarque, docs);
    }

    @Override
    public String toString() {
        return "TransProdApiDto{" +
                "partsFrom=" + partsFrom +
                ", partsTo=" + partsTo +
                ", warehouseFrom=" + warehouseFrom +
                ", warehouseReserved=" + warehouseReserved +
                ", warehouseTo=" + warehouseTo +
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
