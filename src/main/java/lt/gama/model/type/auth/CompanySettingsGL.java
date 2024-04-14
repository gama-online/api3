package lt.gama.model.type.auth;

import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-08-10.
 */
public class CompanySettingsGL implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private GLOperationAccount accVATRec;

    private GLOperationAccount accVATPay;

    private GLOperationAccount accBankOther;

    private GLOperationAccount accPurchaseExpense;

    private GLOperationAccount accTemp;

    private GLOperationAccount accCurrRatePos;

    private GLOperationAccount accCurrRateNeg;

    private GLOperationAccount accProfitLoss;


    /*
     * Products default values
     */
    private GLOperationAccount productAsset;

    private GLOperationAccount productIncome;

    private GLOperationAccount productExpense;


    /*
     * Services default values
     */
    private GLOperationAccount serviceIncome;

    private GLOperationAccount serviceExpense;


    /*
     *  Counterparties (Vendor/Customer) default values
     */
    private GLOperationAccount counterpartyVendor;

    private GLOperationAccount counterpartyCustomer;


    /**
     * G.L. Account used in bank/cash operations with employee and 'noDebt' set to true
     */
    private GLOperationAccount employeeNoDebt;


    /**
     * Generate invoice double-entry records with separate operation line for each part
     */
    private Boolean expandInvoice;

    /**
     * Auto VAT Code totals G.L. account settings,
     * i.e. remember last used G.L. accounts and will use it in future
     */
    private Map<String, GLDC> vatCode;

    // generated

    public GLOperationAccount getAccVATRec() {
        return accVATRec;
    }

    public void setAccVATRec(GLOperationAccount accVATRec) {
        this.accVATRec = accVATRec;
    }

    public GLOperationAccount getAccVATPay() {
        return accVATPay;
    }

    public void setAccVATPay(GLOperationAccount accVATPay) {
        this.accVATPay = accVATPay;
    }

    public GLOperationAccount getAccBankOther() {
        return accBankOther;
    }

    public void setAccBankOther(GLOperationAccount accBankOther) {
        this.accBankOther = accBankOther;
    }

    public GLOperationAccount getAccPurchaseExpense() {
        return accPurchaseExpense;
    }

    public void setAccPurchaseExpense(GLOperationAccount accPurchaseExpense) {
        this.accPurchaseExpense = accPurchaseExpense;
    }

    public GLOperationAccount getAccTemp() {
        return accTemp;
    }

    public void setAccTemp(GLOperationAccount accTemp) {
        this.accTemp = accTemp;
    }

    public GLOperationAccount getAccCurrRatePos() {
        return accCurrRatePos;
    }

    public void setAccCurrRatePos(GLOperationAccount accCurrRatePos) {
        this.accCurrRatePos = accCurrRatePos;
    }

    public GLOperationAccount getAccCurrRateNeg() {
        return accCurrRateNeg;
    }

    public void setAccCurrRateNeg(GLOperationAccount accCurrRateNeg) {
        this.accCurrRateNeg = accCurrRateNeg;
    }

    public GLOperationAccount getAccProfitLoss() {
        return accProfitLoss;
    }

    public void setAccProfitLoss(GLOperationAccount accProfitLoss) {
        this.accProfitLoss = accProfitLoss;
    }

    public GLOperationAccount getProductAsset() {
        return productAsset;
    }

    public void setProductAsset(GLOperationAccount productAsset) {
        this.productAsset = productAsset;
    }

    public GLOperationAccount getProductIncome() {
        return productIncome;
    }

    public void setProductIncome(GLOperationAccount productIncome) {
        this.productIncome = productIncome;
    }

    public GLOperationAccount getProductExpense() {
        return productExpense;
    }

    public void setProductExpense(GLOperationAccount productExpense) {
        this.productExpense = productExpense;
    }

    public GLOperationAccount getServiceIncome() {
        return serviceIncome;
    }

    public void setServiceIncome(GLOperationAccount serviceIncome) {
        this.serviceIncome = serviceIncome;
    }

    public GLOperationAccount getServiceExpense() {
        return serviceExpense;
    }

    public void setServiceExpense(GLOperationAccount serviceExpense) {
        this.serviceExpense = serviceExpense;
    }

    public GLOperationAccount getCounterpartyVendor() {
        return counterpartyVendor;
    }

    public void setCounterpartyVendor(GLOperationAccount counterpartyVendor) {
        this.counterpartyVendor = counterpartyVendor;
    }

    public GLOperationAccount getCounterpartyCustomer() {
        return counterpartyCustomer;
    }

    public void setCounterpartyCustomer(GLOperationAccount counterpartyCustomer) {
        this.counterpartyCustomer = counterpartyCustomer;
    }

    public GLOperationAccount getEmployeeNoDebt() {
        return employeeNoDebt;
    }

    public void setEmployeeNoDebt(GLOperationAccount employeeNoDebt) {
        this.employeeNoDebt = employeeNoDebt;
    }

    public Boolean getExpandInvoice() {
        return expandInvoice;
    }

    public void setExpandInvoice(Boolean expandInvoice) {
        this.expandInvoice = expandInvoice;
    }

    public Map<String, GLDC> getVatCode() {
        return vatCode;
    }

    public void setVatCode(Map<String, GLDC> vatCode) {
        this.vatCode = vatCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanySettingsGL that = (CompanySettingsGL) o;
        return Objects.equals(accVATRec, that.accVATRec) && Objects.equals(accVATPay, that.accVATPay) && Objects.equals(accBankOther, that.accBankOther) && Objects.equals(accPurchaseExpense, that.accPurchaseExpense) && Objects.equals(accTemp, that.accTemp) && Objects.equals(accCurrRatePos, that.accCurrRatePos) && Objects.equals(accCurrRateNeg, that.accCurrRateNeg) && Objects.equals(accProfitLoss, that.accProfitLoss) && Objects.equals(productAsset, that.productAsset) && Objects.equals(productIncome, that.productIncome) && Objects.equals(productExpense, that.productExpense) && Objects.equals(serviceIncome, that.serviceIncome) && Objects.equals(serviceExpense, that.serviceExpense) && Objects.equals(counterpartyVendor, that.counterpartyVendor) && Objects.equals(counterpartyCustomer, that.counterpartyCustomer) && Objects.equals(employeeNoDebt, that.employeeNoDebt) && Objects.equals(expandInvoice, that.expandInvoice) && Objects.equals(vatCode, that.vatCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accVATRec, accVATPay, accBankOther, accPurchaseExpense, accTemp, accCurrRatePos, accCurrRateNeg, accProfitLoss, productAsset, productIncome, productExpense, serviceIncome, serviceExpense, counterpartyVendor, counterpartyCustomer, employeeNoDebt, expandInvoice, vatCode);
    }

    @Override
    public String toString() {
        return "CompanySettingsGL{" +
                "accVATRec=" + accVATRec +
                ", accVATPay=" + accVATPay +
                ", accBankOther=" + accBankOther +
                ", accPurchaseExpense=" + accPurchaseExpense +
                ", accTemp=" + accTemp +
                ", accCurrRatePos=" + accCurrRatePos +
                ", accCurrRateNeg=" + accCurrRateNeg +
                ", accProfitLoss=" + accProfitLoss +
                ", productAsset=" + productAsset +
                ", productIncome=" + productIncome +
                ", productExpense=" + productExpense +
                ", serviceIncome=" + serviceIncome +
                ", serviceExpense=" + serviceExpense +
                ", counterpartyVendor=" + counterpartyVendor +
                ", counterpartyCustomer=" + counterpartyCustomer +
                ", employeeNoDebt=" + employeeNoDebt +
                ", expandInvoice=" + expandInvoice +
                ", vatCode=" + vatCode +
                '}';
    }
}
