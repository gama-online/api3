package lt.gama.model.mappers;

import lt.gama.model.dto.system.CalendarSettingsDto;
import lt.gama.model.sql.system.CalendarSettingsSql;
import lt.gama.model.sql.system.id.CalendarId;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface CalendarSettingsSqlMapper extends IBaseMapper<CalendarSettingsDto, CalendarSettingsSql> {

    @Override
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "year", ignore = true)
    CalendarSettingsDto toDto(CalendarSettingsSql entity);

    @Override
    @Mapping(target = "id", ignore = true)
    CalendarSettingsSql toEntity(CalendarSettingsDto dto);

    @AfterMapping
    default void afterToEntity(CalendarSettingsDto src, @MappingTarget CalendarSettingsSql target) {
        target.setId(new CalendarId(src.getCountry(), src.getYear()));
    }

    @AfterMapping
    default void afterToDto(CalendarSettingsSql src, @MappingTarget CalendarSettingsDto target) {
        target.setCountry(src.getId().getCountry());
        target.setYear(src.getId().getYear());
    }

}
