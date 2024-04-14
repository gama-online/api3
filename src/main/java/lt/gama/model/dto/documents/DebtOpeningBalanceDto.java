package lt.gama.model.dto.documents;

import lt.gama.helpers.BooleanUtils;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.i.IDebtFinished;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

public class DebtOpeningBalanceDto extends BaseDocumentDto implements IDebtFinished {

    @Serial
    private static final long serialVersionUID = -1L;

    private Boolean finishedDebt;

    private List<DebtBalanceDto> counterparties;


    @Override
    public boolean isUnfinishedDebt() {
        return BooleanUtils.isNotTrue(finishedDebt);
    }

    @Override
    public void reset() {
        super.reset();

        finishedDebt = false;

        if (counterparties != null) {
            for (DebtBalanceDto balance : counterparties) {
                balance.setFinished(null);
            }
        }
    }

    // generated

    @Override
    public Boolean getFinishedDebt() {
        return finishedDebt;
    }

    @Override
    public void setFinishedDebt(Boolean finishedDebt) {
        this.finishedDebt = finishedDebt;
    }

    public List<DebtBalanceDto> getCounterparties() {
        return counterparties;
    }

    public void setCounterparties(List<DebtBalanceDto> counterparties) {
        this.counterparties = counterparties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DebtOpeningBalanceDto that = (DebtOpeningBalanceDto) o;
        return Objects.equals(finishedDebt, that.finishedDebt) && Objects.equals(counterparties, that.counterparties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), finishedDebt, counterparties);
    }

    @Override
    public String toString() {
        return "DebtOpeningBalanceDto{" +
                "finishedDebt=" + finishedDebt +
                ", counterparties=" + counterparties +
                "} " + super.toString();
    }
}
