package lt.gama.model.mappers;

import lt.gama.model.dto.entities.WorkScheduleDto;
import lt.gama.model.sql.entities.WorkScheduleSql;
import org.mapstruct.Mapper;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface WorkScheduleSqlMapper extends IBaseMapper<WorkScheduleDto, WorkScheduleSql> {

    @Override
    WorkScheduleDto toDto(WorkScheduleSql entity);

    @Override
    WorkScheduleSql toEntity(WorkScheduleDto dto);
}
