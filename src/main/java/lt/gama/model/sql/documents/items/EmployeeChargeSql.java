package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.i.IEmployeeCharge;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.documents.SalarySql;
import lt.gama.model.sql.entities.EmployeeCardSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocChargeAmount;
import lt.gama.model.type.salary.WorkData;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employee_charge")
@NamedEntityGraph(name = EmployeeChargeSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(EmployeeChargeSql_.EMPLOYEE),
        @NamedAttributeNode(EmployeeChargeSql_.EMPLOYEE_CARD)
})
public class EmployeeChargeSql extends BaseCompanySql implements IEmployeeCharge {

    public static final String GRAPH_ALL = "graph.EmployeeChargeSql.all";

    private static final long serialVersionUID = -1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("employeesCharges")
    private SalarySql salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeSql employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private EmployeeCardSql employeeCard;

    private LocalDate date;

    /**
     * Tax-exempt - calculated or copied from settings
     */
    @Embedded
    private GamaMoney taxExempt;

    /**
     * Additional Tax exempt - copied from settings
     */
    @Embedded
    private GamaMoney addTaxExempt;

    /**
     * List of all charges of employee in accounting period (the month as usually)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocChargeAmount> charges;

    /**
     * Employee Social security tax
     */
    @Embedded
    private GamaMoney employeeSSTax;

    /**
     * Employer/Company Social security tax
     */
    @Embedded
    private GamaMoney companySSTax;

    /**
     * Income tax
     */
    @Embedded
    private GamaMoney incomeTax;

    /**
     * Guaranty fund tax
     */
    @Embedded
    private GamaMoney guarantyFundTax;

    /**
     * Statutory health insurance (SHI) tax
     */
    @Embedded
    private GamaMoney shiTax;

    /**
     * Total amount before all taxes
     */
    @Embedded
    private GamaMoney total;

    /**
     * Total amount of Social security taxes
     */
    @Embedded
    private GamaMoney totalSS;

    /**
     * Total amount of income taxes
     */
    @Embedded
    private GamaMoney totalIncome;

    /**
     * Net amount/salary, i.e. after all taxes
     */
    @Embedded
    private GamaMoney net;

    /**
     * Advance amount
     */
    @Embedded
    private GamaMoney advance;

    /**
     * Net amount total, i.e. net amount without advance amount
     */
    @Embedded
    private GamaMoney netTotal;

    @Embedded
    private WorkData workData;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<GLOperationDto> operations;

    private Boolean finished;


    public void copyFrom(EmployeeChargeSql src) {
        this.taxExempt = src.getTaxExempt();
        this.addTaxExempt = src.getAddTaxExempt();
        this.charges = src.getCharges();
        this.employeeSSTax = src.getEmployeeSSTax();
        this.companySSTax = src.getCompanySSTax();
        this.incomeTax = src.getIncomeTax();
        this.guarantyFundTax = src.getGuarantyFundTax();
        this.shiTax = src.getShiTax();
        this.total = src.getTotal();
        this.totalSS = src.getTotalSS();
        this.totalIncome = src.getTotalIncome();
        this.net = src.getNet();
        this.advance = src.getAdvance();
        this.netTotal = src.getNetTotal();
        this.workData = src.getWorkData();
        this.operations = src.getOperations();
        this.finished = src.getFinished();
    }

    @Override
    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    /**
     * toString except salary, employee, employeeCard
     */
    @Override
    public String toString() {
        return "EmployeeChargeSql{" +
                "date=" + date +
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
                "} " + super.toString();
    }

    // generated

    public SalarySql getSalary() {
        return salary;
    }

    public void setSalary(SalarySql salary) {
        this.salary = salary;
    }

    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
        this.employee = employee;
    }

    public EmployeeCardSql getEmployeeCard() {
        return employeeCard;
    }

    public void setEmployeeCard(EmployeeCardSql employeeCard) {
        this.employeeCard = employeeCard;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public void setDate(LocalDate date) {
        this.date = date;
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
}
