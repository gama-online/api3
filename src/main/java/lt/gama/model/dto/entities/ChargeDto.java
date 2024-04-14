package lt.gama.model.dto.entities;

import lt.gama.model.type.gl.GLDCActive;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.ICharge;
import lt.gama.model.type.enums.AvgSalaryType;

import java.io.Serial;

public class ChargeDto extends BaseCompanyDto implements ICharge {

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
     * SHI tax
     */
    private GLDCActive shiTax;


    @SuppressWarnings("unused")
    protected ChargeDto() {}

    public ChargeDto(String name) {
        this.name = name;
        this.period = 1;
        this.avgSalary = AvgSalaryType.ALL;
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
    public String toString() {
        return "ChargeDto{" +
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
