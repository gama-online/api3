package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.AssetSql;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2015-11-06.
 */
public class MapAsset extends MapBase<AssetSql> {

    @Serial
    private static final long serialVersionUID = -1L;

    @Override
    public Class<AssetSql> getEntityClass() {
        return AssetSql.class;
    }
}
