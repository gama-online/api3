package lt.gama.model.dto.i;

import lt.gama.model.i.*;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;

public interface IDebtDocumentDto extends ICompany, INumberDocument, IId<Long>, IDebtFinished, IDebtDueDate, IDebtNoDebt {

    ICounterparty getCounterparty();


    GamaMoney getDebt();

    GamaMoney getBaseDebt();


    Boolean getFinished();

    void setFinished(Boolean finished);


    Exchange getExchange();
}
