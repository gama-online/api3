package lt.gama.model.type;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;
import java.util.regex.Pattern;

@MappedSuperclass
@JsonIncludeProperties({ "currency", "amount" })
public abstract class GamaAbstractMoney<M extends GamaAbstractMoney<?>> implements Comparable<GamaAbstractMoney<?>>, Serializable {

    @Schema(description = "ISO-4217 three-letter currency code", example = "EUR")
    @Column(length = 3)
    protected String currency;

    @Column
    protected BigDecimal amount;

    protected static final Pattern PARSE_REGEX = Pattern.compile("[+-]?[0-9]*[.]?[0-9]*");

    @SuppressWarnings("unused")
    protected GamaAbstractMoney() {}

    protected GamaAbstractMoney(String currency, BigDecimal amount) {
        setCurrency(currency);
        setAmount(amount);
    }

    public GamaAbstractMoney(String moneyStr) {
        Validators.checkNotNull(moneyStr, "Money must not be null");
        if (moneyStr.length() < 3) {
            throw new IllegalArgumentException("Money '" + moneyStr + "' cannot be parsed");
        }
        String currency = moneyStr.substring(0, 3);
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Money '" + moneyStr + "' cannot be parsed", e);
        }
        int amountStart = 3;
        while (amountStart < moneyStr.length() && moneyStr.charAt(amountStart) == ' ') {
            amountStart++;
        }
        String amountStr = moneyStr.substring(amountStart);
        if (StringHelper.isEmpty(amountStr) || "null".equalsIgnoreCase(amountStr)) amountStr = "0";
        if (!PARSE_REGEX.matcher(amountStr).matches()) {
            throw new IllegalArgumentException("Money amount '" + moneyStr + "' cannot be parsed");
        }
        setCurrency(currency);
        setAmount(new BigDecimal(amountStr));
    }

    //

    public abstract M with(String currency, BigDecimal amount);

    public abstract M withAmount(BigDecimal amount);

    public M withAmount(double amount) {
        return withAmount(BigDecimal.valueOf(amount));
    }

    public M withScale(int scale) {
        return withAmount(this.amount.setScale(scale, RoundingMode.HALF_UP));
    }

    public M plus(M money) {
        Validators.checkArgument(currency.equals(money.currency), "Currencies must be equal: " + currency + " and " + money.currency);
        return withAmount(this.amount.add(money.amount));
    }

    public M plus(double amount) {
        return withAmount(this.amount.add(BigDecimal.valueOf(amount).setScale(this.amount.scale(), RoundingMode.HALF_UP)));
    }

    public M negated() {
        return withAmount(this.amount.negate());
    }

    @SuppressWarnings("unchecked")
    public M abs() {
        if (!isNegative()) return (M) this;
        return withAmount(this.amount.abs());
    }

    public M minus(M money) {
        Validators.checkArgument(currency.equals(money.currency), "Currencies must be equal: " + currency + " and " + money.currency);
        return withAmount(this.amount.subtract(money.amount));
    }

    public M minus(double amount) {
        return this.plus(-amount);
    }

    @SuppressWarnings("unchecked")
    public M multipliedBy(BigDecimal d) {
        Validators.checkNotNull(d, "Multiplier must not be null");
        if (isZero() || d.compareTo(BigDecimal.ONE) == 0) {
            return (M) this;
        }
        return withAmount(this.amount.multiply(d).setScale(this.amount.scale(), RoundingMode.HALF_UP));
    }

    public M multipliedBy(double d) {
        return multipliedBy(BigDecimal.valueOf(d));
    }

    @SuppressWarnings("unchecked")
    public M dividedBy(BigDecimal d) {
        Validators.checkNotNull(d, "Divisor must not be null");
        if (isZero() || d.compareTo(BigDecimal.ONE) == 0) {
            return (M) this;
        }
        return withAmount(this.amount.divide(d, RoundingMode.HALF_UP));
    }

    public M dividedBy(double d) {
        return dividedBy(BigDecimal.valueOf(d));
    }

    @SuppressWarnings("unchecked")
    public M convertedTo(String currency, BigDecimal rate) {
        Validators.checkNotNull(currency, "CurrencyUnit must not be null");
        Validators.checkNotNull(rate, "Multiplier must not be null");
        if (this.currency.equals(currency)) {
            if (rate.compareTo(BigDecimal.ONE) == 0) {
                return (M) this;
            }
            throw new IllegalArgumentException("Cannot convert to the same currency");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot convert using a negative conversion multiplier");
        }
        BigDecimal newAmount = amount.multiply(rate);
        return with(currency, newAmount);
    }

    //

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public int getScale() {
        return this.amount.scale();
    }

    // Object methods

    @Override
    public String toString() {
        if (this.currency == null) return "";
        return this.currency + " " + this.amount;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamaAbstractMoney<?> gamaMoney = (GamaAbstractMoney<?>) o;
        return Objects.equals(currency, gamaMoney.currency) &&
                (amount == gamaMoney.amount || amount.compareTo(gamaMoney.amount) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, amount);
    }

    @Override
    @SuppressWarnings("all")
    public int compareTo(GamaAbstractMoney o) {
        Validators.checkArgument(currency.equals(o.currency));
        return amount == o.amount ? 0 : amount.compareTo(o.amount);
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    protected void setCurrency(String currency) {
        Validators.checkNotNull(currency, "Currency must not be null");
        this.currency = currency;
    }

    protected void setAmount(BigDecimal amount) {
        Validators.checkNotNull(amount, "Amount must not be null");
        this.amount = amount;
    }
}
