package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.EmployeeCardDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.WorkHoursDto;
import lt.gama.model.sql.entities.EmployeeCardSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.WorkHoursSql;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.salary.WorkHoursPosition;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class WorkHoursSqlMapper implements IBaseMapper<WorkHoursDto, WorkHoursSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private EmployeeSqlMapper employeeSqlMapper;

    @Autowired
    private EmployeeCardSqlMapper employeeCardSqlMapper;


    @Override
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "employeeCard", ignore = true)
    public abstract WorkHoursDto toDto(WorkHoursSql entity);

    @Override
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "employeeCard", ignore = true)
    public abstract WorkHoursSql toEntity(WorkHoursDto dto);

    @AfterMapping
    void afterToEntity(WorkHoursDto src, @MappingTarget WorkHoursSql target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
            target.setEmployeeCard(entityManager.getReference(EmployeeCardSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
            target.setEmployeeCard(null);
        }
    }

    @AfterMapping
    void afterToDto(WorkHoursSql src, @MappingTarget WorkHoursDto target) {
        target.setEmployee(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employee")
                ? employeeSqlMapper.toDto(src.getEmployee()) : new EmployeeDto(src.getEmployee().getId(), DBType.POSTGRESQL));

        target.setEmployeeCard(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employeeCard")
                ? employeeCardSqlMapper.toDto(src.getEmployeeCard()) : new EmployeeCardDto());
    }

    public abstract List<WorkHoursPosition> clone(List<WorkHoursPosition> src);

    public abstract WorkHoursPosition clone(WorkHoursPosition src);
}
