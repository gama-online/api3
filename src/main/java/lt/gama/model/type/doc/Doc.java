package lt.gama.model.type.doc;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.i.*;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.type.enums.DBType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Gama
 * Created by valdas on 15-07-17.
 */
public class Doc implements IDocument, Serializable {

    private Long id;

    private String number;

    private String series;

    private Long ordinal;

    private LocalDate date;

    private LocalDate dueDate;

    private String note;

    /**
     * Document type, i.e. simple normalized class name
     */
    private String type;

    private UUID uuid;

    @Transient
    private String exportId;

    @Transient
    private Set<String> labels;

    private DBType db;

    public Doc copy() {
        Doc clone = new Doc();
        clone.id = this.id;
        clone.number = this.number;
        clone.series = this.series;
        clone.ordinal = this.ordinal;
        clone.date = this.date;
        clone.dueDate = this.dueDate;
        clone.note = this.note;
        clone.uuid = this.uuid;
        clone.type = this.type;
        clone.labels = this.labels == null ? null : new HashSet<>(this.labels);
        clone.db = this.db != null ? this.db : DBType.DATASTORE;
        return clone;
    }

    static public <D extends IId<Long> & INumberDocument & IDb & IUuid> Doc of(D doc) {
        return of(doc, doc.getDate());
    }

    static public <D extends IId<Long> & INumberDocument & IDb & IUuid> Doc of(D src, LocalDate date) {
        if (src == null || src instanceof Doc) return (Doc) src;

        Doc document = new Doc();
        document.id = src.getId();
        document.date = date;
        document.dueDate = src instanceof IDebtDueDate && ((IDebtDueDate) src).getDueDate() != null ?
                ((IDebtDueDate) src).getDueDate() : date;
        document.number = src.getNumber();
        if (src instanceof IDocument) {
            document.series = ((IDocument) src).getSeries();
            document.ordinal = ((IDocument) src).getOrdinal();
            document.type = ((IDocument) src).getType();
        } else {
            ISeriesWithOrdinal s = StringHelper.parseDocNumber(document.number);
            document.series = s.getSeries();
            document.ordinal = s.getOrdinal();
        }
        document.note = src.getNote();
        document.type = src instanceof IBaseDocument ? ((IBaseDocument) src).getDocumentType()
                : src instanceof IDocument ? ((IDocument) src).getType() : null;
        Validators.checkNotNull(document.type, "Document type must not be null");

        document.uuid = src.getUuid();
        document.db = src.getDb() != null ? src.getDb() : DBType.DATASTORE;

        return document;
    }

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    @Override
    public Long getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Long ordinal) {
        this.ordinal = ordinal;
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
    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getExportId() {
        return exportId;
    }

    @Override
    public void setExportId(String exportId) {
        this.exportId = exportId;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    @Override
    public DBType getDb() {
        return db;
    }

    @Override
    public void setDb(DBType db) {
        this.db = db;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doc doc = (Doc) o;
        return Objects.equals(id, doc.id) && Objects.equals(number, doc.number) && Objects.equals(series, doc.series) && Objects.equals(ordinal, doc.ordinal) && Objects.equals(date, doc.date) && Objects.equals(dueDate, doc.dueDate) && Objects.equals(note, doc.note) && Objects.equals(type, doc.type) && Objects.equals(uuid, doc.uuid) && Objects.equals(exportId, doc.exportId) && Objects.equals(labels, doc.labels) && db == doc.db;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, series, ordinal, date, dueDate, note, type, uuid, exportId, labels, db);
    }

    @Override
    public String toString() {
        return "Doc{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", series='" + series + '\'' +
                ", ordinal=" + ordinal +
                ", date=" + date +
                ", dueDate=" + dueDate +
                ", note='" + note + '\'' +
                ", type='" + type + '\'' +
                ", uuid=" + uuid +
                ", exportId='" + exportId + '\'' +
                ", labels=" + labels +
                ", db=" + db +
                '}';
    }
}
