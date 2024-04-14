package lt.gama.model.dto.base;

import lt.gama.model.i.IPart;
import lt.gama.model.i.IPartSN;
import lt.gama.model.i.ITranslations;
import lt.gama.model.i.IUuid;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocManufacturer;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.l10n.LangPart;
import lt.gama.model.type.part.PartSN;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasePartPartApiDto extends BaseCompanyDto implements IPart, ITranslations<LangPart>, IPartSN, IUuid {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private String sku;

    private String barcode;

    private String unit;

    private PartType type;

    private PartSN sn;

    private List<CFValue> cf;

    private GamaBigMoney price;

    private BigDecimal quantity;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    private GamaMoney total;

    private GamaMoney baseTotal;


    private DocWarehouse warehouse;

    /**
     * Unique id in part's list in the document
     */
    private UUID uuid;

    private DocManufacturer manufacturer;

    private BigDecimal brutto;

    private BigDecimal netto;

    /**
     * Translations
     */
    private Map<String, LangPart> translation;

    /*
     * accounting
     */

    private GLOperationAccount accountAsset;

    private GLDC glIncome;

    private GLDC glExpense;


    // customized getters/setters

    public boolean isFinished() {
        return finished != null && finished;
    }

    // generated
    // except getFinished()

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

    @Override
    public List<CFValue> getCf() {
        return cf;
    }

    @Override
    public void setCf(List<CFValue> cf) {
        this.cf = cf;
    }

    public GamaBigMoney getPrice() {
        return price;
    }

    public void setPrice(GamaBigMoney price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
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
    public DocManufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(DocManufacturer manufacturer) {
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

    @Override
    public String toString() {
        return "BasePartPartDto{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", type=" + type +
                ", sn=" + sn +
                ", cf=" + cf +
                ", price=" + price +
                ", quantity=" + quantity +
                ", finished=" + finished +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", uuid=" + uuid +
                ", manufacturer=" + manufacturer +
                ", brutto=" + brutto +
                ", netto=" + netto +
                ", translation=" + translation +
                ", accountAsset=" + accountAsset +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                "} " + super.toString();
    }
}
