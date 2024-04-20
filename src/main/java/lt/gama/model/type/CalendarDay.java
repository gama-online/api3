package lt.gama.model.type;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
public class CalendarDay {

    private boolean holiday;

    private boolean weekend;

    private String name;


    public CalendarDay() {
    }

    public CalendarDay(String name) {
        this.holiday = true;
        this.name = name;
    }

    // generated

    public boolean isHoliday() {
        return holiday;
    }

    public void setHoliday(boolean holiday) {
        this.holiday = holiday;
    }

    public boolean isWeekend() {
        return weekend;
    }

    public void setWeekend(boolean weekend) {
        this.weekend = weekend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarDay that = (CalendarDay) o;
        return holiday == that.holiday && weekend == that.weekend && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holiday, weekend, name);
    }

    @Override
    public String toString() {
        return "CalendarDay{" +
                "holiday=" + holiday +
                ", weekend=" + weekend +
                ", name='" + name + '\'' +
                '}';
    }
}
