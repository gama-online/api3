package lt.gama.test.tools;

import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.balance.BalanceDay;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2016-02-25.
 */
public class BalanceDayTest {

    BalanceDay day;
    Exchange exchange;

    private void init() {
        exchange = new Exchange("EUR", BigDecimal.ONE, "USD", BigDecimal.valueOf(1.333), LocalDate.of(2015, 1, 1));
        day = new BalanceDay(LocalDate.of(2015, 1, 1), exchange);
        day.setDebit(GamaMoney.parse("USD 3.00"));
        day.setCredit(GamaMoney.parse("USD 4.00"));
        day.setBaseDebit(exchange.exchange(day.getDebit()));
        day.setBaseCredit(exchange.exchange(day.getCredit()));

        assertThat(day.getBaseDebit()).isEqualTo(GamaMoney.parse("EUR 2.25"));
        assertThat(day.getBaseCredit()).isEqualTo(GamaMoney.parse("EUR 3.00"));
    }

    @Test
    public void testDayDebit() {
        init();

        day.setSums(GamaMoney.parse("USD 2.00"), exchange.exchange(GamaMoney.parse("USD 2.00")), false);
        assertThat(day.getDebit()).isEqualTo(GamaMoney.parse("USD 5.00"));
        assertThat(day.getCredit()).isEqualTo(GamaMoney.parse("USD 4.00"));
        assertThat(day.getBaseDebit()).isEqualTo(GamaMoney.parse("EUR 3.75"));
        assertThat(day.getBaseCredit()).isEqualTo(GamaMoney.parse("EUR 3.00"));
    }

    @Test
    public void testDayCredit() {
        init();

        day.setSums(GamaMoney.parse("USD -2.00"), exchange.exchange(GamaMoney.parse("USD -2.00")), false);
        assertThat(day.getDebit()).isEqualTo(GamaMoney.parse("USD 3.00"));
        assertThat(day.getCredit()).isEqualTo(GamaMoney.parse("USD 6.00"));
        assertThat(day.getBaseDebit()).isEqualTo(GamaMoney.parse("EUR 2.25"));
        assertThat(day.getBaseCredit()).isEqualTo(GamaMoney.parse("EUR 4.50"));
    }

    @Test
    public void testDayDebitRecall() {
        init();

        day.setSums(GamaMoney.parse("USD 2.00"), exchange.exchange(GamaMoney.parse("USD 2.00")), true);
        assertThat(day.getDebit()).isEqualTo(GamaMoney.parse("USD 3.00"));
        assertThat(day.getCredit()).isEqualTo(GamaMoney.parse("USD 2.00"));
        assertThat(day.getBaseDebit()).isEqualTo(GamaMoney.parse("EUR 2.25"));
        assertThat(day.getBaseCredit()).isEqualTo(GamaMoney.parse("EUR 1.50"));
    }

    @Test
    public void testDayCreditRecall() {
        init();

        day.setSums(GamaMoney.parse("USD -2.00"), exchange.exchange(GamaMoney.parse("USD -2.00")), true);
        assertThat(day.getDebit()).isEqualTo(GamaMoney.parse("USD 1.00"));
        assertThat(day.getCredit()).isEqualTo(GamaMoney.parse("USD 4.00"));
        assertThat(day.getBaseDebit()).isEqualTo(GamaMoney.parse("EUR 0.75"));
        assertThat(day.getBaseCredit()).isEqualTo(GamaMoney.parse("EUR 3.00"));
    }

    @Test
    public void testDayDebitZero() {
        init();

        day.setSums(GamaMoney.parse("USD -3.00"), exchange.exchange(GamaMoney.parse("USD -3.00")), true);
        assertThat(day.getDebit()).isNull();
        assertThat(day.getBaseDebit()).isNull();
    }

    @Test
    public void testDayCreditZero() {
        init();

        day.setSums(GamaMoney.parse("USD 4.00"), exchange.exchange(GamaMoney.parse("USD 4.00")), true);
        assertThat(day.getCredit()).isNull();
        assertThat(day.getBaseCredit()).isNull();
    }
}
