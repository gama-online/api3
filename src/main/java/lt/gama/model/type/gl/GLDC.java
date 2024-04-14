package lt.gama.model.type.gl;

import com.google.common.collect.ComparisonChain;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lt.gama.helpers.Validators;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-02-05.
 */
@MappedSuperclass
@Embeddable
public class GLDC implements Comparable<GLDC>, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @Embedded
    private GLOperationAccount debit;

    @Embedded
    private GLOperationAccount credit;

    public GLDC() {
    }

    public GLDC(GLOperationAccount account) {
        this(account, account);
    }

    public GLDC(GLOperationAccount debit, GLOperationAccount credit) {
        this.debit = debit;
        this.credit = credit;
    }

    public GLOperationAccount getCreditEx() {
        return Validators.isValid(credit) ? credit : Validators.isValid(debit) ? debit : null;
    }

    public GLOperationAccount getDebitEx() {
        return Validators.isValid(debit) ? debit : Validators.isValid(credit) ? credit : null;
    }

    @Override
    public int compareTo(GLDC o) {
        return ComparisonChain.start()
                .compare(debit, o.debit)
                .compare(credit, o.credit)
                .result();
    }

    // generated

    public GLOperationAccount getDebit() {
        return debit;
    }

    public void setDebit(GLOperationAccount debit) {
        this.debit = debit;
    }

    public GLOperationAccount getCredit() {
        return credit;
    }

    public void setCredit(GLOperationAccount credit) {
        this.credit = credit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLDC gldc = (GLDC) o;
        return Objects.equals(debit, gldc.debit) && Objects.equals(credit, gldc.credit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(debit, credit);
    }

    @Override
    public String toString() {
        return "GLDC{" +
                "debit=" + debit +
                ", credit=" + credit +
                '}';
    }
}
