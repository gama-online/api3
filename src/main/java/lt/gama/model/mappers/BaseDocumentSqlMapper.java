package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class BaseDocumentSqlMapper<D extends BaseDocumentDto, E extends BaseDocumentSql> implements IBaseMapper<D, E> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected CounterpartySqlMapper counterpartySqlMapper;

    @Autowired
    protected EmployeeSqlMapper employeeSqlMapper;

    @AfterMapping
    void baseAfterToEntity(D src, @MappingTarget E target) {
        if (Validators.isValid(src.getCounterparty())) {
            target.setCounterparty(entityManager.getReference(CounterpartySql.class, src.getCounterparty().getId()));
        } else {
            target.setCounterparty(null);
        }
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
        }
    }

    @AfterMapping
    void baseAfterToDto(E src, @MappingTarget D target) {
        if (Validators.isValid(src.getCounterparty())) {
            target.setCounterparty(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "counterparty")
                    ? counterpartySqlMapper.toDto(src.getCounterparty())
                    : new CounterpartyDto(src.getCounterparty().getId(), DBType.POSTGRESQL));
        } else {
            target.setCounterparty(null);
        }
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employee")
                    ? employeeSqlMapper.toDto(src.getEmployee())
                    : new EmployeeDto(src.getEmployee().getId(), DBType.POSTGRESQL));
        } else {
            target.setEmployee(null);
        }
    }
}
