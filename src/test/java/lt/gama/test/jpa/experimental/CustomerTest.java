package lt.gama.test.jpa.experimental;

import lt.gama.model.type.GamaMoney;
import lt.gama.test.base.BaseDBTest;
import lt.gama.test.jpa.experimental.entities.EntityMaster;
import lt.gama.test.jpa.experimental.types.TestCustomer;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CustomerTest extends BaseDBTest {

    @Test
    void customerJsonTest() throws JSONException {
        var entity = new EntityMaster();
        entity.setId(123L);
        entity.setName("Master 1");
        entity.setMoney(GamaMoney.parse("USD 123456789.12"));
        entity.setCustomer(new TestCustomer(222, "Name", "Address", LocalDate.parse("2019-12-31")));

        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
        clearCaches();

        var entityDb = entityManager.find(EntityMaster.class, 123L);

        assertThat(entityDb.getName()).isEqualTo("Master 1");
        assertThat(entityDb.getMoney()).isEqualTo(GamaMoney.parse("USD 123456789.12"));
        assertThat(entityDb.getCustomer().getName()).isEqualTo("Name");
        assertThat(entityDb.getCustomer().getAddress()).isEqualTo("Address");
        assertThat(entityDb.getCustomer().getDate()).isEqualTo(LocalDate.parse("2019-12-31"));

        @SuppressWarnings("unchecked")
        List<String> customers = entityManager.createNativeQuery("SELECT CAST(customer AS text) FROM " +
                        '"' + user.username() + '"' + ".entity_master WHERE id=123").getResultList();
        assertThat(customers.size()).isEqualTo(1);
        JSONAssert.assertEquals("{\"id\":222,\"name\":\"Name\",\"address\":\"Address\",\"date\":\"2019-12-31\"}",
                customers.getFirst(), true);
    }
}
