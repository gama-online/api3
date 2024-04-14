package lt.gama.model.type;

import jakarta.persistence.Embeddable;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.GamaMoneyUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class Exchange implements Serializable {

	@Serial
    private static final long serialVersionUID = -1L;

	/**
	 * ISO 4217 base currency code
	 */
	private String base;

	/**
	 * Amount of base currency
	 */
	private BigDecimal baseAmount;

	/**
	 * Exchange currency code by ISO 4217
	 */
	private String currency;

	/**
	 * Exchange currency amount from baseAmount
	 */
	private BigDecimal amount;

	/**
	 * Exchange date
	 */
	private LocalDate date;


	public Exchange() {
	}

	/**
	 * Create exchange record.
	 *
	 * @param currency - Exchange currency code by ISO 4217
	 */
	public Exchange(String currency) {
		this.currency = currency;
	}

	/**
	 * Create exchange record.
	 *
	 * @param base - ISO 4217 base currency code
	 * @param baseAmount - Amount of base currency
	 * @param currency - Exchange currency code by ISO 4217
	 * @param amount - Exchange currency amount
	 */
	public Exchange(String base, BigDecimal baseAmount, String currency, BigDecimal amount, LocalDate date) {
		this.base = base;
		this.baseAmount = baseAmount;
		this.currency = currency;
		this.amount = amount;
		this.date = date;
	}

	public Exchange(String base, LocalDate date) {
	    this(base, BigDecimal.ONE, base, BigDecimal.ONE, date);
    }

	public BigDecimal conversionMultiplier() {
		if (BigDecimalUtils.isZero(amount) || BigDecimalUtils.isZero(baseAmount)) return BigDecimal.ZERO;
		return baseAmount.setScale(8, RoundingMode.HALF_UP).divide(amount, RoundingMode.HALF_UP);
	}

    public BigDecimal conversionMultiplierReverse() {
        if (BigDecimalUtils.isZero(amount) || BigDecimalUtils.isZero(baseAmount)) return BigDecimal.ZERO;
        return amount.setScale(8, RoundingMode.HALF_UP).divide(baseAmount, RoundingMode.HALF_UP);
    }

	public GamaMoney exchange(GamaMoney sum) {
		if (GamaMoneyUtils.isZero(sum)) return null;
		if (sum.getCurrency().equals(base)) return sum;
		if (!sum.getCurrency().equals(currency))
			throw new IllegalArgumentException(
					MessageFormat.format("Exchange currency {0} not the same as sum {1}",
							currency, sum.getCurrency()));
		return sum.convertedTo(base, conversionMultiplier());
	}

    // convert from base -> currency
	public GamaMoney exchangeReverse(GamaMoney sum) {
        if (GamaMoneyUtils.isZero(sum)) return null;
        if (sum.getCurrency().equals(currency)) return sum;
        if (!sum.getCurrency().equals(base))
            throw new IllegalArgumentException(
                    MessageFormat.format("Exchange base currency {0} not the same as sum {1}",
                            base, sum.getCurrency()));
        return sum.convertedTo(currency, conversionMultiplierReverse());
    }

	// generated

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public BigDecimal getBaseAmount() {
		return baseAmount;
	}

	public void setBaseAmount(BigDecimal baseAmount) {
		this.baseAmount = baseAmount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Exchange exchange = (Exchange) o;
		return Objects.equals(base, exchange.base) && Objects.equals(baseAmount, exchange.baseAmount) && Objects.equals(currency, exchange.currency) && Objects.equals(amount, exchange.amount) && Objects.equals(date, exchange.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(base, baseAmount, currency, amount, date);
	}

	@Override
	public String toString() {
		return "Exchange{" +
				"base='" + base + '\'' +
				", baseAmount=" + baseAmount +
				", currency='" + currency + '\'' +
				", amount=" + amount +
				", date=" + date +
				'}';
	}
}
