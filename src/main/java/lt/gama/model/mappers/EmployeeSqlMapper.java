package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.RoleSql;
import lt.gama.model.type.auth.EmployeeRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class EmployeeSqlMapper implements IBaseMapper<EmployeeDto, EmployeeSql> {

    @Autowired
    protected EntityManager entityManager;

    @Override
    @Mapping(target = "remainders", ignore = true)
    @Mapping(target = "unionPermissions", ignore = true)
    public abstract EmployeeDto toDto(EmployeeSql entity);

    @Override
    @Mapping(target = "remainders", ignore = true)
    @Mapping(target = "unionPermissions", ignore = true)
    public abstract EmployeeSql toEntity(EmployeeDto dto);

    public abstract Set<EmployeeRole> toDto(Set<RoleSql> entity);

    public abstract Set<RoleSql> toEntity(Set<EmployeeRole> dto);

    public abstract EmployeeRole toDto(RoleSql entity);

    public RoleSql toEntity(EmployeeRole dto) {
        return entityManager.getReference(RoleSql.class, dto.getId());
    }
}
