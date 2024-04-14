package lt.gama.model.type.part;

import lt.gama.model.i.IDb;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;

public class DocPartPlDiscount extends BaseDocPart {

    @Serial
    private static final long serialVersionUID = -1L;


    protected DocPartPlDiscount() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartPlDiscount(P part) {
        super(part);
    }

    // generated

    @Override
    public String toString() {
        return "DocPartPlDiscount{} " + super.toString();
    }
}
