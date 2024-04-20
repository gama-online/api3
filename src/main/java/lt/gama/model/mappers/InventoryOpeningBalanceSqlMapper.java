package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.InventoryOpeningBalanceDto;
import lt.gama.model.dto.documents.items.PartOpeningBalanceDto;
import lt.gama.model.sql.documents.InventoryOpeningBalanceSql;
import lt.gama.model.sql.documents.items.InventoryOpeningBalancePartSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class InventoryOpeningBalanceSqlMapper implements IBaseMapper<InventoryOpeningBalanceDto, InventoryOpeningBalanceSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract InventoryOpeningBalanceDto toDto(InventoryOpeningBalanceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract InventoryOpeningBalanceSql toEntity(InventoryOpeningBalanceDto dto);

    @AfterMapping
    public void afterToEntity(InventoryOpeningBalanceDto src, @MappingTarget InventoryOpeningBalanceSql target) {
        if (target.getParts() != null) {
            target.getParts().forEach(p -> p.setParent(target));
        }
    }

    public abstract List<PartOpeningBalanceDto> setInventoryOpeningBalancePartToDto(List<InventoryOpeningBalancePartSql> entity);

    public abstract List<InventoryOpeningBalancePartSql> setPartInventoryToEntity(List<PartOpeningBalanceDto> dto);

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartOpeningBalanceDto inventoryOpeningBalancePartToDto(InventoryOpeningBalancePartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract InventoryOpeningBalancePartSql partOpeningBalanceToEntity(PartOpeningBalanceDto dto);

    @AfterMapping
    public void afterToEntity(PartOpeningBalanceDto src, @MappingTarget InventoryOpeningBalancePartSql target) {
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
