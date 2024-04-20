package lt.gama.freemarker;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.GamaAbstractMoney;
import lt.gama.model.type.GamaMoney;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-07-14.
 */
public class GamaMoneyFormatter {

    // Formats are generally not synchronized. It is recommended to create separate format instances for each thread.
    // If multiple threads access a format concurrently, it must be synchronized externally.

    private static final ThreadLocal<GamaMoneyFormatter> threadLocal = ThreadLocal.withInitial(GamaMoneyFormatter::new);

    static final char NBSP = '\u00A0';

    private Locale locale;

    private DecimalFormat currencyFormat;
    private DecimalFormat currencyFormatStd;

    private int decimalPrice;

    private static final Map<String, String> CURRENCIES = Map.of(
            "USD", "$",
            "EUR", "€",
            "GBP", "£",
            "PLN", "zł"
    );

    public static GamaMoneyFormatter getInstance(Locale locale, int decimalPrice) {
        GamaMoneyFormatter moneyFormatter = threadLocal.get();
        moneyFormatter.decimalPrice = decimalPrice;
        moneyFormatter.locale = locale;
        moneyFormatter.currencyFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
        moneyFormatter.currencyFormatStd = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
        return moneyFormatter;
    }

    private void updateCurrCode(String code, DecimalFormat decimalFormat, boolean changeCurrency) {
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        if (!changeCurrency) {
            if (!Objects.equals(decimalFormat.getCurrency().getSymbol(locale), symbols.getCurrencySymbol())) {
                symbols.setCurrencySymbol(decimalFormat.getCurrency().getSymbol(locale));
                decimalFormat.setDecimalFormatSymbols(symbols);
            }
        } else {
            String rep = CURRENCIES.get(code);
            if (rep != null && !decimalFormat.getDecimalFormatSymbols().getCurrencySymbol().equals(rep)) {
                symbols.setCurrencySymbol(CURRENCIES.get(code));
                decimalFormat.setDecimalFormatSymbols(symbols);
            }
        }
    }

    /**
     * Print fractional digits from money value
     * @param money to format
     * @param changeCurrency if true - try to change currency code into most used one like USD -> $, GBP -> £ ...
     * @return formatted string
     */
    public String format(GamaAbstractMoney<?> money, boolean changeCurrency) {
        if (money == null) return "-";
        String code = money.getCurrency();
        Currency currency = Currency.getInstance(code);
        currencyFormat.setCurrency(Currency.getInstance(code));
        currencyFormat.setMaximumFractionDigits(Math.min(money.getScale(), currency.getDefaultFractionDigits() + 2));
        currencyFormat.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
        updateCurrCode(code, currencyFormat, changeCurrency);
        return currencyFormat.format(money.getAmount()).replace(' ', NBSP);
    }

    public String format(GamaAbstractMoney<?> money) {
        return format(money, true);
    }

    public String format(String currency, BigDecimal amount) {
        return format(GamaMoney.of(currency, amount), true);
    }


    public String formatZero(String currency) {
        return format(GamaMoney.zero(currency), true);
    }

    /**
     * Print default currency fractional digits
     * @param money to format
     * @param changeCurrency if true - try to change currency code into most used one like USD -> $, GBP -> £ ...
     * @return formatted string
     */
    public String formatStd(GamaAbstractMoney<?> money, boolean changeCurrency) {
        if (money == null) return "-";
        String code = money.getCurrency();
        Currency currency = Currency.getInstance(code);
        currencyFormatStd.setCurrency(currency);
        currencyFormatStd.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        currencyFormatStd.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        currencyFormatStd.setRoundingMode(RoundingMode.HALF_UP);
        updateCurrCode(code, currencyFormatStd, changeCurrency);
        return currencyFormatStd.format(money.getAmount()).replace(' ', NBSP);
    }

    public String formatStd(GamaAbstractMoney<?> money) {
        return formatStd(money, true);
    }

    public String formatStdZero(String currency) {
        return formatStd(GamaMoney.zero(currency), true);
    }

    /**
     * Print decimal digits specified by 'decimalPrice'
     * @param money to format
     * @param changeCurrency if true - try to change currency code into most used one like USD -> $, GBP -> £ ...
     * @return formatted string
     */
    public String formatPrice(GamaAbstractMoney<?> money, boolean changeCurrency) {
        if (money == null) return "-";
        String code = money.getCurrency();
        currencyFormat.setCurrency(Currency.getInstance(code));
        currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
        currencyFormat.setMaximumFractionDigits(decimalPrice);
        currencyFormat.setMinimumFractionDigits(decimalPrice);
        updateCurrCode(code, currencyFormat, changeCurrency);
        return currencyFormat.format(money.getAmount()).replace(' ', NBSP);
    }

    public String formatPrice(GamaAbstractMoney<?> money) {
        return formatPrice(money, true);
    }

    public String formatPriceZero(String currency) {
        return formatPrice(GamaMoney.zero(currency), true);
    }

    private static final String ltLangCode = Locale.of("lt").getLanguage();
    private static final String enLangCode = Locale.of("en").getLanguage();

    public String text(GamaAbstractMoney<?> money) {
        if (money == null) return "";

        Currency currency = Currency.getInstance(money.getCurrency());
        BigDecimal amount = money.getAmount();

        if (Objects.equals(locale.getLanguage(), ltLangCode)) {
            return NumberToWordsLithuanian.moneyToText(amount, currency, locale) + " (" + format(money) + ")";
        } else if (Objects.equals(locale.getLanguage(), enLangCode)) {
            return NumberToWordsEnglish.moneyToText(amount, currency, locale) + " (" + format(money) + ")";
        } else {
            return format(money);
        }
    }

    public String currencyDisplayName(String currency) {
        return Currency.getInstance(currency).getDisplayName(locale);
    }

    public boolean isNegative(GamaAbstractMoney<?> money) {
        return GamaMoneyUtils.isNegative(money);
    }

    public boolean isPositive(GamaAbstractMoney<?> money) {
        return GamaMoneyUtils.isPositive(money);
    }

    public boolean isZero(GamaAbstractMoney<?> money) {
        return GamaMoneyUtils.isZero(money);
    }

    public <M extends GamaAbstractMoney<M>> M abs(M money) {
        return GamaMoneyUtils.abs(money);
    }

    public <M extends GamaAbstractMoney<M>> M negated(M money) {
        return GamaMoneyUtils.negated(money);
    }

}
