package lt.gama.model.dto.entities;

import lt.gama.model.type.Location;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.dto.type.AssetTotal;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.model.type.asset.Depreciation;
import lt.gama.model.type.enums.AssetStatusType;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

public class AssetDto extends BaseCompanyDto {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * If code is automatically assigned.
     * Used for frontend to backend communications only
     */
    private Boolean autoCode;

    private String code;

    private String cipher;

    private String name;

    private boolean tangible;

    private String note;

    private LocalDate acquisitionDate;

    private GamaMoney cost;

    private GamaMoney vat;

    /**
     * Start of accounting date
     */
    private LocalDate date;

    /**
     * cost at accounting date
     */
    private GamaMoney value;

    /**
     * Deprecation expenses at accounting date
     */
    private GamaMoney expenses;

    /**
     * Written-off value
     */
    private GamaMoney writtenOff;

    private List<AssetHistory> history;

    private List<Depreciation> depreciation;

    /**
     * Last responsive employee
     */
    private EmployeeDto responsible;

    /**
     * Last location
     */
    private Location location;

    /**
     * Last asset status
     */
    private AssetStatusType status;

    /**
     * Last deprecation or conservation or write-off date
     */
    private LocalDate lastDate;

    /**
     * Asset cost (..0)
     */
    private GLOperationAccount accountCost;

    /**
     * Revaluation (..1)
     */
    private GLOperationAccount accountRevaluation;

    /**
     * Accumulated Depreciation (..7)
     */
    private GLOperationAccount accountDepreciation;

    /**
     * Depreciation Expense (6..)
     */
    private GLOperationAccount accountExpense;

    private List<DocRC> rcExpense;

    private AssetTotal perPeriod;

    // customized getters/setters

    public boolean isAutoCode() {
        return autoCode != null && autoCode;
    }

    // generated
    // except getAutoCode()

    public void setAutoCode(Boolean autoCode) {
        this.autoCode = autoCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTangible() {
        return tangible;
    }

    public void setTangible(boolean tangible) {
        this.tangible = tangible;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public GamaMoney getCost() {
        return cost;
    }

    public void setCost(GamaMoney cost) {
        this.cost = cost;
    }

    public GamaMoney getVat() {
        return vat;
    }

    public void setVat(GamaMoney vat) {
        this.vat = vat;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public GamaMoney getValue() {
        return value;
    }

    public void setValue(GamaMoney value) {
        this.value = value;
    }

    public GamaMoney getExpenses() {
        return expenses;
    }

    public void setExpenses(GamaMoney expenses) {
        this.expenses = expenses;
    }

    public GamaMoney getWrittenOff() {
        return writtenOff;
    }

    public void setWrittenOff(GamaMoney writtenOff) {
        this.writtenOff = writtenOff;
    }

    public List<AssetHistory> getHistory() {
        return history;
    }

    public void setHistory(List<AssetHistory> history) {
        this.history = history;
    }

    public List<Depreciation> getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(List<Depreciation> depreciation) {
        this.depreciation = depreciation;
    }

    public EmployeeDto getResponsible() {
        return responsible;
    }

    public void setResponsible(EmployeeDto responsible) {
        this.responsible = responsible;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public AssetStatusType getStatus() {
        return status;
    }

    public void setStatus(AssetStatusType status) {
        this.status = status;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public void setLastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
    }

    public GLOperationAccount getAccountCost() {
        return accountCost;
    }

    public void setAccountCost(GLOperationAccount accountCost) {
        this.accountCost = accountCost;
    }

    public GLOperationAccount getAccountRevaluation() {
        return accountRevaluation;
    }

    public void setAccountRevaluation(GLOperationAccount accountRevaluation) {
        this.accountRevaluation = accountRevaluation;
    }

    public GLOperationAccount getAccountDepreciation() {
        return accountDepreciation;
    }

    public void setAccountDepreciation(GLOperationAccount accountDepreciation) {
        this.accountDepreciation = accountDepreciation;
    }

    public GLOperationAccount getAccountExpense() {
        return accountExpense;
    }

    public void setAccountExpense(GLOperationAccount accountExpense) {
        this.accountExpense = accountExpense;
    }

    public List<DocRC> getRcExpense() {
        return rcExpense;
    }

    public void setRcExpense(List<DocRC> rcExpense) {
        this.rcExpense = rcExpense;
    }

    public AssetTotal getPerPeriod() {
        return perPeriod;
    }

    public void setPerPeriod(AssetTotal perPeriod) {
        this.perPeriod = perPeriod;
    }

    @Override
    public String toString() {
        return "AssetDto{" +
                "autoCode=" + autoCode +
                ", code='" + code + '\'' +
                ", cipher='" + cipher + '\'' +
                ", name='" + name + '\'' +
                ", tangible=" + tangible +
                ", note='" + note + '\'' +
                ", acquisitionDate=" + acquisitionDate +
                ", cost=" + cost +
                ", vat=" + vat +
                ", date=" + date +
                ", value=" + value +
                ", expenses=" + expenses +
                ", writtenOff=" + writtenOff +
                ", history=" + history +
                ", depreciation=" + depreciation +
                ", responsible=" + responsible +
                ", location=" + location +
                ", status=" + status +
                ", lastDate=" + lastDate +
                ", accountCost=" + accountCost +
                ", accountRevaluation=" + accountRevaluation +
                ", accountDepreciation=" + accountDepreciation +
                ", accountExpense=" + accountExpense +
                ", rcExpense=" + rcExpense +
                "} " + super.toString();
    }
}
