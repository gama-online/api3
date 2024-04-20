package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.TransProdDto;
import lt.gama.model.dto.documents.items.PartFromDto;
import lt.gama.model.dto.documents.items.PartToDto;
import lt.gama.model.sql.documents.TransProdSql;
import lt.gama.model.sql.documents.items.TransProdPartFromSql;
import lt.gama.model.sql.documents.items.TransProdPartToSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Stream;

@Mapper(uses = {UtilsMapper.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class TransProdSqlMapper implements IBaseMapper<TransProdDto, TransProdSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract TransProdDto toDto(TransProdSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract TransProdSql toEntity(TransProdDto dto);

    @AfterMapping
    public void afterToEntity(TransProdDto src, @MappingTarget TransProdSql target) {
        if (target.getPartsFrom() != null) {
            target.getPartsFrom().forEach(p -> p.setParent(target));
        }
        if (target.getPartsTo() != null) {
            target.getPartsTo().forEach(p -> p.setParent(target));
        }
        target.setParts(Stream.concat(
                CollectionsHelper.streamOf(target.getPartsFrom()),
                CollectionsHelper.streamOf(target.getPartsTo())
        ).toList());
    }

    public abstract List<PartFromDto> setPartFrom2Dto(List<TransProdPartFromSql> entity);

    public abstract List<TransProdPartFromSql> setPartFrom2Entity(List<PartFromDto> dto);

    public abstract List<PartToDto> setPartTo2Dto(List<TransProdPartToSql> entity);

    public abstract List<TransProdPartToSql> setPartTo2Entity(List<PartToDto> dto);


    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartFromDto transProdPartFrom2Dto(TransProdPartFromSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract TransProdPartFromSql partFrom2Entity(PartFromDto dto);

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartToDto transProdPartTo2Dto(TransProdPartToSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract TransProdPartToSql partTo2Entity(PartToDto dto);


    @AfterMapping
    public void afterToEntityFrom(PartFromDto src, @MappingTarget TransProdPartFromSql target) {
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
    public void afterToEntityTo(PartToDto src, @MappingTarget TransProdPartToSql target) {
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
