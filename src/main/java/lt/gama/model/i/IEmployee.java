package lt.gama.model.i;

import lt.gama.model.type.enums.EmployeeType;

import java.util.Set;

public interface IEmployee extends IDocEmployee {

    Set<String> getUnionPermissions();

    EmployeeType getType();
}
