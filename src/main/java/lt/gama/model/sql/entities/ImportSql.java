package lt.gama.model.sql.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.enums.DBType;


@Entity
@Table(name = "import")
public class ImportSql extends BaseEntitySql implements IId<ImportId> {

    /**
     * company id + entity class name + external id
     */
    @EmbeddedId
    private ImportId id;

    private Long entityId;

    private DBType entityDb;


    @SuppressWarnings("unused")
    protected ImportSql() {}

    public ImportSql(Long companyId, String entityClass, String externalId, Long entityId, DBType entityDb) {
        this(new ImportId(companyId, entityClass, externalId), entityId, entityDb);
    }

    public ImportSql(Long companyId, Class<?> entityClass, String externalId, Long entityId, DBType entityDb) {
        this(new ImportId(companyId, entityClass, externalId), entityId, entityDb);
    }

    public ImportSql(ImportId id, Long entityId, DBType entityDb) {
        this.id = id;
        this.entityId = entityId;
        this.entityDb = entityDb;
    }

    // generated

    @Override
    public ImportId getId() {
        return id;
    }

    @Override
    public void setId(ImportId id) {
        this.id = id;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public DBType getEntityDb() {
        return entityDb;
    }

    public void setEntityDb(DBType entityDb) {
        this.entityDb = entityDb;
    }

    @Override
    public String toString() {
        return "ImportSql{" +
                "id=" + id +
                ", entityId=" + entityId +
                ", entityDb=" + entityDb +
                "} " + super.toString();
    }
}
