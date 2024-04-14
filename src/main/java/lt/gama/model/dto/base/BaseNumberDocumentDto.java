package lt.gama.model.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.i.IDate;
import lt.gama.model.i.IDebtDueDate;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Gama
 * Created by valdas on 15-05-22.
 */
public abstract class BaseNumberDocumentDto extends BaseCompanyDto implements IDate, IDebtDueDate {

    @Schema(description = "Document date", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    /**
     * Document complex number.
     * Can be joint from 'series' and 'ordinal'.
     */
    private String number;

    private String series;

    private Long ordinal;

    /**
     * If number is automatically assigned.
     * Used for frontend to backend communications only
     */
    private Boolean autoNumber;

    private String note;

    @Schema(description = "Unique document uuid")
    private UUID uuid = UUID.randomUUID();

    /**
     * Need to know in the frontend
     * @return document type (i.e. class name without Dto suffix)
     */
    public String getDocumentType() {
        return EntityUtils.normalizeEntityClassName(this.getClass());
    }

    @Override
    public LocalDate getDueDate() {
        return date;
    }

    @SuppressWarnings("unused")
    @Hidden
    @JsonIgnore
    public String getSeriesName() {
        if (StringHelper.isEmpty(getNumber())) return null;
        int i = getNumber().indexOf(' ');
        if (i == -1) return null;
        return getNumber().substring(0, i);
    }

    @SuppressWarnings("unused")
    @Hidden
    @JsonIgnore
    public String getSeriesNumber() {
        if (StringHelper.isEmpty(getNumber())) return null;
        int i = getNumber().indexOf(' ');
        if (i == -1) return getNumber();
        return getNumber().substring(i + 1).trim();
    }

    // generated

    @Override
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseNumberDocumentDto that = (BaseNumberDocumentDto) o;
        return Objects.equals(date, that.date) && Objects.equals(number, that.number) && Objects.equals(series, that.series) && Objects.equals(ordinal, that.ordinal) && Objects.equals(autoNumber, that.autoNumber) && Objects.equals(note, that.note) && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), date, number, series, ordinal, autoNumber, note, uuid);
    }

    @Override
    public String toString() {
        return "BaseNumberDocumentDto{" +
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
