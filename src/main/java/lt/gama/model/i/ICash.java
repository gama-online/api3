package lt.gama.model.i;

import lt.gama.model.type.enums.DBType;

public interface ICash extends IId<Long> {

    Long getId();

    String getName();

    String getCashier();

    DBType getDb();
}
