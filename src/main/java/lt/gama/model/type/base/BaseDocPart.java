package lt.gama.model.type.base;

import lt.gama.model.i.*;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.Packaging;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocManufacturer;
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

public abstract class BaseDocPart extends BaseDocEntity implements IPart, ITranslations<LangPart>, IPartSN, IUuid, IPartMessage {

	@Serial
    private static final long serialVersionUID = -1L;

	private String name;

	private String sku;

	private String barcode;

	private String unit;

	private PartType type;

	private PartSN sn;

	private GamaBigMoney price;

    /**
     * Allow forward sell, i.e. to have negative remainder.
     */
	private Boolean forwardSell;

	/**
	 * Part Custom fields values
	 */
	private List<CFValue> cf;

    /**
     * Unique id in part's list in the document
     */
	private UUID uuid;

	private DocManufacturer manufacturer;

	private BigDecimal brutto;

	private BigDecimal netto;

	private List<Packaging> packaging;

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

	protected BaseDocPart() {
	}

	public <P extends IPart & ITranslations<LangPart> & IDb> BaseDocPart(P part) {
		super(part.getId());
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
	// without getForwardSell

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

	public List<Packaging> getPackaging() {
		return packaging;
	}

	public void setPackaging(List<Packaging> packaging) {
		this.packaging = packaging;
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		BaseDocPart that = (BaseDocPart) o;
		return Objects.equals(name, that.name) && Objects.equals(sku, that.sku) && Objects.equals(barcode, that.barcode) && Objects.equals(unit, that.unit) && type == that.type && Objects.equals(sn, that.sn) && Objects.equals(price, that.price) && Objects.equals(forwardSell, that.forwardSell) && Objects.equals(cf, that.cf) && Objects.equals(uuid, that.uuid) && Objects.equals(manufacturer, that.manufacturer) && Objects.equals(brutto, that.brutto) && Objects.equals(netto, that.netto) && Objects.equals(packaging, that.packaging) && Objects.equals(translation, that.translation) && Objects.equals(accountAsset, that.accountAsset) && Objects.equals(glIncome, that.glIncome) && Objects.equals(glExpense, that.glExpense);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, sku, barcode, unit, type, sn, price, forwardSell, cf, uuid, manufacturer, brutto, netto, packaging, translation, accountAsset, glIncome, glExpense);
	}

	@Override
	public String toString() {
		return "BaseDocPart{" +
				"name='" + name + '\'' +
				", sku='" + sku + '\'' +
				", barcode='" + barcode + '\'' +
				", unit='" + unit + '\'' +
				", type=" + type +
				", sn=" + sn +
				", price=" + price +
				", forwardSell=" + forwardSell +
				", cf=" + cf +
				", uuid=" + uuid +
				", manufacturer=" + manufacturer +
				", brutto=" + brutto +
				", netto=" + netto +
				", packaging=" + packaging +
				", translation=" + translation +
				", accountAsset=" + accountAsset +
				", glIncome=" + glIncome +
				", glExpense=" + glExpense +
				"} " + super.toString();
	}
}
