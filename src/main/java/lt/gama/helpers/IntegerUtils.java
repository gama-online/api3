package lt.gama.helpers;

/**
 * gama-online
 * Created by valdas on 2017-03-30.
 */
public final class IntegerUtils {

    private IntegerUtils() {}

    public static int value(Integer value) {
        return value == null ? 0 : value;
    }

    public static boolean isZero(Integer value) {
        return value == null || value == 0;
    }

    public static boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    public static boolean isPositiveOrZero(Integer value) {
        return value == null || value >= 0;
    }

    public static boolean isNegative(Integer value) {
        return value != null && value < 0;
    }

    public static boolean isNegativeOrZero(Integer value) {
        return value == null || value <= 0;
    }

    public static boolean isEqual(Integer value1, Integer value2) {
        return isZero(subtract(value1, value2));
    }

    public static boolean isLessThan(Integer value1, Integer value2) {
        return isNegative(subtract(value1, value2));
    }

    public static boolean isLessThanOrEqual(Integer value1, Integer value2) {
        return isNegativeOrZero(subtract(value1, value2));
    }

    public static boolean isGreaterThan(Integer value1, Integer value2) {
        return isPositive(subtract(value1, value2));
    }

    public static boolean isGreaterThanOrEqual(Integer value1, Integer value2) {
        return isPositiveOrZero(subtract(value1, value2));
    }

    public static Integer max(Integer value1, Integer value2) {
        return isGreaterThan(value1, value2) ? value1 : value2;
    }

    public static Integer min(Integer value1, Integer value2) {
        return isLessThan(value1, value2) ? value1 : value2;
    }

    public static Integer add(Integer value1, Integer value2) {
        return value1 == null?value2:(value2 == null?value1:Integer.valueOf(value1 + value2));
    }

    public static Integer inc(Integer value) {
        return value == null?1:(value + 1);
    }

    public static Integer subtract(Integer value1, Integer value2) {
        return value2 == null?value1:(value1 == null?Integer.valueOf(-value2):Integer.valueOf(value1 - value2));
    }

    public static Integer abs(Integer value) {
        return value == null?null : Math.abs(value);
    }

    public static Integer negated(Integer value) {
        return value == null?null:-value;
    }

    public static Integer total(Integer... values) {
        if(values.length == 0) {
            return null;
        } else {
            Integer total = values[0];
            for(int i = 1; i < values.length; ++i) {
                total = add(total, values[i]);
            }
            return total;
        }
    }

}
