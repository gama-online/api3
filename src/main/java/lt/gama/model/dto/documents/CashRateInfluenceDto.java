package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseMoneyRateInfluenceDto;
import lt.gama.model.dto.documents.items.CashBalanceDto;

import java.io.Serial;
import java.util.List;

public class CashRateInfluenceDto extends BaseMoneyRateInfluenceDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<CashBalanceDto> accounts;

    // generated

    @Override
    public List<CashBalanceDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<CashBalanceDto> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "CashRateInfluenceDto{" +
                "accounts=" + accounts +
                "} " + super.toString();
    }
}
