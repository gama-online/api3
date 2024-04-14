package lt.gama.model.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.Embeddable;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.StringHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Embeddable
@JsonIncludeProperties({ "currency", "amount" })
public class GamaBigMoney extends GamaAbstractMoney<GamaBigMoney> {

    public static GamaBigMoney ofNullable(String currency, BigDecimal amount) {
        if (StringHelper.isEmpty(currency)) {
            if (BigDecimalUtils.isZero(amount)) return null;
            throw new NullPointerException("Currency must not be null");
        }
        if (amount == null) return GamaBigMoney.zero(currency);
        return of(currency, amount);
    }

    public static GamaBigMoney of(GamaMoney money) {
        if (money == null) return null;
        return of(money.getCurrency(), money.getAmount());
    }

    public static GamaBigMoney of(String currency, BigDecimal amount) {
        return new GamaBigMoney(currency, amount);
    }

    public static GamaBigMoney of(String currency, double amount) {
        return of(currency, BigDecimal.valueOf(amount));
    }

    public static GamaBigMoney zero(String currency) {
        return of(currency, BigDecimal.ZERO);
    }

    public static GamaBigMoney parse(String moneyStr) {
        String ms = StringHelper.trim(moneyStr);
        if (StringHelper.isEmpty(ms) || "null".equalsIgnoreCase(ms) || "null null".equalsIgnoreCase(ms)) return null;
        return new GamaBigMoney(moneyStr);
    }

    @SuppressWarnings("unused")
    protected GamaBigMoney() {}

    @JsonCreator
    public GamaBigMoney(String currency, BigDecimal amount) {
        super(currency, amount);
    }

    public GamaBigMoney(String moneyStr) {
        super(moneyStr);
    }

    @Override
    public GamaBigMoney with(String currency, BigDecimal amount) {
        return of(currency, amount);
    }

    @Override
    public GamaBigMoney withAmount(BigDecimal amount) {
        return of(this.currency, amount);
    }

    public GamaMoney toMoney() {
        return GamaMoney.of(this.currency, this.amount.setScale(Currency.getInstance(currency).getDefaultFractionDigits(), RoundingMode.HALF_UP));
    }

    public GamaBigMoney withScale(int scale) {
        return withAmount(this.amount.setScale(scale, RoundingMode.HALF_UP));
    }
}
