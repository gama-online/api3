package lt.gama.model.i;

import lt.gama.model.type.enums.DBType;

public interface IManufacturer {

    Long getId();

    void setId(Long id);

    String getName();

    void setName(String name);

    DBType getDb();

    void setDb(DBType db);
}
