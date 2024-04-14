package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.CashBalanceDto;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

public class CashOpeningBalanceDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<CashBalanceDto> cashes;

    // generated

    public List<CashBalanceDto> getCashes() {
        return cashes;
    }

    public void setCashes(List<CashBalanceDto> cashes) {
        this.cashes = cashes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CashOpeningBalanceDto that = (CashOpeningBalanceDto) o;
        return Objects.equals(cashes, that.cashes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cashes);
    }

    @Override
    public String toString() {
        return "CashOpeningBalanceDto{" +
                "cashes=" + cashes +
                "} " + super.toString();
    }
}
