package lt.gama.model.dto.entities;

import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.enums.PartType;

import java.io.Serial;
import java.util.Objects;

public class PartApiDto extends BaseCompanyDto {

    @Serial
    private static final long serialVersionUID = -3;

    /**
     * Part type - Service / Product / Product with Serials
     */
    private PartType type;

    private String name;

    private String description;

    private String sku;

    private String barcode;

    private String unit;

    /*
     * accounting
     */
    private GLOperationAccount accountAsset;

    private GLDC glIncome;

    private GLDC glExpense;

    //generated

    public PartType getType() {
        return type;
    }

    public void setType(PartType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        PartApiDto that = (PartApiDto) o;
        return type == that.type && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(sku, that.sku) && Objects.equals(barcode, that.barcode) && Objects.equals(unit, that.unit) && Objects.equals(accountAsset, that.accountAsset) && Objects.equals(glIncome, that.glIncome) && Objects.equals(glExpense, that.glExpense);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, name, description, sku, barcode, unit, accountAsset, glIncome, glExpense);
    }

    @Override
    public String toString() {
        return "PartApiDto{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", accountAsset=" + accountAsset +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                '}';
    }
}
