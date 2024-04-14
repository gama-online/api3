package lt.gama.model.type.salary;

import lt.gama.model.type.GamaMoney;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 20/10/2018.
 *
 */
public class SalaryPerMonth implements Serializable {

    /**
     * Date is always the last day of the month
     */
    private LocalDate month;

    /**
     * Work days in month
     */
    private int workDays;
    
    /**
     * Total salary per month
     */
    private GamaMoney salary;


    public SalaryPerMonth() {
    }

    public SalaryPerMonth(LocalDate month, int workDays, GamaMoney salary) {
        this.month = month;
        this.workDays = workDays;
        this.salary = salary;
    }

    // generated

    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public int getWorkDays() {
        return workDays;
    }

    public void setWorkDays(int workDays) {
        this.workDays = workDays;
    }

    public GamaMoney getSalary() {
        return salary;
    }

    public void setSalary(GamaMoney salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalaryPerMonth that = (SalaryPerMonth) o;
        return workDays == that.workDays && Objects.equals(month, that.month) && Objects.equals(salary, that.salary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(month, workDays, salary);
    }

    @Override
    public String toString() {
        return "SalaryPerMonth{" +
                "month=" + month +
                ", workDays=" + workDays +
                ", salary=" + salary +
                '}';
    }
}
