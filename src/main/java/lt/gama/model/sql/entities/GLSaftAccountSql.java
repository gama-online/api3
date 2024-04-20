package lt.gama.model.sql.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.gama.model.i.ITranslations;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.enums.GLAccountType;
import lt.gama.model.type.l10n.LangGLAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "gl_saft_accounts")
public class GLSaftAccountSql extends BaseEntitySql implements ITranslations<LangGLAccount> {

    @Id
    private String number;

    private String name;

    private int depth;

    private boolean inner;

    private GLAccountType type;

    private String parent;

    /**
     * Translations
     */
    @JdbcTypeCode(SqlTypes.JSON)
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
    public String toString() {
        return "GLSaftAccountSql{" +
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
