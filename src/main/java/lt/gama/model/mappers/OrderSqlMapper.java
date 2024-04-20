package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.OrderDto;
import lt.gama.model.dto.documents.items.PartOrderDto;
import lt.gama.model.sql.documents.OrderSql;
import lt.gama.model.sql.documents.items.OrderPartSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.PartSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, CounterpartySql.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class OrderSqlMapper extends BaseDocumentSqlMapper<OrderDto, OrderSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract OrderDto toDto(OrderSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract OrderSql toEntity(OrderDto dto);

    @AfterMapping
    public void afterToEntity(OrderDto src, @MappingTarget OrderSql target) {
        if (target.getParts() != null) {
            target.getParts().forEach(p -> p.setParent(target));
        }
    }

    public abstract List<PartOrderDto> setOrderPartToDto(List<OrderPartSql> entity);

    public abstract List<OrderPartSql> setPartOrderToEntity(List<PartOrderDto> dto);

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartOrderDto orderPartToDto(OrderPartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract OrderPartSql partOrderToEntity(PartOrderDto dto);

    @AfterMapping
    public void afterToEntity(PartOrderDto src, @MappingTarget OrderPartSql target) {
        if (Validators.isValid(src)) {
            target.setPart(entityManager.getReference(PartSql.class, src.getId()));
        } else {
            target.setPart(null);
        }
    }
}
