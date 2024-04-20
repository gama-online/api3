package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.CashSql;

import java.io.Serial;

public class MapCash extends MapBase<CashSql> {

    @Serial
    private static final long serialVersionUID = -1L;

    @Override
    public Class<CashSql> getEntityClass() {
        return CashSql.class;
    }
}
