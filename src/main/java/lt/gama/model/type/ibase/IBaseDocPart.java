package lt.gama.model.type.ibase;

import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocWarehouse;

import java.math.BigDecimal;

public interface IBaseDocPart {

    BigDecimal getQuantity();
    void setQuantity(BigDecimal quantity);

    GamaBigMoney getPrice();
    void setPrice(GamaBigMoney price);

    GamaMoney getTotal();
    void setTotal(GamaMoney total);

    GamaMoney getBaseTotal();
    void setBaseTotal(GamaMoney baseTotal);

    DocWarehouse getWarehouse();
    void setWarehouse(DocWarehouse warehouse);

    /**
     *  Not enough quantity to finish operation - used in frontend only
     */
    BigDecimal getNotEnough();
    void setNotEnough(BigDecimal notEnough);

    /**
     * Allow forward sell, i.e. to have negative remainder.
     */
    boolean isForwardSell();
    void setForwardSell(Boolean forwardSell);
}
