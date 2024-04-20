package lt.gama.model.sql.entities.id;

import jakarta.persistence.Embeddable;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ImportId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long companyId;

    private String entityClass;

    private String externalId;


    protected ImportId() {
    }

    public ImportId(long companyId, String entityClass, String externalId) {
        this.companyId = companyId;
        this.entityClass = entityClass;
        this.externalId = StringHelper.trim(externalId);
    }

    public ImportId(long companyId, Class<?> entityClass, String externalId) {
        this(companyId, EntityUtils.normalizeEntityClassName(entityClass), externalId);
    }

    // generated

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportId importId = (ImportId) o;
        return companyId == importId.companyId && Objects.equals(entityClass, importId.entityClass) && Objects.equals(externalId, importId.externalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, entityClass, externalId);
    }

    @Override
    public String toString() {
        return "ImportId{" +
                "companyId=" + companyId +
                ", entityClass='" + entityClass + '\'' +
                ", externalId='" + externalId + '\'' +
                '}';
    }
}
