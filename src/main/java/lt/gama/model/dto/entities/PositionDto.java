package lt.gama.model.dto.entities;

import lt.gama.model.type.doc.DocWorkSchedule;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IPosition;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.WageType;
import lt.gama.model.type.enums.WorkScheduleType;

import java.io.Serial;
import java.time.LocalDate;

public class PositionDto extends BaseCompanyDto implements IPosition {

    @Serial
    private static final long serialVersionUID = -1L;

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
    public DocWorkSchedule getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(DocWorkSchedule workSchedule) {
        this.workSchedule = workSchedule;
    }

    @Override
    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    @Override
    public WageType getWageType() {
        return wageType;
    }

    public void setWageType(WageType wageType) {
        this.wageType = wageType;
    }

    @Override
    public GamaBigMoney getWage() {
        return wage;
    }

    public void setWage(GamaBigMoney wage) {
        this.wage = wage;
    }

    @Override
    public GamaMoney getAdvance() {
        return advance;
    }

    public void setAdvance(GamaMoney advance) {
        this.advance = advance;
    }

    @Override
    public String toString() {
        return "PositionDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", workSchedule=" + workSchedule +
                ", start=" + start +
                ", wageType=" + wageType +
                ", wage=" + wage +
                ", advance=" + advance +
                "} " + super.toString();
    }
}
