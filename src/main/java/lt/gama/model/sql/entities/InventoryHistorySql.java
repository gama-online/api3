package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.model.i.IDoc;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.InventoryType;
import lt.gama.model.type.part.PartSN;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_history")
@NamedEntityGraph(name = InventoryHistorySql.GRAPH_ALL,
        attributeNodes = {
                @NamedAttributeNode("part"),
                @NamedAttributeNode("warehouse"),
                @NamedAttributeNode("counterparty"),
                @NamedAttributeNode("originCounterparty")
        }
)
public class InventoryHistorySql extends BaseCompanySql implements IDoc {

    public static final String GRAPH_ALL = "graph.InventoryHistorySql.all";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private PartSql part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    @Embedded
    private PartSN sn;

    private UUID uuid;

    private InventoryType inventoryType;

    @Embedded
    private Doc doc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    @Embedded
    private Doc originDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_counterparty_id")
    private CounterpartySql originCounterparty;

    /**
     * Quantity at the end of period (the last date or until now)
     */
    private BigDecimal quantity;

    /**
     * Cost total at the end of period (the last date or until now)
     */
    @Embedded
    private GamaMoney costTotal;

    /**
     * Selling totals
     */
    @Embedded
    private GamaMoney sellTotal;

    /**
     * Selling totals in base currency
     */
    @Embedded
    private GamaMoney sellBaseTotal;

    public InventoryHistorySql() {
    }

    public InventoryHistorySql(long companyId, PartSql part, WarehouseSql warehouse, PartSN sn, UUID uuid, InventoryType inventoryType,
                               Doc originDoc, CounterpartySql originCounterparty,
                               Doc doc, CounterpartySql counterparty,
                               BigDecimal quantity, GamaMoney costTotal, GamaMoney sellTotal, GamaMoney sellBaseTotal) {
        setCompanyId(companyId);
        this.part = part;
        this.warehouse = warehouse;
        this.sn = sn;
        this.uuid = uuid;
        this.inventoryType = inventoryType;
        this.originDoc = originDoc;
        this.originCounterparty = originCounterparty;
        this.doc = doc;
        this.counterparty = counterparty;
        this.quantity = quantity;
        this.costTotal = costTotal;
        this.sellTotal = sellTotal;
        this.sellBaseTotal = sellBaseTotal;
    }

    // generated

    public PartSql getPart() {
        return part;
    }

    public void setPart(PartSql part) {
        this.part = part;
    }

    public WarehouseSql getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseSql warehouse) {
        this.warehouse = warehouse;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    @Override
    public Doc getDoc() {
        return doc;
    }

    @Override
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public CounterpartySql getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartySql counterparty) {
        this.counterparty = counterparty;
    }

    public Doc getOriginDoc() {
        return originDoc;
    }

    public void setOriginDoc(Doc originDoc) {
        this.originDoc = originDoc;
    }

    public CounterpartySql getOriginCounterparty() {
        return originCounterparty;
    }

    public void setOriginCounterparty(CounterpartySql originCounterparty) {
        this.originCounterparty = originCounterparty;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public GamaMoney getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    public GamaMoney getSellTotal() {
        return sellTotal;
    }

    public void setSellTotal(GamaMoney sellTotal) {
        this.sellTotal = sellTotal;
    }

    public GamaMoney getSellBaseTotal() {
        return sellBaseTotal;
    }

    public void setSellBaseTotal(GamaMoney sellBaseTotal) {
        this.sellBaseTotal = sellBaseTotal;
    }

    @Override
    public String toString() {
        return "InventoryHistorySql{" +
                "sn=" + sn +
                ", uuid=" + uuid +
                ", inventoryType=" + inventoryType +
                ", doc=" + doc +
                ", originDoc=" + originDoc +
                ", quantity=" + quantity +
                ", costTotal=" + costTotal +
                ", sellTotal=" + sellTotal +
                ", sellBaseTotal=" + sellBaseTotal +
                "} " + super.toString();
    }
}
