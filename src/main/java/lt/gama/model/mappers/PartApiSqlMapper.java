package lt.gama.model.mappers;

import lt.gama.model.dto.entities.PartApiDto;
import lt.gama.model.sql.entities.PartSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface PartApiSqlMapper extends IBaseMapper<PartApiDto, PartSql> {

    @Override
    PartApiDto toDto(PartSql entity);

    @Override
    PartSql toEntity(PartApiDto dto);
}
