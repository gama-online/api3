package lt.gama.model.i;


import lt.gama.model.dto.entities.CounterpartyDto;

public interface IDebtDto extends IDebt {

    CounterpartyDto getCounterparty();

    void setCounterparty(CounterpartyDto counterparty);
}
