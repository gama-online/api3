package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseMoneyRateInfluenceDto;
import lt.gama.model.dto.documents.items.BankAccountBalanceDto;

import java.io.Serial;
import java.util.List;

public class BankRateInfluenceDto extends BaseMoneyRateInfluenceDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<BankAccountBalanceDto> accounts;

    // generated

    @Override
    public List<BankAccountBalanceDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BankAccountBalanceDto> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "BankRateInfluenceDto{" +
                "accounts=" + accounts +
                "} " + super.toString();
    }
}
