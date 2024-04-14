package lt.gama.model.sql.base;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lt.gama.jpa.generators.GeneratedCreatorValue;
import lt.gama.jpa.generators.GeneratedEditorValue;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IVersion;
import lt.gama.model.i.base.IBaseEntity;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.json.ser.LocalDateTimeTZSerializer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntitySql implements EntitySql, IVersion, IBaseEntity, IDb {

    @JsonSerialize(using = LocalDateTimeTZSerializer.class)
    @CreationTimestamp
    private LocalDateTime createdOn;

    @JsonSerialize(using = LocalDateTimeTZSerializer.class)
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @GeneratedCreatorValue
    private String createdBy;

    @GeneratedEditorValue
    private String updatedBy;

    /**
     * Record is out of system - can't be selected and used in documents
     */
    private Boolean archive = false;

    /**
     * Record hidden - can't be selected but can be unhidden by ordinary user
     */
    private Boolean hidden = false;

    @Version
    private long version;

    @Transient
    private final DBType db = DBType.POSTGRESQL;

    // customized getters/setters

    @Override
    public void setDb(DBType db) {}

    // generated:

    @Override
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    @Override
    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public Boolean getArchive() {
        return archive;
    }

    @Override
    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    @Override
    public Boolean getHidden() {
        return hidden;
    }

    @Override
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public DBType getDb() {
        return db;
    }

    @Override
    public String toString() {
        return "BaseEntitySql{" +
                "createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", archive=" + archive +
                ", hidden=" + hidden +
                ", version=" + version +
                ", db=" + db +
                '}';
    }
}
