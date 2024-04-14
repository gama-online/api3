package lt.gama.model.i.base;

import lt.gama.model.i.IDate;
import lt.gama.model.i.INumberDocument;
import lt.gama.model.i.IUuid;

import java.time.LocalDate;
import java.util.UUID;

public interface IBaseNumberDocument extends INumberDocument, IBaseCompany, IDate, IUuid {

    LocalDate getDate();
    void setDate(LocalDate date);

    String getNumber();
    void setNumber(String number);

    String getSeries();
    void setSeries(String series);

    Long getOrdinal();
    void setOrdinal(Long ordinal);

    Boolean getAutoNumber();
    void setAutoNumber(Boolean autoNumber);

    String getNote();
    void setNote(String note);

    UUID getUuid();
    void setUuid(UUID uuid);
}
