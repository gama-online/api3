package lt.gama.service.repo.base;

import jakarta.persistence.EntityManager;
import lt.gama.auth.impl.Auth;
import lt.gama.model.i.ICompany;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class InCompanyRepository<T, ID> extends SimpleJpaRepository<T, ID> {

    private final Auth auth;

    public InCompanyRepository(JpaEntityInformation entityInformation, EntityManager entityManager, Auth auth) {
        super(entityInformation, entityManager);
        this.auth = auth;
    }

    @Transactional
    @Override
    public <S extends T> S save(S entity) {
        if (entity instanceof ICompany e) e.setCompanyId(auth.getCompanyId());
        return super.save(entity);
    }
}
