package lt.gama.model.type.part;

import lt.gama.model.i.IDb;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;

public class DocPartPlActual extends BaseDocPart {

    @Serial
    private static final long serialVersionUID = -1L;


    protected DocPartPlActual() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartPlActual(P part) {
        super(part);
    }

    // generated

    @Override
    public String toString() {
        return "DocPartPlActual{} " + super.toString();
    }
}
