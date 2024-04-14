package lt.gama.model.i;

import lt.gama.model.type.enums.DBType;

public interface IWarehouse {

    Long getId();

    String getName();

    Boolean getWithTag();

    DBType getDb();
}
