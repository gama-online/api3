package lt.gama.model.i;

import lt.gama.model.type.enums.DBType;

public interface IDb {

    DBType getDb();

    void setDb(DBType db);

}
