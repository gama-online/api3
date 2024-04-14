package lt.gama.model.type.salary;

import lt.gama.model.type.enums.HolidayType;

import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
public class HolidaySettings implements Serializable {

    private HolidayType type;

    /**
     * DAY) the day of the month;
     * example: month=12, day=24 - 24'th of December
     */
    private Integer month;

    private Integer day;

    /**
     *  WEEK) n'th of weekDay in the month;
     *  n > 0 - from the beginning of the month;
     *  n < 0 - from the end of the month;
     *  example: n=1, weekDay=7, month=5 - first sunday of May
     *  example: n=-1, weekDay=1, month=9 - last monday of September
     */
    private Integer n;

    private Integer weekDay;

    /**
     * OTHER) Other day with +/- n;
     * code: 'E' - Easter
     * examples:
     *  n=0, code='E' - Easter day + 0 - first day;
     *  n=1, code='E' - Easter day + 1 - second day;
     */
    private String code;

    /**
     * if holiday will be moved to the next working day if on the weekend
     */
    private boolean transferable;

    /**
     * holiday name
     */
    private String name;


    static public HolidaySettings Day(Integer month, Integer day, String name) {
        HolidaySettings holidaySettings = new HolidaySettings();
        holidaySettings.type = HolidayType.DAY;
        holidaySettings.month = month;
        holidaySettings.day = day;
        holidaySettings.name = name;
        return holidaySettings;
    }

    static public HolidaySettings Week(Integer month, Integer n, Integer weekDay, String name) {
        HolidaySettings holidaySettings = new HolidaySettings();
        holidaySettings.type = HolidayType.WEEK;
        holidaySettings.month = month;
        holidaySettings.n = n;
        holidaySettings.weekDay = weekDay;
        holidaySettings.name = name;
        return holidaySettings;
    }

    static public HolidaySettings Other(Integer n, String name) {
        HolidaySettings holidaySettings = new HolidaySettings();
        holidaySettings.type = HolidayType.OTHER;
        holidaySettings.code = "E";
        holidaySettings.n = n;
        holidaySettings.name = name;
        return holidaySettings;
    }

    // generated

    public HolidayType getType() {
        return type;
    }

    public void setType(HolidayType type) {
        this.type = type;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public Integer getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(Integer weekDay) {
        this.weekDay = weekDay;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isTransferable() {
        return transferable;
    }

    public void setTransferable(boolean transferable) {
        this.transferable = transferable;
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
        HolidaySettings that = (HolidaySettings) o;
        return transferable == that.transferable && type == that.type && Objects.equals(month, that.month) && Objects.equals(day, that.day) && Objects.equals(n, that.n) && Objects.equals(weekDay, that.weekDay) && Objects.equals(code, that.code) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, month, day, n, weekDay, code, transferable, name);
    }

    @Override
    public String toString() {
        return "HolidaySettings{" +
                "type=" + type +
                ", month=" + month +
                ", day=" + day +
                ", n=" + n +
                ", weekDay=" + weekDay +
                ", code='" + code + '\'' +
                ", transferable=" + transferable +
                ", name='" + name + '\'' +
                '}';
    }
}
