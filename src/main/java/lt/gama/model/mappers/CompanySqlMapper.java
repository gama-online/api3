package lt.gama.model.mappers;

import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.sql.system.CompanySql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface CompanySqlMapper extends IBaseMapper<CompanyDto, CompanySql> {

    @Override
    @Mapping(target = "otherAccountsList", ignore = true)
    CompanyDto toDto(CompanySql entity);

    @Override
    @Mapping(target = "otherAccountsList", ignore = true)
    CompanySql toEntity(CompanyDto dto);
}
