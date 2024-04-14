package lt.gama.service.repo;

import lt.gama.model.sql.system.CompanySql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanySql, Long> {
}
