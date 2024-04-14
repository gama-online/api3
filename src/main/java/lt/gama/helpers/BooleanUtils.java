package lt.gama.helpers;

/**
 * gama-online
 * Created by valdas on 2018-09-17.
 */
public final class BooleanUtils {

    private BooleanUtils() {}

    public static boolean isTrue(Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

    public static boolean isNotTrue(Boolean bool) {
        return !isTrue(bool);
    }

    public static boolean isSame(Boolean b1, Boolean b2) {
        return isTrue(b1) && isTrue(b2) || isNotTrue(b1) && isNotTrue(b2);
    }

    public static boolean isNotSame(Boolean b1, Boolean b2) {
        return !isSame(b1, b2);
    }
}
