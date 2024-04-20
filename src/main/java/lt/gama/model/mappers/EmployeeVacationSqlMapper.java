package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.EmployeeVacationDto;
import lt.gama.model.sql.entities.EmployeeCardSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.EmployeeVacationSql;
import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.type.salary.EmployeeCardInfo;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class EmployeeVacationSqlMapper implements IBaseMapper<EmployeeVacationDto, EmployeeVacationSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "employeeCard", ignore = true)
    public abstract EmployeeVacationDto toDto(EmployeeVacationSql entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "employeeCard", ignore = true)
    public abstract EmployeeVacationSql toEntity(EmployeeVacationDto dto);

    @AfterMapping
    void afterToEntity(EmployeeVacationDto src, @MappingTarget EmployeeVacationSql target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
            target.setEmployeeCard(entityManager.getReference(EmployeeCardSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
            target.setEmployeeCard(null);
        }
    }

    @AfterMapping
    void afterToDto(EmployeeVacationSql src, @MappingTarget EmployeeVacationDto target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employee")
                    ? new DocEmployee(src.getEmployee()) : new DocEmployee(src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
        }
        if (Validators.isValid(src.getEmployeeCard())) {
            target.setEmployeeCard(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employeeCard")
                    ? new EmployeeCardInfo(src.getEmployeeCard()) : new EmployeeCardInfo());
        } else {
            target.setEmployeeCard(null);
        }
    }
}
