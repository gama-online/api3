package lt.gama.model.sql.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.i.IFinished;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.ExternalUrl;
import lt.gama.model.type.auth.CompanySettings;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "documents")
public abstract class BaseDocumentSql extends BaseNumberDocumentSql implements IBaseDocument, IFinished {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeSql employee;

    /**
     * Is the document finished? - i.e. the finishing procedure started.
     * Some attributes of document (date, number, finished parts) cannot be edited
     * but others can (like unfinished parts or counterparty if debt is unfinished)
     */
    private Boolean finished = false;

    @Embedded
    private Exchange exchange;

    @Transient
    private DoubleEntryDto doubleEntry;

    /**
     * Are double-entry operations finished? - value copied from doubleEntry on finishing
     */
    private Boolean finishedGL = false;

    /**
     * Is the document recallable?
     */
    private Boolean recallable = false;

    /**
     * if document has unfinished updating all forward-sells documents
     */
    private Boolean fs = false;

    /**
     * External documents urls
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ExternalUrl> urls;


    public BaseDocumentSql() {
    }

    @JsonProperty("@class")
    public String getSubclassType() {
        return EntityUtils.normalizeEntityClassName(this.getClass());
    }

    /**
     * Check if the document is fully Finished
     * Must be overridden for a new finishing attribute.
     */
    public boolean isFullyFinished() {
        return BooleanUtils.isTrue(getFinished());
    }


    /**
     * Set document as fully finished.
     * @return true if document changed, i.e. need to save
     */
    public boolean setFullyFinished() {
        boolean changed = BooleanUtils.isNotTrue(getFinished());
        finished = true;
        return changed;
    }

    /**
     * Clear document fully finished flag.
     * @return true if document changed, i.e. need to save
     */
    public boolean clearFullyFinished() {
        boolean changed = BooleanUtils.isTrue(getFinished());
        finished = null;
        return changed;
    }

    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        if (getUuid() == null) setUuid(UUID.randomUUID());
    }

    @Override
    public void reset() {
        super.reset();
        finished = null;
        finishedGL = null;
    }

    @Override
    public String makeContent(CompanySettings companySettings) {
        StringJoiner sb = new StringJoiner(" ");
        sb.add(companySettings.getDocName(getDocumentType()));
        sb.add(getDate().toString());
        if (StringHelper.hasValue(getNumber())) sb.add(getNumber().trim());
        if (Validators.isValid(getCounterparty()) && StringHelper.hasValue(getCounterparty().getName())) {
            sb.add(getCounterparty().getName().trim());
        }
        if (Validators.isValid(getEmployee()) && StringHelper.hasValue(getEmployee().getName())) {
            sb.add(getEmployee().getName().trim());
        }
        return sb.toString().trim();
    }

    /**
     * toString without employee, counterparty, doubleEntry
     */
    @Override
    public String toString() {
        return "BaseDocumentSql{" +
                "inished=" + finished +
                ", exchange=" + exchange +
                ", finishedGL=" + finishedGL +
                ", recallable=" + recallable +
                ", fs=" + fs +
                ", urls=" + urls +
                "} " + super.toString();
    }

    // customized getters/setters

    public boolean isRecallable() {
        return recallable != null && recallable;
    }

    public boolean hasFs() {
        return fs != null && fs;
    }

    // generated

    @Override
    public CounterpartySql getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartySql counterparty) {
        this.counterparty = counterparty;
    }

    @Override
    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
        this.employee = employee;
    }

    @Override
    public Boolean getFinished() {
        return finished;
    }

    @Override
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public DoubleEntryDto getDoubleEntry() {
        return doubleEntry;
    }

    @Override
    public void setDoubleEntry(DoubleEntryDto doubleEntry) {
        this.doubleEntry = doubleEntry;
    }

    @Override
    public Boolean getFinishedGL() {
        return finishedGL;
    }

    @Override
    public void setFinishedGL(Boolean finishedGL) {
        this.finishedGL = finishedGL;
    }

    public Boolean getRecallable() {
        return recallable;
    }

    public void setRecallable(Boolean recallable) {
        this.recallable = recallable;
    }

    public Boolean getFs() {
        return fs;
    }

    public void setFs(Boolean fs) {
        this.fs = fs;
    }

    public List<ExternalUrl> getUrls() {
        return urls;
    }

    public void setUrls(List<ExternalUrl> urls) {
        this.urls = urls;
    }
}
