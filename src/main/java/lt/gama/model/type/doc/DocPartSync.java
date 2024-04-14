package lt.gama.model.type.doc;

import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.part.VATRate;

import java.io.Serial;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 13/11/2018.
 */
public class DocPartSync extends BaseDocEntity {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private String sku;

    private String barcode;

    private String unit;

    private PartType type;

    private boolean taxable;

    private String vatRateCode;

    private VATRate vat;

    /*
     * accounting
     */

    private GLOperationAccount accountAsset;

    private GLDC glIncome;

    private GLDC glExpense;

    public DocPartSync() {
    }
//TODO remove comments
//    public DocPartSync(PartDto part) {
//        if (part == null) return;
//        setId(part.getId());
//        setExportId(part.getExportId());
//        setDb(part.getDb());
//        setName(part.getName());
//        setSku(part.getSku());
//        setBarcode(part.getBarcode());
//        setUnit(part.getUnit());
//        setType(part.getType());
//        setTaxable(part.isTaxable());
//        setVatRateCode(part.getVatRateCode());
//        setVat(part.getVat());
//        setAccountAsset(part.getAccountAsset());
//        setGlIncome(part.getGlIncome());
//        setGlExpense(part.getGlExpense());
//    }

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public PartType getType() {
        return type;
    }

    public void setType(PartType type) {
        this.type = type;
    }

    public boolean isTaxable() {
        return taxable;
    }

    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public String getVatRateCode() {
        return vatRateCode;
    }

    public void setVatRateCode(String vatRateCode) {
        this.vatRateCode = vatRateCode;
    }

    public VATRate getVat() {
        return vat;
    }

    public void setVat(VATRate vat) {
        this.vat = vat;
    }

    public GLOperationAccount getAccountAsset() {
        return accountAsset;
    }

    public void setAccountAsset(GLOperationAccount accountAsset) {
        this.accountAsset = accountAsset;
    }

    public GLDC getGlIncome() {
        return glIncome;
    }

    public void setGlIncome(GLDC glIncome) {
        this.glIncome = glIncome;
    }

    public GLDC getGlExpense() {
        return glExpense;
    }

    public void setGlExpense(GLDC glExpense) {
        this.glExpense = glExpense;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocPartSync that = (DocPartSync) o;
        return taxable == that.taxable && Objects.equals(name, that.name) && Objects.equals(sku, that.sku) && Objects.equals(barcode, that.barcode) && Objects.equals(unit, that.unit) && type == that.type && Objects.equals(vatRateCode, that.vatRateCode) && Objects.equals(vat, that.vat) && Objects.equals(accountAsset, that.accountAsset) && Objects.equals(glIncome, that.glIncome) && Objects.equals(glExpense, that.glExpense);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, sku, barcode, unit, type, taxable, vatRateCode, vat, accountAsset, glIncome, glExpense);
    }

    @Override
    public String toString() {
        return "DocPartSync{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", type=" + type +
                ", taxable=" + taxable +
                ", vatRateCode='" + vatRateCode + '\'' +
                ", vat=" + vat +
                ", accountAsset=" + accountAsset +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                "} " + super.toString();
    }
}
