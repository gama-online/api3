package lt.gama.model.dto.entities;

import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.part.PartRemainder;
import lt.gama.model.type.part.VATRate;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.i.IVatRate;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Packaging;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.*;

public class PartDto extends BaseCompanyDto implements IPart, ITranslations<LangPart>, IVatRate {

    @Serial
    private static final long serialVersionUID = -3;

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
    private Boolean autoBarcode;

    private String barcode;

    private String unit;

    private GamaBigMoney price;

    private List<PartPartDto> parts = new ArrayList<>();

    /*
     * accounting
     */

    private GLOperationAccount accountAsset;

    @Deprecated
    private GLOperationAccount accountIncome;

    @Deprecated
    private GLOperationAccount accountExpense;

    private GLDC glIncome;

    private GLDC glExpense;

    /*
     * VAT info
     */

    @Deprecated
    private GLOperationAccount glVATRec;

    @Deprecated
    private GLOperationAccount glVATPay;

    private Boolean taxable;

    /**
     * VAT rate in percent (always >= 0)
     * p.s. can be taxable but with vatRate = 0. There is difference in tax between vat = 0 and no vat.
     */
    @Deprecated
    private Double vatRate;

    /**
     * Lookup to VAT rate info
     */
    private String vatRateCode;

    /**
     * Used in frontend only
     */
    private VATRate vat;

    /**
     * Custom fields values
     */
    private List<CFValue> cf;

    /**
     * Remainders by warehouse
     */
    private Map<Long, PartRemainder> remainder;

    private Set<DocWarehouse> usedWarehouses = new HashSet<>();

    /**
     * Total remainders quantity
     */
    private BigDecimal quantityTotal;

    /**
     * Total remainders cost
     */
    private GamaMoney costTotal;

    /**
     * Not DB field - used in reports only
     */
    private List<InventoryNowDto> remaindersNow;

    /**
     * Allow forward sell, i.e. to have negative remainder.
     */
    private Boolean forwardSell;

    private ManufacturerDto manufacturer;

    /*
     * Packing info:
     */

    private BigDecimal brutto;

    private BigDecimal netto;

    private List<Packaging> packaging;

    /**
     * units of Weight: kg, g
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
     * units of Length: m, mm, cm
     */
    private String unitsLength;

    private CounterpartyDto vendor;

    /**
     * Part code in vendor system.
     * Can be used in purchase order.
     */
    private String vendorCode;

    /**
     * Translations
     */
    private Map<String, LangPart> translation;

    public PartDto() {
    }

    public String toMessage() {
        return ((sku == null ? "" : sku) + ' ' + (name == null ? "" : name)).trim();
    }

    /**
     * Return as array to frontend
     * @return map as array
     */
    public Collection<PartRemainder> getRemainders() {
        return remainder == null ? null : remainder.values();
    }

    /**
     * Do nothing - need for endpoint serialization
     * @param remainders - none
     */
    private void setRemainders(Collection<GamaMoney> remainders) {}

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

    // customized getters/setters

    @Override
    public boolean isTaxable() {
        return taxable != null && taxable;
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

    public GamaBigMoney getPrice() {
        return price;
    }

    public void setPrice(GamaBigMoney price) {
        this.price = price;
    }

    public List<PartPartDto> getParts() {
        return parts;
    }

    public void setParts(List<PartPartDto> parts) {
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

    public GLOperationAccount getAccountIncome() {
        return accountIncome;
    }

    public void setAccountIncome(GLOperationAccount accountIncome) {
        this.accountIncome = accountIncome;
    }

    public GLOperationAccount getAccountExpense() {
        return accountExpense;
    }

    public void setAccountExpense(GLOperationAccount accountExpense) {
        this.accountExpense = accountExpense;
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

    public GLOperationAccount getGlVATRec() {
        return glVATRec;
    }

    public void setGlVATRec(GLOperationAccount glVATRec) {
        this.glVATRec = glVATRec;
    }

    public GLOperationAccount getGlVATPay() {
        return glVATPay;
    }

    public void setGlVATPay(GLOperationAccount glVATPay) {
        this.glVATPay = glVATPay;
    }

    public Double getVatRate() {
        return vatRate;
    }

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
    public VATRate getVat() {
        return vat;
    }

    @Override
    public void setVat(VATRate vat) {
        this.vat = vat;
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

    @Override
    public ManufacturerDto getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(ManufacturerDto manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Set<DocWarehouse> getUsedWarehouses() {
        return usedWarehouses;
    }

    public void setUsedWarehouses(Set<DocWarehouse> usedWarehouses) {
        this.usedWarehouses = usedWarehouses;
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

    public List<InventoryNowDto> getRemaindersNow() {
        return remaindersNow;
    }

    public void setRemaindersNow(List<InventoryNowDto> remaindersNow) {
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

    public CounterpartyDto getVendor() {
        return vendor;
    }

    public void setVendor(CounterpartyDto vendor) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartDto partDto = (PartDto) o;
        return type == partDto.type && Objects.equals(name, partDto.name) && Objects.equals(description, partDto.description) && Objects.equals(sku, partDto.sku) && Objects.equals(autoBarcode, partDto.autoBarcode) && Objects.equals(barcode, partDto.barcode) && Objects.equals(unit, partDto.unit) && Objects.equals(price, partDto.price) && Objects.equals(parts, partDto.parts) && Objects.equals(accountAsset, partDto.accountAsset) && Objects.equals(accountIncome, partDto.accountIncome) && Objects.equals(accountExpense, partDto.accountExpense) && Objects.equals(glIncome, partDto.glIncome) && Objects.equals(glExpense, partDto.glExpense) && Objects.equals(glVATRec, partDto.glVATRec) && Objects.equals(glVATPay, partDto.glVATPay) && Objects.equals(taxable, partDto.taxable) && Objects.equals(vatRate, partDto.vatRate) && Objects.equals(vatRateCode, partDto.vatRateCode) && Objects.equals(vat, partDto.vat) && Objects.equals(cf, partDto.cf) && Objects.equals(remainder, partDto.remainder) && Objects.equals(usedWarehouses, partDto.usedWarehouses) && Objects.equals(quantityTotal, partDto.quantityTotal) && Objects.equals(costTotal, partDto.costTotal) && Objects.equals(remaindersNow, partDto.remaindersNow) && Objects.equals(forwardSell, partDto.forwardSell) && Objects.equals(manufacturer, partDto.manufacturer) && Objects.equals(brutto, partDto.brutto) && Objects.equals(netto, partDto.netto) && Objects.equals(packaging, partDto.packaging) && Objects.equals(unitsWeight, partDto.unitsWeight) && Objects.equals(packUnits, partDto.packUnits) && Objects.equals(length, partDto.length) && Objects.equals(width, partDto.width) && Objects.equals(height, partDto.height) && Objects.equals(unitsLength, partDto.unitsLength) && Objects.equals(vendor, partDto.vendor) && Objects.equals(vendorCode, partDto.vendorCode) && Objects.equals(translation, partDto.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, name, description, sku, autoBarcode, barcode, unit, price, parts, accountAsset, accountIncome, accountExpense, glIncome, glExpense, glVATRec, glVATPay, taxable, vatRate, vatRateCode, vat, cf, remainder, usedWarehouses, quantityTotal, costTotal, remaindersNow, forwardSell, manufacturer, brutto, netto, packaging, unitsWeight, packUnits, length, width, height, unitsLength, vendor, vendorCode, translation);
    }

    @Override
    public String toString() {
        return "PartDto{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", autoBarcode=" + autoBarcode +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", price=" + price +
                ", parts=" + parts +
                ", accountAsset=" + accountAsset +
                ", accountIncome=" + accountIncome +
                ", accountExpense=" + accountExpense +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                ", glVATRec=" + glVATRec +
                ", glVATPay=" + glVATPay +
                ", taxable=" + taxable +
                ", vatRate=" + vatRate +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", vat=" + vat +
                ", cf=" + cf +
                ", remainder=" + remainder +
                ", usedWarehouses=" + usedWarehouses +
                ", quantityTotal=" + quantityTotal +
                ", costTotal=" + costTotal +
                ", remaindersNow=" + remaindersNow +
                ", forwardSell=" + forwardSell +
                ", manufacturer=" + manufacturer +
                ", brutto=" + brutto +
                ", netto=" + netto +
                ", packaging=" + packaging +
                ", unitsWeight='" + unitsWeight + '\'' +
                ", packUnits=" + packUnits +
                ", length=" + length +
                ", width=" + width +
                ", height=" + height +
                ", unitsLength='" + unitsLength + '\'' +
                ", vendor=" + vendor +
                ", vendorCode='" + vendorCode + '\'' +
                ", translation=" + translation +
                "} " + super.toString();
    }
}
