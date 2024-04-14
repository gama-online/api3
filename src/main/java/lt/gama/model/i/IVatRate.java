package lt.gama.model.i;

import lt.gama.model.type.part.VATRate;

/**
 * gama-online
 * Created by valdas on 2017-04-12.
 */
public interface IVatRate {

    boolean isTaxable();

    void setTaxable(boolean taxable);

    String getVatRateCode();

    void setVatRateCode(String vatRateCode);

    VATRate getVat();

    void setVat(VATRate vat);
}
