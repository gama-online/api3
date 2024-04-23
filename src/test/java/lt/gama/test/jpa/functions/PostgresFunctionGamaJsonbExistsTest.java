package lt.gama.test.jpa.functions;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lt.gama.test.base.BaseDBTest;
import lt.gama.test.jpa.experimental.entities.EntityMaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

public class PostgresFunctionGamaJsonbExistsTest extends BaseDBTest {

    // gama_jsonb_exists

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        EntityMaster m1 = new EntityMaster();
        m1.setId(1L);
        m1.setLabels(Set.of("LabelA", "LabelB"));
        dbServiceSQL.executeInTransaction(entityManager -> entityManager.persist(m1));

        EntityMaster m2 = new EntityMaster();
        m2.setId(2L);
        m2.setLabels(Set.of("LabelA"));
        dbServiceSQL.executeInTransaction(entityManager -> entityManager.persist(m2));

        EntityMaster m3 = new EntityMaster();
        m3.setId(3L);
        dbServiceSQL.executeInTransaction(entityManager -> entityManager.persist(m3));
    }

    @Test
    void criteriaQueryTest() {
        {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<EntityMaster> cq = cb.createQuery(EntityMaster.class);
            Root<EntityMaster> root = cq.from(EntityMaster.class);
            cq.where(cb.isTrue(cb.function(JSONB_EXISTS, Boolean.class, root.get("labels"), cb.literal("LabelA"))));
            cq.orderBy(cb.asc(root.get("id")));

            List<EntityMaster> list = entityManager.createQuery(cq).getResultList();

            assertThat(list.size()).isEqualTo(2);
            assertThat(list.get(0).getId()).isEqualTo(1L);
            assertThat(list.get(1).getId()).isEqualTo(2L);
        }
        {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<EntityMaster> cq = cb.createQuery(EntityMaster.class);
            Root<EntityMaster> root = cq.from(EntityMaster.class);
            cq.where(cb.isTrue(cb.function(JSONB_EXISTS, Boolean.class, root.get("labels"), cb.literal("LabelB"))));
            cq.orderBy(cb.asc(root.get("id")));

            List<EntityMaster> list = entityManager.createQuery(cq).getResultList();

            assertThat(list.size()).isEqualTo(1);
            assertThat(list.get(0).getId()).isEqualTo(1L);
        }
    }


    @Test
    void jpaQueryTest() {
        List<EntityMaster> list = entityManager.createQuery(
                "SELECT a FROM " + EntityMaster.class.getName() + " a " +
                        " WHERE " + JSONB_EXISTS + "(labels, :label)" +
                        " ORDER BY a.id",
                        EntityMaster.class)
                .setParameter("label", "LabelA")
                .getResultList();

        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getId()).isEqualTo(2L);

        list = entityManager.createQuery(
                "SELECT a FROM " + EntityMaster.class.getName() + " a " +
                        " WHERE " + JSONB_EXISTS + "(labels, :label)",
                        EntityMaster.class)
                .setParameter("label", "LabelB")
                .getResultList();

        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).getId()).isEqualTo(1L);
    }
}
