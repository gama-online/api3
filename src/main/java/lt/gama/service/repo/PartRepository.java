package lt.gama.service.repo;

import lt.gama.model.sql.entities.PartSql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<PartSql, Long> {
}
