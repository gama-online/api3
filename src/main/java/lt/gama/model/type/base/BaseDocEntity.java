package lt.gama.model.type.base;

import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IExportId;
import lt.gama.model.i.IId;
import lt.gama.model.type.enums.DBType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-25.
 */
@MappedSuperclass
@Embeddable
public abstract class BaseDocEntity implements IId<Long>, IExportId, IDb, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long id;

    @Transient
    private String exportId;

    @Transient
    private Long foreignId;

    private DBType db;

    public BaseDocEntity() {}

    public BaseDocEntity(Long id) {
        this.id = id;
    }

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getExportId() {
        return exportId;
    }

    @Override
    public void setExportId(String exportId) {
        this.exportId = exportId;
    }

    public Long getForeignId() {
        return foreignId;
    }

    public void setForeignId(Long foreignId) {
        this.foreignId = foreignId;
    }

    @Override
    public DBType getDb() {
        return db;
    }

    @Override
    public void setDb(DBType db) {
        this.db = db;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseDocEntity that = (BaseDocEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(exportId, that.exportId) && db == that.db;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, exportId, db);
    }

    @Override
    public String toString() {
        return "BaseDocEntity{" +
                "id=" + id +
                ", exportId='" + exportId + '\'' +
                ", db=" + db +
                '}';
    }
}
