package lt.gama.model.i;

import lt.gama.helpers.BooleanUtils;
import lt.gama.model.type.ibase.IBaseDocPartSql;


public interface IDocPartsSql<E extends IBaseDocPartSql> extends IParts<E> {

    Boolean getFinishedParts();

    void setFinishedParts(Boolean finishedParts);

    default boolean isFinishedParts() {
        return BooleanUtils.isTrue(getFinishedParts());
    }
}
