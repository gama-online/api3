package lt.gama.model.i;

import lt.gama.model.type.doc.DocBank;
import lt.gama.model.type.enums.DBType;

public interface IBankAccount extends IId<Long> {

    Long getId();

    String getAccount();

    DocBank getBank();

    DBType getDb();
}
