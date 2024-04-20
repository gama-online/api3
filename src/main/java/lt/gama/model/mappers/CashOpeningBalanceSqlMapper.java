package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.CashOpeningBalanceDto;
import lt.gama.model.dto.documents.items.CashBalanceDto;
import lt.gama.model.sql.documents.CashOpeningBalanceSql;
import lt.gama.model.sql.documents.items.CashOpeningBalanceCashSql;
import lt.gama.model.sql.entities.CashSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, CashSqlMapper.class}, componentModel = "spring")
public abstract class CashOpeningBalanceSqlMapper implements IBaseMapper<CashOpeningBalanceDto, CashOpeningBalanceSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract CashOpeningBalanceDto toDto(CashOpeningBalanceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract CashOpeningBalanceSql toEntity(CashOpeningBalanceDto dto);

    @AfterMapping
    public void afterToEntity(CashOpeningBalanceDto src, @MappingTarget CashOpeningBalanceSql target) {
        if (target.getCashes() != null) {
            target.getCashes().forEach(cp -> cp.setParent(target));
        }
    }

    public abstract List<CashBalanceDto> toBalanceDtoList(List<CashOpeningBalanceCashSql> entity);

    public abstract List<CashOpeningBalanceCashSql> toBalanceEntityList(List<CashBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    public abstract CashBalanceDto toBalanceDto(CashOpeningBalanceCashSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "cash", ignore = true)
    public abstract CashOpeningBalanceCashSql toBalanceEntity(CashBalanceDto dto);

    @AfterMapping
    public void afterToEntity(CashBalanceDto src, @MappingTarget CashOpeningBalanceCashSql target) {
        if (Validators.isValid(src.getCash())) {
            target.setCash(entityManager.getReference(CashSql.class, src.getCash().getId()));
        } else {
            target.setCash(null);
        }
    }
}
