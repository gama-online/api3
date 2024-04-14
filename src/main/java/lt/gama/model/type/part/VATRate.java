package lt.gama.model.type.part;

import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-04-07.
 */
@Embeddable
public class VATRate implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String code;

    private String name;

    /**
     * VAT rate in percent (always >= 0)
     */
    private Double rate;

    public VATRate() {
    }

    public VATRate(String code, String name, Double rate) {
        this.code = code;
        this.name = name;
        this.rate = rate;
    }

    // generated

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VATRate vatRate = (VATRate) o;
        return Objects.equals(code, vatRate.code) && Objects.equals(name, vatRate.name) && Objects.equals(rate, vatRate.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, rate);
    }

    @Override
    public String toString() {
        return "VATRate{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", rate=" + rate +
                '}';
    }
}
