package lt.gama.model.type.salary;


import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-04-04.
 */
public class WorkScheduleDay implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Work hours in day
     */
    private BigDecimal hours;


    protected WorkScheduleDay() {
    }

    public WorkScheduleDay(BigDecimal hours) {
        this.hours = hours;
    }

    public WorkScheduleDay(int hours) {
        this.hours = BigDecimal.valueOf(hours);
    }

    // generated

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkScheduleDay that = (WorkScheduleDay) o;
        return Objects.equals(hours, that.hours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hours);
    }

    @Override
    public String toString() {
        return "WorkScheduleDay{" +
                "hours=" + hours +
                '}';
    }
}
