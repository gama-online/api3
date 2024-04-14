package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseMoneyRateInfluenceDto;
import lt.gama.model.dto.documents.items.EmployeeBalanceDto;

import java.io.Serial;
import java.util.List;

public class EmployeeRateInfluenceDto extends BaseMoneyRateInfluenceDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<EmployeeBalanceDto> accounts;

    // generated

    @Override
    public List<EmployeeBalanceDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<EmployeeBalanceDto> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "EmployeeRateInfluenceDto{" +
                "accounts=" + accounts +
                "} " + super.toString();
    }
}
