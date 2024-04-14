package lt.gama.model.type.auth;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-06-12.
 */
public class CompanySalarySettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private LocalDate date;

    /**
     * Coefficient of overtime work hours - default 1.5
     */
    private Double overtimeC = 1.5;

    /**
     * Coefficient of night work hours - default 1.5
     */
    private Double nightC = 1.5;

    /**
     * Coefficient of work hours in weekends - default 2.0
     */
    private Double weekendC = 2.0;

    /**
     * Coefficient of work hours in state holiday - default 2.0
     */
    private Double holidayC = 2.0;

    /**
     * Coefficient of overtime work hours in weekends - default 2.0
     */
    private Double overtimeWeekendC = 2.0;

    /**
     * Coefficient of overtime work hours in weekends - default 2.0
     */
    private Double overtimeNightC = 2.0;

    /**
     * Coefficient of overtime work hours in state holiday - default 2.5
     */
    private Double overtimeHolidayC = 2.5;

    /**
     * Additional employees vacations in days
     */
    private Integer addVacationLength = 0;

    /**
     * shorter working day before state holidays
     */
    private Boolean shorterWorkingDay = false;

    /**
     * Illness paid days by employer
     */
    private Integer illnessDays = 2;

    /**
     * Illness percent of salary (ex.: 80%)
     */
    private Double illnessPct = 80.0;


    // customized getters

    public Double getOvertimeC() {
        return overtimeC == null ? 0 : overtimeC;
    }

    public Double getNightC() {
        return nightC == null ? 0 : nightC;
    }

    public Double getWeekendC() {
        return weekendC == null ? 0 : weekendC;
    }

    public Double getHolidayC() {
        return holidayC == null ? 0 : holidayC;
    }

    public Double getOvertimeWeekendC() {
        return overtimeWeekendC == null ? 0 : overtimeWeekendC;
    }

    public Double getOvertimeNightC() {
        return overtimeNightC == null ? 0 : overtimeNightC;
    }

    public Double getOvertimeHolidayC() {
        return overtimeHolidayC == null ? 0 : overtimeHolidayC;
    }

    public boolean isShorterWorkingDay() {
        return shorterWorkingDay != null && shorterWorkingDay;
    }

    public Integer getIllnessDays() {
        return illnessDays == null ? 0 : illnessDays;
    }

    public Double getIllnessPct() {
        return illnessPct == null ? 0 : illnessPct;
    }

    // generated
    // without getShorterWorkingDay

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setOvertimeC(Double overtimeC) {
        this.overtimeC = overtimeC;
    }

    public void setNightC(Double nightC) {
        this.nightC = nightC;
    }

    public void setWeekendC(Double weekendC) {
        this.weekendC = weekendC;
    }

    public void setHolidayC(Double holidayC) {
        this.holidayC = holidayC;
    }

    public void setOvertimeWeekendC(Double overtimeWeekendC) {
        this.overtimeWeekendC = overtimeWeekendC;
    }

    public void setOvertimeNightC(Double overtimeNightC) {
        this.overtimeNightC = overtimeNightC;
    }

    public void setOvertimeHolidayC(Double overtimeHolidayC) {
        this.overtimeHolidayC = overtimeHolidayC;
    }

    public Integer getAddVacationLength() {
        return addVacationLength;
    }

    public void setAddVacationLength(Integer addVacationLength) {
        this.addVacationLength = addVacationLength;
    }

    public void setShorterWorkingDay(Boolean shorterWorkingDay) {
        this.shorterWorkingDay = shorterWorkingDay;
    }

    public void setIllnessDays(Integer illnessDays) {
        this.illnessDays = illnessDays;
    }

    public void setIllnessPct(Double illnessPct) {
        this.illnessPct = illnessPct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanySalarySettings that = (CompanySalarySettings) o;
        return Objects.equals(date, that.date) && Objects.equals(overtimeC, that.overtimeC) && Objects.equals(nightC, that.nightC) && Objects.equals(weekendC, that.weekendC) && Objects.equals(holidayC, that.holidayC) && Objects.equals(overtimeWeekendC, that.overtimeWeekendC) && Objects.equals(overtimeNightC, that.overtimeNightC) && Objects.equals(overtimeHolidayC, that.overtimeHolidayC) && Objects.equals(addVacationLength, that.addVacationLength) && Objects.equals(shorterWorkingDay, that.shorterWorkingDay) && Objects.equals(illnessDays, that.illnessDays) && Objects.equals(illnessPct, that.illnessPct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, overtimeC, nightC, weekendC, holidayC, overtimeWeekendC, overtimeNightC, overtimeHolidayC, addVacationLength, shorterWorkingDay, illnessDays, illnessPct);
    }

    @Override
    public String toString() {
        return "CompanySalarySettings{" +
                "date=" + date +
                ", overtimeC=" + overtimeC +
                ", nightC=" + nightC +
                ", weekendC=" + weekendC +
                ", holidayC=" + holidayC +
                ", overtimeWeekendC=" + overtimeWeekendC +
                ", overtimeNightC=" + overtimeNightC +
                ", overtimeHolidayC=" + overtimeHolidayC +
                ", addVacationLength=" + addVacationLength +
                ", shorterWorkingDay=" + shorterWorkingDay +
                ", illnessDays=" + illnessDays +
                ", illnessPct=" + illnessPct +
                '}';
    }
}
