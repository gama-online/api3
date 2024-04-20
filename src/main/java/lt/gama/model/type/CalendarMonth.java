package lt.gama.model.type;

import java.util.ArrayList;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-05-26.
 */
public class CalendarMonth {

    private int workingDays;

    private int workingHours;

    private ArrayList<CalendarDay> days;

    // generated

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }

    public ArrayList<CalendarDay> getDays() {
        return days;
    }

    public void setDays(ArrayList<CalendarDay> days) {
        this.days = days;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarMonth that = (CalendarMonth) o;
        return workingDays == that.workingDays && workingHours == that.workingHours && Objects.equals(days, that.days);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workingDays, workingHours, days);
    }

    @Override
    public String toString() {
        return "CalendarMonth{" +
                "workingDays=" + workingDays +
                ", workingHours=" + workingHours +
                ", days=" + days +
                '}';
    }
}
