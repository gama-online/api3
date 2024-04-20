package lt.gama.service.repo;

import lt.gama.model.sql.entities.CounterSql;
import lt.gama.model.sql.entities.id.CounterId;
import org.springframework.data.repository.CrudRepository;

public interface CounterRepository extends CrudRepository<CounterSql, CounterId> {
}
