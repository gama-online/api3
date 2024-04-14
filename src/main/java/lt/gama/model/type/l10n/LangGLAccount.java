package lt.gama.model.type.l10n;

import java.io.Serial;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-08-25.
 */
public class LangGLAccount extends LangBase {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    public LangGLAccount() {
    }

    public LangGLAccount(String language, String name) {
        super(language);
        this.name = name;
    }

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LangGLAccount that = (LangGLAccount) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "LangGLAccount{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
