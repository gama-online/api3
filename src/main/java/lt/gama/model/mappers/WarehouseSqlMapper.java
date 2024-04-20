package lt.gama.model.mappers;

import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.sql.entities.WarehouseSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface WarehouseSqlMapper extends IBaseMapper<WarehouseDto, WarehouseSql> {

    @Override
    WarehouseDto toDto(WarehouseSql entity);

    @Override
    WarehouseSql toEntity(WarehouseDto dto);
}
