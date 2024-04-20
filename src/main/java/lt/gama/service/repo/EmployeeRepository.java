package lt.gama.service.repo;

import lt.gama.model.sql.entities.EmployeeSql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeSql, Long> {
}
