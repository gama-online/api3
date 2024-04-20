package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.i.IInventoryNow;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.inventory.InventoryQ;
import lt.gama.model.type.part.PartSN;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static lt.gama.helpers.EntityUtils.DATE_FMT;

@Entity
@Table(name = "inventory_now")
@NamedEntityGraphs({
        @NamedEntityGraph(name = InventoryNowSql.GRAPH_ALL,
                attributeNodes = {
                        @NamedAttributeNode(InventoryNowSql_.COUNTERPARTY),
                        @NamedAttributeNode(InventoryNowSql_.PART),
                        @NamedAttributeNode(InventoryNowSql_.WAREHOUSE)
                }),
        @NamedEntityGraph(name = InventoryNowSql.GRAPH_COUNTERPARTY,
                attributeNodes = @NamedAttributeNode(InventoryNowSql_.COUNTERPARTY))
})
public class InventoryNowSql extends BaseCompanySql implements IInventoryNow {

    public static final String GRAPH_ALL = "graph.InventoryNowSql.all";
    public static final String GRAPH_COUNTERPARTY = "graph.InventoryNowSql.counterparty";

    private String timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private PartSql part;

    @Embedded
    private PartSN sn;

    @Embedded
    private Doc doc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<InventoryQ> remainder;

    private BigDecimal quantity;

    @Embedded
    private GamaMoney costTotal;

    /**
     * Tag value if warehouse is tagged, i.e. 'withTag' is set
     */
    private String tag;

    public InventoryNowSql() {
    }

    public InventoryNowSql(long companyId, PartSql part, PartSN sn, WarehouseSql warehouse,
                           Doc doc, CounterpartySql counterparty) {
        setCompanyId(companyId);
        this.doc = Validators.checkNotNull(doc, "No doc");
        if (this.doc.getDb() == null) this.doc.setDb(DBType.DATASTORE);
        this.part = part;
        this.sn = sn;
        this.warehouse = warehouse;
        this.counterparty = counterparty;

        LocalDate date = doc.getDate() != null ? doc.getDate() : DateUtils.date();
        timestamp = date.format(DATE_FMT) + Long.toHexString(new Date().getTime());
    }

    public InventoryNowSql(long companyId, PartSql part, PartSN sn, WarehouseSql warehouse,
                           Doc doc, CounterpartySql counterparty, String tag) {
        this(companyId, part, sn, warehouse, doc, counterparty);
        this.tag = tag;
    }

    // customized getters/setters

    @Override
    public BigDecimal getQuantity() {
        if (remainder != null && remainder.size() > 0) {
            BigDecimal quantity = null;
            for (InventoryQ r : remainder) quantity = BigDecimalUtils.add(quantity, r.getQuantity());
            return quantity;
        }
        return this.quantity;
    }

    @Override
    public GamaMoney getCostTotal() {
        if (remainder != null && remainder.size() > 0) {
            GamaMoney costTotal = null;
            for (InventoryQ r : remainder) costTotal = GamaMoneyUtils.add(costTotal, r.getCostTotal());
            return costTotal;
        }
        return this.costTotal;
    }

    // generated

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public PartSql getPart() {
        return part;
    }

    public void setPart(PartSql part) {
        this.part = part;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    @Override
    public Doc getDoc() {
        return doc;
    }

    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    @Override
    public CounterpartySql getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartySql counterparty) {
        this.counterparty = counterparty;
    }

    public WarehouseSql getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseSql warehouse) {
        this.warehouse = warehouse;
    }

    public List<InventoryQ> getRemainder() {
        return remainder;
    }

    public void setRemainder(List<InventoryQ> remainder) {
        this.remainder = remainder;
    }

    @Override
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "InventoryNowSql{" +
                "timestamp='" + timestamp + '\'' +
                ", sn=" + sn +
                ", doc=" + doc +
                ", remainder=" + remainder +
                ", quantity=" + quantity +
                ", costTotal=" + costTotal +
                ", tag='" + tag + '\'' +
                "} " + super.toString();
    }
}

