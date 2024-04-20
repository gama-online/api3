package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.entities.GLAccountDto;
import lt.gama.model.sql.entities.GLAccountSql;
import lt.gama.model.sql.entities.GLSaftAccountSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(uses = {UtilsMapper.class, GLSaftAccountSqlMapper.class}, componentModel = "spring")
public abstract class GLAccountSqlMapper implements IBaseMapper<GLAccountDto, GLAccountSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    abstract public GLAccountDto toDto(GLAccountSql entity);

    @Override
    @Mapping(target = "saftAccount", ignore = true)
    abstract public GLAccountSql toEntity(GLAccountDto dto);

    @AfterMapping
    void afterToEntity(GLAccountDto src, @MappingTarget GLAccountSql target) {
        if (src.getSaftAccount() != null && StringHelper.hasValue(src.getSaftAccount().getNumber())) {
            target.setSaftAccount(entityManager.getReference(GLSaftAccountSql.class, src.getSaftAccount().getNumber()));
        } else {
            target.setSaftAccount(null);
        }
    }
}
