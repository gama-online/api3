package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseMoneyRateInfluenceDto;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.i.IDebtFinished;

import java.io.Serial;
import java.util.List;

public class DebtRateInfluenceDto extends BaseMoneyRateInfluenceDto implements IDebtFinished  {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<DebtBalanceDto> accounts;

    @Override
    public Boolean getFinishedDebt() {
        return accounts != null && accounts.size() > 0 && !hasItemUnfinished();
    }

    @Override
    public void setFinishedDebt(Boolean finished) {
        setItemsFinished(finished);
    }

    @Override
    public boolean isUnfinishedDebt() {
        return !hasItemFinished();
    }

    // generated

    @Override
    public List<DebtBalanceDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<DebtBalanceDto> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "DebtRateInfluenceDto{" +
                "accounts=" + accounts +
                "} " + super.toString();
    }
}
