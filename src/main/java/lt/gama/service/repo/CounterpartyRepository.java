package lt.gama.service.repo;

import lt.gama.model.sql.entities.CounterpartySql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterpartyRepository extends JpaRepository<CounterpartySql, Long> {
}
