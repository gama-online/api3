package lt.gama.test.tools;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrencyTest {

    @Test
    void testCurrency() {
        Set<Currency> currencies = Currency.getAvailableCurrencies();
        assertThat(currencies).contains(Currency.getInstance("LTL"));
        assertThat(Currency.getInstance("LTL").getDefaultFractionDigits()).isEqualTo(2);
    }
}
