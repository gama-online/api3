package lt.gama.model.dto.base;

import lt.gama.model.dto.entities.ManufacturerDto;
import lt.gama.model.i.*;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.l10n.LangPart;
import lt.gama.model.type.part.PartSN;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BaseDocPartDto extends BaseCompanyDto implements IPart, ITranslations<LangPart>,
        IPartSN, IUuid, IPartMessage {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private String sku;

    private String barcode;

    private String unit;

    private PartType type;

    private PartSN sn;

    private GamaBigMoney price;

    private GLOperationAccount accountAsset;

    private GLDC glIncome;

    private GLDC glExpense;

    private Boolean forwardSell;

    private List<CFValue> cf;

    /**
     * Unique id in part's list in the document
     */
    private UUID uuid;

    private ManufacturerDto manufacturer;

    private BigDecimal brutto;

    private BigDecimal netto;

    private Map<String, LangPart> translation;

    public BaseDocPartDto() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> BaseDocPartDto(P part) {
        this.setId(part.getId());
        this.name = part.getName();
        this.sku = part.getSku();
        this.barcode = part.getBarcode();
        this.unit = part.getUnit();
        this.type = part.getType();
        this.cf = part.getCf();
        this.translation = part.getTranslation();
        this.manufacturer = new ManufacturerDto(part.getManufacturer());
        this.brutto = part.getBrutto();
        this.netto = part.getNetto();
        this.accountAsset = part.getAccountAsset();
        this.glIncome = part.getGlIncome();
        this.glExpense = part.getGlExpense();
        this.setDb(part.getDb());
    }

    @Override
    public String toMessage() {
        return ((sku == null ? "" : sku) + ' ' + (name == null ? "" : name) +
                (!PartType.PRODUCT_SN.equals(type) ? "" : sn == null ? "" : (", S/N: " + sn))).trim();
    }

    // customized getters/setters

    public boolean isForwardSell() {
        return forwardSell != null && forwardSell;
    }

    // generated

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSku() {
        return sku;
    }

    @Override
    public void setSku(String sku) {
        this.sku = sku;
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
    public PartType getType() {
        return type;
    }

    @Override
    public void setType(PartType type) {
        this.type = type;
    }

    @Override
    public PartSN getSn() {
        return sn;
    }

    @Override
    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    public GamaBigMoney getPrice() {
        return price;
    }

    public void setPrice(GamaBigMoney price) {
        this.price = price;
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

    public void setForwardSell(Boolean forwardSell) {
        this.forwardSell = forwardSell;
    }

    @Override
    public List<CFValue> getCf() {
        return cf;
    }

    @Override
    public void setCf(List<CFValue> cf) {
        this.cf = cf;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public ManufacturerDto getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(ManufacturerDto manufacturer) {
        this.manufacturer = manufacturer;
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
        BaseDocPartDto that = (BaseDocPartDto) o;
        return Objects.equals(name, that.name) && Objects.equals(sku, that.sku) && Objects.equals(barcode, that.barcode) && Objects.equals(unit, that.unit) && type == that.type && Objects.equals(sn, that.sn) && Objects.equals(price, that.price) && Objects.equals(accountAsset, that.accountAsset) && Objects.equals(glIncome, that.glIncome) && Objects.equals(glExpense, that.glExpense) && Objects.equals(forwardSell, that.forwardSell) && Objects.equals(cf, that.cf) && Objects.equals(uuid, that.uuid) && Objects.equals(manufacturer, that.manufacturer) && Objects.equals(brutto, that.brutto) && Objects.equals(netto, that.netto) && Objects.equals(translation, that.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, sku, barcode, unit, type, sn, price, accountAsset, glIncome, glExpense, forwardSell, cf, uuid, manufacturer, brutto, netto, translation);
    }

    @Override
    public String toString() {
        return "BaseDocPartDto{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", type=" + type +
                ", sn=" + sn +
                ", price=" + price +
                ", accountAsset=" + accountAsset +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                ", forwardSell=" + forwardSell +
                ", cf=" + cf +
                ", uuid=" + uuid +
                ", manufacturer=" + manufacturer +
                ", brutto=" + brutto +
                ", netto=" + netto +
                ", translation=" + translation +
                "} " + super.toString();
    }
}
