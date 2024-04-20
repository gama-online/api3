package lt.gama.model.mappers;

import lt.gama.model.dto.documents.GLOpeningBalanceDto;
import lt.gama.model.dto.documents.items.GLOpeningBalanceOperationDto;
import lt.gama.model.sql.documents.GLOpeningBalanceSql;
import lt.gama.model.sql.documents.items.GLOpeningBalanceOperationSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface GLOpeningBalanceSqlMapper extends IBaseMapper<GLOpeningBalanceDto, GLOpeningBalanceSql> {

    @Override
    @Mapping(target = "IBalance", ignore = true)
    GLOpeningBalanceDto toDto(GLOpeningBalanceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "exchange", ignore = true)
    @Mapping(target = "doubleEntry", ignore = true)
    @Mapping(target = "recallable", ignore = true)
    @Mapping(target = "fs", ignore = true)
    @Mapping(target = "urls", ignore = true)
    GLOpeningBalanceSql toEntity(GLOpeningBalanceDto dto);

    @AfterMapping
    default void afterToEntity(GLOpeningBalanceDto src, @MappingTarget GLOpeningBalanceSql target) {
        if (target.getBalances() != null) {
            target.getBalances().forEach(op -> op.setGlOpeningBalance(target));
        }
    }

    List<GLOpeningBalanceOperationDto> toOperationsListDto(List<GLOpeningBalanceOperationSql> dto);

    List<GLOpeningBalanceOperationSql> toOperationsListEntity(List<GLOpeningBalanceOperationDto> dto);

    GLOpeningBalanceOperationDto toOperationDto(GLOpeningBalanceOperationSql entity);

    @Mapping(target = "sortNr", ignore = true)
    @Mapping(target = "glOpeningBalance", ignore = true)
    GLOpeningBalanceOperationSql toOperationEntity(GLOpeningBalanceOperationDto dto);
}
