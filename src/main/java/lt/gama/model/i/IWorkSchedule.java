package lt.gama.model.i;

import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.WorkScheduleType;
import lt.gama.model.type.salary.WorkScheduleDay;

import java.time.LocalDate;
import java.util.List;

public interface IWorkSchedule {

    Long getId();

    String getName();

    String getDescription();

    WorkScheduleType getType();

    int getPeriod();

    LocalDate getStart();

    List<WorkScheduleDay> getSchedule();

    DBType getDb();
}
