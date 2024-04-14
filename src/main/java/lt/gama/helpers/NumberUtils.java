package lt.gama.helpers;

import java.math.BigDecimal;

/**
 * gama-online
 * Created by valdas on 2017-08-06.
 */
public final class NumberUtils {

    private NumberUtils() {}

    public static BigDecimal add(BigDecimal n1, BigDecimal n2) {
        return n1 == null ? n2 : n2 == null ? n1 : n1.add(n2);
    }

    public static Number multipliedBy(Number n1, Number n2) {
        return n1 == null || n2 == null ? null : n1.doubleValue() * n2.doubleValue();
    }

    public static double doubleValue(Number n) {
        return n == null ? 0 : n.doubleValue();
    }

    /**
     * Is d1 is equal d2 with precision
     * @param d1 number 1
     * @param d2 number 2
     * @param precision decimal places
     * @return true if equal
     */
    public static boolean isEq(Double d1, Double d2, int precision) {
        return Math.abs(doubleValue(d1) - doubleValue(d2)) < Math.pow(10, -precision);
    }

    /**
     * Is number equal 0 with precision
     * @param d number
     * @param precision decimal places
     * @return true if equal
     */
    public static boolean isZero(Double d, int precision) {
        return Math.abs(doubleValue(d)) < Math.pow(10, -precision);
    }

    public static Long toLong(Number n) {
        return n == null ? null : n.longValue();
    }
}
