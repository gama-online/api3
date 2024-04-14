package lt.gama.model.i;

import lt.gama.model.type.doc.DocPosition;
import lt.gama.model.type.enums.SexType;
import lt.gama.model.type.salary.EmployeeTaxSettings;

import java.time.LocalDate;
import java.util.List;

public interface IEmployeeCard {

    Long getId();

    String getSsn();

    String getNin();

    LocalDate getHired();

    LocalDate getFired();

    SexType getSex();

    List<EmployeeTaxSettings> getTaxes();

    List<DocPosition> getPositions();
}
