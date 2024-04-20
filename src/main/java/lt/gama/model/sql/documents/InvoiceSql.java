package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.i.IDocPartsSql;
import lt.gama.model.i.IISaf;
import lt.gama.model.i.ITranslations;
import lt.gama.model.sql.base.BaseDebtDocumentSql;
import lt.gama.model.sql.documents.items.InvoiceBasePartSql;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.inventory.Packing;
import lt.gama.model.type.inventory.TaxFree;
import lt.gama.model.type.inventory.VATCodeTotal;
import lt.gama.model.type.l10n.LangInvoice;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "invoice")
@NamedEntityGraphs({
        @NamedEntityGraph(name = InvoiceSql.GRAPH_ALL, attributeNodes = {
                @NamedAttributeNode(InvoiceSql_.COUNTERPARTY),
                @NamedAttributeNode(InvoiceSql_.EMPLOYEE),
                @NamedAttributeNode(InvoiceSql_.WAREHOUSE),
                @NamedAttributeNode(InvoiceSql_.ACCOUNT),
                @NamedAttributeNode(InvoiceSql_.PARTS)
        }),
        @NamedEntityGraph(name = InvoiceSql.GRAPH_NO_PARTS, attributeNodes = {
                @NamedAttributeNode(InvoiceSql_.COUNTERPARTY),
                @NamedAttributeNode(InvoiceSql_.EMPLOYEE),
                @NamedAttributeNode(InvoiceSql_.WAREHOUSE),
                @NamedAttributeNode(InvoiceSql_.ACCOUNT)
        })
})
public class InvoiceSql extends BaseDebtDocumentSql implements IDocPartsSql<InvoiceBasePartSql>, IISaf, ITranslations<LangInvoice> {

    public static final String GRAPH_ALL = "graph.InvoiceSql.all";
    public static final String GRAPH_NO_PARTS = "graph.InvoiceSql.noParts";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<InvoiceBasePartSql> parts = new ArrayList<>();

    @Embedded
    private Location location;

    @Embedded
    private NameContact contact;

    @Embedded
    private GamaMoney partsTotal;

    /**
     * Document's discount in percent
     */
    private Double discount;

    @Embedded
    private GamaMoney discountTotal;

    /**
     *  subtotal = partsTotal - discountTotal
     */
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
    private GamaMoney costTotal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    private Boolean finishedParts;

    private LocalDate dueDate;

    private String according;

    /**
     * if bank account is null - all banks account will be printed on invoice
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private BankAccountSql account;

    private Boolean zeroVAT;

    /*
     * Transportation info
     */

    private String driver;

    @Embedded
    private Location loadAddress;

    @Embedded
    private Location unloadAddress;

    private String transportId;

    private String transportMarque;

    /**
     * VATs totals list
     */
    @JdbcTypeCode(SqlTypes.JSON)
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
    @JdbcTypeCode(SqlTypes.JSON)
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
     * Translations
     */
    @JdbcTypeCode(SqlTypes.JSON)
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
    @JdbcTypeCode(SqlTypes.JSON)
    private TaxFree taxFree;

    /**
     * Document registration date
     * @return document date
     */
    @Override
    public LocalDate getRegDate() {
        return getDate();
    }

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
        return BooleanUtils.isTrue(getReverseVAT()) ? getSubtotal() : getTotal();
    }

    @Override
    public GamaMoney getBaseDebt() {
        return BooleanUtils.isTrue(getReverseVAT()) ? getBaseSubtotal() : getBaseTotal();
    }

    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        costTotal = null;
        if (parts != null) {
            for (InvoiceBasePartSql part : parts) {
                costTotal = GamaMoneyUtils.add(costTotal, part.getCostTotal());
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        finishedParts = false;
        if (parts != null) {
            for (InvoiceBasePartSql part : parts) {
                part.reset();
            }
        }
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
    public List<InvoiceBasePartSql> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<InvoiceBasePartSql> parts) {
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

    public String getAccording() {
        return according;
    }

    public void setAccording(String according) {
        this.according = according;
    }

    public BankAccountSql getAccount() {
        return account;
    }

    public void setAccount(BankAccountSql account) {
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
    public String toString() {
        return "InvoiceSql{" +
                ", location=" + location +
                ", contact=" + contact +
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
                ", translation=" + translation +
                ", advance=" + advance +
                ", creditInvoice=" + creditInvoice +
                ", taxFree=" + taxFree +
                "} " + super.toString();
    }
}