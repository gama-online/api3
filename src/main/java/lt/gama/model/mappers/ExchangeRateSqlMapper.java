package lt.gama.model.mappers;

import lt.gama.model.dto.system.ExchangeRateDto;
import lt.gama.model.sql.system.ExchangeRateSql;
import lt.gama.model.sql.system.id.ExchangeRateId;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface ExchangeRateSqlMapper extends IBaseMapper<ExchangeRateDto, ExchangeRateSql> {

    @Override
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "date", ignore = true)
    ExchangeRateDto toDto(ExchangeRateSql entity);

    @Override
    @Mapping(target = "id", ignore = true)
    ExchangeRateSql toEntity(ExchangeRateDto dto);

    @AfterMapping
    default void afterToEntity(ExchangeRateDto src, @MappingTarget ExchangeRateSql target) {
        target.setId(new ExchangeRateId(src.getType(), src.getCurrency(), src.getDate()));
    }

    @AfterMapping
    default void afterToDto(ExchangeRateSql src, @MappingTarget ExchangeRateDto target) {
        target.setType(src.getId().getType());
        target.setCurrency(src.getId().getCurrency());
        target.setDate(src.getId().getDate());
    }

}
