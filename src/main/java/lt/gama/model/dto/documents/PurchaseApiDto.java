package lt.gama.model.dto.documents;

import io.swagger.v3.oas.annotations.Hidden;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocExpense;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.dto.base.BaseDebtDocumentDto;
import lt.gama.model.dto.base.BaseNumberDocumentDto;
import lt.gama.model.dto.entities.PartPurchaseApiDto;
import lt.gama.model.dto.i.IPartsDto;
import lt.gama.model.i.IISaf;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.inventory.VATCodeTotal;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

public class PurchaseApiDto extends BaseDebtDocumentDto implements IPartsDto<PartPurchaseApiDto>, IISaf {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<PartPurchaseApiDto> parts;

    private GamaMoney subtotal;

    private GamaMoney taxTotal;

    private GamaMoney total;

    private GamaMoney baseSubtotal;

    private GamaMoney baseTaxTotal;

    private GamaMoney baseTotal;

    private DocWarehouse warehouse;

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
     * Document registration date if not the same as document {@link BaseNumberDocumentDto#getDate() date}
     */
    private LocalDate regDate;

    /**
     * Invoice for paid advance payment.
     * G.L. operation with VAT only.
     * No debt are registered.
     */
    private Boolean advance;


    @Hidden
    @Override
    public GamaMoney getDebt() {
        return GamaMoneyUtils.negated(BooleanUtils.isTrue(getReverseVAT()) ? getSubtotal() : getTotal());
    }

    @Hidden
    @Override
    public GamaMoney getBaseDebt() {
        return GamaMoneyUtils.negated(BooleanUtils.isTrue(getReverseVAT()) ? getBaseSubtotal() : getBaseTotal());
    }

    // customized getters/setters

    @Override
    public boolean isIsafSpecialTaxation() {
        return isafSpecialTaxation != null && isafSpecialTaxation;
    }

    // generated
    // except getIsafSpecialTaxation()

    @Override
    public List<PartPurchaseApiDto> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<PartPurchaseApiDto> parts) {
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

    @Override
    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
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
        return "PurchaseDto{" +
                "parts=" + parts +
                ", subtotal=" + subtotal +
                ", taxTotal=" + taxTotal +
                ", total=" + total +
                ", baseSubtotal=" + baseSubtotal +
                ", baseTaxTotal=" + baseTaxTotal +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
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
