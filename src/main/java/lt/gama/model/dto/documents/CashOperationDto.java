package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseMoneyDocumentDto;
import lt.gama.model.dto.entities.CashDto;

import java.io.Serial;

public class CashOperationDto extends BaseMoneyDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private CashDto cash;

    // generated

    @Override
    public CashDto getCash() {
        return cash;
    }

    public void setCash(CashDto cash) {
        this.cash = cash;
    }

    @Override
    public String toString() {
        return "CashOperationDto{" +
                "cash=" + cash +
                "} " + super.toString();
    }
}
