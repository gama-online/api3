package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.items.PartFromDto;
import lt.gama.model.dto.documents.items.PartToDto;
import lt.gama.model.dto.entities.RecipeDto;
import lt.gama.model.sql.entities.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Stream;

@Mapper(uses = {UtilsMapper.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class RecipeSqlMapper implements IBaseMapper<RecipeDto, RecipeSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    public abstract RecipeDto toDto(RecipeSql entity);

    @Override
    public abstract RecipeSql toEntity(RecipeDto dto);

    @AfterMapping
    public void afterToEntity(RecipeDto src, @MappingTarget RecipeSql target) {
        if (target.getPartsFrom() != null) {
            target.getPartsFrom().forEach(p -> p.setParent(target));
        }
        if (target.getPartsTo() != null) {
            target.getPartsTo().forEach(p -> p.setParent(target));
        }
        target.setParts(Stream.concat(
                (Stream<? extends RecipePartSql>) (CollectionsHelper.streamOf(target.getPartsFrom())),
                CollectionsHelper.streamOf(target.getPartsTo())
        ).toList());
    }

    public abstract List<PartFromDto> setPartFrom2Dto(List<RecipePartFromSql> entity);

    public abstract List<RecipePartFromSql> setPartFrom2Entity(List<PartFromDto> dto);

    public abstract List<PartToDto> setPartTo2Dto(List<RecipePartToSql> entity);

    public abstract List<RecipePartToSql> setPartTo2Entity(List<PartToDto> dto);


    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartFromDto recipePartFrom2Dto(RecipePartFromSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract RecipePartFromSql partFrom2Entity(PartFromDto dto);

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartToDto recipePartTo2Dto(RecipePartToSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract RecipePartToSql partTo2Entity(PartToDto dto);


    @AfterMapping
    public void afterToEntityFrom(PartFromDto src, @MappingTarget RecipePartFromSql target) {
        if (Validators.isValid(src)) {
            target.setPart(entityManager.getReference(PartSql.class, src.getId()));
        } else {
            target.setPart(null);
        }
        if (Validators.isValid(src.getWarehouse())) {
            target.setWarehouse(entityManager.getReference(WarehouseSql.class, src.getWarehouse().getId()));
        } else {
            target.setWarehouse(null);
        }
    }

    @AfterMapping
    public void afterToEntityTo(PartToDto src, @MappingTarget RecipePartToSql target) {
        if (Validators.isValid(src)) {
            target.setPart(entityManager.getReference(PartSql.class, src.getId()));
        } else {
            target.setPart(null);
        }
        if (Validators.isValid(src.getWarehouse())) {
            target.setWarehouse(entityManager.getReference(WarehouseSql.class, src.getWarehouse().getId()));
        } else {
            target.setWarehouse(null);
        }
    }
}
