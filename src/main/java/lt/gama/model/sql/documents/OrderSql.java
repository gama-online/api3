package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.i.IParts;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.OrderPartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.doc.Doc;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order")
@NamedEntityGraph(name = OrderSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(OrderSql_.COUNTERPARTY),
        @NamedAttributeNode(OrderSql_.PARTS),
        @NamedAttributeNode(OrderSql_.WAREHOUSE)
})
public class OrderSql extends BaseDocumentSql implements IParts<OrderPartSql> {

    public static final String GRAPH_ALL = "graph.OrderSql.all";

    @Embedded
    private NameContact contact;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<OrderPartSql> parts = new ArrayList<>();

    /**
     * Document's discount in percent
     */
    private Double discount;

    @Embedded
    private GamaMoney subtotal;

    @Embedded
    private GamaMoney taxTotal;

    @Embedded
    private GamaMoney total;

    @Embedded
    private GamaMoney baseSubtotal;

    @Embedded
    private GamaMoney baseTaxTotal;

    @Embedded
    private GamaMoney baseTotal;

    @Embedded
    private GamaMoney prepayment;

    @Embedded
    private GamaMoney currentSubtotal;

    @Embedded
    private GamaMoney currentTaxTotal;

    @Embedded
    private GamaMoney currentTotal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    private LocalDate dueDate;

    /**
     * Links to generated purchases
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Doc> docs;

    private Boolean zeroVAT;

    public boolean isZeroVAT() {
        return zeroVAT != null && zeroVAT;
    }

    @Override
    public boolean setFullyFinished() {
        boolean changed = BooleanUtils.isNotTrue(getFinished());
        setFinished(true);
        return changed;
    }

    // generated

    public NameContact getContact() {
        return contact;
    }

    public void setContact(NameContact contact) {
        this.contact = contact;
    }

    public List<OrderPartSql> getParts() {
        return parts;
    }

    public void setParts(List<OrderPartSql> parts) {
        this.parts = parts;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public GamaMoney getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(GamaMoney subtotal) {
        this.subtotal = subtotal;
    }

    public GamaMoney getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(GamaMoney taxTotal) {
        this.taxTotal = taxTotal;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getBaseSubtotal() {
        return baseSubtotal;
    }

    public void setBaseSubtotal(GamaMoney baseSubtotal) {
        this.baseSubtotal = baseSubtotal;
    }

    public GamaMoney getBaseTaxTotal() {
        return baseTaxTotal;
    }

    public void setBaseTaxTotal(GamaMoney baseTaxTotal) {
        this.baseTaxTotal = baseTaxTotal;
    }

    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    public GamaMoney getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(GamaMoney prepayment) {
        this.prepayment = prepayment;
    }

    public GamaMoney getCurrentSubtotal() {
        return currentSubtotal;
    }

    public void setCurrentSubtotal(GamaMoney currentSubtotal) {
        this.currentSubtotal = currentSubtotal;
    }

    public GamaMoney getCurrentTaxTotal() {
        return currentTaxTotal;
    }

    public void setCurrentTaxTotal(GamaMoney currentTaxTotal) {
        this.currentTaxTotal = currentTaxTotal;
    }

    public GamaMoney getCurrentTotal() {
        return currentTotal;
    }

    public void setCurrentTotal(GamaMoney currentTotal) {
        this.currentTotal = currentTotal;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<Doc> getDocs() {
        return docs;
    }

    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }

    public void setZeroVAT(Boolean zeroVAT) {
        this.zeroVAT = zeroVAT;
    }

    @Override
    public String toString() {
        return "OrderSql{" +
                "contact=" + contact +
                ", discount=" + discount +
                ", subtotal=" + subtotal +
                ", taxTotal=" + taxTotal +
                ", total=" + total +
                ", baseSubtotal=" + baseSubtotal +
                ", baseTaxTotal=" + baseTaxTotal +
                ", baseTotal=" + baseTotal +
                ", prepayment=" + prepayment +
                ", currentSubtotal=" + currentSubtotal +
                ", currentTaxTotal=" + currentTaxTotal +
                ", currentTotal=" + currentTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", dueDate=" + dueDate +
                ", docs=" + docs +
                ", zeroVAT=" + zeroVAT +
                "} " + super.toString();
    }
}
