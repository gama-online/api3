package lt.gama.model.type.part;

import lt.gama.model.i.IDb;
import lt.gama.model.i.IId;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocManufacturer;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DocPart implements IPart, ITranslations<LangPart>, IDb, IId<Long>, Serializable {

    private static final long serialVersionUID = -1L;

    private Long id;

    private DBType db;

    private String name;

    private String sku;

    private String barcode;

    private String unit;

    private PartType type;

    private GamaBigMoney price;

    private GLOperationAccount accountAsset;

    private GLDC glIncome;

    private GLDC glExpense;

    /**
     * Allow forward sell, i.e. to have negative remainder.
     */
    private Boolean forwardSell;

    /**
     * Part Custom fields values
     */
    private List<CFValue> cf;

    private DocManufacturer manufacturer;

    private BigDecimal brutto;

    private BigDecimal netto;

    /**
     * Translations
     */
    private Map<String, LangPart> translation;

    public DocPart() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPart(P part) {
        this.id = part.getId();
        this.name = part.getName();
        this.sku = part.getSku();
        this.barcode = part.getBarcode();
        this.unit = part.getUnit();
        this.type = part.getType();
        this.cf = part.getCf();
        this.translation = part.getTranslation();
        this.manufacturer = new DocManufacturer(part.getManufacturer());
        this.brutto = part.getBrutto();
        this.netto = part.getNetto();
        this.accountAsset = part.getAccountAsset();
        this.glIncome = part.getGlIncome();
        this.glExpense = part.getGlExpense();
        this.db = part.getDb();
    }

    // customized getters/setters

    public boolean isForwardSell() {
        return forwardSell != null && forwardSell;
    }

    //generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public DBType getDb() {
        return db;
    }

    @Override
    public void setDb(DBType db) {
        this.db = db;
    }

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
    public String toString() {
        return "DocPart{" +
                "id=" + id +
                ", db=" + db +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", type=" + type +
                ", price=" + price +
                ", accountAsset=" + accountAsset +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                ", forwardSell=" + forwardSell +
                ", cf=" + cf +
                ", manufacturer=" + manufacturer +
                ", brutto=" + brutto +
                ", netto=" + netto +
                ", translation=" + translation +
                '}';
    }
}
