package lt.gama.model.type.doc;

import lt.gama.model.dto.entities.PositionDto;
import lt.gama.model.i.IName;
import lt.gama.model.i.IPosition;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.enums.WageType;
import lt.gama.model.type.enums.WorkScheduleType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-04-07.
 */
public class DocPosition extends BaseDocEntity implements IName, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long id;

    /**
     * record date
     */
    private LocalDate date;

    private String name;

    private String description;

    private DocWorkSchedule workSchedule;

    /**
     * Period start date if {@link WorkScheduleType#PERIODIC}
     */
    private LocalDate start;

    private WageType wageType;

    private GamaBigMoney wage;

    private GamaMoney advance;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    /**
     * If aggregate this position working hours with main position
     */
    private Boolean aggregate;

    /**
     * If this position is main, i.e. aggregated working hours and vacation length will be calculated by this position work schedule
     */
    private Boolean main;

    /**
     * position is not active, so do not show this position in employee positions list
     */
    private Boolean hidden;


    public DocPosition() {
    }

    public DocPosition(DocPosition position) {
        if (position == null) return;
        this.id = position.id;
        this.date = position.date;
        this.name = position.name;
        this.description = position.description;
        this.workSchedule = position.workSchedule;
        this.start = position.start;
        this.wageType = position.wageType;
        this.wage = position.wage;
        this.advance = position.advance;
        this.dateFrom = position.dateFrom;
        this.dateTo = position.dateTo;
        this.aggregate = position.aggregate;
        this.main = position.main;
        this.hidden = position.hidden;
    }

    public DocPosition(PositionDto position) {
        if (position == null) return;
        this.id = position.getId();
        this.name = position.getName();
        this.description = position.getDescription();
        this.workSchedule = position.getWorkSchedule();
        this.start = position.getStart();
        this.wageType = position.getWageType();
        this.wage = position.getWage();
        this.advance = position.getAdvance();
    }

    public DocPosition(IPosition position) {
        copyFrom(position);
    }

    public void copyFrom(IPosition position) {
        if (position == null) {
            id = null;
            name = null;
            description = null;
            workSchedule = null;
            start = null;
            wageType = null;
            wage = null;
            advance = null;

        } else {
            id = position.getId();
            name = position.getName();
            description = position.getDescription();
            workSchedule = position.getWorkSchedule();
            start = position.getStart();
            wageType = position.getWageType();
            wage = position.getWage();
            advance = position.getAdvance();
        }
    }

    public boolean isAggregate() {
        return aggregate != null && aggregate;
    }

    public boolean isMain() {
        return main != null && main;
    }

    public boolean isHidden() {
        return hidden != null && hidden;
    }

    // generated
    // except getAggregate, getMain, getHidden

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DocWorkSchedule getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(DocWorkSchedule workSchedule) {
        this.workSchedule = workSchedule;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public WageType getWageType() {
        return wageType;
    }

    public void setWageType(WageType wageType) {
        this.wageType = wageType;
    }

    public GamaBigMoney getWage() {
        return wage;
    }

    public void setWage(GamaBigMoney wage) {
        this.wage = wage;
    }

    public GamaMoney getAdvance() {
        return advance;
    }

    public void setAdvance(GamaMoney advance) {
        this.advance = advance;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public void setAggregate(Boolean aggregate) {
        this.aggregate = aggregate;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocPosition that = (DocPosition) o;
        return Objects.equals(id, that.id) && Objects.equals(date, that.date) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(workSchedule, that.workSchedule) && Objects.equals(start, that.start) && wageType == that.wageType && Objects.equals(wage, that.wage) && Objects.equals(advance, that.advance) && Objects.equals(dateFrom, that.dateFrom) && Objects.equals(dateTo, that.dateTo) && Objects.equals(aggregate, that.aggregate) && Objects.equals(main, that.main) && Objects.equals(hidden, that.hidden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, date, name, description, workSchedule, start, wageType, wage, advance, dateFrom, dateTo, aggregate, main, hidden);
    }
}
