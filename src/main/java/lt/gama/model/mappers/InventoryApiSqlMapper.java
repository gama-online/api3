package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.InventoryApiDto;
import lt.gama.model.dto.documents.InventoryDto;
import lt.gama.model.dto.documents.items.PartInventoryDto;
import lt.gama.model.dto.entities.PartInventoryApiDto;
import lt.gama.model.sql.documents.InventorySql;
import lt.gama.model.sql.documents.items.InventoryPartSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.doc.DocWarehouse;
import org.mapstruct.*;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class InventoryApiSqlMapper implements IBaseMapper<InventoryApiDto, InventorySql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract InventoryApiDto toDto(InventorySql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract InventorySql toEntity(InventoryApiDto dto);

    @Mapping(target = "translation", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract InventoryDto apiDtoToDto(InventoryApiDto apiDto);

    public abstract InventoryApiDto dtoToApiDto(InventoryDto dto);

    @AfterMapping
    public void afterToDto(InventorySql src, @MappingTarget InventoryApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void afterToEntity(InventoryApiDto src, @MappingTarget InventorySql target) {
        if (target.getParts() != null) {
            target.getParts().forEach(p -> p.setParent(target));
        }
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }

    @AfterMapping
    public void afterApiDtoToDto(InventoryApiDto src, @MappingTarget InventoryDto target) {
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }

    @AfterMapping
    public void afterDtoToApiDto(InventoryDto src, @MappingTarget InventoryApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    public abstract List<PartInventoryApiDto> toDto(List<InventoryPartSql> entity);

    public abstract List<InventoryPartSql> toEntity(List<PartInventoryApiDto> dto);

    public abstract List<PartInventoryDto> setPartApiDtoToDto(List<PartInventoryApiDto> apiDto);

    public abstract List<PartInventoryApiDto> setDtoToPartApiDto(List<PartInventoryDto> dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "db", ignore = true)
    @Mapping(target = "recordId", source = "id")
    public abstract PartInventoryApiDto toDto(InventoryPartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract InventoryPartSql toEntity(PartInventoryApiDto dto);

    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "costTotal", ignore = true)
    @Mapping(target = "costInfo", ignore = true)
    @Mapping(target = "forwardSell", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract PartInventoryDto partApiDtoToDto(PartInventoryApiDto apiDto);

    public abstract PartInventoryApiDto dtoToPartApiDto(PartInventoryDto dto);


    public abstract DocWarehouse warehouseSql2docWarehouse(WarehouseSql entity);

    public abstract WarehouseSql DocWarehouse2warehouseSql(DocWarehouse dto);

    public abstract PartInventoryApiDto setPartFields(PartSql partSql);

    @ObjectFactory
    public PartInventoryApiDto createPartInventoryApiDto(InventoryPartSql inventoryPartSql) {
        return setPartFields(inventoryPartSql.getPart());
    }

    @AfterMapping
    public void afterToDto(InventoryPartSql src, @MappingTarget PartInventoryApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void afterToEntity(PartInventoryApiDto src, @MappingTarget InventoryPartSql target) {
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
    public void afterToPartApiDto(PartInventoryDto src, @MappingTarget PartInventoryApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void afterToPartDto(PartInventoryApiDto src, @MappingTarget PartInventoryDto target) {
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }
}
