package lt.gama.test.tools;

import lt.gama.helpers.BigDecimalUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2016-01-23.
 */
public class BigDecimalUtilsTest {

    @Test
    public void testBigDecimalUtils() {

        assertThat(BigDecimalUtils.isZero(new BigDecimal("1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isZero(new BigDecimal("0.00"))).isTrue();
        assertThat(BigDecimalUtils.isZero(null)).isTrue();

        assertThat(BigDecimalUtils.isPositive(new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isPositive(new BigDecimal("-1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isPositive(new BigDecimal("0.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isPositive(null)).isEqualTo(false);

        assertThat(BigDecimalUtils.isPositiveOrZero(new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isPositiveOrZero(new BigDecimal("-1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isPositiveOrZero(new BigDecimal("0.00"))).isTrue();
        assertThat(BigDecimalUtils.isPositiveOrZero(null)).isTrue();

        assertThat(BigDecimalUtils.isNegative(new BigDecimal("1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isNegative(new BigDecimal("-1.00"))).isTrue();
        assertThat(BigDecimalUtils.isNegative(new BigDecimal("0.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isNegative(null)).isEqualTo(false);

        assertThat(BigDecimalUtils.isNegativeOrZero(new BigDecimal("1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isNegativeOrZero(new BigDecimal("-1.00"))).isTrue();
        assertThat(BigDecimalUtils.isNegativeOrZero(new BigDecimal("0.00"))).isTrue();
        assertThat(BigDecimalUtils.isNegativeOrZero(null)).isTrue();

        assertThat(BigDecimalUtils.max(new BigDecimal("1.00"), new BigDecimal("2.00"))).isEqualTo(new BigDecimal("2.00"));
        assertThat(BigDecimalUtils.max(new BigDecimal("2.00"), new BigDecimal("1.00"))).isEqualTo(new BigDecimal("2.00"));
        assertThat(BigDecimalUtils.max(new BigDecimal("2.00"), null)).isEqualTo(new BigDecimal("2.00"));
        assertThat(BigDecimalUtils.max(new BigDecimal("-2.00"), null)).isNull();
        assertThat(BigDecimalUtils.max(null, new BigDecimal("1.00"))).isEqualTo(new BigDecimal("1.00"));
        assertThat(BigDecimalUtils.max(null, new BigDecimal("-1.00"))).isNull();

        assertThat(BigDecimalUtils.min(new BigDecimal("1.00"), new BigDecimal("2.00"))).isEqualTo(new BigDecimal("1.00"));
        assertThat(BigDecimalUtils.min(new BigDecimal("2.00"), new BigDecimal("1.00"))).isEqualTo(new BigDecimal("1.00"));
        assertThat(BigDecimalUtils.min(new BigDecimal("2.00"), null)).isNull();
        assertThat(BigDecimalUtils.min(null, new BigDecimal("1.00"))).isNull();
        assertThat(BigDecimalUtils.min(null, new BigDecimal("-1.00"))).isEqualTo(new BigDecimal("-1.00"));

        assertThat(BigDecimalUtils.add(new BigDecimal("1.00"), new BigDecimal("2.00"))).isEqualTo(new BigDecimal("3.00"));
        assertThat(BigDecimalUtils.add(new BigDecimal("1.00"), new BigDecimal("-2.00"))).isEqualTo(new BigDecimal("-1.00"));
        assertThat(BigDecimalUtils.add(new BigDecimal("1.00"), new BigDecimal("-1.00"))).isEqualTo(new BigDecimal("0.00"));
        assertThat(BigDecimalUtils.add(new BigDecimal("1.00"), null)).isEqualTo(new BigDecimal("1.00"));
        assertThat(BigDecimalUtils.add(null, new BigDecimal("1.00"))).isEqualTo(new BigDecimal("1.00"));

        assertThat(BigDecimalUtils.subtract(new BigDecimal("1.00"), new BigDecimal("2.00"))).isEqualTo(new BigDecimal("-1.00"));
        assertThat(BigDecimalUtils.subtract(new BigDecimal("1.00"), new BigDecimal("-2.00"))).isEqualTo(new BigDecimal("3.00"));
        assertThat(BigDecimalUtils.subtract(new BigDecimal("1.00"), new BigDecimal("1.00"))).isEqualTo(new BigDecimal("0.00"));
        assertThat(BigDecimalUtils.subtract(new BigDecimal("1.00"), null)).isEqualTo(new BigDecimal("1.00"));
        assertThat(BigDecimalUtils.subtract(null, new BigDecimal("1.00"))).isEqualTo(new BigDecimal("-1.00"));

        assertThat(BigDecimalUtils.isEqual(new BigDecimal("1.00"), new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isEqual(new BigDecimal("-1.00"), new BigDecimal("1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isEqual(new BigDecimal("0.00"), null)).isTrue();
        assertThat(BigDecimalUtils.isEqual(null, new BigDecimal("0.00"))).isTrue();
        assertThat(BigDecimalUtils.isEqual(null, null)).isTrue();

        assertThat(BigDecimalUtils.isLessThan(new BigDecimal("1.00"), new BigDecimal("1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isLessThan(new BigDecimal("-1.00"), new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isLessThan(new BigDecimal("0.00"), null)).isEqualTo(false);
        assertThat(BigDecimalUtils.isLessThan(null, new BigDecimal("0.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isLessThan(null, null)).isEqualTo(false);
        assertThat(BigDecimalUtils.isLessThan(new BigDecimal("-1.00"), null)).isTrue();
        assertThat(BigDecimalUtils.isLessThan(null, new BigDecimal("1.00"))).isTrue();

        assertThat(BigDecimalUtils.isLessThanOrEqual(new BigDecimal("2.00"), new BigDecimal("1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isLessThanOrEqual(new BigDecimal("1.00"), new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isLessThanOrEqual(new BigDecimal("-1.00"), new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isLessThanOrEqual(new BigDecimal("0.00"), null)).isTrue();
        assertThat(BigDecimalUtils.isLessThanOrEqual(null, new BigDecimal("0.00"))).isTrue();
        assertThat(BigDecimalUtils.isLessThanOrEqual(null, null)).isTrue();
        assertThat(BigDecimalUtils.isLessThanOrEqual(new BigDecimal("-1.00"), null)).isTrue();
        assertThat(BigDecimalUtils.isLessThanOrEqual(new BigDecimal("1.00"), null)).isEqualTo(false);
        assertThat(BigDecimalUtils.isLessThanOrEqual(null, new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isLessThanOrEqual(null, new BigDecimal("-1.00"))).isEqualTo(false);

        assertThat(BigDecimalUtils.isGreaterThan(new BigDecimal("1.00"), new BigDecimal("1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isGreaterThan(new BigDecimal("1.00"), new BigDecimal("-1.00"))).isTrue();
        assertThat(BigDecimalUtils.isGreaterThan(new BigDecimal("0.00"), null)).isEqualTo(false);
        assertThat(BigDecimalUtils.isGreaterThan(null, new BigDecimal("0.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isGreaterThan(null, null)).isEqualTo(false);
        assertThat(BigDecimalUtils.isGreaterThan(new BigDecimal("1.00"), null)).isTrue();
        assertThat(BigDecimalUtils.isGreaterThan(null, new BigDecimal("-1.00"))).isTrue();

        assertThat(BigDecimalUtils.isGreaterThanOrEqual(new BigDecimal("-2.00"), new BigDecimal("-1.00"))).isEqualTo(false);
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(new BigDecimal("1.00"), new BigDecimal("1.00"))).isTrue();
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(new BigDecimal("1.00"), new BigDecimal("-1.00"))).isTrue();
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(new BigDecimal("0.00"), null)).isTrue();
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(null, new BigDecimal("0.00"))).isTrue();
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(null, null)).isTrue();
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(new BigDecimal("1.00"), null)).isTrue();
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(new BigDecimal("-1.00"), null)).isEqualTo(false);
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(null, new BigDecimal("-1.00"))).isTrue();
        assertThat(BigDecimalUtils.isGreaterThanOrEqual(null, new BigDecimal("1.00"))).isEqualTo(false);

        assertThat(BigDecimalUtils.abs(new BigDecimal("1.00"))).isEqualTo(new BigDecimal("1.00"));
        assertThat(BigDecimalUtils.abs(new BigDecimal("-1.00"))).isEqualTo(new BigDecimal("1.00"));
        assertThat(BigDecimalUtils.abs(new BigDecimal("0.00"))).isEqualTo(new BigDecimal("0.00"));
    }
}
