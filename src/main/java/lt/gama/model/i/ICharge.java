package lt.gama.model.i;

import lt.gama.model.type.enums.AvgSalaryType;
import lt.gama.model.type.gl.GLDCActive;
import lt.gama.model.type.gl.GLOperationAccount;

public interface ICharge {

    Long getId();
    
    String getName();

    GLOperationAccount getDebit();

    GLOperationAccount getCredit();

    /**
     * Average avgSalary calculation type
     */
    AvgSalaryType getAvgSalary();

    /**
     * Period of charge in months - default 1.
     */
    int getPeriod();

    /**
     * Employee Social security tax
     */
    GLDCActive getEmployeeSSTax();

    /**
     * Employer/Company Social security tax
     */
    GLDCActive getCompanySSTax();

    /**
     * Income tax
     */
    GLDCActive getIncomeTax();

    /**
     * Guaranty fund tax
     */
    GLDCActive getGuarantyFund();

    /**
     * Statutory health insurance (SHI) tax rate
     */
    GLDCActive getShiTax();
}
