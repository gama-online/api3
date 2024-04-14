package lt.gama.model.i;

import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;

import java.math.BigDecimal;

public interface IInvoicePartPrice {
    boolean isFixTotal();

    BigDecimal getQuantity();

    GamaBigMoney getPrice();

    Double getDiscount();

    Double getDiscountDoc();

    GamaBigMoney getDiscountedPrice();

    GamaMoney getDiscountedTotal();

    GamaMoney getDiscountDocTotal();
}
