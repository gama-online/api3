package lt.gama.model.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.ExternalUrl;
import lt.gama.model.type.auth.CompanySettings;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public abstract class BaseDocumentDto extends BaseNumberDocumentDto implements IBaseDocument {

    @Serial
    private static final long serialVersionUID = -1L;

    private CounterpartyDto counterparty;

    private EmployeeDto employee;

    /**
     * Is the document finished? - i.e. the finishing procedure started.
     * Some attributes of document (date, number, finished parts) cannot be edited
     * but others can (like unfinished parts or counterparty if debt is unfinished)
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean finished = false;

    private Exchange exchange;

    private DoubleEntryDto doubleEntry;

    /**
     * Are double-entry operations finished? - value copied from doubleEntry on finishing
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean finishedGL = false;

    /**
     * Is the document recallable?
     */
    @Hidden
    private Boolean recallable = false;

    /**
     * if document has unfinished updating all forward-sells documents
     */
    @Hidden
    private Boolean fs = false;

    /**
     * External documents urls
     */
    private List<ExternalUrl> urls;


    @JsonProperty("@class")
    public String getSubclassType() {
        return this.getDocumentType();
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

    // generated

    @Override
    public CounterpartyDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyDto counterparty) {
        this.counterparty = counterparty;
    }

    @Override
    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseDocumentDto that = (BaseDocumentDto) o;
        return Objects.equals(counterparty, that.counterparty) && Objects.equals(employee, that.employee) && Objects.equals(finished, that.finished) && Objects.equals(exchange, that.exchange) && Objects.equals(doubleEntry, that.doubleEntry) && Objects.equals(finishedGL, that.finishedGL) && Objects.equals(recallable, that.recallable) && Objects.equals(fs, that.fs) && Objects.equals(urls, that.urls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), counterparty, employee, finished, exchange, doubleEntry, finishedGL, recallable, fs, urls);
    }

    @Override
    public String toString() {
        return "BaseDocumentDto{" +
                "counterparty=" + counterparty +
                ", employee=" + employee +
                ", finished=" + finished +
                ", exchange=" + exchange +
                ", doubleEntry=" + doubleEntry +
                ", finishedGL=" + finishedGL +
                ", recallable=" + recallable +
                ", fs=" + fs +
                ", urls=" + urls +
                "} " + super.toString();
    }
}
