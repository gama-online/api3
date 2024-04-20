package lt.gama.model.mappers;

import lt.gama.model.dto.entities.InventoryHistoryDto;
import lt.gama.model.sql.entities.InventoryHistorySql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class, CounterpartySqlMapper.class, PartSqlMapper.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class InventoryHistorySqlMapper implements IBaseMapper<InventoryHistoryDto, InventoryHistorySql> {

    @Override
    public abstract InventoryHistoryDto toDto(InventoryHistorySql entity);

    @Override
    public abstract InventoryHistorySql toEntity(InventoryHistoryDto dto);
}
