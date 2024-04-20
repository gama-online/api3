package lt.gama.model.sql.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lt.gama.helpers.EntityUtils;
import lt.gama.model.i.IDate;
import lt.gama.model.i.INumberDocument;
import lt.gama.model.i.IUuid;

import java.time.LocalDate;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseNumberDocumentSql extends BaseCompanySql implements INumberDocument, IDate, IUuid {

    private LocalDate date;

    /**
     * Document complex number.
     * Can be joint from 'series' and 'ordinal'.
     */
    private String number;

    private String series;

    private Long ordinal;

    @Transient
    private Boolean autoNumber;

    private String note;

    /**
     * Unique document uuid
     */
    private UUID uuid = UUID.randomUUID();

    /**
     * Need to know in the frontend
     * @return document type (i.e. class name without Sql or Dto suffix)
     */
    public String getDocumentType() {
        return EntityUtils.normalizeEntityClassName(this.getClass());
    }

    // generated

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public void setNumber(String number) {
        this.number = number;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public Long getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Long ordinal) {
        this.ordinal = ordinal;
    }

    public Boolean getAutoNumber() {
        return autoNumber;
    }

    public void setAutoNumber(Boolean autoNumber) {
        this.autoNumber = autoNumber;
    }

    @Override
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
    public String toString() {
        return "BaseNumberDocumentSql{" +
                "date=" + date +
                ", number='" + number + '\'' +
                ", series='" + series + '\'' +
                ", ordinal=" + ordinal +
                ", autoNumber=" + autoNumber +
                ", note='" + note + '\'' +
                ", uuid=" + uuid +
                "} " + super.toString();
    }
}
