package lt.gama.model.type.salary;

import jakarta.persistence.Embeddable;
import lt.gama.helpers.IntegerUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-07-18.
 */
@Embeddable
public class WorkData implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Total working days in period by schedule
     */
    private Integer days;

    /**
     * Total working hours in period by schedule
     */
    private Integer hours;

    /**
     * Total normal hours worked in period
     */
    private Integer worked;

    /**
     * Total overtime hours worked in period
     */
    private Integer overtime;

    /**
     * Total night hours worked in period
     */
    private Integer night;

    /**
     * Total hours worked in weekend time in period
     */
    private Integer weekend;

    /**
     * Total hours worked in holiday time in period
     */
    private Integer holiday;

    /**
     * Total overtime at night hours worked in period
     */
    private Integer overtimeNight;

    /**
     * Total overtime at weekend hours worked in period
     */
    private Integer overtimeWeekend;

    /**
     * Total overtime at holiday hours worked in period
     */
    private Integer overtimeHoliday;

    /**
     * Vacations paid days
     */
    private Integer vacation;

    /**
     * Child-days paid days
     */
    private Integer childDays;

    /**
     * Illness paid days
     */
    private Integer illness;


    public WorkData() {
    }

    public WorkData(WorkData workData) {
        if (workData == null) return;

        this.days = workData.days;
        this.hours = workData.hours;
        this.worked = workData.worked;

        this.overtime = workData.overtime;
        this.night = workData.night;
        this.weekend = workData.weekend;
        this.holiday = workData.holiday;

        this.overtimeNight = workData.overtimeNight;
        this.overtimeWeekend = workData.overtimeWeekend;
        this.overtimeHoliday = workData.overtimeHoliday;

        this.vacation = workData.vacation;
        this.illness = workData.illness;
        this.childDays = workData.childDays;
    }

    public WorkData(Integer days, Integer hours, Integer worked, Integer overtime, Integer night, Integer weekend,
                    Integer holiday, Integer overtimeNight, Integer overtimeWeekend, Integer overtimeHoliday,
                    Integer vacation, Integer childDays, Integer illness) {
        this.days = days;
        this.hours = hours;
        this.worked = worked;
        this.overtime = overtime;
        this.night = night;
        this.weekend = weekend;
        this.holiday = holiday;
        this.overtimeNight = overtimeNight;
        this.overtimeWeekend = overtimeWeekend;
        this.overtimeHoliday = overtimeHoliday;
        this.vacation = vacation;
        this.childDays = childDays;
        this.illness = illness;
    }

    /**
     * Total hours worked in period - including normal hours, overtime, night and etc.
     */
    public Integer getTotal() {
        return IntegerUtils.total(worked, overtime, night, weekend, holiday, overtimeNight, overtimeWeekend, overtimeHoliday);
    }

    // generated

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getWorked() {
        return worked;
    }

    public void setWorked(Integer worked) {
        this.worked = worked;
    }

    public Integer getOvertime() {
        return overtime;
    }

    public void setOvertime(Integer overtime) {
        this.overtime = overtime;
    }

    public Integer getNight() {
        return night;
    }

    public void setNight(Integer night) {
        this.night = night;
    }

    public Integer getWeekend() {
        return weekend;
    }

    public void setWeekend(Integer weekend) {
        this.weekend = weekend;
    }

    public Integer getHoliday() {
        return holiday;
    }

    public void setHoliday(Integer holiday) {
        this.holiday = holiday;
    }

    public Integer getOvertimeNight() {
        return overtimeNight;
    }

    public void setOvertimeNight(Integer overtimeNight) {
        this.overtimeNight = overtimeNight;
    }

    public Integer getOvertimeWeekend() {
        return overtimeWeekend;
    }

    public void setOvertimeWeekend(Integer overtimeWeekend) {
        this.overtimeWeekend = overtimeWeekend;
    }

    public Integer getOvertimeHoliday() {
        return overtimeHoliday;
    }

    public void setOvertimeHoliday(Integer overtimeHoliday) {
        this.overtimeHoliday = overtimeHoliday;
    }

    public Integer getVacation() {
        return vacation;
    }

    public void setVacation(Integer vacation) {
        this.vacation = vacation;
    }

    public Integer getChildDays() {
        return childDays;
    }

    public void setChildDays(Integer childDays) {
        this.childDays = childDays;
    }

    public Integer getIllness() {
        return illness;
    }

    public void setIllness(Integer illness) {
        this.illness = illness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkData workData = (WorkData) o;
        return Objects.equals(days, workData.days) && Objects.equals(hours, workData.hours) && Objects.equals(worked, workData.worked) && Objects.equals(overtime, workData.overtime) && Objects.equals(night, workData.night) && Objects.equals(weekend, workData.weekend) && Objects.equals(holiday, workData.holiday) && Objects.equals(overtimeNight, workData.overtimeNight) && Objects.equals(overtimeWeekend, workData.overtimeWeekend) && Objects.equals(overtimeHoliday, workData.overtimeHoliday) && Objects.equals(vacation, workData.vacation) && Objects.equals(childDays, workData.childDays) && Objects.equals(illness, workData.illness);
    }

    @Override
    public int hashCode() {
        return Objects.hash(days, hours, worked, overtime, night, weekend, holiday, overtimeNight, overtimeWeekend, overtimeHoliday, vacation, childDays, illness);
    }

    @Override
    public String toString() {
        return "WorkData{" +
                "days=" + days +
                ", hours=" + hours +
                ", worked=" + worked +
                ", overtime=" + overtime +
                ", night=" + night +
                ", weekend=" + weekend +
                ", holiday=" + holiday +
                ", overtimeNight=" + overtimeNight +
                ", overtimeWeekend=" + overtimeWeekend +
                ", overtimeHoliday=" + overtimeHoliday +
                ", vacation=" + vacation +
                ", childDays=" + childDays +
                ", illness=" + illness +
                '}';
    }
}
