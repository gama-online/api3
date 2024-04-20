package lt.gama.model.mappers;

import lt.gama.model.dto.entities.InventoryNowDto;
import lt.gama.model.sql.entities.InventoryNowSql;
import lt.gama.model.type.inventory.InventoryQ;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, CounterpartySqlMapper.class, PartSqlMapper.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class InventoryNowSqlMapper implements IBaseMapper<InventoryNowDto, InventoryNowSql> {

    @Override
    public abstract InventoryNowDto toDto(InventoryNowSql entity);

    @Override
    public abstract InventoryNowSql toEntity(InventoryNowDto dto);

    public abstract List<InventoryQ> clone(List<InventoryQ> src);

    public abstract InventoryQ clone(InventoryQ src);
}
