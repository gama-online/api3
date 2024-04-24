package lt.gama.test.tools;

import lt.gama.helpers.IntegerUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * gama-online
 * Created by valdas on 2017-04-01.
 */
public class IntegerUtilsTest {

    @Test
    public void testIntegerUtils() {
        assertThat(IntegerUtils.abs(null)).isNull();
        assertThat(IntegerUtils.abs(1)).isEqualTo(1);
        assertThat(IntegerUtils.abs(-2)).isEqualTo(2);

        assertThat(IntegerUtils.negated(null)).isNull();
        assertThat(IntegerUtils.negated(0)).isEqualTo(0);
        assertThat(IntegerUtils.negated(-2)).isEqualTo(2);

        assertThat(IntegerUtils.add(null, null)).isNull();
        assertThat(IntegerUtils.add(null, 1)).isEqualTo(1);
        assertThat(IntegerUtils.add(2, null)).isEqualTo(2);
        assertThat(IntegerUtils.add(2, 3)).isEqualTo(5);

        assertThat(IntegerUtils.subtract(null, null)).isNull();
        assertThat(IntegerUtils.subtract(null, 1)).isEqualTo(-1);
        assertThat(IntegerUtils.subtract(2, null)).isEqualTo(2);
        assertThat(IntegerUtils.subtract(2, 3)).isEqualTo(-1);

        assertThat(IntegerUtils.max(null, null)).isNull();
        assertThat(IntegerUtils.max(null, 1)).isEqualTo(1);
        assertThat(IntegerUtils.max(null, -1)).isNull();
        assertThat(IntegerUtils.max(2, null)).isEqualTo(2);
        assertThat(IntegerUtils.max(-2, null)).isNull();
        assertThat(IntegerUtils.max(2, 3)).isEqualTo(3);
        assertThat(IntegerUtils.max(4, 3)).isEqualTo(4);

        assertThat(IntegerUtils.min(null, null)).isNull();
        assertThat(IntegerUtils.min(null, 1)).isNull();
        assertThat(IntegerUtils.min(null, -1)).isEqualTo(-1);
        assertThat(IntegerUtils.min(2, null)).isNull();
        assertThat(IntegerUtils.min(-2, null)).isEqualTo(-2);
        assertThat(IntegerUtils.min(2, 3)).isEqualTo(2);
        assertThat(IntegerUtils.min(4, 3)).isEqualTo(3);

        assertThat(IntegerUtils.isEqual(null, null)).isTrue();
        assertThat(IntegerUtils.isEqual(null, 0)).isTrue();
        assertThat(IntegerUtils.isEqual(0, null)).isTrue();
        assertThat(IntegerUtils.isEqual(1, 1)).isTrue();

        assertThat(IntegerUtils.isGreaterThan(1, null)).isTrue();
        assertThat(IntegerUtils.isGreaterThan(null, -2)).isTrue();
        assertThat(IntegerUtils.isGreaterThan(5, -2)).isTrue();
        assertThat(IntegerUtils.isGreaterThan(null, 1)).isFalse();
        assertThat(IntegerUtils.isGreaterThan(-2, null)).isFalse();
        assertThat(IntegerUtils.isGreaterThan(-2, 5)).isFalse();

        assertThat(IntegerUtils.isGreaterThanOrEqual(null, null)).isTrue();
        assertThat(IntegerUtils.isGreaterThanOrEqual(null, 0)).isTrue();
        assertThat(IntegerUtils.isGreaterThanOrEqual(0, null)).isTrue();
        assertThat(IntegerUtils.isGreaterThanOrEqual(1, null)).isTrue();
        assertThat(IntegerUtils.isGreaterThanOrEqual(1, 0)).isTrue();
        assertThat(IntegerUtils.isGreaterThanOrEqual(1, 1)).isTrue();
        assertThat(IntegerUtils.isGreaterThanOrEqual(-1, -2)).isTrue();
        assertThat(IntegerUtils.isGreaterThanOrEqual(null, 1)).isFalse();
        assertThat(IntegerUtils.isGreaterThanOrEqual(-1, null)).isFalse();
        assertThat(IntegerUtils.isGreaterThanOrEqual(-2, -1)).isFalse();
    }
}
