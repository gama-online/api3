package lt.gama.model.sql.system;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.sql.system.id.CalendarId;
import lt.gama.model.type.CalendarMonth;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;


@Entity
@Table(name = "calendar")
public class CalendarSql extends BaseEntitySql implements IId<CalendarId> {

    /**
     * composite index: country + year
     */
    @EmbeddedId
    private CalendarId id;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<CalendarMonth> months;


    @SuppressWarnings("unused")
    protected CalendarSql() {}

    public CalendarSql(String country, int year) {
        this.id = new CalendarId(country, year);
    }

    // generated

    @Override
    public CalendarId getId() {
        return id;
    }

    @Override
    public void setId(CalendarId id) {
        this.id = id;
    }

    public List<CalendarMonth> getMonths() {
        return months;
    }

    public void setMonths(List<CalendarMonth> months) {
        this.months = months;
    }

    @Override
    public String toString() {
        return "CalendarSql{" +
                "id=" + id +
                ", months=" + months +
                "} " + super.toString();
    }
}
