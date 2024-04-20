package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.model.i.ITranslations;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.enums.GLAccountType;
import lt.gama.model.type.l10n.LangGLAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;


@Entity
@Table(name = "gl_accounts")
@NamedEntityGraph(name = GLAccountSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode("saftAccount"))
public class GLAccountSql extends BaseCompanySql implements ITranslations<LangGLAccount> {

    public static final String GRAPH_ALL = "graph.GLAccountSql.all";

    private long companyId;

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

    /**
     * G.L. account number from SAF-T G.L. accounts
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saft_number")
    GLSaftAccountSql saftAccount;

    /**
     * toString without saftAccount
     */
    @Override
    public String toString() {
        return "GLAccountSql{" +
                "companyId=" + companyId +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", depth=" + depth +
                ", inner=" + inner +
                ", type=" + type +
                ", parent='" + parent + '\'' +
                ", translation=" + translation +
                "} " + super.toString();
    }

    // generated

    @Override
    public long getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

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

    public GLSaftAccountSql getSaftAccount() {
        return saftAccount;
    }

    public void setSaftAccount(GLSaftAccountSql saftAccount) {
        this.saftAccount = saftAccount;
    }
}
