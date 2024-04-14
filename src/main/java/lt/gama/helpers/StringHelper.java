package lt.gama.helpers;

import lt.gama.model.i.ISeriesWithOrdinal;
import lt.gama.service.ex.rt.GamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * gama-online
 * Created by valdas on 2016-11-04.
 */
public final class StringHelper {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private StringHelper() {
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public static String trim2null(String s) {
        return isEmpty(s) ? null : s.trim();
    }

    public static String trimNormalize2null(String s) {
        return isEmpty(s) ? null : s.replaceAll("\\s+", " ").trim();
    }

    public static String deleteSpaces(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "");
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean hasValue(String s) {
        return !isEmpty(s);
    }

    public static String concatenate(String s1, String s2) {
        return isEmpty(s1) ? s2 : isEmpty(s2) ? s1 : s1 + ' ' + s2;
    }

    public static boolean isEquals(String s1, String s2) {
        return trim(s1).equals(trim(s2));
    }

    public static boolean isEqualsBankAccounts(String account1, String account2) {
        return normalizeBankAccount(account1).equals(normalizeBankAccount(account2));
    }

    public static String normalizeBankAccount(String account) {
        return StringHelper.deleteSpaces(account).toUpperCase();
    }

    private static void addToken(Collection<String> list, StringBuilder sb) {
        if (!sb.isEmpty()) {
            String token = sb.toString().trim();
            if (!isEmpty(token)) list.add(sb.toString().trim());
            sb.setLength(0);
        }
    }

    public static Collection<String> splitTokens(String token) {
        if (isEmpty(token)) return null;
        Collection<String> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        char q = 0;
        for (char c : token.toCharArray()) {
            if (q != 0) {
                // if in quoted string
                if (c == q) {
                    // end of quote
                    q = 0;
                    addToken(result, sb);
                } else {
                    sb.append(c);
                }
                continue;
            }

            if (c == '"' || c == '\'') {
                // if quote starts
                q = c;
                addToken(result, sb);
                continue;
            }

            if (!Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                addToken(result, sb);
            }
        }

        // final check
        addToken(result, sb);

        return result;
    }

    public static String firstWord(String text) {
        if (isEmpty(text)) return null;
        StringBuilder sb = new StringBuilder();
        for (Character c : text.trim().toCharArray()) {
            if (Character.isWhitespace(c)) break;
            sb.append(c);
        }
        return !sb.isEmpty() ? sb.toString() : null;
    }

    public static String readFromFile(String fileName) {
        try (var is = StringHelper.class.getClassLoader().getResourceAsStream(fileName);
             var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("StringHelper: " + e.getMessage(), e);
            throw new GamaException("File '" + fileName + "' read error", e);
        }
    }

    public static String loadSQLFromFile(String path, String file) {
        return readFromFile("sql" + File.separator + path + File.separator + file);
    }

    private static class SeriesWithOrdinal implements ISeriesWithOrdinal {
        String number;
        String series;
        Long ordinal;

        // generated

        @Override
        public String getNumber() {
            return number;
        }

        @Override
        public String getSeries() {
            return series;
        }

        @Override
        public Long getOrdinal() {
            return ordinal;
        }
    }

    private static final int MAX_NUMBER_DIGITS = 9;

    public static ISeriesWithOrdinal parseDocNumber(String number) {
        SeriesWithOrdinal seriesWithOrdinal = new SeriesWithOrdinal();
        if (isEmpty(number)) return seriesWithOrdinal;
        number = number.trim();
        int lastSpaceIndex = number.lastIndexOf(' ');
        if (lastSpaceIndex > 0) {
            seriesWithOrdinal.series = number.substring(0, lastSpaceIndex).trim();
            lastSpaceIndex++;
        } else {
            lastSpaceIndex = 0;
            while (lastSpaceIndex < number.length() && !Character.isDigit(number.charAt(lastSpaceIndex))) {
                lastSpaceIndex++;
            }
            if (lastSpaceIndex > 0) {
                seriesWithOrdinal.series = number.substring(0, lastSpaceIndex).trim();
            }
        }
        // leave only first digits
        int lastDigitIndex = lastSpaceIndex;
        while (lastDigitIndex < number.length() && Character.isDigit(number.charAt(lastDigitIndex))) {
            lastDigitIndex++;
        }
        // skip leading zeroes
        while (lastSpaceIndex < lastDigitIndex && number.charAt(lastSpaceIndex) == '0') lastSpaceIndex++;
        String ordinal = number.substring(lastSpaceIndex, lastDigitIndex);
        if (ordinal.length() > MAX_NUMBER_DIGITS || ordinal.isEmpty()) return seriesWithOrdinal;
        try {
            seriesWithOrdinal.ordinal = Long.parseLong(ordinal);
        } catch (NumberFormatException ignored) {
        }
        return seriesWithOrdinal;
    }

    public static String normalizeFileName(String filename) {
        return filename == null ? "" : filename.replaceAll("[\\s:/\\\\'\"_]+", "_");
    }

    public static String toRfc5987(final String s) {
        final byte[] rawBytes = s.getBytes(StandardCharsets.UTF_8);
        final int len = rawBytes.length;
        final StringBuilder sb = new StringBuilder(len << 1);
        final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        final byte[] attributeChars = {'!', '#', '$', '&', '+', '-', '.', '0',
                '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '^', '_', '`', 'a',
                'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '|',
                '~'};
        for (final byte b : rawBytes) {
            if (Arrays.binarySearch(attributeChars, b) >= 0) {
                sb.append((char) b);
            } else {
                sb.append('%');
                sb.append(digits[0x0f & (b >>> 4)]);
                sb.append(digits[b & 0x0f]);
            }
        }
        return sb.toString();
    }
}
