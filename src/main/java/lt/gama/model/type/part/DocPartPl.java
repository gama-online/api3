package lt.gama.model.type.part;

import lt.gama.model.i.IDb;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;

public class DocPartPl extends BaseDocPart {

    @Serial
    private static final long serialVersionUID = -1L;


    protected DocPartPl() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartPl(P part) {
        super(part);
    }

    // generated

    @Override
    public String toString() {
        return "DocPartPl{} " + super.toString();
    }
}
