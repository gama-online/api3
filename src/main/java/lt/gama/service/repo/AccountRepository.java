package lt.gama.service.repo;

import lt.gama.model.sql.system.AccountSql;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountSql, String> {

    Optional<AccountSql> findByRefreshToken(String refreshToken);

}
