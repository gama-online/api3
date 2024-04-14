package lt.gama.model.type.l10n;

import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-08-25.
 */
public abstract class LangBase implements Serializable {

    private String language;

    public LangBase() {
    }

    public LangBase(String language) {
        this.language = language;
    }

    // generated

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LangBase langBase = (LangBase) o;
        return Objects.equals(language, langBase.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language);
    }

    @Override
    public String toString() {
        return "LangBase{" +
                "language='" + language + '\'' +
                '}';
    }
}
