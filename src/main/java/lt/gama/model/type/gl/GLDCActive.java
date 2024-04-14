package lt.gama.model.type.gl;

import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-02-05.
 */
@Embeddable
public class GLDCActive extends GLDC implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private Boolean active;

    /**
     * tax rate, if null - use default
     */
    private BigDecimal rate;


    public GLDCActive() {
    }

    public GLDCActive(Boolean active) {
        this.active = active;
    }

    public GLDCActive(Boolean active, GLOperationAccount debit, GLOperationAccount credit) {
        super(debit, credit);
        this.active = active;
    }

    public GLDCActive(Boolean active, BigDecimal rate, GLOperationAccount debit, GLOperationAccount credit) {
        this(active, debit, credit);
        this.rate = rate;
    }

    public GLDCActive(BigDecimal rate) {
        this.active = true;
        this.rate = rate;
    }

    // customized getters/setters

    public boolean isActive() {
        return active != null && active;
    }

    // generated
    // without getActive()

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GLDCActive that = (GLDCActive) o;
        return Objects.equals(active, that.active) && Objects.equals(rate, that.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), active, rate);
    }

    @Override
    public String toString() {
        return "GLDCActive{" +
                "active=" + active +
                ", rate=" + rate +
                "} " + super.toString();
    }
}
