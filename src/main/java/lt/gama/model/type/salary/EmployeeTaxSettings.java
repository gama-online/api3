package lt.gama.model.type.salary;

import lt.gama.model.type.GamaMoney;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-03-08.
 */
public class EmployeeTaxSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Valid date from (until next valid date)
     */
    private LocalDate date;

    /**
     * Amount of tax-exempt income - if fixed for this employee
     */
    private GamaMoney taxExempt;

    /**
     * Additional amount of tax-exempt income - if fixed for this employee
     */
    private GamaMoney addTaxExempt;

    /**
     * Employee social security base tax rate
     */
    private BigDecimal employeeSSTaxRate;

    /**
     * Employee social security additional tax rate index: null or 0 or 1
     */
    private Integer employeeSSAddTaxRateIndex;

    /**
     * Employee social security tax rate from index - used in frontend
     */
    private BigDecimal employeeSSTaxRateTotal;

    /**
     * The length of the employee vacation in working days
     */
    private Integer vacationLength;

    /**
     * Income tax rate
     */
    private BigDecimal incomeTaxRate;

    public EmployeeTaxSettings() {
    }

    public EmployeeTaxSettings(EmployeeTaxSettings src) {
        this.date = src.date;
        this.taxExempt = src.taxExempt;
        this.addTaxExempt = src.addTaxExempt;
        this.employeeSSTaxRate = src.employeeSSTaxRate;
        this.employeeSSAddTaxRateIndex = src.employeeSSAddTaxRateIndex;
        this.employeeSSTaxRateTotal = src.employeeSSTaxRateTotal;
        this.vacationLength = src.vacationLength;
        this.incomeTaxRate = src.incomeTaxRate;
    }

    public EmployeeTaxSettings(LocalDate date, GamaMoney taxExempt, GamaMoney addTaxExempt,
                               BigDecimal employeeSSTaxRate,
                               Integer employeeSSAddTaxRateIndex,
                               BigDecimal employeeSSTaxRateTotal,
                               Integer vacationLength,
                               BigDecimal incomeTaxRate) {
        this.date = date;
        this.taxExempt = taxExempt;
        this.addTaxExempt = addTaxExempt;
        this.employeeSSTaxRate = employeeSSTaxRate;
        this.employeeSSAddTaxRateIndex = employeeSSAddTaxRateIndex;
        this.employeeSSTaxRateTotal = employeeSSTaxRateTotal;
        this.vacationLength = vacationLength;
        this.incomeTaxRate = incomeTaxRate;
    }

    // generated

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public GamaMoney getTaxExempt() {
        return taxExempt;
    }

    public void setTaxExempt(GamaMoney taxExempt) {
        this.taxExempt = taxExempt;
    }

    public GamaMoney getAddTaxExempt() {
        return addTaxExempt;
    }

    public void setAddTaxExempt(GamaMoney addTaxExempt) {
        this.addTaxExempt = addTaxExempt;
    }

    public BigDecimal getEmployeeSSTaxRate() {
        return employeeSSTaxRate;
    }

    public void setEmployeeSSTaxRate(BigDecimal employeeSSTaxRate) {
        this.employeeSSTaxRate = employeeSSTaxRate;
    }

    public Integer getEmployeeSSAddTaxRateIndex() {
        return employeeSSAddTaxRateIndex;
    }

    public void setEmployeeSSAddTaxRateIndex(Integer employeeSSAddTaxRateIndex) {
        this.employeeSSAddTaxRateIndex = employeeSSAddTaxRateIndex;
    }

    public BigDecimal getEmployeeSSTaxRateTotal() {
        return employeeSSTaxRateTotal;
    }

    public void setEmployeeSSTaxRateTotal(BigDecimal employeeSSTaxRateTotal) {
        this.employeeSSTaxRateTotal = employeeSSTaxRateTotal;
    }

    public Integer getVacationLength() {
        return vacationLength;
    }

    public void setVacationLength(Integer vacationLength) {
        this.vacationLength = vacationLength;
    }

    public BigDecimal getIncomeTaxRate() {
        return incomeTaxRate;
    }

    public void setIncomeTaxRate(BigDecimal incomeTaxRate) {
        this.incomeTaxRate = incomeTaxRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeTaxSettings that = (EmployeeTaxSettings) o;
        return Objects.equals(date, that.date) && Objects.equals(taxExempt, that.taxExempt) && Objects.equals(addTaxExempt, that.addTaxExempt) && Objects.equals(employeeSSTaxRate, that.employeeSSTaxRate) && Objects.equals(employeeSSAddTaxRateIndex, that.employeeSSAddTaxRateIndex) && Objects.equals(employeeSSTaxRateTotal, that.employeeSSTaxRateTotal) && Objects.equals(vacationLength, that.vacationLength) && Objects.equals(incomeTaxRate, that.incomeTaxRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, taxExempt, addTaxExempt, employeeSSTaxRate, employeeSSAddTaxRateIndex, employeeSSTaxRateTotal, vacationLength, incomeTaxRate);
    }

    @Override
    public String toString() {
        return "EmployeeTaxSettings{" +
                "date=" + date +
                ", taxExempt=" + taxExempt +
                ", addTaxExempt=" + addTaxExempt +
                ", employeeSSTaxRate=" + employeeSSTaxRate +
                ", employeeSSAddTaxRateIndex=" + employeeSSAddTaxRateIndex +
                ", employeeSSTaxRateTotal=" + employeeSSTaxRateTotal +
                ", vacationLength=" + vacationLength +
                ", incomeTaxRate=" + incomeTaxRate +
                '}';
    }
}
