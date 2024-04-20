package lt.gama.test.jpa.experimental;

import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.test.base.BaseDBTest;
import lt.gama.test.jpa.experimental.entities.EntityMoney;
import lt.gama.test.jpa.experimental.types.TestCostMoney;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MoneyCustomTypeTest extends BaseDBTest {

    @Test
    void testMoneyCustomType() {
        EntityMoney entity = new EntityMoney();
        entity.setId(123L);
        entity.setAmount(GamaMoney.parse("EUR 987654321.12"));
        entity.setBig(GamaBigMoney.parse("EUR 987654321.123456"));

        entity.setMoney(GamaMoney.parse("USD 123456789.12"));
        entity.setCost(new TestCostMoney(new BigDecimal("123.456"), GamaMoney.parse("EUR 456.78")));
        entity.setRemainders(Arrays.asList(
                GamaMoney.parse("USD 123456789.12"),
                GamaMoney.parse("EUR 987654321.12")));

        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
        clearCaches();

        EntityMoney e = entityManager.find(EntityMoney.class, 123L);
        clearCaches();

        assertThat(e.getAmount()).isEqualTo(GamaMoney.parse("EUR 987654321.12"));
        assertThat(e.getBig()).isEqualTo(GamaBigMoney.parse("EUR 987654321.123456"));
        assertThat(e.getMoney()).isEqualTo(GamaMoney.parse("USD 123456789.12"));
        assertThat(e.getCost()).isEqualTo(new TestCostMoney(new BigDecimal("123.456"), GamaMoney.parse("EUR 456.78")));
        assertThat(e.getRemainders()).containsExactly(GamaMoney.parse("USD 123456789.12"), GamaMoney.parse("EUR 987654321.12"));
    }

    @Test
    void testMoneyCustomType2() {
        EntityMoney entity = new EntityMoney();
        entity.setId(123L);
        entity.setAmount(GamaMoney.parse("EUR 123"));
        entity.setBig(GamaBigMoney.parse("EUR 1.123"));

        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();

        assertThat(entity.getId()).isNotNull();
        clearCaches();

        EntityMoney e = entityManager.find(EntityMoney.class, 123L);
        clearCaches();

        assertThat(e.getAmount()).isEqualTo(GamaMoney.parse("EUR 123.00"));
        assertThat(e.getBig()).isEqualTo(GamaBigMoney.parse("EUR 1.123"));
    }

    @Test
    void testCurrency() {
        Set<Currency> currencies = Currency.getAvailableCurrencies();
        assertThat(currencies).contains(Currency.getInstance("LTL"));
        assertThat(Currency.getInstance("LTL").getDefaultFractionDigits()).isEqualTo(2);
    }
}
