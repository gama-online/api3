package lt.gama.model.dto.documents;

import lt.gama.model.type.NameContact;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.PartOrderDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.type.GamaMoney;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private NameContact contact;

    private List<PartOrderDto> parts = new ArrayList<>();

    /**
     * Document's discount in percent
     */
    private Double discount;

    private GamaMoney subtotal;

    private GamaMoney taxTotal;

    private GamaMoney total;

    private GamaMoney baseSubtotal;

    private GamaMoney baseTaxTotal;

    private GamaMoney baseTotal;

    private GamaMoney prepayment;

    private GamaMoney currentSubtotal;

    private GamaMoney currentTaxTotal;

    private GamaMoney currentTotal;

    private WarehouseDto warehouse;

    private String tag;

    private LocalDate dueDate;

    /**
     * Links to generated invoices
     */
    private List<Doc> docs;

    private Boolean zeroVAT;


    public boolean isZeroVAT() {
        return zeroVAT != null && zeroVAT;
    }

    public NameContact getContact() {
        return contact;
    }

    public void setContact(NameContact contact) {
        this.contact = contact;
    }

    public List<PartOrderDto> getParts() {
        return parts;
    }

    public void setParts(List<PartOrderDto> parts) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OrderDto orderDto = (OrderDto) o;
        return Objects.equals(contact, orderDto.contact) && Objects.equals(parts, orderDto.parts) && Objects.equals(discount, orderDto.discount) && Objects.equals(subtotal, orderDto.subtotal) && Objects.equals(taxTotal, orderDto.taxTotal) && Objects.equals(total, orderDto.total) && Objects.equals(baseSubtotal, orderDto.baseSubtotal) && Objects.equals(baseTaxTotal, orderDto.baseTaxTotal) && Objects.equals(baseTotal, orderDto.baseTotal) && Objects.equals(prepayment, orderDto.prepayment) && Objects.equals(currentSubtotal, orderDto.currentSubtotal) && Objects.equals(currentTaxTotal, orderDto.currentTaxTotal) && Objects.equals(currentTotal, orderDto.currentTotal) && Objects.equals(warehouse, orderDto.warehouse) && Objects.equals(tag, orderDto.tag) && Objects.equals(dueDate, orderDto.dueDate) && Objects.equals(docs, orderDto.docs) && Objects.equals(zeroVAT, orderDto.zeroVAT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), contact, parts, discount, subtotal, taxTotal, total, baseSubtotal, baseTaxTotal, baseTotal, prepayment, currentSubtotal, currentTaxTotal, currentTotal, warehouse, tag, dueDate, docs, zeroVAT);
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "contact=" + contact +
                ", parts=" + parts +
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
