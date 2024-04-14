package lt.gama.model.i;

/**
 * gama-online
 * Created by valdas on 2016-02-29.
 */
public interface IDebtFinished {

    Boolean getFinishedDebt();

    boolean isUnfinishedDebt();

    void setFinishedDebt(Boolean finished);
}
