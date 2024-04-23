package lt.gama.model.mappers;

import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.dto.entities.PartPartDto;
import lt.gama.model.sql.entities.PartPartSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.service.repo.PartRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, CounterpartySqlMapper.class, ManufacturerSqlMapper.class}, componentModel = "spring")
public abstract class PartSqlMapper implements IBaseMapper<PartDto, PartSql> {

    @Autowired
    protected PartRepository partRespository;

    @Override
    @Mapping(target = "remainders", ignore = true)
    public abstract PartDto toDto(PartSql entity);

    @Override
    @Mapping(target = "remainders", ignore = true)
    public abstract PartSql toEntity(PartDto dto);

    @AfterMapping
    void afterToEntity(PartDto src, @MappingTarget PartSql target) {
        if (target.getParts() != null) {
            target.getParts().forEach(p -> p.setParent(target));
        }
    }

    public abstract List<PartPartDto> listPartPartSqlToDto(List<PartPartSql> entity);

    public abstract List<PartPartSql> listPartPartDtoToEntity(List<PartPartDto> dto);

    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sn", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    public abstract PartPartDto partPartToDto(PartPartSql entity);

    @Named("setPartFields")
    @Mapping(target = "foreignId", ignore = true)
    @Mapping(target = "labels", ignore = true)
    @Mapping(target = "db", ignore = true)
    public abstract PartPartDto setPartFields(PartSql partSql);

    @ObjectFactory
    public PartPartDto createPartPartDto(PartPartSql partPartSql) {
        return setPartFields(partPartSql.getPart());
    }

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "id", source = "recordId")
    public abstract PartPartSql partPartToEntity(PartPartDto dto);

    @AfterMapping
    void afterToEntity(PartPartDto src, @MappingTarget PartPartSql target) {
        if (src.getId() != null) {
            target.setPart(partRespository.getReferenceById(src.getId()));
        }
    }

}
