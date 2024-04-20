package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.InventoryDto;
import lt.gama.model.dto.documents.items.PartInventoryDto;
import lt.gama.model.sql.documents.InventorySql;
import lt.gama.model.sql.documents.items.InventoryPartSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.service.InventoryCheckService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class InventorySqlMapper implements IBaseMapper<InventoryDto, InventorySql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected InventoryCheckService inventoryCheckService;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract InventoryDto toDto(InventorySql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract InventorySql toEntity(InventoryDto dto);

    @AfterMapping
    public void afterToEntity(InventoryDto src, @MappingTarget InventorySql target) {
        if (target.getParts() != null) {
            target.getParts().forEach(p -> p.setParent(target));
        }
    }

    public abstract List<PartInventoryDto> setInventoryPartToDto(List<InventoryPartSql> entity);

    public abstract List<InventoryPartSql> setPartInventoryToEntity(List<PartInventoryDto> dto);

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartInventoryDto inventoryPartToDto(InventoryPartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract InventoryPartSql partInventoryToEntity(PartInventoryDto dto);

    @AfterMapping
    public void afterToEntity(PartInventoryDto src, @MappingTarget InventoryPartSql target) {
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
