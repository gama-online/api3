package lt.gama.model.type.salary;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-12-15.
 */
public class WorkHoursDay implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * is state holiday?
     */
    private Boolean holiday;

    /**
     * is weekend, i.e. Saturday or Sunday?
     */
    private Boolean weekend;

    /**
     * is rest day from work schedule?
     */
    private Boolean rest;

    private WorkTimeCode code;

    private WorkData workData;


    public WorkHoursDay() {
    }

    public WorkHoursDay(Boolean holiday, Boolean weekend, Boolean rest, WorkTimeCode code, WorkData workData) {
        this.holiday = holiday;
        this.weekend = weekend;
        this.rest = rest;
        this.code = code;
        this.workData = workData;
    }

    public boolean isHoliday() {
        return holiday != null && holiday;
    }

    public boolean isWeekend() {
        return weekend != null && weekend;
    }

    public boolean isRest() {
        return rest != null && rest;
    }

    // generated
    // except getHoliday(), getWeekend(), getRest()

    public void setHoliday(Boolean holiday) {
        this.holiday = holiday;
    }

    public void setWeekend(Boolean weekend) {
        this.weekend = weekend;
    }

    public void setRest(Boolean rest) {
        this.rest = rest;
    }

    public WorkTimeCode getCode() {
        return code;
    }

    public void setCode(WorkTimeCode code) {
        this.code = code;
    }

    public WorkData getWorkData() {
        return workData;
    }

    public void setWorkData(WorkData workData) {
        this.workData = workData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkHoursDay that = (WorkHoursDay) o;
        return Objects.equals(holiday, that.holiday) && Objects.equals(weekend, that.weekend) && Objects.equals(rest, that.rest) && Objects.equals(code, that.code) && Objects.equals(workData, that.workData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holiday, weekend, rest, code, workData);
    }

    @Override
    public String toString() {
        return "WorkHoursDay{" +
                "holiday=" + holiday +
                ", weekend=" + weekend +
                ", rest=" + rest +
                ", code=" + code +
                ", workData=" + workData +
                '}';
    }
}
