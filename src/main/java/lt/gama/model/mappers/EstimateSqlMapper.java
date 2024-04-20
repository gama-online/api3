package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.EstimateDto;
import lt.gama.model.dto.documents.items.PartEstimateDto;
import lt.gama.model.dto.documents.items.PartEstimateSubpartDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.sql.documents.EstimateSql;
import lt.gama.model.sql.documents.items.EstimatePartSql;
import lt.gama.model.sql.documents.items.EstimateSubpartSql;
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
public abstract class EstimateSqlMapper extends BaseDocumentSqlMapper<EstimateDto, EstimateSql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected InventoryCheckService inventoryCheckService;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "parts", ignore = true)
    public abstract EstimateDto toDto(EstimateSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "parts", ignore = true)
    public abstract EstimateSql toEntity(EstimateDto dto);

    @AfterMapping
    public void afterToEntity(EstimateDto src, @MappingTarget EstimateSql target) {
        var parts = CollectionsHelper.streamOf(src.getParts())
                .flatMap(p -> Stream.concat(
                        Stream.of(partEstimateToEntity(p)),
                        CollectionsHelper.streamOf(p.getParts()).map(this::subpartEstimateToEntity)))
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
    public void afterToDto (EstimateSql src, @MappingTarget EstimateDto target) {
        if (Validators.isValid(src.getAccount())) {
            target.setAccount(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "account")
                    ? new BankAccountDto(src.getAccount())
                    : new BankAccountDto(src.getAccount().getId(), DBType.POSTGRESQL));
        } else {
            target.setAccount(null);
        }
        if (entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "parts") && CollectionsHelper.hasValue(src.getParts())) {
            List<PartEstimateDto> parts = CollectionsHelper.streamOf(src.getParts())
                    .filter(p -> p instanceof EstimatePartSql).map(p -> estimatePartToDto((EstimatePartSql) p)).toList();

            Map<UUID, PartEstimateDto> partsMap = parts.stream().collect(Collectors.toMap(PartEstimateDto::getLinkUuid, Function.identity()));

            Map<UUID, List<EstimateSubpartSql>> subpartsMap = CollectionsHelper.streamOf(src.getParts())
                    .filter(p -> p instanceof EstimateSubpartSql).map(p -> (EstimateSubpartSql) p).collect(Collectors.groupingBy(EstimateSubpartSql::getParentLinkUuid));

            subpartsMap.forEach((key, value) -> {
                if (partsMap.get(key) == null) throw new GamaException("Parent not exists");
                partsMap.get(key).setParts(value.stream().map(this::estimateSubpartToDto).collect(Collectors.toList()));
            });
            target.setParts(parts);
        }
    }

    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    @Mapping(target = "parts", ignore = true)
    public abstract PartEstimateDto estimatePartToDto(EstimatePartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract EstimatePartSql partEstimateToEntity(PartEstimateDto dto);


    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "id", source = "partId")
    public abstract PartEstimateSubpartDto estimateSubpartToDto(EstimateSubpartSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "exportId", ignore = true)
    @Mapping(target = "id", source = "recordId")
    @Mapping(target = "docPart", source = "dto")
    public abstract EstimateSubpartSql subpartEstimateToEntity(PartEstimateSubpartDto dto);


    @AfterMapping
    public void afterToEntity(PartEstimateDto src, @MappingTarget EstimatePartSql target) {
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
    public void afterToEntity(PartEstimateSubpartDto src, @MappingTarget EstimateSubpartSql target) {
        if (Validators.isValid(src)) {
            target.setPart(entityManager.getReference(PartSql.class, src.getId()));
        } else {
            target.setPart(null);
        }
    }
}
