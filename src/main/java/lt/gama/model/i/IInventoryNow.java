package lt.gama.model.i;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.InventoryType;

import java.math.BigDecimal;

public interface IInventoryNow {

    Doc getDoc();

    ICounterparty getCounterparty();

    BigDecimal getQuantity();

    GamaMoney getCostTotal();

    String getTag();

    void setQuantity(BigDecimal quantity);

    void setCostTotal(GamaMoney costTotal);

    default InventoryType getInventoryType() {
        return null;
    }
}
