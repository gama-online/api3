package lt.gama.model.i.base;

import lt.gama.model.dto.documents.*;
import lt.gama.model.i.ICounterparty;
import lt.gama.model.i.IDocEmployee;
import lt.gama.model.i.IDocumentType;
import lt.gama.model.i.IHasEmployee;
import lt.gama.model.type.auth.CompanySettings;

public interface IBaseDocument extends IBaseNumberDocument, IDocumentType, IHasEmployee {

    String getDocumentType();

    IDocEmployee getEmployee();

    DoubleEntryDto getDoubleEntry();
    void setDoubleEntry(DoubleEntryDto doubleEntry);

    ICounterparty getCounterparty();

    Boolean getFinishedGL();

    void setFinishedGL(Boolean finishedGL);

    Boolean getFinished();

    void setFinished(Boolean finished);

    default String makeContent(CompanySettings companySettings) {
        return null;
    }
}



