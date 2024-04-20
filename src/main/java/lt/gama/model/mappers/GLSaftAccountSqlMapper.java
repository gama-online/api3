package lt.gama.model.mappers;

import lt.gama.model.dto.entities.GLSaftAccountDto;
import lt.gama.model.sql.entities.GLSaftAccountSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface GLSaftAccountSqlMapper extends IBaseMapper<GLSaftAccountDto, GLSaftAccountSql> {

    @Override
    GLSaftAccountDto toDto(GLSaftAccountSql entity);

    @Override
    GLSaftAccountSql toEntity(GLSaftAccountDto dto);
}
