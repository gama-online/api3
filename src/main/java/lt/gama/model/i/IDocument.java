package lt.gama.model.i;

import java.io.Serializable;

public interface IDocument extends IId<Long>, INumberDocument, IUuid, IExportId, IDb, IDebtDueDate, Serializable {

    String getSeries();

    Long getOrdinal();

    // private String note;

    /**
     * Document type, i.e. simple normalized class name
     */
    String getType();
}
