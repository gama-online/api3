package lt.gama.model.mappers;

import jakarta.persistence.EntityManagerFactory;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.items.GLOperationSql;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.service.repo.CounterpartyRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class DoubleEntrySqlMapper implements IBaseMapper<DoubleEntryDto, DoubleEntrySql> {

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;


    @Override
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "parentCounterparty", ignore = true)
    abstract public DoubleEntryDto toDto(DoubleEntrySql entity);

    @Override
    @Mapping(target = "parentCounterparty", ignore = true)
    abstract public DoubleEntrySql toEntity(DoubleEntryDto dto);

    @AfterMapping
    void afterToEntity(DoubleEntryDto src, @MappingTarget DoubleEntrySql target) {
        if (Validators.isValid(src.getParentCounterparty())) {
            target.setParentCounterparty(counterpartyRepository.getReferenceById(src.getParentCounterparty().getId()));
        }
        if (target.getOperations() != null) {
            target.getOperations().forEach(op -> op.setDoubleEntry(target));
        }
    }

    @AfterMapping
    void afterToDto(DoubleEntrySql src, @MappingTarget DoubleEntryDto target) {
        if (Validators.isValid(src.getParentCounterparty())) {
            target.setParentCounterparty(entityManagerFactory.getPersistenceUnitUtil().isLoaded(src, "counterparty")
                    ? new DocCounterparty(src.getParentCounterparty())
                    : new DocCounterparty(src.getParentCounterparty().getId()));
        } else {
            target.setParentCounterparty(null);
        }
        target.setParent(src.getParentId() != null ? src.getParentId().toString() : null);
        target.calculateTotals();
    }

    @BeforeMapping
    void beforeFromDto(DoubleEntryDto src, @MappingTarget DoubleEntrySql target) {
        src.calculateTotals();
    }


    abstract public List<GLOperationDto> toOperationDtoList(List<GLOperationSql> entity);

    abstract public List<GLOperationSql> toOperationEntityList(List<GLOperationDto> dto);

    @Mapping(target = "sum", ignore = true)
    abstract public GLOperationDto toOperationDto(GLOperationSql entity);

    @Mapping(target = "doubleEntry", ignore = true)
    abstract public GLOperationSql toOperationEntity(GLOperationDto dto);
}
