package lt.gama.model.i;

import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocWorkSchedule;
import lt.gama.model.type.enums.WageType;

import java.time.LocalDate;

public interface IPosition {

    Long getId();

    String getName();

    String getDescription();

    DocWorkSchedule getWorkSchedule();

    LocalDate getStart();

    WageType getWageType();

    GamaBigMoney getWage();

    GamaMoney getAdvance();
}
