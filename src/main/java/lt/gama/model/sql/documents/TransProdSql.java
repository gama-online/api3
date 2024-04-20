package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.model.i.IParts;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.TransProdPartFromSql;
import lt.gama.model.sql.documents.items.TransProdPartSql;
import lt.gama.model.sql.documents.items.TransProdPartToSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.Location;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocRecipe;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trans_prod")
@NamedEntityGraph(name = TransProdSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(TransProdSql_.PARTS),
        @NamedAttributeNode(TransProdSql_.WAREHOUSE_FROM),
        @NamedAttributeNode(TransProdSql_.WAREHOUSE_RESERVED),
        @NamedAttributeNode(TransProdSql_.WAREHOUSE_TO)
})
public class TransProdSql extends BaseDocumentSql implements IParts<TransProdPartSql> {

    public static final String GRAPH_ALL = "graph.TransProdSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<TransProdPartSql> parts = new ArrayList<>();

    @Transient
    private List<TransProdPartFromSql> partsFrom;

    @Transient
    private List<TransProdPartToSql> partsTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_from_id")
    private WarehouseSql warehouseFrom;

    private String tagFrom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_reserved_id")
    private WarehouseSql warehouseReserved;

    private String tagReserved;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_to_id")
    private WarehouseSql warehouseTo;

    private String tagTo;

    private Boolean finishedPartsFrom;

    private Boolean finishedPartsTo;

    /**
     * Is the document reserved? - i.e. the reserving procedure started.
     * Some attributes of document (date, number, finished parts) cannot be edited
     * but others can (like unreserved parts)
     */
    private Boolean reserved;

    /**
     * Are all parts (partsFrom) reserved, i.e. moved to special warehouse with tag?
     */
    private Boolean reservedParts;

    @JdbcTypeCode(SqlTypes.JSON)
    private DocRecipe recipe;

    /*
     * Transportation info
     */

    private String driver;

    @Embedded
    private Location loadAddress;

    @Embedded
    private Location unloadAddress;

    private String transportId;

    private String transportMarque;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Doc> docs;

    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() && BooleanUtils.isTrue(finishedPartsFrom) && BooleanUtils.isTrue(finishedPartsTo);
    }

    @Override
    public boolean setFullyFinished() {
        boolean changed = super.setFullyFinished() || BooleanUtils.isNotTrue(finishedPartsFrom) || BooleanUtils.isNotTrue(finishedPartsTo);
        finishedPartsFrom = true;
        finishedPartsTo = true;
        return changed;
    }

    // customized getters/setters

    public List<TransProdPartFromSql> getPartsFrom() {
        if (partsFrom == null) {
            partsFrom = CollectionsHelper.streamOf(parts)
                    .filter(p -> p instanceof TransProdPartFromSql).map(p -> (TransProdPartFromSql) p).toList();
        }
        return partsFrom;
    }

    public List<TransProdPartToSql> getPartsTo() {
        if (partsTo == null) {
            partsTo = CollectionsHelper.streamOf(parts)
                    .filter(p -> p instanceof TransProdPartToSql).map(p -> (TransProdPartToSql) p).toList();
        }
        return partsTo;
    }

    //generated

    public List<TransProdPartSql> getParts() {
        return parts;
    }

    public void setParts(List<TransProdPartSql> parts) {
        this.parts = parts;
    }

    public void setPartsFrom(List<TransProdPartFromSql> partsFrom) {
        this.partsFrom = partsFrom;
    }

    public void setPartsTo(List<TransProdPartToSql> partsTo) {
        this.partsTo = partsTo;
    }

    public WarehouseSql getWarehouseFrom() {
        return warehouseFrom;
    }

    public void setWarehouseFrom(WarehouseSql warehouseFrom) {
        this.warehouseFrom = warehouseFrom;
    }

    public String getTagFrom() {
        return tagFrom;
    }

    public void setTagFrom(String tagFrom) {
        this.tagFrom = tagFrom;
    }

    public WarehouseSql getWarehouseReserved() {
        return warehouseReserved;
    }

    public void setWarehouseReserved(WarehouseSql warehouseReserved) {
        this.warehouseReserved = warehouseReserved;
    }

    public String getTagReserved() {
        return tagReserved;
    }

    public void setTagReserved(String tagReserved) {
        this.tagReserved = tagReserved;
    }

    public WarehouseSql getWarehouseTo() {
        return warehouseTo;
    }

    public void setWarehouseTo(WarehouseSql warehouseTo) {
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
    public String toString() {
        return "TransProdSql{" +
                "warehouseFrom=" + warehouseFrom +
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
