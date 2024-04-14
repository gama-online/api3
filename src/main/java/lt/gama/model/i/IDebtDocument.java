package lt.gama.model.i;

import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocCounterparty;

/**
 * Gama
 * Created by valdas on 15-04-09.
 */
public interface IDebtDocument extends ICompany, INumberDocument, IId<Long>, IDebtFinished, IDebtDueDate, IDebtNoDebt {

    DocCounterparty getCounterparty();


    GamaMoney getDebt();

    GamaMoney getBaseDebt();


    Boolean getFinished();

    void setFinished(Boolean finished);


    Exchange getExchange();

}
