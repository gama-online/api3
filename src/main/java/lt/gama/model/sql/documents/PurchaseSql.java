package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.i.IDocPartsSql;
import lt.gama.model.i.IISaf;
import lt.gama.model.sql.base.BaseDebtDocumentSql;
import lt.gama.model.sql.documents.items.PurchasePartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocExpense;
import lt.gama.model.type.inventory.VATCodeTotal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase")
@NamedEntityGraph(name = PurchaseSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(PurchaseSql_.COUNTERPARTY),
        @NamedAttributeNode(PurchaseSql_.PARTS),
        @NamedAttributeNode(PurchaseSql_.WAREHOUSE)
})
public class PurchaseSql extends BaseDebtDocumentSql implements IDocPartsSql<PurchasePartSql>, IISaf {

    public static final String GRAPH_ALL = "graph.PurchaseSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<PurchasePartSql> parts = new ArrayList<>();

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    private Boolean finishedParts;

    private LocalDate dueDate;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocExpense> expenses;

    /**
     * VATs totals list
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<VATCodeTotal> vatCodeTotals;

    /**
     * invoice type needed for iSAF export
     */
    private String isafInvoiceType;

    /**
     * if the VAT invoice is subject to the “monetary accounting system” (application of special procedure
     * for establishing the chargeable event according to Article 14(9) of the VAT Law).
     */
    private Boolean isafSpecialTaxation;

    private Boolean reverseVAT;

    /**
     *
     * from which purchase document was generated
     */
    @Embedded
    private Doc order;

    /**
     * Document registration date if not the same as document
     */
    private LocalDate regDate;

    /**
     * Invoice for paid advance payment.
     * G.L. operation with VAT only.
     * No debt are registered.
     */
    private Boolean advance;

    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() && BooleanUtils.isTrue(finishedParts);
    }

    @Override
    public boolean setFullyFinished() {
        boolean changed = super.setFullyFinished() || BooleanUtils.isNotTrue(finishedParts);
        finishedParts = true;
        return changed;
    }

    @Override
    public GamaMoney getDebt() {
        return GamaMoneyUtils.negated(BooleanUtils.isTrue(getReverseVAT()) ? getSubtotal() : getTotal());
    }

    @Override
    public GamaMoney getBaseDebt() {
        return GamaMoneyUtils.negated(BooleanUtils.isTrue(getReverseVAT()) ? getBaseSubtotal() : getBaseTotal());
    }

    @Override
    public void reset() {
        super.reset();
        finishedParts = false;
        if (parts != null) {
            for (PurchasePartSql part : parts) {
                part.reset();
            }
        }
    }

    // customized getters/setters

    @Override
    public boolean isIsafSpecialTaxation() {
        return isafSpecialTaxation != null && isafSpecialTaxation;
    }

    /**
     * If advance is true - no debt registered
     */
    @Override
    public Boolean getNoDebt() {
        return advance;
    }

    // generated
    // except getIsafSpecialTaxation()

    @Override
    public List<PurchasePartSql> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<PurchasePartSql> parts) {
        this.parts = parts;
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

    @Override
    public GamaMoney getBaseSubtotal() {
        return baseSubtotal;
    }

    public void setBaseSubtotal(GamaMoney baseSubtotal) {
        this.baseSubtotal = baseSubtotal;
    }

    @Override
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

    @Override
    public Boolean getFinishedParts() {
        return finishedParts;
    }

    @Override
    public void setFinishedParts(Boolean finishedParts) {
        this.finishedParts = finishedParts;
    }

    @Override
    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<DocExpense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<DocExpense> expenses) {
        this.expenses = expenses;
    }

    @Override
    public List<VATCodeTotal> getVatCodeTotals() {
        return vatCodeTotals;
    }

    public void setVatCodeTotals(List<VATCodeTotal> vatCodeTotals) {
        this.vatCodeTotals = vatCodeTotals;
    }

    @Override
    public String getIsafInvoiceType() {
        return isafInvoiceType;
    }

    public void setIsafInvoiceType(String isafInvoiceType) {
        this.isafInvoiceType = isafInvoiceType;
    }

    public void setIsafSpecialTaxation(Boolean isafSpecialTaxation) {
        this.isafSpecialTaxation = isafSpecialTaxation;
    }

    public Boolean getReverseVAT() {
        return reverseVAT;
    }

    public void setReverseVAT(Boolean reverseVAT) {
        this.reverseVAT = reverseVAT;
    }

    public Doc getOrder() {
        return order;
    }

    public void setOrder(Doc order) {
        this.order = order;
    }

    @Override
    public LocalDate getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDate regDate) {
        this.regDate = regDate;
    }

    public Boolean getAdvance() {
        return advance;
    }

    public void setAdvance(Boolean advance) {
        this.advance = advance;
    }

    @Override
    public String toString() {
        return "PurchaseSql{" +
                "parts=" + parts +
                ", subtotal=" + subtotal +
                ", taxTotal=" + taxTotal +
                ", total=" + total +
                ", baseSubtotal=" + baseSubtotal +
                ", baseTaxTotal=" + baseTaxTotal +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finishedParts=" + finishedParts +
                ", dueDate=" + dueDate +
                ", expenses=" + expenses +
                ", vatCodeTotals=" + vatCodeTotals +
                ", isafInvoiceType='" + isafInvoiceType + '\'' +
                ", isafSpecialTaxation=" + isafSpecialTaxation +
                ", reverseVAT=" + reverseVAT +
                ", order=" + order +
                ", regDate=" + regDate +
                ", advance=" + advance +
                "} " + super.toString();
    }
}