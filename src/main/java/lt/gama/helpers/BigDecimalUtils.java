package lt.gama.helpers;

import java.math.BigDecimal;

/**
 * Gama
 * Created by valdas on 15-04-10.
 */
public final class BigDecimalUtils {

    private BigDecimalUtils() {}

    public static boolean isZero(BigDecimal bigDecimal) {
        return bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isNonZero(BigDecimal bigDecimal) {
        return !isZero(bigDecimal);
    }

    public static boolean isPositive(BigDecimal bigDecimal) {
        return bigDecimal != null && bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isPositiveOrZero(BigDecimal bigDecimal) {
        return bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isNegative(BigDecimal bigDecimal) {
        return bigDecimal != null && bigDecimal.compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean isNegativeOrZero(BigDecimal bigDecimal) {
        return bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static boolean isEqual(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isZero(subtract(bigDecimal1, bigDecimal2));
    }

    public static boolean isLessThan(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isNegative(subtract(bigDecimal1, bigDecimal2));
    }

    public static boolean isLessThanOrEqual(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isNegativeOrZero(subtract(bigDecimal1, bigDecimal2));
    }

    public static boolean isGreaterThan(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isPositive(subtract(bigDecimal1, bigDecimal2));
    }

    public static boolean isGreaterThanOrEqual(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isPositiveOrZero(subtract(bigDecimal1, bigDecimal2));
    }

    public static BigDecimal max(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isGreaterThan(bigDecimal1, bigDecimal2) ? bigDecimal1 : bigDecimal2;
    }

    public static BigDecimal min(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isLessThan(bigDecimal1, bigDecimal2) ? bigDecimal1 : bigDecimal2;
    }

    public static BigDecimal add(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return bigDecimal1 == null?bigDecimal2:(bigDecimal2 == null?bigDecimal1:bigDecimal1.add(bigDecimal2));
    }

    public static BigDecimal subtract(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return bigDecimal2 == null?bigDecimal1:(bigDecimal1 == null?bigDecimal2.negate():bigDecimal1.subtract(bigDecimal2));
    }

    public static BigDecimal abs(BigDecimal bigDecimal) {
        return bigDecimal == null?null:bigDecimal.abs();
    }

    public static BigDecimal negated(BigDecimal bigDecimal) {
        return bigDecimal == null?null:bigDecimal.negate();
    }

    public static double doubleValue(BigDecimal bigDecimal) {
        return isZero(bigDecimal) ? 0 : bigDecimal.doubleValue();
    }

    public static BigDecimal firstNotNull(BigDecimal... numbers) {
        for (BigDecimal number : numbers) {
            if (number != null) return number;
        }
        return null;
    }

    public static BigDecimal multiply(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return isZero(bigDecimal1) || isZero(bigDecimal2) ? BigDecimal.ZERO : bigDecimal1.multiply(bigDecimal2);
    }

    public static BigDecimal notNull(BigDecimal bigDecimal) {
        return bigDecimal == null ? BigDecimal.ZERO : bigDecimal;
    }
}
