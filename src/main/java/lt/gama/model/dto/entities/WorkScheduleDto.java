package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.ICompany;
import lt.gama.model.i.IWorkSchedule;
import lt.gama.model.type.enums.WorkScheduleType;
import lt.gama.model.type.salary.WorkScheduleDay;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

public class WorkScheduleDto extends BaseCompanyDto implements IWorkSchedule {

    @Serial
    private static final long serialVersionUID = -1L;

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

    private List<WorkScheduleDay> schedule;


    public WorkScheduleDto() {
    }

    public <I extends IWorkSchedule & ICompany> WorkScheduleDto(I workSchedule) {
        super(workSchedule.getCompanyId(), workSchedule.getId(), workSchedule.getDb());
        this.name = workSchedule.getName();
        this.description = workSchedule.getDescription();
        this.type = workSchedule.getType();
        this.period = workSchedule.getPeriod();
        this.start = workSchedule.getStart();
        this.schedule = workSchedule.getSchedule();
    }

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
        return "WorkScheduleDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", period=" + period +
                ", start=" + start +
                ", schedule=" + schedule +
                "} " + super.toString();
    }
}
