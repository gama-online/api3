package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.PositionSql;

/**
 * gama-online
 * Created by valdas on 2017-03-10.
 */
public class MapPosition extends MapBase<PositionSql> {

    @Override
    public Class<PositionSql> getEntityClass() {
        return PositionSql.class;
    }
}