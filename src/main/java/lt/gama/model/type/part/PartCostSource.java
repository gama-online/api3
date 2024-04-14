package lt.gama.model.type.part;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.enums.InventoryType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-07-15.
 */
public class PartCostSource implements Serializable {

    private Doc doc;

    private DocCounterparty counterparty;

    private BigDecimal quantity;

    private GamaMoney costTotal;

    /**
     * Need recalls
     */
    private InventoryType inventoryType;

    /**
     * Returned quantity. Initial value null. Can be up to quantity.
     */
    private BigDecimal retQuantity;

    /**
     * Returned cost. Initial value null. Can be up to costTotal.
     */
    private GamaMoney retCostTotal;

    private Boolean forwardSell;

    /**
     *  Flag to know if forward-sell document is updated.
     */
    private Boolean fsUpdated;



    // customized getters/setters

    public boolean isForwardSell() {
        return forwardSell != null && forwardSell;
    }

    public boolean isFsUpdated() {
        return fsUpdated != null && fsUpdated;
    }

    // generated
    // except getForwardSell(), getFsUpdated()

    public Doc getDoc() {
        return doc;
    }

    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public DocCounterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(DocCounterparty counterparty) {
        this.counterparty = counterparty;
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

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    public BigDecimal getRetQuantity() {
        return retQuantity;
    }

    public void setRetQuantity(BigDecimal retQuantity) {
        this.retQuantity = retQuantity;
    }

    public GamaMoney getRetCostTotal() {
        return retCostTotal;
    }

    public void setRetCostTotal(GamaMoney retCostTotal) {
        this.retCostTotal = retCostTotal;
    }

    public void setForwardSell(Boolean forwardSell) {
        this.forwardSell = forwardSell;
    }

    public void setFsUpdated(Boolean fsUpdated) {
        this.fsUpdated = fsUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartCostSource that = (PartCostSource) o;
        return Objects.equals(doc, that.doc) && Objects.equals(counterparty, that.counterparty) && Objects.equals(quantity, that.quantity) && Objects.equals(costTotal, that.costTotal) && inventoryType == that.inventoryType && Objects.equals(retQuantity, that.retQuantity) && Objects.equals(retCostTotal, that.retCostTotal) && Objects.equals(forwardSell, that.forwardSell) && Objects.equals(fsUpdated, that.fsUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doc, counterparty, quantity, costTotal, inventoryType, retQuantity, retCostTotal, forwardSell, fsUpdated);
    }

    @Override
    public String toString() {
        return "PartCostSource{" +
                "doc=" + doc +
                ", counterparty=" + counterparty +
                ", quantity=" + quantity +
                ", costTotal=" + costTotal +
                ", inventoryType=" + inventoryType +
                ", retQuantity=" + retQuantity +
                ", retCostTotal=" + retCostTotal +
                ", forwardSell=" + forwardSell +
                ", fsUpdated=" + fsUpdated +
                '}';
    }
}
