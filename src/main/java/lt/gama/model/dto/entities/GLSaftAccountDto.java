package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseEntityDto;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.enums.GLAccountType;
import lt.gama.model.type.l10n.LangGLAccount;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

public class GLSaftAccountDto extends BaseEntityDto implements ITranslations<LangGLAccount> {

    @Serial
    private static final long serialVersionUID = -1L;

    private String number;

    private String name;

    /**
     * length of the path to its root
     */
    private int depth;

    /**
     * true if account has child accounts
     */
    private boolean inner;

    /**
     * Account group type
     */
    private GLAccountType type;

    private String parent;

    /**
     * Translations
     */
    private Map<String, LangGLAccount> translation;

    // generated


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isInner() {
        return inner;
    }

    public void setInner(boolean inner) {
        this.inner = inner;
    }

    public GLAccountType getType() {
        return type;
    }

    public void setType(GLAccountType type) {
        this.type = type;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public Map<String, LangGLAccount> getTranslation() {
        return translation;
    }

    @Override
    public void setTranslation(Map<String, LangGLAccount> translation) {
        this.translation = translation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GLSaftAccountDto that = (GLSaftAccountDto) o;
        return depth == that.depth && inner == that.inner && Objects.equals(number, that.number) && Objects.equals(name, that.name) && type == that.type && Objects.equals(parent, that.parent) && Objects.equals(translation, that.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), number, name, depth, inner, type, parent, translation);
    }

    @Override
    public String toString() {
        return "GLSaftAccountDto{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", depth=" + depth +
                ", inner=" + inner +
                ", type=" + type +
                ", parent='" + parent + '\'' +
                ", translation=" + translation +
                "} " + super.toString();
    }
}
