package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.PurchaseDto;
import lt.gama.model.dto.documents.items.PartPurchaseDto;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.documents.items.PurchasePartSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, CounterpartySql.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class PurchaseSqlMapper extends BaseDocumentSqlMapper<PurchaseDto, PurchaseSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract PurchaseDto toDto(PurchaseSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract PurchaseSql toEntity(PurchaseDto dto);

    @AfterMapping
    public void afterToEntity(PurchaseDto src, @MappingTarget PurchaseSql target) {
        if (target.getParts() != null) {
            target.getParts().forEach(p -> p.setParent(target));
        }
    }

    public abstract List<PartPurchaseDto> purchasePartToDtoList(List<PurchasePartSql> entity);

    public abstract List<PurchasePartSql> partPurchaseToEntityList(List<PartPurchaseDto> dto);

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartPurchaseDto purchasePartToDto(PurchasePartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract PurchasePartSql partPurchaseToEntity(PartPurchaseDto dto);

    @AfterMapping
    public void afterToEntity(PartPurchaseDto src, @MappingTarget PurchasePartSql target) {
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
