package lt.gama.model.type.doc;

import lt.gama.model.i.ICharge;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.enums.AvgSalaryType;
import lt.gama.model.type.gl.GLDCActive;
import lt.gama.model.type.gl.GLOperationAccount;

import java.io.Serial;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-03-09.
 */
public class DocCharge extends BaseDocEntity implements ICharge {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private GLOperationAccount debit;

    private GLOperationAccount credit;

    /**
     * Average avgSalary calculation type
     */
    private AvgSalaryType avgSalary;

    /**
     * Period of charge in months - default 1.
     */
    private int period;

    /**
     * Employee Social security tax
     */
    private GLDCActive employeeSSTax;

    /**
     * Employer/Company Social security tax
     */
    private GLDCActive companySSTax;

    /**
     * Income tax
     */
    private GLDCActive incomeTax;

    /**
     * Guaranty fund tax
     */
    private GLDCActive guarantyFund;

    /**
     * Statutory health insurance (SHI) tax rate
     */
    private GLDCActive shiTax;


    public DocCharge() {
    }

    public DocCharge(ICharge charge) {
        if (charge == null) return;
        setId(charge.getId());
        name = charge.getName();
        debit = charge.getDebit();
        credit = charge.getCredit();
        avgSalary = charge.getAvgSalary();
        period = charge.getPeriod();
        employeeSSTax = charge.getEmployeeSSTax();
        companySSTax = charge.getCompanySSTax();
        incomeTax = charge.getIncomeTax();
        guarantyFund = charge.getGuarantyFund();
        shiTax = charge.getShiTax();
    }

    // generated

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public GLOperationAccount getDebit() {
        return debit;
    }

    public void setDebit(GLOperationAccount debit) {
        this.debit = debit;
    }

    @Override
    public GLOperationAccount getCredit() {
        return credit;
    }

    public void setCredit(GLOperationAccount credit) {
        this.credit = credit;
    }

    @Override
    public AvgSalaryType getAvgSalary() {
        return avgSalary;
    }

    public void setAvgSalary(AvgSalaryType avgSalary) {
        this.avgSalary = avgSalary;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public GLDCActive getEmployeeSSTax() {
        return employeeSSTax;
    }

    public void setEmployeeSSTax(GLDCActive employeeSSTax) {
        this.employeeSSTax = employeeSSTax;
    }

    @Override
    public GLDCActive getCompanySSTax() {
        return companySSTax;
    }

    public void setCompanySSTax(GLDCActive companySSTax) {
        this.companySSTax = companySSTax;
    }

    @Override
    public GLDCActive getIncomeTax() {
        return incomeTax;
    }

    public void setIncomeTax(GLDCActive incomeTax) {
        this.incomeTax = incomeTax;
    }

    @Override
    public GLDCActive getGuarantyFund() {
        return guarantyFund;
    }

    public void setGuarantyFund(GLDCActive guarantyFund) {
        this.guarantyFund = guarantyFund;
    }

    @Override
    public GLDCActive getShiTax() {
        return shiTax;
    }

    public void setShiTax(GLDCActive shiTax) {
        this.shiTax = shiTax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocCharge docCharge = (DocCharge) o;
        return period == docCharge.period && Objects.equals(name, docCharge.name) && Objects.equals(debit, docCharge.debit) && Objects.equals(credit, docCharge.credit) && avgSalary == docCharge.avgSalary && Objects.equals(employeeSSTax, docCharge.employeeSSTax) && Objects.equals(companySSTax, docCharge.companySSTax) && Objects.equals(incomeTax, docCharge.incomeTax) && Objects.equals(guarantyFund, docCharge.guarantyFund) && Objects.equals(shiTax, docCharge.shiTax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, debit, credit, avgSalary, period, employeeSSTax, companySSTax, incomeTax, guarantyFund, shiTax);
    }

    @Override
    public String toString() {
        return "DocCharge{" +
                "name='" + name + '\'' +
                ", debit=" + debit +
                ", credit=" + credit +
                ", avgSalary=" + avgSalary +
                ", period=" + period +
                ", employeeSSTax=" + employeeSSTax +
                ", companySSTax=" + companySSTax +
                ", incomeTax=" + incomeTax +
                ", guarantyFund=" + guarantyFund +
                ", shiTax=" + shiTax +
                "} " + super.toString();
    }
}
