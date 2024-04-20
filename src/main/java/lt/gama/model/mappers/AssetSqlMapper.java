package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(uses = UtilsMapper.class, componentModel = "spring")
public abstract class AssetSqlMapper implements IBaseMapper<AssetDto, AssetSql> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "autoCode", ignore = true)
    @Mapping(target = "perPeriod", ignore = true)
    public abstract AssetDto toDto(AssetSql entity);

    @Override
    @Mapping(target = "responsible", ignore = true)
    public abstract AssetSql toEntity(AssetDto dto);

    @AfterMapping
    void afterToEntity(AssetDto src, @MappingTarget AssetSql target) {
        if (Validators.isValid(src.getResponsible())) {
            target.setResponsible(entityManager.getReference(EmployeeSql.class, src.getResponsible().getId()));
        } else {
            target.setResponsible(null);
        }
    }

    @AfterMapping
    void afterToDto(AssetSql src, @MappingTarget AssetDto target) {
        if (Validators.isValid(src.getResponsible())) {
            target.setResponsible(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "responsible")
                    ? new EmployeeDto(src.getResponsible())
                    : new EmployeeDto(src.getResponsible().getId(), DBType.POSTGRESQL));
        } else {
            target.setResponsible(null);
        }
    }
}
