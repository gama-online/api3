package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.EmployeeCardDto;
import lt.gama.model.dto.entities.EmployeeChargeDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.sql.documents.SalarySql;
import lt.gama.model.sql.documents.items.EmployeeChargeSql;
import lt.gama.model.sql.entities.EmployeeCardSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.auth.CompanySalarySettings;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CompanyTaxSettings;
import lt.gama.model.type.doc.DocChargeAmount;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.salary.EmployeeTaxSettings;
import lt.gama.service.SalarySettingsService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class EmployeeChargeSqlMapper implements IBaseMapper<EmployeeChargeDto, EmployeeChargeSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private Auth auth;

    @Autowired
    private SalarySettingsService salarySettingsService;

    @Autowired
    private EmployeeSqlMapper employeeSqlMapper;

    @Autowired
    private EmployeeCardSqlMapper employeeCardSqlMapper;


    @Override
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "employeeCard", ignore = true)
    @Mapping(target = "employeeTaxSettings", ignore = true)
    @Mapping(target = "companyTaxSettings", ignore = true)
    public abstract EmployeeChargeDto toDto(EmployeeChargeSql entity);

    @Override
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "employeeCard", ignore = true)
    @Mapping(target = "salary", ignore = true)
    public abstract EmployeeChargeSql toEntity(EmployeeChargeDto dto);

    @AfterMapping
    void afterToEntity(EmployeeChargeDto src, @MappingTarget EmployeeChargeSql target) {
        if (src.getParentId() != null) {
            target.setSalary(entityManager.getReference(SalarySql.class, src.getParentId()));
        } else {
            target.setSalary(null);
        }
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
            target.setEmployeeCard(entityManager.getReference(EmployeeCardSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
            target.setEmployeeCard(null);
        }
    }

    @AfterMapping
    void afterToDto(EmployeeChargeSql src, @MappingTarget EmployeeChargeDto target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employee")
                    ? employeeSqlMapper.toDto(src.getEmployee()) : new EmployeeDto(src.getEmployee().getId(), DBType.POSTGRESQL));
        } else {
            target.setEmployee(null);
        }
        if (Validators.isValid(src.getEmployeeCard())) {
            target.setEmployeeCard(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employeeCard")
                    ? employeeCardSqlMapper.toDto(src.getEmployeeCard()) : new EmployeeCardDto());

            CompanySettings companySettings = auth.getSettings();
            CompanySalarySettings companySalarySettings = salarySettingsService.getCompanySalarySettings(target.getDate());
            CompanyTaxSettings companyTaxSettings = salarySettingsService.getCompanyTaxSettings(target.getDate());
            EmployeeTaxSettings employeeTaxSettings = salarySettingsService.generateEmployeeTaxSettings(companySettings,
                    companyTaxSettings, companySalarySettings, target.getEmployeeCard(), target.getDate());

            target.setCompanyTaxSettings(companyTaxSettings);
            target.setEmployeeTaxSettings(employeeTaxSettings);
        } else {
            target.setEmployeeCard(null);
        }
        target.setParentId(src.getSalary().getId());
    }

    public abstract List<DocChargeAmount> clone(List<DocChargeAmount> src);

    public abstract DocChargeAmount clone(DocChargeAmount src);

}
