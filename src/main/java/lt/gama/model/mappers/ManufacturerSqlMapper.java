package lt.gama.model.mappers;

import lt.gama.model.dto.entities.ManufacturerDto;
import lt.gama.model.sql.entities.ManufacturerSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface ManufacturerSqlMapper extends IBaseMapper<ManufacturerDto, ManufacturerSql> {

    @Override
    ManufacturerDto toDto(ManufacturerSql entity);

    @Override
    ManufacturerSql toEntity(ManufacturerDto dto);
}
