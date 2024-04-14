package lt.gama.model.type.salary;

import lt.gama.model.i.IEmployeeCard;
import lt.gama.model.type.enums.SexType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-06-17.
 */
public class EmployeeCardInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Social Security Number
     */
    private String ssn;

    /**
     * National identification number
     */
    private String nin;

    private LocalDate hired;

    private LocalDate fired;

    private SexType sex;


    public EmployeeCardInfo() {
    }

    public EmployeeCardInfo(IEmployeeCard employeeCard) {
        ssn = employeeCard.getSsn();
        nin = employeeCard.getNin();
        hired = employeeCard.getHired();
        fired = employeeCard.getFired();
        sex = employeeCard.getSex();
    }

    // generated

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
    }

    public LocalDate getHired() {
        return hired;
    }

    public void setHired(LocalDate hired) {
        this.hired = hired;
    }

    public LocalDate getFired() {
        return fired;
    }

    public void setFired(LocalDate fired) {
        this.fired = fired;
    }

    public SexType getSex() {
        return sex;
    }

    public void setSex(SexType sex) {
        this.sex = sex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeCardInfo that = (EmployeeCardInfo) o;
        return Objects.equals(ssn, that.ssn) && Objects.equals(nin, that.nin) && Objects.equals(hired, that.hired) && Objects.equals(fired, that.fired) && sex == that.sex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ssn, nin, hired, fired, sex);
    }

    @Override
    public String toString() {
        return "EmployeeCardInfo{" +
                "ssn='" + ssn + '\'' +
                ", nin='" + nin + '\'' +
                ", hired=" + hired +
                ", fired=" + fired +
                ", sex=" + sex +
                '}';
    }
}
