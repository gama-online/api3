package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.EmployeeAbsenceDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.sql.entities.EmployeeAbsenceSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class EmployeeAbsenceSqlMapper implements IBaseMapper<EmployeeAbsenceDto, EmployeeAbsenceSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private EmployeeSqlMapper employeeSqlMapper;

    @Override
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeAbsenceDto toDto(EmployeeAbsenceSql entity);

    @Override
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeAbsenceSql toEntity(EmployeeAbsenceDto dto);

    @AfterMapping
    void afterToEntity(EmployeeAbsenceDto src, @MappingTarget EmployeeAbsenceSql target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
        }
    }

    @AfterMapping
    void afterToDto(EmployeeAbsenceSql src, @MappingTarget EmployeeAbsenceDto target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employee")
                    ? employeeSqlMapper.toDto(src.getEmployee()) : new EmployeeDto(src.getEmployee().getId(), DBType.POSTGRESQL));
        } else {
            target.setEmployee(null);
        }
    }
}
