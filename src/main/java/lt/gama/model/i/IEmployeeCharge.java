package lt.gama.model.i;

import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.i.base.IBaseCompany;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocChargeAmount;
import lt.gama.model.type.salary.WorkData;

import java.time.LocalDate;
import java.util.List;

public interface IEmployeeCharge extends IBaseCompany {

    Long getId();

    Long getEmployeeId();

    /**
     * Charges accounting day - it will be the first day of the month
     */
    LocalDate getDate();
    void setDate(LocalDate date);

    /**
     * Tax-exempt - calculated or copied from settings
     */
    GamaMoney getTaxExempt();
    void setTaxExempt(GamaMoney taxExempt);

    /**
     * Additional Tax exempt - copied from settings
     */
    GamaMoney getAddTaxExempt();
    void setAddTaxExempt(GamaMoney addTaxExempt);

    /**
     * List of all charges of employee in accounting period (the month as usually)
     */
    List<DocChargeAmount> getCharges();
    void setCharges(List<DocChargeAmount> charges);

    /**
     * Employee Social security tax
     */
    GamaMoney getEmployeeSSTax();
    void setEmployeeSSTax(GamaMoney employeeSSTax);

    /**
     * Employer/Company Social security tax
     */
    GamaMoney getCompanySSTax();
    void setCompanySSTax(GamaMoney companySSTax);

    /**
     * Income tax
     */
    GamaMoney getIncomeTax();
    void setIncomeTax(GamaMoney incomeTax);

    /**
     * Guaranty fund tax
     */
    GamaMoney getGuarantyFundTax();
    void setGuarantyFundTax(GamaMoney guarantyFundTax);

    /**
     * Statutory health insurance (SHI) tax
     */
    GamaMoney getShiTax();
    void setShiTax(GamaMoney shiTax);

    /**
     * Total amount before all taxes
     */
    GamaMoney getTotal();
    void setTotal(GamaMoney total);

    /**
     * Total amount of Social security taxes
     */
    GamaMoney getTotalSS();
    void setTotalSS(GamaMoney totalSS);

    /**
     * Total amount of income taxes
     */
    GamaMoney getTotalIncome();
    void setTotalIncome(GamaMoney totalIncome);

    /**
     * Net amount/salary, i.e. after all taxes
     */
    GamaMoney getNet();
    void setNet(GamaMoney net);

    /**
     * Advance amount
     */
    GamaMoney getAdvance();
    void setAdvance(GamaMoney advance);

    /**
     * Net amount total, i.e. net amount without advance amount
     */
    GamaMoney getNetTotal();
    void setNetTotal(GamaMoney netTotal);

    WorkData getWorkData();
    void setWorkData(WorkData workData);

    List<GLOperationDto> getOperations();
    void setOperations(List<GLOperationDto> operations);

    Boolean getFinished();
    void setFinished(Boolean finished);
}
