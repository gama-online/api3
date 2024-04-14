package lt.gama.model.dto.documents;

import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.LocationUtils;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.dto.base.BaseDebtDocumentDto;
import lt.gama.model.dto.documents.items.PartInvoiceDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.i.IDocPartsDto;
import lt.gama.model.i.IISaf;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.inventory.Packing;
import lt.gama.model.type.inventory.TaxFree;
import lt.gama.model.type.inventory.VATCodeTotal;
import lt.gama.model.type.l10n.LangInvoice;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@DynamicUpdate
public class InvoiceDto extends BaseDebtDocumentDto implements IDocPartsDto<PartInvoiceDto>, IISaf, ITranslations<LangInvoice> {

    @Serial
    private static final long serialVersionUID = -1L;

    private Location location;

    private NameContact contact;

    private List<PartInvoiceDto> parts = new ArrayList<>();

    private GamaMoney partsTotal;

    /**
     * Document's discount in percent
     */
    private Double discount;

    private GamaMoney discountTotal;

    /**
     *  subtotal = partsTotal - discountTotal
     */
    private GamaMoney subtotal;

    private GamaMoney taxTotal;

    private GamaMoney total;

    private GamaMoney baseSubtotal;

    private GamaMoney baseTaxTotal;

    private GamaMoney baseTotal;

    private GamaMoney costTotal;

    private WarehouseDto warehouse;

    private String tag;

    private Boolean finishedParts;

    private LocalDate dueDate;

    private String according;

    /**
     * if bank account is null - all banks account will be printed on invoice
     */
    private BankAccountDto account;

    private Boolean zeroVAT;

    /*
     * Transportation info
     */

    private String driver;

    private Location loadAddress;

    private Location unloadAddress;

    private String transportId;

    private String transportMarque;

    /**
     * VATs totals list
     */
    private List<VATCodeTotal> vatCodeTotals;

    /**
     * invoice type needed for iSAF export
     */
    private String isafInvoiceType;

    /*
     * if the VAT invoice is subject to the “monetary accounting system” (application of special procedure
     * for establishing the chargeable event according to Article 14(9) of the VAT Law).
     */
    private Boolean isafSpecialTaxation;


    /**
     * Note visible to customer.
     * Can be translated
     */
    private String invoiceNote;	// Up to 500 Unicode characters


    /*
     * ECR info
     */

    private Boolean ecr;

    private String ecrNo;

    private String ecrReceipt;

    /**
     * Packing list
     */
    private List<Packing> packing;

    /**
     * is reverse VAT?
     */
    private Boolean reverseVAT;

    /**
     * Payment id can be used in bank order to automatically link payment with invoice
     */
    private String paymentId;

    /**
     * Index made from date or regDate for iSAF in form of 'yyyymm'
     */
    private String isafIndex;


    /**
     * Translations
     */
    private Map<String, LangInvoice> translation;

    /**
     * Invoice for received advance payment.
     * G.L. operation with VAT only.
     * No debt are registered.
     */
    private Boolean advance;

    /**
     * print invoice as credit invoice even if positive amount
     */
    private Boolean creditInvoice;

    /**
     * LT Tax Free information
     */
    private TaxFree taxFree;

    public String getAddress() {
        return LocationUtils.isValid(location)
                ? location.getAddress()
                : getCounterparty() != null
                ? getCounterparty().getAddress()
                : "";
    }

    /**
     * Document registration date
     * @return document date
     */
    @Override
    public LocalDate getRegDate() {
        return getDate();
    }

    @Override
    public GamaMoney getDebt() {
        return BooleanUtils.isTrue(getReverseVAT()) ? getSubtotal() : getTotal();
    }

    @Override
    public GamaMoney getBaseDebt() {
        return BooleanUtils.isTrue(getReverseVAT()) ? getBaseSubtotal() : getBaseTotal();
    }

    // customized getters/setters

    /**
     * If advance is true - no debt registered
     */
    @Override
    public Boolean getNoDebt() {
        return advance;
    }

    public boolean isZeroVAT() {
        return zeroVAT != null && zeroVAT;
    }

    @Override
    public boolean isIsafSpecialTaxation() {
        return isafSpecialTaxation != null && isafSpecialTaxation;
    }

    // generated
    // except getIsafSpecialTaxation(), getZeroVAT()

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public NameContact getContact() {
        return contact;
    }

    public void setContact(NameContact contact) {
        this.contact = contact;
    }

    @Override
    public List<PartInvoiceDto> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<PartInvoiceDto> parts) {
        this.parts = parts;
    }

    public GamaMoney getPartsTotal() {
        return partsTotal;
    }

    public void setPartsTotal(GamaMoney partsTotal) {
        this.partsTotal = partsTotal;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public GamaMoney getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(GamaMoney discountTotal) {
        this.discountTotal = discountTotal;
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

    public GamaMoney getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
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

    public String getAccording() {
        return according;
    }

    public void setAccording(String according) {
        this.according = according;
    }

    public BankAccountDto getAccount() {
        return account;
    }

    public void setAccount(BankAccountDto account) {
        this.account = account;
    }

    public void setZeroVAT(Boolean zeroVAT) {
        this.zeroVAT = zeroVAT;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Location getLoadAddress() {
        return loadAddress;
    }

    public void setLoadAddress(Location loadAddress) {
        this.loadAddress = loadAddress;
    }

    public Location getUnloadAddress() {
        return unloadAddress;
    }

    public void setUnloadAddress(Location unloadAddress) {
        this.unloadAddress = unloadAddress;
    }

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    public String getTransportMarque() {
        return transportMarque;
    }

    public void setTransportMarque(String transportMarque) {
        this.transportMarque = transportMarque;
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

    public String getInvoiceNote() {
        return invoiceNote;
    }

    public void setInvoiceNote(String invoiceNote) {
        this.invoiceNote = invoiceNote;
    }

    public Boolean getEcr() {
        return ecr;
    }

    public void setEcr(Boolean ecr) {
        this.ecr = ecr;
    }

    public String getEcrNo() {
        return ecrNo;
    }

    public void setEcrNo(String ecrNo) {
        this.ecrNo = ecrNo;
    }

    public String getEcrReceipt() {
        return ecrReceipt;
    }

    public void setEcrReceipt(String ecrReceipt) {
        this.ecrReceipt = ecrReceipt;
    }

    public List<Packing> getPacking() {
        return packing;
    }

    public void setPacking(List<Packing> packing) {
        this.packing = packing;
    }

    public Boolean getReverseVAT() {
        return reverseVAT;
    }

    public void setReverseVAT(Boolean reverseVAT) {
        this.reverseVAT = reverseVAT;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getIsafIndex() {
        return isafIndex;
    }

    public void setIsafIndex(String isafIndex) {
        this.isafIndex = isafIndex;
    }

    @Override
    public Map<String, LangInvoice> getTranslation() {
        return translation;
    }

    @Override
    public void setTranslation(Map<String, LangInvoice> translation) {
        this.translation = translation;
    }

    public Boolean getAdvance() {
        return advance;
    }

    public void setAdvance(Boolean advance) {
        this.advance = advance;
    }

    public Boolean getCreditInvoice() {
        return creditInvoice;
    }

    public void setCreditInvoice(Boolean creditInvoice) {
        this.creditInvoice = creditInvoice;
    }

    public TaxFree getTaxFree() {
        return taxFree;
    }

    public void setTaxFree(TaxFree taxFree) {
        this.taxFree = taxFree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InvoiceDto that = (InvoiceDto) o;
        return Objects.equals(location, that.location) && Objects.equals(contact, that.contact) && Objects.equals(parts, that.parts) && Objects.equals(partsTotal, that.partsTotal) && Objects.equals(discount, that.discount) && Objects.equals(discountTotal, that.discountTotal) && Objects.equals(subtotal, that.subtotal) && Objects.equals(taxTotal, that.taxTotal) && Objects.equals(total, that.total) && Objects.equals(baseSubtotal, that.baseSubtotal) && Objects.equals(baseTaxTotal, that.baseTaxTotal) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(costTotal, that.costTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(tag, that.tag) && Objects.equals(finishedParts, that.finishedParts) && Objects.equals(dueDate, that.dueDate) && Objects.equals(according, that.according) && Objects.equals(account, that.account) && Objects.equals(zeroVAT, that.zeroVAT) && Objects.equals(driver, that.driver) && Objects.equals(loadAddress, that.loadAddress) && Objects.equals(unloadAddress, that.unloadAddress) && Objects.equals(transportId, that.transportId) && Objects.equals(transportMarque, that.transportMarque) && Objects.equals(vatCodeTotals, that.vatCodeTotals) && Objects.equals(isafInvoiceType, that.isafInvoiceType) && Objects.equals(isafSpecialTaxation, that.isafSpecialTaxation) && Objects.equals(invoiceNote, that.invoiceNote) && Objects.equals(ecr, that.ecr) && Objects.equals(ecrNo, that.ecrNo) && Objects.equals(ecrReceipt, that.ecrReceipt) && Objects.equals(packing, that.packing) && Objects.equals(reverseVAT, that.reverseVAT) && Objects.equals(paymentId, that.paymentId) && Objects.equals(isafIndex, that.isafIndex) && Objects.equals(translation, that.translation) && Objects.equals(advance, that.advance) && Objects.equals(creditInvoice, that.creditInvoice) && Objects.equals(taxFree, that.taxFree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), location, contact, parts, partsTotal, discount, discountTotal, subtotal, taxTotal, total, baseSubtotal, baseTaxTotal, baseTotal, costTotal, warehouse, tag, finishedParts, dueDate, according, account, zeroVAT, driver, loadAddress, unloadAddress, transportId, transportMarque, vatCodeTotals, isafInvoiceType, isafSpecialTaxation, invoiceNote, ecr, ecrNo, ecrReceipt, packing, reverseVAT, paymentId, isafIndex, translation, advance, creditInvoice, taxFree);
    }

    @Override
    public String toString() {
        return "InvoiceDto{" +
                "location=" + location +
                ", contact=" + contact +
                ", parts=" + parts +
                ", partsTotal=" + partsTotal +
                ", discount=" + discount +
                ", discountTotal=" + discountTotal +
                ", subtotal=" + subtotal +
                ", taxTotal=" + taxTotal +
                ", total=" + total +
                ", baseSubtotal=" + baseSubtotal +
                ", baseTaxTotal=" + baseTaxTotal +
                ", baseTotal=" + baseTotal +
                ", costTotal=" + costTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finishedParts=" + finishedParts +
                ", dueDate=" + dueDate +
                ", according='" + according + '\'' +
                ", account=" + account +
                ", zeroVAT=" + zeroVAT +
                ", driver='" + driver + '\'' +
                ", loadAddress=" + loadAddress +
                ", unloadAddress=" + unloadAddress +
                ", transportId='" + transportId + '\'' +
                ", transportMarque='" + transportMarque + '\'' +
                ", vatCodeTotals=" + vatCodeTotals +
                ", isafInvoiceType='" + isafInvoiceType + '\'' +
                ", isafSpecialTaxation=" + isafSpecialTaxation +
                ", invoiceNote='" + invoiceNote + '\'' +
                ", ecr=" + ecr +
                ", ecrNo='" + ecrNo + '\'' +
                ", ecrReceipt='" + ecrReceipt + '\'' +
                ", packing=" + packing +
                ", reverseVAT=" + reverseVAT +
                ", paymentId='" + paymentId + '\'' +
                ", isafIndex='" + isafIndex + '\'' +
                ", translation=" + translation +
                ", advance=" + advance +
                ", creditInvoice=" + creditInvoice +
                ", taxFree=" + taxFree +
                "} " + super.toString();
    }
}