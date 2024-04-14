package lt.gama.model.type.auth;

import lt.gama.model.type.gl.GLDC;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-02-02.
 */
public class CompanyTaxSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Valid date from (until next valid date)
     */
    private LocalDate date;

    /**
     * Employee Social security tax G.L. accounts
     */
    private GLDC employeeSS;

    /**
     * Employee Social security base tax rate
     */
    private BigDecimal employeeSSTaxRate;

    /**
     * Employee social security additional tax rates
     */
    private List<BigDecimal> employeeSSAddTaxRates;

    /**
     * Employer/Company Social security tax G.L. accounts
     */
    private GLDC companySS;

    /**
     * Employer/Company social security tax rate
     */
    private BigDecimal companySSTaxRate;

    /**
     * Income tax G.L. accounts
     */
    private GLDC income;

    /**
     * Income tax rate
     */
    private BigDecimal incomeTaxRate;

    /**
     * Guaranty fund tax G.L. accounts
     */
    private GLDC guarantyFund;

    /**
     * Guaranty fund tax rate
     */
    private BigDecimal guarantyFundTaxRate;

    /**
     * SHI tax G.L. accounts
     */
    private GLDC shi;

    /**
     * Statutory health insurance (SHI) tax rate
     */
    private BigDecimal shiTaxRate;

    public CompanyTaxSettings() {}

    public CompanyTaxSettings(LocalDate date) {
        this.date = date;
    }

    // generated


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public GLDC getEmployeeSS() {
        return employeeSS;
    }

    public void setEmployeeSS(GLDC employeeSS) {
        this.employeeSS = employeeSS;
    }

    public BigDecimal getEmployeeSSTaxRate() {
        return employeeSSTaxRate;
    }

    public void setEmployeeSSTaxRate(BigDecimal employeeSSTaxRate) {
        this.employeeSSTaxRate = employeeSSTaxRate;
    }

    public List<BigDecimal> getEmployeeSSAddTaxRates() {
        return employeeSSAddTaxRates;
    }

    public void setEmployeeSSAddTaxRates(List<BigDecimal> employeeSSAddTaxRates) {
        this.employeeSSAddTaxRates = employeeSSAddTaxRates;
    }

    public GLDC getCompanySS() {
        return companySS;
    }

    public void setCompanySS(GLDC companySS) {
        this.companySS = companySS;
    }

    public BigDecimal getCompanySSTaxRate() {
        return companySSTaxRate;
    }

    public void setCompanySSTaxRate(BigDecimal companySSTaxRate) {
        this.companySSTaxRate = companySSTaxRate;
    }

    public GLDC getIncome() {
        return income;
    }

    public void setIncome(GLDC income) {
        this.income = income;
    }

    public BigDecimal getIncomeTaxRate() {
        return incomeTaxRate;
    }

    public void setIncomeTaxRate(BigDecimal incomeTaxRate) {
        this.incomeTaxRate = incomeTaxRate;
    }

    public GLDC getGuarantyFund() {
        return guarantyFund;
    }

    public void setGuarantyFund(GLDC guarantyFund) {
        this.guarantyFund = guarantyFund;
    }

    public BigDecimal getGuarantyFundTaxRate() {
        return guarantyFundTaxRate;
    }

    public void setGuarantyFundTaxRate(BigDecimal guarantyFundTaxRate) {
        this.guarantyFundTaxRate = guarantyFundTaxRate;
    }

    public GLDC getShi() {
        return shi;
    }

    public void setShi(GLDC shi) {
        this.shi = shi;
    }

    public BigDecimal getShiTaxRate() {
        return shiTaxRate;
    }

    public void setShiTaxRate(BigDecimal shiTaxRate) {
        this.shiTaxRate = shiTaxRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyTaxSettings that = (CompanyTaxSettings) o;
        return Objects.equals(date, that.date) && Objects.equals(employeeSS, that.employeeSS) && Objects.equals(employeeSSTaxRate, that.employeeSSTaxRate) && Objects.equals(employeeSSAddTaxRates, that.employeeSSAddTaxRates) && Objects.equals(companySS, that.companySS) && Objects.equals(companySSTaxRate, that.companySSTaxRate) && Objects.equals(income, that.income) && Objects.equals(incomeTaxRate, that.incomeTaxRate) && Objects.equals(guarantyFund, that.guarantyFund) && Objects.equals(guarantyFundTaxRate, that.guarantyFundTaxRate) && Objects.equals(shi, that.shi) && Objects.equals(shiTaxRate, that.shiTaxRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, employeeSS, employeeSSTaxRate, employeeSSAddTaxRates, companySS, companySSTaxRate, income, incomeTaxRate, guarantyFund, guarantyFundTaxRate, shi, shiTaxRate);
    }

    @Override
    public String toString() {
        return "CompanyTaxSettings{" +
                "date=" + date +
                ", employeeSS=" + employeeSS +
                ", employeeSSTaxRate=" + employeeSSTaxRate +
                ", employeeSSAddTaxRates=" + employeeSSAddTaxRates +
                ", companySS=" + companySS +
                ", companySSTaxRate=" + companySSTaxRate +
                ", income=" + income +
                ", incomeTaxRate=" + incomeTaxRate +
                ", guarantyFund=" + guarantyFund +
                ", guarantyFundTaxRate=" + guarantyFundTaxRate +
                ", shi=" + shi +
                ", shiTaxRate=" + shiTaxRate +
                '}';
    }
}
