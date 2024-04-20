package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.ResponsibilityCenterDto;
import lt.gama.model.sql.entities.ResponsibilityCenterSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class ResponsibilityCenterSqlMapper implements IBaseMapper<ResponsibilityCenterDto, ResponsibilityCenterSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public abstract ResponsibilityCenterDto toDto(ResponsibilityCenterSql entity);

    @Override
    public abstract ResponsibilityCenterSql toEntity(ResponsibilityCenterDto dto);

    @AfterMapping
    void afterToEntity(ResponsibilityCenterDto src, @MappingTarget ResponsibilityCenterSql target) {
        if (Validators.isValid(src.getParent())) {
            target.setParent(entityManager.getReference(ResponsibilityCenterSql.class, src.getParent().getId()));
        } else {
            target.setParent(null);
        }
    }

    @AfterMapping
    void afterToDto(ResponsibilityCenterSql src, @MappingTarget ResponsibilityCenterDto target) {
        if (Validators.isValid(src.getParent())) {
            target.setParent(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "parent")
                    ? toDto(src.getParent()) : new ResponsibilityCenterDto(src.getParent().getId()));
        } else {
            target.setParent(null);
        }
    }
}
