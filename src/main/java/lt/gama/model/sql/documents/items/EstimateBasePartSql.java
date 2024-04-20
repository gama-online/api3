package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.helpers.Validators;
import lt.gama.model.i.IDocPart;
import lt.gama.model.i.IQuantity;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.i.IUuid;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.documents.EstimateSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.PartSN;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "disc")
@Table(name="estimate_parts")
public class EstimateBasePartSql extends BaseCompanySql implements IDocPart, IUuid, ISortOrder, IQuantity {

    private Double sortOrder;

    private BigDecimal quantity;

    @Embedded
    private GamaMoney total;

    @Embedded
    private GamaMoney baseTotal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    /**
     * remainder to sell
     */
    private BigDecimal remainder;

    @Embedded
    private PartSN sn;

    /**
     * Unique id in part's list in the document
     */
    private UUID uuid;

    @JdbcTypeCode(SqlTypes.JSON)
    private DocPart docPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("parts")
    private EstimateSql parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private PartSql part;

    // customized getters/setters

    public Long getPartId() {
        return Validators.isValid(getDocPart()) ? getDocPart().getId() : Validators.isValid(getPart()) ? getPart().getId() : null;
    }

    // generated

    @Override
    public Double getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    public WarehouseSql getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseSql warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public BigDecimal getRemainder() {
        return remainder;
    }

    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public DocPart getDocPart() {
        return docPart;
    }

    public void setDocPart(DocPart docPart) {
        this.docPart = docPart;
    }

    public EstimateSql getParent() {
        return parent;
    }

    public void setParent(EstimateSql parent) {
        this.parent = parent;
    }

    public PartSql getPart() {
        return part;
    }

    public void setPart(PartSql part) {
        this.part = part;
    }

    @Override
    public String toString() {
        return "EstimateBasePartSql{" +
                "sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finished=" + finished +
                ", remainder=" + remainder +
                ", sn=" + sn +
                ", uuid=" + uuid +
                ", docPart=" + docPart +
                "} " + super.toString();
    }
}
