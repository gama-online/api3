package lt.gama.freemarker;

import lt.gama.helpers.StringHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

/**
 * gama-online
 * Created by valdas on 2018-09-14.
 */
public final class NumberToWordsLithuanian {

    private static final String[] tensNames = {
            "",
            " dešimt",
            " dvidešimt",
            " trisdešimt",
            " keturiasdešimt",
            " penkiasdešimt",
            " šešiasdešimt",
            " septyniasdešimt",
            " aštuoniasdešimt",
            " devyniasdešimt"
    };

    private static final String[] numNames = {
            "",
            " vienas",
            " du",
            " trys",
            " keturi",
            " penki",
            " šeši",
            " septyni",
            " aštuoni",
            " devyni",
            " dešimt",
            " vienuolika",
            " dvylika",
            " trylika",
            " keturiolika",
            " penkiolika",
            " šešiolika",
            " septyniolika",
            " aštuoniolika",
            " devyniolika"
    };

    private static final Map<String, String> fractions = Map.of(
            "EUR", "ct",
            "USD", "ct",
            "PLN", "gr"
    );

    private NumberToWordsLithuanian() {
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
        if (integer.compareTo(BigInteger.ZERO) == 0) return "nulis";
        if (integer.abs().compareTo(BigInteger.valueOf(999_999_999_999L)) > 0) return "***";

        StringBuilder ans = new StringBuilder();
        if (integer.compareTo(BigInteger.ZERO) < 0) ans.append("minus ");

        final long number = integer.abs().longValue();
        final int billions = (int) (number / 1_000_000_000L);
        if (billions > 0) {
            ans.append(convertLessThanOneThousand(billions, new String[]{"milijardas", "milijardai", "milijardų"})).append(" ");
        }
        final int millions = (int) (number % 1_000_000_000L) / 1_000_000;
        if (millions > 0) {
            ans.append(convertLessThanOneThousand(millions, new String[]{"milijonas", "milijonai", "milijonų"})).append(" ");
        }
        final int thousands = (int) (number % 1_000_000L) / 1000;
        if (thousands > 0) {
            ans.append(convertLessThanOneThousand(thousands, new String[]{"tūkstantis", "tūkstančiai", "tūkstančių"})).append(" ");
        }
        final int hundreds = (int) (number % 1000L);
        ans.append(convertLessThanOneThousand(hundreds, null));

        // remove extra spaces!
        return ans.toString().replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
    }

    /*
        1 - milijonas - names[0]
        2..9 - milijonai - names[1]
        10..20,30,40,50,60,70,80,90,100,200,300,400,500,600,700,800,900 - milijonų - names[2]
    */
    private static String convertLessThanOneThousand(int number, String[] names) {
        if (number == 1) return " vienas" + (names != null ? " " + names[0] : "");

        String soFar;

        if (number % 100 < 20) {
            soFar = numNames[number % 100] +
                    (names == null ? "" : " " + (number % 100 == 1 ? names[0] : number % 100 < 10 ? names[1] : names[2]));
            number /= 100;
        } else {
            soFar = numNames[number % 10] +
                    (names == null ? "" : " " + (number % 10 == 1 ? names[0] : number % 10 > 0 ? names[1] : names[2]));
            number /= 10;

            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) return soFar;
        return numNames[number] + (number == 1 ? " šimtas" : " šimtai") + soFar;
    }
}

