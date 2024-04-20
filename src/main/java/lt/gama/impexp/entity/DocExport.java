package lt.gama.impexp.entity;

import lt.gama.model.type.doc.Doc;

import java.time.LocalDateTime;

/**
 * gama-online
 * Created by valdas on 2016-04-03.
 */
public class DocExport {

    private Long id;

    private String number;

    private LocalDateTime date;

    private LocalDateTime dueDate;

    /**
     * Document type, i.e. simple class name
     */
    private String type;


    @SuppressWarnings("unused")
    protected DocExport() {}

    public DocExport(Doc src) {
        if (src == null) return;
        id = src.getId();
        number = src.getNumber();
        date = src.getDate() != null ? src.getDate().atStartOfDay() : null;
        dueDate = src.getDueDate() != null ? src.getDueDate().atStartOfDay() : null;
    }

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DocExport{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", date=" + date +
                ", dueDate=" + dueDate +
                ", type='" + type + '\'' +
                '}';
    }
}
