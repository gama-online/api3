package lt.gama.model.i;

import io.swagger.v3.oas.annotations.*;
import lt.gama.model.type.l10n.*;

import java.util.Collection;
import java.util.Map;

/**
 * gama-online
 * Created by valdas on 2017-09-05.
 */
public interface ITranslations<T extends LangBase> {

    Map<String, T> getTranslation();

    void setTranslation(Map<String, T> translation);

    /**
     * Return as array to frontend
     * @return map as array
     */
    @Hidden
    default Collection<T> getTranslations() {
        return getTranslation() == null ? null : getTranslation().values();
    }

    /**
     * Get from frontend
     * implementation must be empty
     */
    default void setTranslations(Collection<T> translations) {}
}
