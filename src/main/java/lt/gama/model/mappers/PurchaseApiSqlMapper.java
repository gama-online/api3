package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.PurchaseApiDto;
import lt.gama.model.dto.documents.PurchaseDto;
import lt.gama.model.dto.documents.items.PartPurchaseDto;
import lt.gama.model.dto.entities.PartPurchaseApiDto;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.documents.items.PurchasePartSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.doc.DocWarehouse;
import org.mapstruct.*;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, CounterpartySqlMapper.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class PurchaseApiSqlMapper extends BaseDocumentSqlMapper<PurchaseApiDto, PurchaseSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract PurchaseApiDto toDto(PurchaseSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract PurchaseSql toEntity(PurchaseApiDto dto);

    @Mapping(target = "isafIndex", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract PurchaseDto apiDtoToDto(PurchaseApiDto apiDto);

    public abstract PurchaseApiDto dtoToApiDto(PurchaseDto dto);

    @AfterMapping
    public void afterToDto(PurchaseSql src, @MappingTarget PurchaseApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void afterToEntity(PurchaseApiDto src, @MappingTarget PurchaseSql target) {
        if (target.getParts() != null) {
            target.getParts().forEach(p -> p.setParent(target));
        }
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }

    @AfterMapping
    public void afterApiDtoToDto(PurchaseApiDto src, @MappingTarget PurchaseDto target) {
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }

    @AfterMapping
    public void afterDtoToApiDto(PurchaseDto src, @MappingTarget PurchaseApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    public abstract List<PartPurchaseApiDto> toDto(List<PurchasePartSql> entity);

    public abstract List<PurchasePartSql> toEntity(List<PartPurchaseApiDto> dto);

    public abstract List<PartPurchaseDto> setPartApiDtoToDto(List<PartPurchaseApiDto> apiDto);

    public abstract List<PartPurchaseApiDto> setDtoToPartApiDto(List<PartPurchaseDto> dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "db", ignore = true)
    @Mapping(target = "recordId", source = "id")
    public abstract PartPurchaseApiDto toDto(PurchasePartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract PurchasePartSql toEntity(PartPurchaseApiDto dto);

    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "costTotal", ignore = true)
    @Mapping(target = "addExp", ignore = true)
    @Mapping(target = "docReturn", ignore = true)
    @Mapping(target = "costInfo", ignore = true)
    @Mapping(target = "notEnough", ignore = true)
    @Mapping(target = "forwardSell", ignore = true)
    @Mapping(target = "accountAsset", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract PartPurchaseDto partApiDtoToDto(PartPurchaseApiDto apiDto);

    public abstract PartPurchaseApiDto dtoToPartApiDto(PartPurchaseDto dto);


    public abstract DocWarehouse warehouseSql2docWarehouse(WarehouseSql entity);

    public abstract WarehouseSql DocWarehouse2warehouseSql(DocWarehouse dto);

    public abstract PartPurchaseApiDto setPartFields(PartSql partSql);

    @ObjectFactory
    public PartPurchaseApiDto createPartPurchaseApiDto(PurchasePartSql purchasePartSql) {
        return setPartFields(purchasePartSql.getPart());
    }

    @AfterMapping
    public void afterToDto(PurchasePartSql src, @MappingTarget PartPurchaseApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void afterToEntity(PartPurchaseApiDto src, @MappingTarget PurchasePartSql target) {
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
    public void afterToPartApiDto(PartPurchaseDto src, @MappingTarget PartPurchaseApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void afterToPartDto(PartPurchaseApiDto src, @MappingTarget PartPurchaseDto target) {
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }
}
