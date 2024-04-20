package lt.gama.model.mappers;

import lt.gama.model.dto.entities.RoleDto;
import lt.gama.model.sql.entities.RoleSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface RoleSqlMapper extends IBaseMapper<RoleDto, RoleSql> {

    @Override
    RoleDto toDto(RoleSql entity);

    @Override
    RoleSql toEntity(RoleDto dto);
}
