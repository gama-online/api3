package lt.gama.service.repo;

import lt.gama.model.sql.entities.RoleSql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleSql, Long> {
}
