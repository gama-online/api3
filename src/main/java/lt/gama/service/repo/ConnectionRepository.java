package lt.gama.service.repo;

import lt.gama.model.sql.system.ConnectionSql;
import org.springframework.data.repository.CrudRepository;

public interface ConnectionRepository extends CrudRepository<ConnectionSql, Long> {
}
