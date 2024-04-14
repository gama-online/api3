package lt.gama.helpers;

import lt.gama.model.type.GamaAbstractMoney;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;

import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-07-14.
 */
public final class GamaMoneyUtils {

    private GamaMoneyUtils() {}

    public static GamaMoney toMoney(GamaBigMoney money) {
        return money == null ? null : money.toMoney();
    }

    public static boolean isNullOrSameCurrency(GamaAbstractMoney<?> money1, GamaAbstractMoney<?> money2) {
        return money1 == null || money2 == null ||
                Objects.equals(money1.getCurrency(), money2.getCurrency());
    }

    public static boolean isZero(GamaAbstractMoney<?> money) {
        return money == null || money.isZero();
    }

    public static boolean isNonZero(GamaAbstractMoney<?> money) {
        return !isZero(money);
    }

    public static boolean isPositive(GamaAbstractMoney<?> money) {
        return money != null && money.isPositive();
    }

    public static boolean isPositiveOrZero(GamaAbstractMoney<?> money) {
        return isZero(money) || isPositive(money);
    }

    public static boolean isNegative(GamaAbstractMoney<?> money) {
        return money != null && money.isNegative();
    }

    public static boolean isNegativeOrZero(GamaAbstractMoney<?> money) {
        return isZero(money) || isNegative(money);
    }

    public static <M extends GamaAbstractMoney<M>> boolean isEqual(M money1, M money2) {
        return isZero(subtract(money1, money2));
    }

    public static <M extends GamaAbstractMoney<M>> boolean isSameCurrencyAndEqual(M money1, M money2) {
        return isNullOrSameCurrency(money1, money2) && isZero(subtract(money1, money2));
    }

    public static <M extends GamaAbstractMoney<M>> boolean isLessThan(M money1, M money2) {
        return isNegative(subtract(money1, money2));
    }

    public static <M extends GamaAbstractMoney<M>> boolean isLessThanOrEqual(M money1, M money2) {
        return isNegativeOrZero(subtract(money1, money2));
    }

    public static <M extends GamaAbstractMoney<M>> boolean isGreaterThan(M money1, M money2) {
        return isPositive(subtract(money1, money2));
    }

    public static <M extends GamaAbstractMoney<M>> boolean isGreaterThanOrEqual(M money1, M money2) {
        return isPositiveOrZero(subtract(money1, money2));
    }


    public static <M extends GamaAbstractMoney<M>> M max(M money1, M money2) {
        return isGreaterThan(money1, money2) ? money1 : money2;
    }

    public static <M extends GamaAbstractMoney<M>> M min(M money1, M money2) {
        return isLessThan(money1, money2) ? money1 : money2;
    }


    public static <M extends GamaAbstractMoney<M>> M add(M money1, M money2) {
        if (money1 == null) {
            return money2;
        }
        if (money2 == null) {
            return money1;
        }
        return money1.plus(money2);
    }

    public static <M extends GamaAbstractMoney<M>> M subtract(M money1, M money2) {
        if (money2 == null) {
            return money1;
        }
        if (money1 == null) {
            return money2.negated();
        }
        return money1.minus(money2);
    }

    public static <M extends GamaAbstractMoney<M>> M multipliedBy(M money, Number d) {
        return isZero(money) ? money : money.multipliedBy(d == null ? 0 : d.doubleValue());
    }

    public static <M extends GamaAbstractMoney<M>> M dividedBy(M money, Number d) {
        return isZero(money) ? money : money.dividedBy(d.doubleValue());
    }

    public static <M extends GamaAbstractMoney<M>> M discountBy(M money, Double discount) {
        return isZero(money) || NumberUtils.isZero(discount, 1) ? money : money.multipliedBy(1.0 - discount / 100.0);
    }

    public static <M extends GamaAbstractMoney<M>> M taxBy(M money, Double tax) {
        return isZero(money) || NumberUtils.isZero(tax, 2) ? money : money.multipliedBy(1.0 + tax / 100.0);
    }

    public static <M extends GamaAbstractMoney<M>> M abs(M money) {
        return money == null ? null : money.abs();
    }

    public static <M extends GamaAbstractMoney<M>> M negated(M money) {
        return money == null ? null : money.negated();
    }

    @SafeVarargs
    public static <M extends GamaAbstractMoney<M>> M total(M... monies) {
        if(monies.length == 0) {
            return null;
        } else {
            M total = monies[0];
            for(int i = 1; i < monies.length; ++i) {
                total = add(total, monies[i]);
            }
            return total;
        }
    }


    public static <M extends GamaAbstractMoney<M>> int comparator(M m1, M m2) {
       return isEqual(m1, m2) ? 0 : isLessThan(m1, m2) ? -1 : 1;
    }
}
