package lt.gama.model.sql.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.i.IPart;
import lt.gama.model.i.IPrice;
import lt.gama.model.i.ITranslations;
import lt.gama.model.i.IVat;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Packaging;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.l10n.LangPart;
import lt.gama.model.type.part.PartRemainder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.*;

/**
 * Products / services / bundles
 */
@Entity
@Table(name = "parts")
@NamedEntityGraphs({
        @NamedEntityGraph(name = PartSql.GRAPH_ALL,
                attributeNodes = {
                        @NamedAttributeNode(PartSql_.VENDOR),
                        @NamedAttributeNode(PartSql_.MANUFACTURER)
                }
        ),
        @NamedEntityGraph(name = PartSql.GRAPH_VENDOR, attributeNodes = @NamedAttributeNode(PartSql_.VENDOR)),
        @NamedEntityGraph(name = PartSql.GRAPH_MANUFACTURER, attributeNodes = @NamedAttributeNode(PartSql_.MANUFACTURER))
})
//TODO *** fix after migration. ***
// IPrice added here and to the part. Part- getPrice needed in inventoryService getDiscount and calcActualPrice methods,
// IPartExt renamed to IVat- IVat similar to IVatRate just has no getVat, setVat methods, PartSql has no vat property.
public class PartSql extends BaseCompanySql implements IPart, ITranslations<LangPart>, IPrice, IVat {

    public static final String GRAPH_ALL = "graph.PartSql.all";
    public static final String GRAPH_VENDOR = "graph.PartSql.vendor";
    public static final String GRAPH_MANUFACTURER = "graph.PartSql.manufacturer";

    /**
     * Part type - Service / Product / Product with Serials
     */
    private PartType type;

    private String name;

    private String description;

    private String sku;

    /**
     * If barcode is automatically generated.
     * Used for frontend to backend communications only
     */
    @Transient
    private Boolean autoBarcode;

    String barcode;

    private String unit;

    @Embedded
    private GamaBigMoney price;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"parent"}, allowSetters = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<PartPartSql> parts = new ArrayList<>();

    /*
     * accounting
     */

    @Embedded
    private GLOperationAccount accountAsset;

    @Embedded
    private GLDC glIncome;

    @Embedded
    private GLDC glExpense;

    /**
     * VAT info
     */
    private Boolean taxable;

    /**
     * VAT rate in percent (always >= 0)
     * p.s. can be taxable but with vatRate = 0. There is difference in tax between vat = 0 and no vat.
     */
    @Deprecated
    @Transient
    private Double vatRate;

    /**
     * Lookup to VAT rate info
     */
    private String vatRateCode;

    /**
     * Custom fields values
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<CFValue> cf;

    /**
     * Remainders by warehouse
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, PartRemainder> remainder;

    /**
     * Total remainders quantity
     */
    private BigDecimal quantityTotal;

    /**
     * Total remainders cost
     */
    @Embedded
    private GamaMoney costTotal;

    /**
     * Not DB field - used in reports only
     */
    @Transient
    private List<InventoryNowSql> remaindersNow;

    /**
     * Allow forward sell, i.e. to have negative remainder.
     */
    private Boolean forwardSell;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private ManufacturerSql manufacturer;

    /*
     * Packing info:
     */

    private BigDecimal brutto;

    private BigDecimal netto;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Packaging> packaging;

    /**
     * Units of Weight: kg, g
     */
    private String unitsWeight;

    /**
     * How many units are in one package (default 1)
     */
    private Integer packUnits;

    /**
     * Pack length
     */
    private BigDecimal length;

    /**
     * Pack width
     */
    private BigDecimal width;

    /**
     * Pack height
     */
    private BigDecimal height;

    /**
     * Units of Length: m, mm, cm
     */
    private String unitsLength;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private CounterpartySql vendor;

    /**
     * Part code in vendor system.
     * Can be used in purchase order.
     */
    private String vendorCode;

    /**
     * Translations
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, LangPart> translation;

    public PartSql() {
    }

    public PartSql(String name) {
        this.name = name;
    }

    public String toMessage() {
        return ((sku == null ? "" : sku) + ' ' + (name == null ? "" : name)).trim();
    }

    /**
     * Return as array to frontend
     *
     * @return map as array
     */
    public Collection<PartRemainder> getRemainders() {
        return remainder == null ? null : remainder.values();
    }

    /**
     * Do nothing - need for endpoint serialization
     *
     * @param remainders - none
     */
    private void setRemainders(Collection<PartRemainder> remainders) {
    }

    public void updateRemainder(DocWarehouse warehouse, BigDecimal quantity, GamaMoney cost) {
        // update remainder
        setQuantityTotal(BigDecimalUtils.add(getQuantityTotal(), quantity));
        setCostTotal(GamaMoneyUtils.add(getCostTotal(), cost));

        if (getRemainder() == null) setRemainder(new HashMap<>());
        PartRemainder remainder = getRemainder().remove(warehouse.getId());
        if (remainder == null) remainder = new PartRemainder();
        remainder.setWarehouse(warehouse);
        remainder.setQuantity(BigDecimalUtils.add(remainder.getQuantity(), quantity));
        remainder.setCost(GamaMoneyUtils.add(remainder.getCost(), cost));

        if (!GamaMoneyUtils.isZero(remainder.getCost()) || !BigDecimalUtils.isZero(remainder.getQuantity())) {
            getRemainder().put(remainder.getWarehouse().getId(), remainder);
        }
    }

    public void reset() {
        quantityTotal = null;
        costTotal = null;
        remainder = null;
    }

    private GLDC checkGLOnSave(GLDC gldc) {
        if (gldc == null || Validators.isValid(gldc.getDebit()) && Validators.isValid(gldc.getCredit())) return gldc;
        if (Validators.isValid(gldc.getDebit())) gldc.setCredit(gldc.getDebit());
        else if (Validators.isValid(gldc.getCredit())) gldc.setDebit(gldc.getCredit());
        else gldc = null;

        return gldc;
    }

    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        if (PartType.SERVICE.equals(getType())) setForwardSell(null);

        glExpense = checkGLOnSave(glExpense);
        glIncome = checkGLOnSave(glIncome);
    }

    // customized getters/setters

    @Override
    public boolean isTaxable() {
        return taxable != null && taxable;
    }

    public boolean unknownTaxable() {
        return taxable == null;
    }

    @Override
    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public boolean isForwardSell() {
        return forwardSell != null && forwardSell;
    }

    // generated

    @Override
    public PartType getType() {
        return type;
    }

    @Override
    public void setType(PartType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ManufacturerSql getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(ManufacturerSql manufacturer) {
        this.manufacturer = manufacturer;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getSku() {
        return sku;
    }

    @Override
    public void setSku(String sku) {
        this.sku = sku;
    }

    public Boolean getAutoBarcode() {
        return autoBarcode;
    }

    public void setAutoBarcode(Boolean autoBarcode) {
        this.autoBarcode = autoBarcode;
    }

    @Override
    public String getBarcode() {
        return barcode;
    }

    @Override
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public GamaBigMoney getPrice() {
        return price;
    }

    public void setPrice(GamaBigMoney price) {
        this.price = price;
    }

    public List<PartPartSql> getParts() {
        return parts;
    }

    public void setParts(List<PartPartSql> parts) {
        this.parts = parts;
    }

    @Override
    public GLOperationAccount getAccountAsset() {
        return accountAsset;
    }

    @Override
    public void setAccountAsset(GLOperationAccount accountAsset) {
        this.accountAsset = accountAsset;
    }

    @Override
    public GLDC getGlIncome() {
        return glIncome;
    }

    @Override
    public void setGlIncome(GLDC glIncome) {
        this.glIncome = glIncome;
    }

    @Override
    public GLDC getGlExpense() {
        return glExpense;
    }

    @Override
    public void setGlExpense(GLDC glExpense) {
        this.glExpense = glExpense;
    }

    @Deprecated
    public Double getVatRate() {
        return vatRate;
    }

    @Deprecated
    public void setVatRate(Double vatRate) {
        this.vatRate = vatRate;
    }

    @Override
    public String getVatRateCode() {
        return vatRateCode;
    }

    @Override
    public void setVatRateCode(String vatRateCode) {
        this.vatRateCode = vatRateCode;
    }

    @Override
    public List<CFValue> getCf() {
        return cf;
    }

    @Override
    public void setCf(List<CFValue> cf) {
        this.cf = cf;
    }

    public Map<Long, PartRemainder> getRemainder() {
        return remainder;
    }

    public void setRemainder(Map<Long, PartRemainder> remainder) {
        this.remainder = remainder;
    }

    public BigDecimal getQuantityTotal() {
        return quantityTotal;
    }

    public void setQuantityTotal(BigDecimal quantityTotal) {
        this.quantityTotal = quantityTotal;
    }

    public GamaMoney getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    public List<InventoryNowSql> getRemaindersNow() {
        return remaindersNow;
    }

    public void setRemaindersNow(List<InventoryNowSql> remaindersNow) {
        this.remaindersNow = remaindersNow;
    }

    public Boolean getForwardSell() {
        return forwardSell;
    }

    public void setForwardSell(Boolean forwardSell) {
        this.forwardSell = forwardSell;
    }

    @Override
    public BigDecimal getBrutto() {
        return brutto;
    }

    @Override
    public void setBrutto(BigDecimal brutto) {
        this.brutto = brutto;
    }

    @Override
    public BigDecimal getNetto() {
        return netto;
    }

    @Override
    public void setNetto(BigDecimal netto) {
        this.netto = netto;
    }

    public List<Packaging> getPackaging() {
        return packaging;
    }

    public void setPackaging(List<Packaging> packaging) {
        this.packaging = packaging;
    }

    public String getUnitsWeight() {
        return unitsWeight;
    }

    public void setUnitsWeight(String unitsWeight) {
        this.unitsWeight = unitsWeight;
    }

    public Integer getPackUnits() {
        return packUnits;
    }

    public void setPackUnits(Integer packUnits) {
        this.packUnits = packUnits;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public String getUnitsLength() {
        return unitsLength;
    }

    public void setUnitsLength(String unitsLength) {
        this.unitsLength = unitsLength;
    }

    public CounterpartySql getVendor() {
        return vendor;
    }

    public void setVendor(CounterpartySql vendor) {
        this.vendor = vendor;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    @Override
    public Map<String, LangPart> getTranslation() {
        return translation;
    }

    @Override
    public void setTranslation(Map<String, LangPart> translation) {
        this.translation = translation;
    }

    @Override
    public String toString() {
        return "PartSql{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", autoBarcode=" + autoBarcode +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", price=" + price +
                ", accountAsset=" + accountAsset +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                ", taxable=" + taxable +
                ", vatRate=" + vatRate +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", cf=" + cf +
                ", remainder=" + remainder +
                ", quantityTotal=" + quantityTotal +
                ", costTotal=" + costTotal +
                ", remaindersNow=" + remaindersNow +
                ", forwardSell=" + forwardSell +
                ", brutto=" + brutto +
                ", netto=" + netto +
                ", packaging=" + packaging +
                ", unitsWeight='" + unitsWeight + '\'' +
                ", packUnits=" + packUnits +
                ", length=" + length +
                ", width=" + width +
                ", height=" + height +
                ", unitsLength='" + unitsLength + '\'' +
                ", vendorCode='" + vendorCode + '\'' +
                ", translation=" + translation +
                "} " + super.toString();
    }
}
