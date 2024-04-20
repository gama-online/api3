package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.WorkScheduleSql;

public class MapWorkSchedule extends MapBase<WorkScheduleSql> {

    @Override
    public Class<WorkScheduleSql> getEntityClass() {
        return WorkScheduleSql.class;
    }
}
