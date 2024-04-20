package lt.gama.service.repo;

import lt.gama.model.sql.entities.AssetSql;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface AssetRepository extends ListPagingAndSortingRepository<AssetSql, Long> {
}
