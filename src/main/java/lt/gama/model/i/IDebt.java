package lt.gama.model.i;

import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;

public interface IDebt {
    long getCompanyId();

    long getCounterpartyId();

    Doc getDoc();

    String getCurrency();

    DebtType getType();

    DBType getDb();
}
