package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.WarehouseSql;

public class MapWarehouseSql extends MapBase<WarehouseSql> {

    @Override
    public Class<WarehouseSql> getEntityClass() {
        return WarehouseSql.class;
    }
 }
