package lt.gama.model.dto.base;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IVersion;
import lt.gama.model.i.base.IBaseEntity;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.json.ser.LocalDateTimeTZSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class BaseEntityDto implements IBaseEntity, IVersion, IDb, Serializable {

    /**
     * backend DB type
     */
    @Hidden
    private DBType db;


    @JsonSerialize(using = LocalDateTimeTZSerializer.class)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdOn;

    @JsonSerialize(using = LocalDateTimeTZSerializer.class)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedOn;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String createdBy;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String updatedBy;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY,
            description = "Record is out of system - can't be selected and used. Can be restored by admin only")
    private Boolean archive = false;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY,
            description = "Record can't be selected but can be unhidden by ordinary user")
    private Boolean hidden = false;

    @Schema(description = "Record version. Can't be modified")
    private long version;

    public BaseEntityDto() {
    }

    // generated

    @Override
    public DBType getDb() {
        return db;
    }

    @Override
    public void setDb(DBType db) {
        this.db = db;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntityDto that = (BaseEntityDto) o;
        return version == that.version && db == that.db && Objects.equals(createdOn, that.createdOn) && Objects.equals(updatedOn, that.updatedOn) && Objects.equals(createdBy, that.createdBy) && Objects.equals(updatedBy, that.updatedBy) && Objects.equals(archive, that.archive) && Objects.equals(hidden, that.hidden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(db, createdOn, updatedOn, createdBy, updatedBy, archive, hidden, version);
    }

    @Override
    public String toString() {
        return "BaseEntityDto{" +
                "db=" + db +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", archive=" + archive +
                ", hidden=" + hidden +
                ", version=" + version +
                '}';
    }
}
