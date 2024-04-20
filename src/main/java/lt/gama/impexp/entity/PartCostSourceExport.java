package lt.gama.impexp.entity;

import lt.gama.helpers.BooleanUtils;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.InventoryType;
import lt.gama.model.type.part.PartCostSource;

import java.math.BigDecimal;

/**
 * gama-online
 * Created by valdas on 2016-04-03.
 */
public class PartCostSourceExport {

    DocExport doc;

    DocCounterpartyExport counterparty;

    BigDecimal quantity;

    GamaMoney costTotal;

    /**
     * Need recalls
     */
    InventoryType inventoryType;

    /**
     * Returned quantity. Initial value null. Can be up to quantity.
     */
    BigDecimal retQuantity;

    /**
     * Returned cost. Initial value null. Can be up to costTotal.
     */
    GamaMoney retCostTotal;

    boolean forwardSell;


    @SuppressWarnings("unused")
    protected PartCostSourceExport() {}

    public PartCostSourceExport(PartCostSource src) {
        if (src == null) return;
        doc = new DocExport(src.getDoc());
        counterparty = new DocCounterpartyExport(src.getCounterparty());
        quantity = src.getQuantity();
        costTotal = src.getCostTotal();
        inventoryType = src.getInventoryType();
        retQuantity = src.getRetQuantity();
        retCostTotal = src.getRetCostTotal();
        forwardSell = BooleanUtils.isTrue(src.isForwardSell());
    }

    public DocExport getDoc() {
        return doc;
    }

    public void setDoc(DocExport doc) {
        this.doc = doc;
    }

    public DocCounterpartyExport getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(DocCounterpartyExport counterparty) {
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

    public boolean isForwardSell() {
        return forwardSell;
    }

    public void setForwardSell(boolean forwardSell) {
        this.forwardSell = forwardSell;
    }

    // generated

    @Override
    public String toString() {
        return "PartCostSourceExport{" +
                "doc=" + doc +
                ", counterparty=" + counterparty +
                ", quantity=" + quantity +
                ", costTotal=" + costTotal +
                ", inventoryType=" + inventoryType +
                ", retQuantity=" + retQuantity +
                ", retCostTotal=" + retCostTotal +
                ", forwardSell=" + forwardSell +
                '}';
    }
}
