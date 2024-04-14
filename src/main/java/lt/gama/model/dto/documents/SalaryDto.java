package lt.gama.model.dto.documents;

import lt.gama.model.type.doc.DocChargeAmount;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.type.GamaMoney;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

public class SalaryDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * List of all charges of all employees in accounting period (the month as usually)
     */
    private List<DocChargeAmount> charges;

    /**
     * Employee Social security tax
     */
    private GamaMoney employeeSSTax;

    /**
     * Employer/Company social security tax rate
     */
    private BigDecimal companySSTaxRate;

    /**
     * Employer/Company Social security tax
     */
    private GamaMoney companySSTax;

    /**
     * Income tax rate
     */
    private BigDecimal incomeTaxRate;

    /**
     * Income tax
     */
    private GamaMoney incomeTax;

    /**
     * Guaranty fund tax rate
     */
    private BigDecimal guarantyFundTaxRate;

    /**
     * Guaranty fund tax
     */
    private GamaMoney guarantyFundTax;

    /**
     * Statutory health insurance (SHI) tax rate
     */
    private BigDecimal shiTaxRate;

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
        return "SalaryDto{" +
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
