package lt.gama.model.mappers;

import lt.gama.model.dto.entities.ChargeDto;
import lt.gama.model.sql.entities.ChargeSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface ChargeSqlMapper extends IBaseMapper<ChargeDto, ChargeSql> {

    @Override
    ChargeDto toDto(ChargeSql entity);

    @Override
    ChargeSql toEntity(ChargeDto dto);
}
