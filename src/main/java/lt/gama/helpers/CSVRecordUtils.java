package lt.gama.helpers;

import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.regex.Pattern;

public final class CSVRecordUtils {

    private static final DecimalFormat decimalFormat;
    private static final DecimalFormat decimalFormatComma;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        symbols.setMinusSign('-'); // 002D - Hyphen-Minus (ANSI)
        String pattern = "#,##0.#";
        decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);

        symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        symbols.setMinusSign('-'); // 002D - Hyphen-Minus (ANSI)
        decimalFormatComma = new DecimalFormat(pattern, symbols);
        decimalFormatComma.setParseBigDecimal(true);
    }



	public static String getString(CSVRecord record, String name) {
		return getString(record, name, null);
	}

	public static String getString(CSVRecord record, String name, String defaultValue) {
		try {
			String value = record.get(name);
			return StringHelper.hasValue(value) ? value : defaultValue;
		} catch (IllegalArgumentException | IllegalStateException e) {
			return null;
		}
	}

	private final static Pattern notDigits = Pattern.compile("[^\\d-]+");
    private final static Pattern whiteSpace = Pattern.compile("\\s+");

    public static BigDecimal getDecimal(CSVRecord record, String name) {
        String value = getString(record, name);
        return parseAmount(value, null);
    }

	public static BigDecimal getDecimalMoneyPart(CSVRecord record, String name, String currency) {
        String value = getString(record, name);
        return parseAmount(value, currency);
	}

    private static BigDecimal parseAmount(String value, String currency) {
        if (value == null) return null;
        value = whiteSpace.matcher(value).replaceAll("");
        if (value.isEmpty()) return null;

        try {
            if (StringHelper.hasValue(currency)) {
                Currency currencyUnit = Currency.getInstance(currency);
                if (currencyUnit.getDefaultFractionDigits() == 0) {
                    return BigDecimal.valueOf(Long.parseLong(notDigits.matcher(value).replaceAll("")));
                }
            }
            // try to find first not digit symbol from right - it must be decimal separator
            char c = 0;
            for (int i = value.length() - 1; i >= 0; i--) {
                if (!Character.isDigit(value.charAt(i)) && !Character.isWhitespace(value.charAt(i))) {
                    c = value.charAt(i);
                    break;
                }
            }
            if (c == 0) return BigDecimal.valueOf(Long.parseLong(notDigits.matcher(value).replaceAll("")));
            if (c != '.' && c != ',') return null;

            return (BigDecimal) (c == '.' ? decimalFormat.parse(value) : decimalFormatComma.parse(value));

        } catch (NumberFormatException | ParseException e) {
            return null;
        }
    }

	static class MoneyParts {
	    String currency;
	    BigDecimal amount;

        @SuppressWarnings("unused")
        private MoneyParts() {}

        public MoneyParts(String currency, BigDecimal amount) {
            this.currency = currency;
            this.amount = amount;
        }
    }

	private static MoneyParts parseMoneyParts(String moneyStr) {
        if (moneyStr == null) return null;

        moneyStr = moneyStr.trim();
        if (moneyStr.trim().length() < 5) return null;

        String currStr = moneyStr.substring(0, 3);
        int amountStart = 3;
        while (amountStart < moneyStr.length() && Character.isWhitespace(moneyStr.charAt(amountStart))) {
            amountStart++;
        }
        String amountStr = moneyStr.substring(amountStart);
        return new MoneyParts(currStr, parseAmount(amountStr, currStr));
    }

	public static GamaMoney getMoney(CSVRecord record, String name) {
        MoneyParts moneyParts = parseMoneyParts(getString(record, name));
        if (moneyParts == null || moneyParts.amount == null) return null;

        try {
            return GamaMoney.of(moneyParts.currency, moneyParts.amount);
        } catch (IllegalArgumentException | ArithmeticException e) {
            return null;
        }
	}

    public static GamaBigMoney getBigMoney(CSVRecord record, String name) {
        MoneyParts moneyParts = parseMoneyParts(getString(record, name));
        if (moneyParts == null || moneyParts.amount == null) return null;

        try {
            return GamaBigMoney.of(moneyParts.currency, moneyParts.amount);
        } catch (IllegalArgumentException | ArithmeticException e) {
            return null;
        }
    }

    public static GamaMoney getMoney(CSVRecord record, String name, String currency) {
        BigDecimal value = getDecimalMoneyPart(record, name, currency);
        try {
            return GamaMoney.ofNullable(currency, value);
        } catch (IllegalArgumentException | ArithmeticException e) {
            return null;
        }
    }

	public static Integer getInteger(CSVRecord record, String name) {
        String value = getString(record, name);
        try {
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
	}

	public static int getInt(CSVRecord record, String name, int defaultValue) {
		Integer value = getInteger(record, name);
		return value == null ? defaultValue : value;
	}

	public static LocalDate getLocalDateMDY(CSVRecord record, String name) {
        String value = getString(record, name);
        try {
            return value != null ? LocalDate.parse(value, DateTimeFormatter.ofPattern("M/d/y")) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
	}

    public static LocalDate getLocalDateDMY(CSVRecord record, String name) {
        String value = getString(record, name);
        try {
            return value != null ? LocalDate.parse(value, DateTimeFormatter.ofPattern("d/M/y")) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static LocalDate getLocalDate(CSVRecord record, String name) {
        String value = getString(record, name);
        try {
            return value != null ? LocalDate.parse(value) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static LocalTime getLocalTime(CSVRecord record, String name) {
        String value = getString(record, name);
        try {
            return value != null ? LocalTime.parse(value) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
