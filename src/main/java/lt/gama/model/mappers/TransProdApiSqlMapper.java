package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.TransProdApiDto;
import lt.gama.model.dto.documents.TransProdDto;
import lt.gama.model.dto.documents.items.PartFromDto;
import lt.gama.model.dto.documents.items.PartToDto;
import lt.gama.model.dto.entities.PartFromApiDto;
import lt.gama.model.dto.entities.PartToApiDto;
import lt.gama.model.sql.documents.TransProdSql;
import lt.gama.model.sql.documents.items.TransProdPartFromSql;
import lt.gama.model.sql.documents.items.TransProdPartToSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.doc.DocWarehouse;
import org.mapstruct.*;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class TransProdApiSqlMapper implements IBaseMapper<TransProdApiDto, TransProdSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract TransProdApiDto toDto(TransProdSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "tagFrom", ignore = true)
    @Mapping(target = "tagReserved", ignore = true)
    @Mapping(target = "tagTo", ignore = true)
    public abstract TransProdSql toEntity(TransProdApiDto dto);

    @Mapping(target = "tagFrom", ignore = true)
    @Mapping(target = "tagReserved", ignore = true)
    @Mapping(target = "tagTo", ignore = true)
    public abstract TransProdDto apiDtoToDto(TransProdApiDto apiDto);

    public abstract TransProdApiDto dtoToApiDto(TransProdDto dto);


    @AfterMapping
    public void afterToDto(TransProdSql src, @MappingTarget TransProdApiDto target) {
        if (target.getWarehouseFrom() != null) target.getWarehouseFrom().setTag(src.getTagFrom());
        if (target.getWarehouseReserved() != null) target.getWarehouseReserved().setTag(src.getTagReserved());
        if (target.getWarehouseTo() != null) target.getWarehouseTo().setTag(src.getTagTo());
    }

    @AfterMapping
    public void afterToEntity(TransProdApiDto src, @MappingTarget TransProdSql target) {
        if (target.getPartsFrom() != null) {
            target.getPartsFrom().forEach(p -> p.setParent(target));
        }
        if (target.getPartsTo() != null) {
            target.getPartsTo().forEach(p -> p.setParent(target));
        }
        if (src.getWarehouseFrom() != null) target.setTagFrom(src.getWarehouseFrom().getTag());
        if (src.getWarehouseReserved() != null) target.setTagReserved(src.getWarehouseReserved().getTag());
        if (src.getWarehouseTo() != null) target.setTagTo(src.getWarehouseTo().getTag());
    }

    @AfterMapping
    public void afterApiDtoToDto(TransProdApiDto src, @MappingTarget TransProdDto target) {
        if (src.getWarehouseFrom() != null) target.setTagFrom(src.getWarehouseFrom().getTag());
        if (src.getWarehouseReserved() != null) target.setTagReserved(src.getWarehouseReserved().getTag());
        if (src.getWarehouseTo() != null) target.setTagTo(src.getWarehouseTo().getTag());
    }

    @AfterMapping
    public void afterDtoToApiDto(TransProdDto src, @MappingTarget TransProdApiDto target) {
        if (target.getWarehouseFrom() != null) target.getWarehouseFrom().setTag(src.getTagFrom());
        if (target.getWarehouseReserved() != null) target.getWarehouseReserved().setTag(src.getTagReserved());
        if (target.getWarehouseTo() != null) target.getWarehouseTo().setTag(src.getTagTo());
    }

    public abstract List<PartFromApiDto> setPartFrom2Dto(List<TransProdPartFromSql> entity);

    public abstract List<TransProdPartFromSql> setPartFrom2Entity(List<PartFromApiDto> dto);

    public abstract List<PartToApiDto> setPartTo2Dto(List<TransProdPartToSql> entity);

    public abstract List<TransProdPartToSql> setPartTo2Entity(List<PartToApiDto> dto);

    public abstract List<PartFromDto> setPartFromApiDto2Dto(List<PartFromApiDto> apiDto);

    public abstract List<PartFromApiDto> setDto2PartFromApiDto(List<PartFromDto> dto);

    public abstract List<PartToDto> setPartToApiDto2Dto(List<PartToApiDto> apiDto);

    public abstract List<PartToApiDto> setDto2PartToApiDto(List<PartToDto> dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "db", ignore = true)
    @Mapping(target = "recordId", source = "id")
    public abstract PartFromApiDto transProdPartFrom2Dto(TransProdPartFromSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    @Mapping(target = "tag", ignore = true)
    public abstract TransProdPartFromSql partFrom2Entity(PartFromApiDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "db", ignore = true)
    @Mapping(target = "recordId", source = "id")
    public abstract PartToApiDto transProdPartTo2Dto(TransProdPartToSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    @Mapping(target = "tag", ignore = true)
    public abstract TransProdPartToSql partTo2Entity(PartToApiDto dto);

    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "costTotal", ignore = true)
    @Mapping(target = "costInfo", ignore = true)
    @Mapping(target = "remainder", ignore = true)
    @Mapping(target = "reserved", ignore = true)
    @Mapping(target = "reservedQuantity", ignore = true)
    @Mapping(target = "forwardSell", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract PartFromDto partFromApiDto2Dto(PartFromApiDto apiDto);

    @Mapping(target = "vendorCode", ignore = true)
    public abstract PartFromApiDto dto2PartFromApiDto(PartFromDto dto);

    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "costTotal", ignore = true)
    @Mapping(target = "costInfo", ignore = true)
    @Mapping(target = "forwardSell", ignore = true)
    @Mapping(target = "tag", ignore = true)
    public abstract PartToDto partToApiDto2Dto(PartToApiDto apiDto);

    @Mapping(target = "costPercent", ignore = true)
    public abstract PartToApiDto dto2PartToApiDto(PartToDto dto);


    public abstract DocWarehouse warehouseSql2docWarehouse(WarehouseSql entity);

    public abstract WarehouseSql DocWarehouse2warehouseSql(DocWarehouse dto);


    public abstract PartFromApiDto setPartFromFields(PartSql partSql);

    public abstract PartToApiDto setPartToFields(PartSql partSql);


    @ObjectFactory
    public PartFromApiDto createPartFromApiDto(TransProdPartFromSql partFromSql) {
        return setPartFromFields(partFromSql.getPart());
    }

    @ObjectFactory
    public PartToApiDto createPartToApiDto(TransProdPartToSql partToSql) {
        return setPartToFields(partToSql.getPart());
    }

    @AfterMapping
    public void after2DtoFrom(TransProdPartFromSql src, @MappingTarget PartFromApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void after2DtoTo(TransProdPartToSql src, @MappingTarget PartToApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void after2EntityFrom(PartFromApiDto src, @MappingTarget TransProdPartFromSql target) {
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
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }


    @AfterMapping
    public void after2EntityTo(PartToApiDto src, @MappingTarget TransProdPartToSql target) {
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
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }

    @AfterMapping
    public void after2DtoApiFrom(PartFromApiDto src, @MappingTarget PartFromDto target) {
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }

    @AfterMapping
    public void after2DtoApiTo(PartToApiDto src, @MappingTarget PartToDto target) {
        if (src.getWarehouse() != null) target.setTag(src.getWarehouse().getTag());
    }

    @AfterMapping
    public void after2ApiDtoFrom(PartFromDto src, @MappingTarget PartFromApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

    @AfterMapping
    public void after2ApiDtoTo(PartToDto src, @MappingTarget PartToApiDto target) {
        if (target.getWarehouse() != null) target.getWarehouse().setTag(src.getTag());
    }

}
