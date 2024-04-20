package lt.gama.model.sql.base;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IExportId;
import lt.gama.model.i.IId;
import lt.gama.model.i.base.IBaseCompany;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;

@MappedSuperclass
public abstract class BaseCompanySql extends BaseEntitySql implements IBaseCompany, IExportId, IId<Long>, IDb {

    @Id
    @GeneratedValue(generator = "gama_sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long foreignId;

    private long companyId;

    /**
     * Used for import/export only
     */
    private String exportId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> labels;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseCompanySql that = (BaseCompanySql) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    public void reset() {}

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getForeignId() {
        return foreignId;
    }

    public void setForeignId(Long foreignId) {
        this.foreignId = foreignId;
    }

    @Override
    public long getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    @Override
    public String getExportId() {
        return exportId;
    }

    @Override
    public void setExportId(String exportId) {
        this.exportId = exportId;
    }

    @Override
    public Set<String> getLabels() {
        return labels;
    }

    @Override
    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "BaseCompanySql{" +
                "id=" + id +
                ", foreignId=" + foreignId +
                ", companyId=" + companyId +
                ", exportId='" + exportId + '\'' +
                ", labels=" + labels +
                "} " + super.toString();
    }
}
