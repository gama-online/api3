package lt.gama.model.i;

import lt.gama.helpers.BooleanUtils;
import lt.gama.model.dto.base.BaseDocPartDto;


public interface IDocPartsDto<E extends BaseDocPartDto> extends IParts<E> {

    Boolean getFinishedParts();

    void setFinishedParts(Boolean finishedParts);

    default boolean isFinishedParts() {
        return BooleanUtils.isTrue(getFinishedParts());
    }
}
