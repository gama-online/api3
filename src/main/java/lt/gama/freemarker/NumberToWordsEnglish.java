package lt.gama.freemarker;

import lt.gama.helpers.StringHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

public class NumberToWordsEnglish {

    private static final String[] tensNames = {
            "",
            " ten",
            " twenty",
            " thirty",
            " forty",
            " fifty",
            " sixty",
            " seventy",
            " eighty",
            " ninety"
    };

    private static final String[] numNames = {
            "",
            " one",
            " two",
            " three",
            " four",
            " five",
            " six",
            " seven",
            " eight",
            " nine",
            " ten",
            " eleven",
            " twelve",
            " thirteen",
            " fourteen",
            " fifteen",
            " sixteen",
            " seventeen",
            " eighteen",
            " nineteen"
    };

    private static final Map<String, String> fractions = Map.of(
            "EUR", "ct",
            "USD", "ct",
            "PLN", "gr"
    );


    private NumberToWordsEnglish() {
    }

    public static String moneyToText(BigDecimal decimal, Currency currency, Locale locale) {
        if (decimal == null) return "";

        String result;

        final BigInteger integer = decimal.toBigInteger();
        final BigInteger fractional = decimal.abs().remainder(BigDecimal.ONE).movePointRight(decimal.scale()).toBigInteger();

        String text = integerToText(integer);

        String currencyCode = currency != null ? currency.getSymbol(locale) : null;
        String fractionName = currencyFractionName(currency);

        result = text + (StringHelper.isEmpty(currencyCode) ? "" : " " + currencyCode) +
                ", " + fractional + (fractionName == null ? "" : " " + fractionName);

        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    private static String currencyFractionName(Currency currency) {
        if (currency == null || currency.getCurrencyCode() == null) return null;
        return fractions.get(currency.getCurrencyCode());
    }

    private static String integerToText(final BigInteger integer) {
        // 0 to 999 999 999 999
        if (integer == null) return null;
        if (integer.compareTo(BigInteger.ZERO) == 0) return "zero";
        if (integer.abs().compareTo(BigInteger.valueOf(999_999_999_999L)) > 0) return "***";

        StringBuilder ans = new StringBuilder();
        if (integer.compareTo(BigInteger.ZERO) < 0) ans.append("minus ");

        final long number = integer.abs().longValue();
        final int billions = (int) (number / 1_000_000_000L);
        if (billions == 1) {
            ans.append("one billion ");
        } else if (billions > 0) {
            ans.append(convertLessThanOneThousand(billions)).append(" billions ");
        }
        final int millions = (int) (number % 1_000_000_000L) / 1_000_000;
        if (millions == 1) {
            ans.append("one million ");
        } else if (millions > 0) {
            ans.append(convertLessThanOneThousand(millions)).append(" millions ");
        }
        final int thousands = (int) (number % 1_000_000L) / 1000;
        if (thousands == 1) {
            ans.append("one thousand ");
        } else if (thousands > 0) {
            ans.append(convertLessThanOneThousand(thousands)).append(" thousand ");
        }
        final int hundreds = (int) (number % 1000L);
        ans.append(convertLessThanOneThousand(hundreds));

        // remove extra spaces!
        return ans.toString().replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
    }

    private static String convertLessThanOneThousand(int number) {
        String soFar;

        if (number % 100 < 20) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;

            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) return soFar;
        return numNames[number] + " hundred" + soFar;
    }
}
