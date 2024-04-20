package lt.gama.model.type.gl;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lt.gama.model.dto.entities.GLAccountDto;
import lt.gama.model.i.ITranslations;
import lt.gama.model.sql.entities.GLAccountSql;
import lt.gama.model.type.l10n.LangGLAccount;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-03-20.
 */
public class GLOperationAccount implements Comparable<GLOperationAccount>,
        ITranslations<LangGLAccount>, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String number;

    private String name;

    /**
     * Translations
     */
    @Transient
    private Map<String, LangGLAccount> translation;

    public GLOperationAccount() {
    }

    public GLOperationAccount(String number, String name) {
        this.number = number;
        this.name = name;
    }

    public GLOperationAccount(String number) {
        this.number = number;
    }

    public GLOperationAccount(GLAccountSql account) {
        if (account != null) {
            this.number = account.getNumber();
            this.name = account.getName();
            this.translation = account.getTranslation() != null ? new HashMap<>(account.getTranslation()) : null;
        }
    }

    public GLOperationAccount(GLAccountDto account) {
        if (account != null) {
            this.number = account.getNumber();
            this.name = account.getName();
            this.translation = account.getTranslation() != null ? new HashMap<>(account.getTranslation()) : null;
        }
    }

    @Override
    public int compareTo(GLOperationAccount o) {
        return ComparisonChain.start()
                .compare(number, o.number, Ordering.natural().nullsFirst())
                .compare(name, o.name, Ordering.natural().nullsFirst())
                .result();
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
        GLOperationAccount that = (GLOperationAccount) o;
        return Objects.equals(number, that.number) && Objects.equals(name, that.name) && Objects.equals(translation, that.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, name, translation);
    }

    @Override
    public String toString() {
        return "GLOperationAccount{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", translation=" + translation +
                '}';
    }
}
