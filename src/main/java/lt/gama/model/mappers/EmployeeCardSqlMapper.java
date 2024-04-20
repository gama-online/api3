package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.EmployeeCardDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.sql.entities.EmployeeCardSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.doc.DocPosition;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.salary.EmployeeTaxSettings;
import lt.gama.model.type.salary.SalaryPerMonth;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class EmployeeCardSqlMapper implements IBaseMapper<EmployeeCardDto, EmployeeCardSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private EmployeeSqlMapper employeeSqlMapper;


    @Override
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeCardDto toDto(EmployeeCardSql entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeCardSql toEntity(EmployeeCardDto dto);

    @AfterMapping
    void afterToEntity(EmployeeCardDto src, @MappingTarget EmployeeCardSql target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
        }
    }

    @AfterMapping
    void afterToDto(EmployeeCardSql src, @MappingTarget EmployeeCardDto target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employee")
                    ? employeeSqlMapper.toDto(src.getEmployee()) : new EmployeeDto(src.getEmployee().getId(), DBType.POSTGRESQL));
        } else {
            target.setEmployee(null);
        }
    }

    // List<EmployeeTaxSettings>
    public abstract List<EmployeeTaxSettings> cloneEmployeeTaxSettings(List<EmployeeTaxSettings> src);

    public abstract EmployeeTaxSettings cloneEmployeeTaxSettings(EmployeeTaxSettings src);

    // List<DocPosition>
    public abstract List<DocPosition> cloneDocPosition(List<DocPosition> src);

    public abstract DocPosition cloneDocPosition(DocPosition src);

    // List<SalaryPerMonth>
    public abstract List<SalaryPerMonth> cloneSalaryPerMonth(List<SalaryPerMonth> src);

    public abstract SalaryPerMonth cloneSalaryPerMonth(SalaryPerMonth src);
}
