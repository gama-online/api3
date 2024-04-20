package lt.gama.model.mappers;

import lt.gama.model.dto.entities.PositionDto;
import lt.gama.model.sql.entities.PositionSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface PositionSqlMapper extends IBaseMapper<PositionDto, PositionSql> {

    @Override
    PositionDto toDto(PositionSql entity);

    @Override
    PositionSql toEntity(PositionDto dto);
}
