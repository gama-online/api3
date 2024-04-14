package lt.gama.model.dto.entities;

import lt.gama.model.type.auth.CompanyTaxSettings;
import lt.gama.model.type.doc.DocChargeAmount;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IEmployeeCharge;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.salary.EmployeeTaxSettings;
import lt.gama.model.type.salary.WorkData;

import java.time.LocalDate;
import java.util.List;

public class EmployeeChargeDto extends BaseCompanyDto implements IEmployeeCharge {

    /**
     * Parent (Salary) id
     * ignored in DB. Used in backend and frontend communications only.
     */
    private Long parentId;

    /**
     * Charges accounting day - it will be the first day of the month
     */
    private LocalDate date;

    private EmployeeDto employee;

    private EmployeeCardDto employeeCard;

    /**
     * Tax-exempt - calculated or copied from settings
     */
    private GamaMoney taxExempt;

    /**
     * Additional Tax exempt - copied from settings
     */
    private GamaMoney addTaxExempt;

    /**
     * List of all charges of employee in accounting period (the month as usually)
     */
    private List<DocChargeAmount> charges;

    /**
     * Employee Social security tax
     */
    private GamaMoney employeeSSTax;

    /**
     * Employer/Company Social security tax
     */
    private GamaMoney companySSTax;

    /**
     * Income tax
     */
    private GamaMoney incomeTax;

    /**
     * Guaranty fund tax
     */
    private GamaMoney guarantyFundTax;

    /**
     * Statutory health insurance (SHI) tax
     */
    private GamaMoney shiTax;

    /**
     * Total amount before all taxes
     */
    private GamaMoney total;

    /**
     * Total amount of Social security taxes
     */
    private GamaMoney totalSS;

    /**
     * Total amount of income taxes
     */
    private GamaMoney totalIncome;

    /**
     * Net amount/salary, i.e. after all taxes
     */
    private GamaMoney net;

    /**
     * Advance amount
     */
    private GamaMoney advance;

    /**
     * Net amount total, i.e. net amount without advance amount
     */
    private GamaMoney netTotal;

    private WorkData workData;


    private List<GLOperationDto> operations;

    private Boolean finished;

    private EmployeeTaxSettings employeeTaxSettings;

    private CompanyTaxSettings companyTaxSettings;


    public EmployeeChargeDto() {
    }

    public EmployeeChargeDto(Long parentId, EmployeeDto employee, EmployeeCardDto employeeCard, CompanyTaxSettings companyTaxSettings) {
        this.employee = employee;
        this.employeeCard = employeeCard;
        this.companyTaxSettings = companyTaxSettings;

        this.parentId = parentId;
    }

    public Boolean getFullyFinished() {
        return finished;
    }

    @Override
    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    // generated

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public EmployeeCardDto getEmployeeCard() {
        return employeeCard;
    }

    public void setEmployeeCard(EmployeeCardDto employeeCard) {
        this.employeeCard = employeeCard;
    }

    @Override
    public GamaMoney getTaxExempt() {
        return taxExempt;
    }

    @Override
    public void setTaxExempt(GamaMoney taxExempt) {
        this.taxExempt = taxExempt;
    }

    @Override
    public GamaMoney getAddTaxExempt() {
        return addTaxExempt;
    }

    @Override
    public void setAddTaxExempt(GamaMoney addTaxExempt) {
        this.addTaxExempt = addTaxExempt;
    }

    @Override
    public List<DocChargeAmount> getCharges() {
        return charges;
    }

    @Override
    public void setCharges(List<DocChargeAmount> charges) {
        this.charges = charges;
    }

    @Override
    public GamaMoney getEmployeeSSTax() {
        return employeeSSTax;
    }

    @Override
    public void setEmployeeSSTax(GamaMoney employeeSSTax) {
        this.employeeSSTax = employeeSSTax;
    }

    @Override
    public GamaMoney getCompanySSTax() {
        return companySSTax;
    }

    @Override
    public void setCompanySSTax(GamaMoney companySSTax) {
        this.companySSTax = companySSTax;
    }

    @Override
    public GamaMoney getIncomeTax() {
        return incomeTax;
    }

    @Override
    public void setIncomeTax(GamaMoney incomeTax) {
        this.incomeTax = incomeTax;
    }

    @Override
    public GamaMoney getGuarantyFundTax() {
        return guarantyFundTax;
    }

    @Override
    public void setGuarantyFundTax(GamaMoney guarantyFundTax) {
        this.guarantyFundTax = guarantyFundTax;
    }

    @Override
    public GamaMoney getShiTax() {
        return shiTax;
    }

    @Override
    public void setShiTax(GamaMoney shiTax) {
        this.shiTax = shiTax;
    }

    @Override
    public GamaMoney getTotal() {
        return total;
    }

    @Override
    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    @Override
    public GamaMoney getTotalSS() {
        return totalSS;
    }

    @Override
    public void setTotalSS(GamaMoney totalSS) {
        this.totalSS = totalSS;
    }

    @Override
    public GamaMoney getTotalIncome() {
        return totalIncome;
    }

    @Override
    public void setTotalIncome(GamaMoney totalIncome) {
        this.totalIncome = totalIncome;
    }

    @Override
    public GamaMoney getNet() {
        return net;
    }

    @Override
    public void setNet(GamaMoney net) {
        this.net = net;
    }

    @Override
    public GamaMoney getAdvance() {
        return advance;
    }

    @Override
    public void setAdvance(GamaMoney advance) {
        this.advance = advance;
    }

    @Override
    public GamaMoney getNetTotal() {
        return netTotal;
    }

    @Override
    public void setNetTotal(GamaMoney netTotal) {
        this.netTotal = netTotal;
    }

    @Override
    public WorkData getWorkData() {
        return workData;
    }

    @Override
    public void setWorkData(WorkData workData) {
        this.workData = workData;
    }

    @Override
    public List<GLOperationDto> getOperations() {
        return operations;
    }

    @Override
    public void setOperations(List<GLOperationDto> operations) {
        this.operations = operations;
    }

    @Override
    public Boolean getFinished() {
        return finished;
    }

    @Override
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public EmployeeTaxSettings getEmployeeTaxSettings() {
        return employeeTaxSettings;
    }

    public void setEmployeeTaxSettings(EmployeeTaxSettings employeeTaxSettings) {
        this.employeeTaxSettings = employeeTaxSettings;
    }

    public CompanyTaxSettings getCompanyTaxSettings() {
        return companyTaxSettings;
    }

    public void setCompanyTaxSettings(CompanyTaxSettings companyTaxSettings) {
        this.companyTaxSettings = companyTaxSettings;
    }

    @Override
    public String toString() {
        return "EmployeeChargeDto{" +
                "parentId=" + parentId +
                ", date=" + date +
                ", employee=" + employee +
                ", employeeCard=" + employeeCard +
                ", taxExempt=" + taxExempt +
                ", addTaxExempt=" + addTaxExempt +
                ", charges=" + charges +
                ", employeeSSTax=" + employeeSSTax +
                ", companySSTax=" + companySSTax +
                ", incomeTax=" + incomeTax +
                ", guarantyFundTax=" + guarantyFundTax +
                ", shiTax=" + shiTax +
                ", total=" + total +
                ", totalSS=" + totalSS +
                ", totalIncome=" + totalIncome +
                ", net=" + net +
                ", advance=" + advance +
                ", netTotal=" + netTotal +
                ", workData=" + workData +
                ", operations=" + operations +
                ", finished=" + finished +
//                ", employeeTaxSettings=" + employeeTaxSettings +
//                ", companyTaxSettings=" + companyTaxSettings +
                "} " + super.toString();
    }
}
