package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.EmployeeCardSql;

import java.io.Serial;

public class MapEmployeeCard extends MapBase<EmployeeCardSql> {

    @Serial
    private static final long serialVersionUID = -1L;

    @Override
    public Class<EmployeeCardSql> getEntityClass() {
        return EmployeeCardSql.class;
    }
}
