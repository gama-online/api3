package lt.gama.model.sql.system;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.sql.system.id.CalendarId;
import lt.gama.model.type.salary.HolidaySettings;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "calendar_settings")
public class CalendarSettingsSql extends BaseEntitySql implements IId<CalendarId> {

    /**
     * composite index: country + year
     */
    @EmbeddedId
    private CalendarId id;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<HolidaySettings> holidays;


    @SuppressWarnings("unused")
    protected CalendarSettingsSql() {}

    public CalendarSettingsSql(String country, int year) {
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

    public List<HolidaySettings> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<HolidaySettings> holidays) {
        this.holidays = holidays;
    }

    @Override
    public String toString() {
        return "CalendarSettingsSql{" +
                "id=" + id +
                ", holidays=" + holidays +
                "} " + super.toString();
    }
}
