package lt.gama.model.sql.entities;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.i.IPosition;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocWorkSchedule;
import lt.gama.model.type.enums.WageType;
import lt.gama.model.type.enums.WorkScheduleType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "positions")
public class PositionSql extends BaseCompanySql implements IPosition {

    private String name;

    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    private DocWorkSchedule workSchedule;

    /**
     * Period start date if {@link WorkScheduleType#PERIODIC}
     */
    private LocalDate start;

    private WageType wageType;

    @Embedded
    private GamaBigMoney wage;

    @Embedded
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
        return "PositionSql{" +
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
