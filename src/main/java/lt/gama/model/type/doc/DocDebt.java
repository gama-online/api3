package lt.gama.model.type.doc;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.GamaMoney;

import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-03-25.
 */
public class DocDebt implements Serializable {

    /**
     * Document info
     */
    private Doc doc;

    /**
     * Document's total amount
     */
    private GamaMoney amount;

    /**
     * Amount covered (must be not greater than total amount)
     */
    private GamaMoney covered;


    protected DocDebt() {
    }

    public DocDebt(Doc doc, GamaMoney amount, GamaMoney covered) {
        this.doc = doc;
        this.amount = amount;
        this.covered = covered;
    }

    public boolean isCovered() {
        return GamaMoneyUtils.isEqual(amount, covered);
    }

    public GamaMoney getRemainder() {
        return GamaMoneyUtils.subtract(amount, covered);
    }

    // generated

    public Doc getDoc() {
        return doc;
    }

    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public GamaMoney getCovered() {
        return covered;
    }

    public void setCovered(GamaMoney covered) {
        this.covered = covered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocDebt docDebt = (DocDebt) o;
        return Objects.equals(doc, docDebt.doc) && Objects.equals(amount, docDebt.amount) && Objects.equals(covered, docDebt.covered);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doc, amount, covered);
    }

    @Override
    public String toString() {
        return "DocDebt{" +
                "doc=" + doc +
                ", amount=" + amount +
                ", covered=" + covered +
                '}';
    }
}
