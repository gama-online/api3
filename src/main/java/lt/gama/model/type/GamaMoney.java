package lt.gama.model.type;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.Embeddable;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;


@Embeddable
@JsonIncludeProperties({ "currency", "amount" })
public class GamaMoney extends GamaAbstractMoney<GamaMoney> {

    public static GamaMoney ofNullable(String currency, BigDecimal amount) {
        if (StringHelper.isEmpty(currency)) {
            if (BigDecimalUtils.isZero(amount)) return null;
            throw new NullPointerException("Currency must not be null");
        }
        if (amount == null) return GamaMoney.zero(currency);
        return of(currency, amount);
    }

    public static GamaMoney of(String currency, BigDecimal amount) {
        return new GamaMoney(currency, amount);
    }

    public static GamaMoney of(String currency, double amount) {
        return of(currency, BigDecimal.valueOf(amount));
    }

    public static GamaMoney zero(String currency) {
        return of(currency, BigDecimal.ZERO);
    }

    public static GamaMoney parse(String moneyStr) {
        String ms = StringHelper.trim(moneyStr);
        if (StringHelper.isEmpty(ms) || "null".equalsIgnoreCase(ms) || "null null".equalsIgnoreCase(ms)) return null;
        return new GamaMoney(moneyStr);
    }

    @SuppressWarnings("unused")
    protected GamaMoney() {}

    public GamaMoney(String currency, BigDecimal amount) {
        super(currency, amount);
    }

    public GamaMoney(String moneyStr) {
        super(moneyStr);
    }

    @Override
    public GamaMoney with(String currency, BigDecimal amount) {
        return new GamaMoney(currency, amount);
    }

    @Override
    public GamaMoney withAmount(BigDecimal amount) {
        return of(this.currency, amount);
    }

    // special method GamaMoney

    public GamaBigMoney toBigMoney() {
        return GamaBigMoney.of(this);
    }

    // Object methods

    protected void setCurrency(String currency) {
        Validators.checkNotNull(currency, "Currency must not be null");
        this.currency = currency;
        checkScale();
    }

    protected void setAmount(BigDecimal amount) {
        Validators.checkNotNull(amount, "Amount must not be null");
        this.amount = amount;
        checkScale();
    }

    private void checkScale() {
        if (this.currency != null && this.amount != null) {
            this.amount = this.amount.setScale(Currency.getInstance(this.currency).getDefaultFractionDigits(), RoundingMode.HALF_UP);
        }
    }
}
