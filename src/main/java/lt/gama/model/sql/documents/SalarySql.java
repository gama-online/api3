package lt.gama.model.sql.documents;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocChargeAmount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "salary")
public class SalarySql extends BaseDocumentSql {

    /**
     * List of all charges of all employees in accounting period (the month as usually)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocChargeAmount> charges;

    /**
     * Employee Social security tax
     */
    @Embedded
    private GamaMoney employeeSSTax;

    /**
     * Employer/Company social security tax rate
     */
    private BigDecimal companySSTaxRate;

    /**
     * Employer/Company Social security tax
     */
    @Embedded
    private GamaMoney companySSTax;

    /**
     * Income tax rate
     */
    private BigDecimal incomeTaxRate;

    /**
     * Income tax
     */
    @Embedded
    private GamaMoney incomeTax;

    /**
     * Guaranty fund tax rate
     */
    private BigDecimal guarantyFundTaxRate;

    /**
     * Guaranty fund tax
     */
    @Embedded
    private GamaMoney guarantyFundTax;

    /**
     * Statutory health insurance (SHI) tax rate
     */
    private BigDecimal shiTaxRate;

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

    // generated

    public List<DocChargeAmount> getCharges() {
        return charges;
    }

    public void setCharges(List<DocChargeAmount> charges) {
        this.charges = charges;
    }

    public GamaMoney getEmployeeSSTax() {
        return employeeSSTax;
    }

    public void setEmployeeSSTax(GamaMoney employeeSSTax) {
        this.employeeSSTax = employeeSSTax;
    }

    public BigDecimal getCompanySSTaxRate() {
        return companySSTaxRate;
    }

    public void setCompanySSTaxRate(BigDecimal companySSTaxRate) {
        this.companySSTaxRate = companySSTaxRate;
    }

    public GamaMoney getCompanySSTax() {
        return companySSTax;
    }

    public void setCompanySSTax(GamaMoney companySSTax) {
        this.companySSTax = companySSTax;
    }

    public BigDecimal getIncomeTaxRate() {
        return incomeTaxRate;
    }

    public void setIncomeTaxRate(BigDecimal incomeTaxRate) {
        this.incomeTaxRate = incomeTaxRate;
    }

    public GamaMoney getIncomeTax() {
        return incomeTax;
    }

    public void setIncomeTax(GamaMoney incomeTax) {
        this.incomeTax = incomeTax;
    }

    public BigDecimal getGuarantyFundTaxRate() {
        return guarantyFundTaxRate;
    }

    public void setGuarantyFundTaxRate(BigDecimal guarantyFundTaxRate) {
        this.guarantyFundTaxRate = guarantyFundTaxRate;
    }

    public GamaMoney getGuarantyFundTax() {
        return guarantyFundTax;
    }

    public void setGuarantyFundTax(GamaMoney guarantyFundTax) {
        this.guarantyFundTax = guarantyFundTax;
    }

    public BigDecimal getShiTaxRate() {
        return shiTaxRate;
    }

    public void setShiTaxRate(BigDecimal shiTaxRate) {
        this.shiTaxRate = shiTaxRate;
    }

    public GamaMoney getShiTax() {
        return shiTax;
    }

    public void setShiTax(GamaMoney shiTax) {
        this.shiTax = shiTax;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getTotalSS() {
        return totalSS;
    }

    public void setTotalSS(GamaMoney totalSS) {
        this.totalSS = totalSS;
    }

    public GamaMoney getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(GamaMoney totalIncome) {
        this.totalIncome = totalIncome;
    }

    public GamaMoney getNet() {
        return net;
    }

    public void setNet(GamaMoney net) {
        this.net = net;
    }

    public GamaMoney getAdvance() {
        return advance;
    }

    public void setAdvance(GamaMoney advance) {
        this.advance = advance;
    }

    public GamaMoney getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(GamaMoney netTotal) {
        this.netTotal = netTotal;
    }

    @Override
    public String toString() {
        return "SalarySql{" +
                "charges=" + charges +
                ", employeeSSTax=" + employeeSSTax +
                ", companySSTaxRate=" + companySSTaxRate +
                ", companySSTax=" + companySSTax +
                ", incomeTaxRate=" + incomeTaxRate +
                ", incomeTax=" + incomeTax +
                ", guarantyFundTaxRate=" + guarantyFundTaxRate +
                ", guarantyFundTax=" + guarantyFundTax +
                ", shiTaxRate=" + shiTaxRate +
                ", shiTax=" + shiTax +
                ", total=" + total +
                ", totalSS=" + totalSS +
                ", totalIncome=" + totalIncome +
                ", net=" + net +
                ", advance=" + advance +
                ", netTotal=" + netTotal +
                "} " + super.toString();
    }
}
