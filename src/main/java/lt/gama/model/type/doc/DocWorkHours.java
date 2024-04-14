package lt.gama.model.type.doc;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-10-23.
 */
public class DocWorkHours implements Serializable {

    private Long id;

    private LocalDate date;

    private int vacationsDays;

    protected DocWorkHours() {
    }
//TODO remove comments
//    public DocWorkHours(WorkHoursDto doc, int vacationsDays) {
//        this.id = doc.getId();
//        this.date = doc.getDate();
//        this.vacationsDays = vacationsDays;
//    }

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getVacationsDays() {
        return vacationsDays;
    }

    public void setVacationsDays(int vacationsDays) {
        this.vacationsDays = vacationsDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocWorkHours that = (DocWorkHours) o;
        return vacationsDays == that.vacationsDays && Objects.equals(id, that.id) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, vacationsDays);
    }

    @Override
    public String toString() {
        return "DocWorkHours{" +
                "id=" + id +
                ", date=" + date +
                ", vacationsDays=" + vacationsDays +
                '}';
    }
}
