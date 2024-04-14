package lt.gama.model.i;

public interface IVat {

    boolean isTaxable();

    void setTaxable(boolean taxable);

    String getVatRateCode();

    void setVatRateCode(String code);
}
