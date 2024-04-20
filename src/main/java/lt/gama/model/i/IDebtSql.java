package lt.gama.model.i;

import lt.gama.model.sql.entities.CounterpartySql;

public interface IDebtSql extends IDebt {

    CounterpartySql getCounterparty();

    void setCounterparty(CounterpartySql counterparty);
}
