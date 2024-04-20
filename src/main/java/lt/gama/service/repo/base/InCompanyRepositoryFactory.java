package lt.gama.service.repo.base;

import jakarta.persistence.EntityManager;
import lt.gama.auth.impl.Auth;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;

public class InCompanyRepositoryFactory extends JpaRepositoryFactory {

    private EntityManager entityManager;
    private Auth auth;

    /**
     * Creates a new {@link JpaRepositoryFactory}.
     *
     * @param entityManager must not be {@literal null}
     */
    public InCompanyRepositoryFactory(EntityManager entityManager, Auth auth) {
        super(entityManager);
        this.entityManager = entityManager;
        this.auth = auth;
    }

    @Override
    protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
        return new InCompanyRepository<>(getEntityInformation(information.getDomainType()), entityManager, auth);
    }
}
