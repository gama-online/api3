package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.EmployeeSql;

import java.io.Serial;

public class MapEmployee extends MapBase<EmployeeSql> {

    @Serial
    private static final long serialVersionUID = -1L;

    @Override
    public Class<EmployeeSql> getEntityClass() {
        return EmployeeSql.class;
    }
}
