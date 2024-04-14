package lt.gama.model.type.doc;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.salary.WorkHoursPosition;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-03-09.
 */
public class DocChargeAmount implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private WorkHoursPosition workHoursPosition;

    private DocCharge charge;

    private GamaMoney amount;

    /**
     * Charge is fixes, i.e. do not recalculate on refresh
     */
    private Boolean fixed;


    public DocChargeAmount() {
    }

    public DocChargeAmount(WorkHoursPosition position, DocCharge charge, GamaMoney amount) {
        setWorkHoursPosition(new WorkHoursPosition(position));
        setCharge(charge);
        setAmount(amount);
    }

    public DocChargeAmount(DocChargeAmount src) {
        this.workHoursPosition = new WorkHoursPosition(src.workHoursPosition);
        this.charge = new DocCharge(src.charge);
        this.amount = src.amount;
        this.fixed = src.fixed;
    }

    public boolean isFixed() {
        return fixed != null && fixed;
    }

    // generated
    // except getFixed()

    public WorkHoursPosition getWorkHoursPosition() {
        return workHoursPosition;
    }

    public void setWorkHoursPosition(WorkHoursPosition workHoursPosition) {
        this.workHoursPosition = workHoursPosition;
    }

    public DocCharge getCharge() {
        return charge;
    }

    public void setCharge(DocCharge charge) {
        this.charge = charge;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public void setFixed(Boolean fixed) {
        this.fixed = fixed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocChargeAmount that = (DocChargeAmount) o;
        return Objects.equals(workHoursPosition, that.workHoursPosition) && Objects.equals(charge, that.charge) && Objects.equals(amount, that.amount) && Objects.equals(fixed, that.fixed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workHoursPosition, charge, amount, fixed);
    }

    @Override
    public String toString() {
        return "DocChargeAmount{" +
                "workHoursPosition=" + workHoursPosition +
                ", charge=" + charge +
                ", amount=" + amount +
                ", fixed=" + fixed +
                '}';
    }
}
