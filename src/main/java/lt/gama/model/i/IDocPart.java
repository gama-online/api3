package lt.gama.model.i;

import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.l10n.LangPart;
import lt.gama.model.type.part.DocPart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IDocPart {

    DocPart getDocPart();

    void setDocPart(DocPart docPart);

    Long getPartId();

    //default methods

    default GamaBigMoney getPrice() {
        return getDocPart().getPrice();
    }
    default void setPrice(GamaBigMoney price) {
        getDocPart().setPrice(price);
    }

    default GLOperationAccount getAccountAsset() {
        return getDocPart().getAccountAsset();
    }

    default void setAccountAsset(GLOperationAccount accountAsset) {
        getDocPart().setAccountAsset(accountAsset);
    }

    default GLDC getGlIncome() {
        return getDocPart().getGlIncome();
    }

    default void setGlIncome(GLDC glIncome) {
        getDocPart().setGlIncome(glIncome);
    }

    default GLDC getGlExpense() {
        return getDocPart().getGlExpense();
    }

    default void setGlExpense(GLDC glExpense) {
        getDocPart().setGlExpense(glExpense);
    }

    default boolean isForwardSell() {
        return getDocPart().isForwardSell();
    }

    default void setForwardSell(Boolean forwardSell) {
        getDocPart().setForwardSell(forwardSell);
    }

    default String getName() {
        return getDocPart().getName();
    }

    default void setName(String name) {
        getDocPart().setName(name);
    }

    default String getSku() {
        return getDocPart().getSku();
    }

    default void setSku(String sku) {
        getDocPart().setSku(sku);
    }

    default String getBarcode() {
        return getDocPart().getBarcode();
    }

    default void setBarcode(String barcode) {
        getDocPart().setBarcode(barcode);
    }

    default String getUnit() {
        return getDocPart().getUnit();
    }

    default void setUnit(String unit) {
        getDocPart().setUnit(unit);
    }

    default PartType getType() {
        return getDocPart().getType();
    }

    default void setType(PartType type) {
        getDocPart().setType(type);
    }

    default List<CFValue> getCf() {
        return getDocPart().getCf();
    }

    default void setCf(List<CFValue> cf) {
        getDocPart().setCf(cf);
    }

    default IManufacturer getManufacturer() {
        return getDocPart().getManufacturer();
    }

    default BigDecimal getBrutto() {
        return getDocPart().getBrutto();
    }

    default void setBrutto(BigDecimal brutto) {
        getDocPart().setBrutto(brutto);
    }

    default BigDecimal getNetto() {
        return getDocPart().getNetto();
    }

    default void setNetto(BigDecimal netto) {
        getDocPart().setNetto(netto);
    }

    default Map<String, LangPart> getTranslation() {
        return getDocPart().getTranslation();
    }

    default void setTranslation(Map<String, LangPart> translation) {
        getDocPart().setTranslation(translation);
    }

}
