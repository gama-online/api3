package lt.gama.model.mappers;

import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.sql.entities.CashSql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface CashSqlMapper extends IBaseMapper<CashDto, CashSql> {

    @Override
    @Mapping(target = "remainders", ignore = true)
    CashDto toDto(CashSql entity);

    @Override
    @Mapping(target = "remainders", ignore = true)
    CashSql toEntity(CashDto dto);
}
