package lt.gama.model.mappers;

import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.base.BaseNumberDocumentDto;
import lt.gama.model.i.IDebt;
import lt.gama.model.i.ISeriesWithOrdinal;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * Base mappers interface
 * @param <D> Dto class
 * @param <E> Entity class
 */
public interface IBaseMapper<D, E> {

    D toDto(E entity);

    E toEntity(D dto);

    @BeforeMapping
    default void beforeToDto(E src, @MappingTarget D target) {
        if (target instanceof BaseNumberDocumentDto targetDoc) {
            if (StringHelper.hasValue(targetDoc.getNumber())) {
                ISeriesWithOrdinal seriesWithOrdinal = StringHelper.parseDocNumber(targetDoc.getNumber());
                targetDoc.setSeries(seriesWithOrdinal.getSeries());
                targetDoc.setOrdinal(seriesWithOrdinal.getOrdinal());
            } else {
                ((BaseNumberDocumentDto) target).setOrdinal(0L);
            }
        }
    }

    @AfterMapping
    default void afterToDtoAddDb(E src, @MappingTarget D target) {
        if (target instanceof IDebt && ((IDebt) target).getDoc().getDb() == null) {
            ((IDebt) target).getDoc().setDb(DBType.DATASTORE);
        }
    }
}
