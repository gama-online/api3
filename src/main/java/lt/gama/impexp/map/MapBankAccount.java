package lt.gama.impexp.map;

import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.BankAccountSql;

import java.io.Serial;

public class MapBankAccount extends MapBase<BankAccountSql> {

    @Serial
    private static final long serialVersionUID = -1L;

    @Override
    public Class<BankAccountSql> getEntityClass() {
        return BankAccountSql.class;
    }
}
