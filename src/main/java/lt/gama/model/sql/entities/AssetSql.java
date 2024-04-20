package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.model.type.asset.Depreciation;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.enums.AssetStatusType;
import lt.gama.model.type.gl.GLOperationAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "assets")
@NamedEntityGraph(name = AssetSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(AssetSql_.RESPONSIBLE))
public class AssetSql extends BaseCompanySql {

    public static final String GRAPH_ALL = "graph.AssetSql.all";

    private String code;

    private String cipher;

    private String name;

    private boolean tangible;

    private String note;

    private LocalDate acquisitionDate;

    @Embedded
    private GamaMoney cost;

    @Embedded
    private GamaMoney vat;

    /**
     * Start of accounting date
     */
    private LocalDate date;

    /**
     * cost at accounting date
     */
    @Embedded
    private GamaMoney value;

    /**
     * Deprecation expenses at accounting date
     */
    @Embedded
    private GamaMoney expenses;

    /**
     * Written-off value
     */
    @Embedded
    private GamaMoney writtenOff;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<AssetHistory> history;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Depreciation> depreciation;

    /**
     * Last responsive employee
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id")
    private EmployeeSql responsible;

    /**
     * Last location
     */
    @Embedded
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
    @Embedded
    private GLOperationAccount accountCost;

    /**
     * Revaluation (..1)
     */
    @Embedded
    private GLOperationAccount accountRevaluation;

    /**
     * Accumulated Depreciation (..7)
     */
    @Embedded
    private GLOperationAccount accountDepreciation;

    /**
     * Depreciation Expense (6..)
     */
    @Embedded
    private GLOperationAccount accountExpense;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocRC> rcExpense;

    public String toMessage() {
        return ((code == null ? "" : code) + ' ' + (cipher == null ? "" : cipher) + ' ' + (name == null ? "" : name)).trim();
    }

    /**
     * toString except responsible
     */
    @Override
    public String toString() {
        return "AssetSql{" +
                "code='" + code + '\'' +
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

    // generated

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

    public EmployeeSql getResponsible() {
        return responsible;
    }

    public void setResponsible(EmployeeSql responsible) {
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
}
