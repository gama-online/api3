package lt.gama.test.jpa.functions;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lt.gama.test.base.BaseDBTest;
import lt.gama.test.jpa.experimental.entities.EntityMaster;
import lt.gama.test.jpa.experimental.types.TestCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_CONTAINS;
import static org.assertj.core.api.Assertions.assertThat;

public class PostgresFunctionGamaJsonbContainsTest extends BaseDBTest {

    // gama_jsonb_contains

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        EntityMaster m1 = new EntityMaster();
        m1.setId(1L);
        m1.setCustomer(new TestCustomer(1, "NameA", "AddressA"));
        dbServiceSQL.executeInTransaction(entityManager -> entityManager.persist(m1));

        EntityMaster m2 = new EntityMaster();
        m2.setId(2L);
        m2.setCustomer(new TestCustomer(2, "NameA", "AddressB"));
        dbServiceSQL.executeInTransaction(entityManager -> entityManager.persist(m2));

        EntityMaster m3 = new EntityMaster();
        m3.setId(3L);
        m3.setCustomer(new TestCustomer(3, "NameC", "AddressA"));
        dbServiceSQL.executeInTransaction(entityManager -> entityManager.persist(m3));
    }

    @Test
    void criteriaQueryTest() {
        {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<EntityMaster> cq = cb.createQuery(EntityMaster.class);
            Root<EntityMaster> root = cq.from(EntityMaster.class);
            cq.where(cb.isTrue(cb.function(JSONB_CONTAINS, Boolean.class, root.get("customer"),
                    cb.literal("{\"address\":\"AddressA\"}"))));
            cq.orderBy(cb.asc(root.get("id")));

            List<EntityMaster> list = entityManager.createQuery(cq).getResultList();

            assertThat(list.size()).isEqualTo(2);
            assertThat(list.get(0).getId()).isEqualTo(1L);
            assertThat(list.get(1).getId()).isEqualTo(3L);
        }
        {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<EntityMaster> cq = cb.createQuery(EntityMaster.class);
            Root<EntityMaster> root = cq.from(EntityMaster.class);
            cq.where(cb.isTrue(cb.function(JSONB_CONTAINS, Boolean.class, root.get("customer"),
                    cb.literal("{\"name\":\"NameA\"}"))));
            cq.orderBy(cb.asc(root.get("id")));

            List<EntityMaster> list = entityManager.createQuery(cq).getResultList();

            assertThat(list.size()).isEqualTo(2);
            assertThat(list.get(0).getId()).isEqualTo(1L);
            assertThat(list.get(1).getId()).isEqualTo(2L);
        }
    }

    @Test
    void jpaQueryTest() {
        List<EntityMaster> list = entityManager.createQuery(
                "SELECT a FROM " + EntityMaster.class.getName() + " a " +
                        " WHERE " + JSONB_CONTAINS + "(customer, '{\"address\":\"AddressA\"}') = true" +
                        " ORDER BY a.id",
                EntityMaster.class).getResultList();

        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getId()).isEqualTo(3L);

        list = entityManager.createQuery(
                "SELECT a FROM " + EntityMaster.class.getName() + " a "
                        + " WHERE " + JSONB_CONTAINS + "(customer, '{\"name\":\"NameA\"}') = true",
                EntityMaster.class).getResultList();

        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getId()).isEqualTo(2L);
    }
}
