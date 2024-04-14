package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.enums.GLAccountType;
import lt.gama.model.type.l10n.LangGLAccount;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

public class GLAccountDto extends BaseCompanyDto implements ITranslations<LangGLAccount> {

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

    /**
     * G.L. account number from SAF-T G.L. accounts
     */
    private GLSaftAccountDto saftAccount;

    public GLAccountDto() {}

    public GLAccountDto(String number, String name, GLAccountType type) {
        this.number = number;
        this.name = name;
        this.type = type;
    }

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

    public GLSaftAccountDto getSaftAccount() {
        return saftAccount;
    }

    public void setSaftAccount(GLSaftAccountDto saftAccount) {
        this.saftAccount = saftAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GLAccountDto that = (GLAccountDto) o;
        return depth == that.depth && inner == that.inner && Objects.equals(number, that.number) && Objects.equals(name, that.name) && type == that.type && Objects.equals(parent, that.parent) && Objects.equals(translation, that.translation) && Objects.equals(saftAccount, that.saftAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), number, name, depth, inner, type, parent, translation, saftAccount);
    }

    @Override
    public String toString() {
        return "GLAccountDto{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", depth=" + depth +
                ", inner=" + inner +
                ", type=" + type +
                ", parent='" + parent + '\'' +
                ", translation=" + translation +
                ", saftAccount=" + saftAccount +
                "} " + super.toString();
    }
}
