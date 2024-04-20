package lt.gama.service.repo;

import lt.gama.model.sql.documents.DoubleEntrySql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoubleEntryRepository extends JpaRepository<DoubleEntrySql, Long> {
}
