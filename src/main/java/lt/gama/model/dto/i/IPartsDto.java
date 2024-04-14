package lt.gama.model.dto.i;

import lt.gama.model.dto.base.*;
import lt.gama.model.type.doc.DocWarehouse;

import java.util.*;

public interface IPartsDto<E extends BasePartPartApiDto> {

    List<E> getParts();

    void setParts(List<E> parts);

    Boolean getFinishedParts();

    void setFinishedParts(Boolean finishedParts);

    DocWarehouse getWarehouse();
}
