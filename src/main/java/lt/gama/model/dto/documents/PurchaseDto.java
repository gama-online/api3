package lt.gama.model.dto.documents;

import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocExpense;
import lt.gama.model.dto.base.BaseDebtDocumentDto;
import lt.gama.model.dto.documents.items.PartPurchaseDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.i.IDocPartsDto;
import lt.gama.model.i.IISaf;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.inventory.VATCodeTotal;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseDto extends BaseDebtDocumentDto implements IDocPartsDto<PartPurchaseDto>, IISaf {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<PartPurchaseDto> parts = new ArrayList<>();

    private GamaMoney subtotal;

    private GamaMoney taxTotal;

    private GamaMoney total;

    private GamaMoney baseSubtotal;

    private GamaMoney baseTaxTotal;

    private GamaMoney baseTotal;

    private WarehouseDto warehouse;

    private String tag;

    private Boolean finishedParts;

    private LocalDate dueDate;

    private List<DocExpense> expenses;

    /**
     * VATs totals list
     */
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
    private Doc order;

    /**
     * Document registration date if not the same as document
     */
    private LocalDate regDate;

    /**
     * Index made from date or regDate for iSAF in form of 'yyyymm'
     */
    private String isafIndex;

    /**
     * Invoice for paid advance payment.
     * G.L. operation with VAT only.
     * No debt are registered.
     */
    private Boolean advance;

    @Override
    public GamaMoney getDebt() {
        return GamaMoneyUtils.negated(BooleanUtils.isTrue(getReverseVAT()) ? getSubtotal() : getTotal());
    }

    @Override
    public GamaMoney getBaseDebt() {
        return GamaMoneyUtils.negated(BooleanUtils.isTrue(getReverseVAT()) ? getBaseSubtotal() : getBaseTotal());
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
    public List<PartPurchaseDto> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<PartPurchaseDto> parts) {
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

    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDto warehouse) {
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

    public String getIsafIndex() {
        return isafIndex;
    }

    public void setIsafIndex(String isafIndex) {
        this.isafIndex = isafIndex;
    }

    public Boolean getAdvance() {
        return advance;
    }

    public void setAdvance(Boolean advance) {
        this.advance = advance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PurchaseDto that = (PurchaseDto) o;
        return Objects.equals(parts, that.parts) && Objects.equals(subtotal, that.subtotal) && Objects.equals(taxTotal, that.taxTotal) && Objects.equals(total, that.total) && Objects.equals(baseSubtotal, that.baseSubtotal) && Objects.equals(baseTaxTotal, that.baseTaxTotal) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(tag, that.tag) && Objects.equals(finishedParts, that.finishedParts) && Objects.equals(dueDate, that.dueDate) && Objects.equals(expenses, that.expenses) && Objects.equals(vatCodeTotals, that.vatCodeTotals) && Objects.equals(isafInvoiceType, that.isafInvoiceType) && Objects.equals(isafSpecialTaxation, that.isafSpecialTaxation) && Objects.equals(reverseVAT, that.reverseVAT) && Objects.equals(order, that.order) && Objects.equals(regDate, that.regDate) && Objects.equals(isafIndex, that.isafIndex) && Objects.equals(advance, that.advance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parts, subtotal, taxTotal, total, baseSubtotal, baseTaxTotal, baseTotal, warehouse, tag, finishedParts, dueDate, expenses, vatCodeTotals, isafInvoiceType, isafSpecialTaxation, reverseVAT, order, regDate, isafIndex, advance);
    }

    @Override
    public String toString() {
        return "PurchaseDto{" +
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
                ", isafIndex='" + isafIndex + '\'' +
                ", advance=" + advance +
                "} " + super.toString();
    }
}