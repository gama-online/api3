package lt.gama.model.type.inventory;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DBType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-07-23.
 */
public class InventoryQ {

    private BigDecimal quantity;

    private GamaMoney costTotal;

    private Long documentId;

    private DBType db;


    public InventoryQ() {
    }

    public InventoryQ(BigDecimal quantity, GamaMoney costTotal) {
        this.quantity = quantity;
        this.costTotal = costTotal;
    }

    public InventoryQ(BigDecimal quantity, GamaMoney costTotal, Long documentId, DBType db) {
        this.quantity = quantity;
        this.costTotal = costTotal;
        this.documentId = documentId;
        this.db = db;
    }

    // generated

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

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public DBType getDb() {
        return db;
    }

    public void setDb(DBType db) {
        this.db = db;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryQ that = (InventoryQ) o;
        return Objects.equals(quantity, that.quantity) && Objects.equals(costTotal, that.costTotal) && Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, costTotal, documentId);
    }

    @Override
    public String toString() {
        return "InventoryQ{" +
                "quantity=" + quantity +
                ", costTotal=" + costTotal +
                ", documentId=" + documentId +
                '}';
    }
}
