package lt.gama.model.mappers;

import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.sql.entities.CounterpartySql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = UtilsMapper.class, componentModel = "spring")
public interface CounterpartySqlMapper extends IBaseMapper<CounterpartyDto, CounterpartySql> {

    @Override
    @Mapping(target = "debtTypeImport", ignore = true)
    CounterpartyDto toDto(CounterpartySql entity);

    @Override
    CounterpartySql toEntity(CounterpartyDto dto);

}
