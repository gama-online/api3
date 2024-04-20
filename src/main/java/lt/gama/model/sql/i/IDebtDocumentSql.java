package lt.gama.model.sql.i;

import lt.gama.model.i.*;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;

public interface IDebtDocumentSql extends ICompany, INumberDocument, IId<Long>, IDebtFinished, IDebtDueDate, IDebtNoDebt {

    CounterpartySql getCounterparty();


    GamaMoney getDebt();

    GamaMoney getBaseDebt();


    Boolean getFinished();

    void setFinished(Boolean finished);


    Exchange getExchange();
}
