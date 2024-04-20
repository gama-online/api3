package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.i.IDebtDto;
import lt.gama.model.i.IDebtSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;


public abstract class DebtSqlMapper<D extends IDebtDto, E extends IDebtSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @AfterMapping
    void afterToEntity(D src, @MappingTarget E target) {
        if (Validators.isValid(src.getCounterparty())) {
            target.setCounterparty(entityManager.getReference(CounterpartySql.class, src.getCounterparty().getId()));
        } else {
            target.setCounterparty(null);
        }
    }

    @AfterMapping
    void afterToDto(E src, @MappingTarget D target) {
        if (Validators.isValid(src.getCounterparty())) {
            target.setCounterparty(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "counterparty")
                    ? new CounterpartyDto(src.getCounterparty())
                    : new CounterpartyDto(src.getCounterparty().getId(), DBType.POSTGRESQL));
        } else {
            target.setCounterparty(null);
        }
    }
}
