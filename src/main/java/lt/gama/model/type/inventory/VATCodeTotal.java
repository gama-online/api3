package lt.gama.model.type.inventory;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.gl.GLDC;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-10-15.
 */
public class VATCodeTotal implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String code;

    private Double rate;

    /**
     * Taxable amount
     */
    private GamaMoney amount;

    /**
     * Calculated tax: amount * rate %
     */
    private GamaMoney tax;

    private GLDC gl;

    // generated

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public GamaMoney getTax() {
        return tax;
    }

    public void setTax(GamaMoney tax) {
        this.tax = tax;
    }

    public GLDC getGl() {
        return gl;
    }

    public void setGl(GLDC gl) {
        this.gl = gl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VATCodeTotal that = (VATCodeTotal) o;
        return Objects.equals(code, that.code) && Objects.equals(rate, that.rate) && Objects.equals(amount, that.amount) && Objects.equals(tax, that.tax) && Objects.equals(gl, that.gl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, rate, amount, tax, gl);
    }

    @Override
    public String toString() {
        return "VATCodeTotal{" +
                "code='" + code + '\'' +
                ", rate=" + rate +
                ", amount=" + amount +
                ", tax=" + tax +
                ", gl=" + gl +
                '}';
    }
}
