package lt.gama.model.mappers;

import lt.gama.model.dto.entities.LabelDto;
import lt.gama.model.sql.entities.LabelSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface LabelSqlMapper extends IBaseMapper<LabelDto, LabelSql> {

    @Override
    LabelDto toDto(LabelSql entity);

    @Override
    LabelSql toEntity(LabelDto dto);
}
