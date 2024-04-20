package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.dto.documents.items.PartInvoiceDto;
import lt.gama.model.dto.documents.items.PartInvoiceSubpartDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.sql.documents.items.InvoiceSubpartSql;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.InventoryCheckService;
import lt.gama.service.ex.rt.GamaException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(uses = {UtilsMapper.class, CounterpartySql.class, WarehouseSqlMapper.class}, componentModel = "spring")
public abstract class InvoiceSqlMapper extends BaseDocumentSqlMapper<InvoiceDto, InvoiceSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected InventoryCheckService inventoryCheckService;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "parts", ignore = true)
    public abstract InvoiceDto toDto(InvoiceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "parts", ignore = true)
    public abstract InvoiceSql toEntity(InvoiceDto dto);

    @AfterMapping
    public void afterToEntity(InvoiceDto src, @MappingTarget InvoiceSql target) {
        var parts = CollectionsHelper.streamOf(src.getParts())
                .flatMap(p -> Stream.concat(
                        Stream.of(partInvoiceToEntity(p)),
                        CollectionsHelper.streamOf(p.getParts()).map(this::subpartInvoiceToEntity)))
                .collect(Collectors.toList());
        parts.forEach(p -> p.setParent(target));
        target.setParts(parts);

        if (Validators.isValid(src.getAccount())) {
            target.setAccount(entityManager.getReference(BankAccountSql.class, src.getAccount().getId()));
        } else {
            target.setAccount(null);
        }
    }

    @AfterMapping
    public void afterToDto (InvoiceSql src, @MappingTarget InvoiceDto target) {
        if (Validators.isValid(src.getAccount())) {
            target.setAccount(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "account")
                    ? new BankAccountDto(src.getAccount())
                    : new BankAccountDto(src.getAccount().getId(), DBType.POSTGRESQL));
        } else {
            target.setAccount(null);
        }
        if (entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src.getParts()) && CollectionsHelper.hasValue(src.getParts())) {
            List<PartInvoiceDto> parts = CollectionsHelper.streamOf(src.getParts())
                    .filter(p -> p instanceof InvoicePartSql).map(p -> invoicePartToDto((InvoicePartSql) p)).toList();

            Map<UUID, PartInvoiceDto> partsMap = parts.stream().collect(Collectors.toMap(PartInvoiceDto::getLinkUuid, Function.identity()));

            Map<UUID, List<InvoiceSubpartSql>> subpartsMap = CollectionsHelper.streamOf(src.getParts())
                    .filter(p -> p instanceof InvoiceSubpartSql).map(p -> (InvoiceSubpartSql) p).collect(Collectors.groupingBy(InvoiceSubpartSql::getParentLinkUuid));

            subpartsMap.forEach((key, value) -> {
                if (partsMap.get(key) == null) throw new GamaException("Parent not exists");
                partsMap.get(key).setParts(value.stream().map(this::invoiceSubpartToDto).collect(Collectors.toList()));
            });
            target.setParts(parts);
        }
    }

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    @Mapping(target = "parts", ignore = true)
    public abstract PartInvoiceDto invoicePartToDto(InvoicePartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract InvoicePartSql partInvoiceToEntity(PartInvoiceDto dto);


    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartInvoiceSubpartDto invoiceSubpartToDto(InvoiceSubpartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract InvoiceSubpartSql subpartInvoiceToEntity(PartInvoiceSubpartDto dto);


    @AfterMapping
    public void afterToEntity(PartInvoiceDto src, @MappingTarget InvoicePartSql target) {
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
    public void afterToEntity(PartInvoiceSubpartDto src, @MappingTarget InvoiceSubpartSql target) {
        if (Validators.isValid(src)) {
            target.setPart(entityManager.getReference(PartSql.class, src.getId()));
        } else {
            target.setPart(null);
        }
    }
}
