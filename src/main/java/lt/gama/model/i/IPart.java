package lt.gama.model.i;

import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;

import java.math.BigDecimal;
import java.util.List;

public interface IPart {

	Long getId();
	void setId(Long id);

	String getName();
	void setName(String name);

	String getSku();
	void setSku(String sku);

	String getBarcode();
	void setBarcode(String barcode);

	String getUnit();
	void setUnit(String unit);

	PartType getType();
	void setType(PartType type);

	List<CFValue> getCf();
	void setCf(List<CFValue> cf);

	IManufacturer getManufacturer();

	BigDecimal getBrutto();
	void setBrutto(BigDecimal brutto);

	BigDecimal getNetto();
	void setNetto(BigDecimal netto);

	/*
	 * accounting
	 */

	GLOperationAccount getAccountAsset();
	void setAccountAsset(GLOperationAccount accountAsset);

	GLDC getGlIncome();
	void setGlIncome(GLDC glIncome);

	GLDC getGlExpense();
	void setGlExpense(GLDC glExpense);
}
