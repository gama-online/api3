package lt.gama.freemarker;

import freemarker.template.*;
import lt.gama.helpers.DateUtils;

import java.time.LocalDate;

/**
 * Gama
 * Created by valdas on 15-07-13.
 */
public class JodaObjectWrapper extends DefaultObjectWrapper {

    public JodaObjectWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if (obj instanceof LocalDate) {
            return new SimpleDate(DateUtils.toDate((LocalDate) obj), TemplateDateModel.DATE);
//        } else if (obj instanceof BigMoneyProvider) {
//            return new SimpleNumber(((BigMoneyProvider) obj).toBigMoney().getAmount());
        }

        return super.handleUnknownType(obj);
    }
}
