package lt.gama.model.sql.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.i.IWorkSchedule;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.enums.WorkScheduleType;
import lt.gama.model.type.salary.WorkScheduleDay;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "work_schedules")
public class WorkScheduleSql extends BaseCompanySql implements IWorkSchedule {

    private String name;

    private String description;

    private WorkScheduleType type;

    /**
     * length of period if {@link WorkScheduleType#PERIODIC}
     */
    private int period;

    /**
     * Period start date if {@link WorkScheduleType#PERIODIC}
     */
    private LocalDate start;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<WorkScheduleDay> schedule;

    // generated

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public WorkScheduleType getType() {
        return type;
    }

    public void setType(WorkScheduleType type) {
        this.type = type;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    @Override
    public List<WorkScheduleDay> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<WorkScheduleDay> schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return "WorkScheduleSql{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", period=" + period +
                ", start=" + start +
                ", schedule=" + schedule +
                "} " + super.toString();
    }
}
