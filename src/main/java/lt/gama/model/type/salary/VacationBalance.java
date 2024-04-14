package lt.gama.model.type.salary;

import lt.gama.helpers.IntegerUtils;
import lt.gama.model.type.doc.DocWorkHours;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-06-01.
 * <p>
 * Employee vacation history records
 */
public class VacationBalance implements Serializable {

    /**
     * Accounting year (can start not only on January - look at ).
     * First year can be not full year, it depends on employee hiring day
     */
    private int year;

    /**
     * The employee's total vacation days annually by law
     */
    private Integer days;

    /**
     * Used vacation days (initial) in current year
     */
    private Integer initial;

    /**
     * Used vacation days
     * WIll be calculated from work-hours documents and will be updated on saving any related work-hours document
     */
    private Integer used;

    /**
     * Used vacation days in advance from past
     */
    private Integer past;


    /**
     * Links to WorkHours docs
     */
    private List<DocWorkHours> docs;


    public VacationBalance() {
    }

    public VacationBalance(int year, Integer days, Integer initial, Integer used) {
        this.year = year;
        this.days = days;
        this.initial = initial;
        this.used = used;
    }

    public VacationBalance(int year, Integer days, Integer initial, Integer used, Integer past) {
        this.year = year;
        this.days = days;
        this.initial = initial;
        this.used = used;
        this.past = past;
    }

    public Integer getBalance() {
        return IntegerUtils.total(days, past, IntegerUtils.negated(initial), IntegerUtils.negated(used));
    }

    // generated

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getInitial() {
        return initial;
    }

    public void setInitial(Integer initial) {
        this.initial = initial;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public Integer getPast() {
        return past;
    }

    public void setPast(Integer past) {
        this.past = past;
    }

    public List<DocWorkHours> getDocs() {
        return docs;
    }

    public void setDocs(List<DocWorkHours> docs) {
        this.docs = docs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacationBalance that = (VacationBalance) o;
        return year == that.year && Objects.equals(days, that.days) && Objects.equals(initial, that.initial) && Objects.equals(used, that.used) && Objects.equals(past, that.past) && Objects.equals(docs, that.docs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, days, initial, used, past, docs);
    }

    @Override
    public String toString() {
        return "VacationBalance{" +
                "year=" + year +
                ", days=" + days +
                ", initial=" + initial +
                ", used=" + used +
                ", past=" + past +
                ", docs=" + docs +
                '}';
    }
}
