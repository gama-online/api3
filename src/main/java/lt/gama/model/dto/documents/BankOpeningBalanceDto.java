package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.BankAccountBalanceDto;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

public class BankOpeningBalanceDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<BankAccountBalanceDto> bankAccounts;

    // generated

    public List<BankAccountBalanceDto> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccountBalanceDto> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BankOpeningBalanceDto that = (BankOpeningBalanceDto) o;
        return Objects.equals(bankAccounts, that.bankAccounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bankAccounts);
    }

    @Override
    public String toString() {
        return "BankOpeningBalanceDto{" +
                "bankAccounts=" + bankAccounts +
                "} " + super.toString();
    }
}
