package lt.gama.model.i;

import lt.gama.model.type.base.BaseDocPart;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Gama
 * Created by valdas on 15-04-21.
 */
public interface IDocParts<E extends BaseDocPart> extends IParts<E> {

    Boolean getFinishedParts();

    void setFinishedParts(Boolean finishedParts);

    default boolean isFinishedParts() {
        return BooleanUtils.isTrue(getFinishedParts());
    }
}
