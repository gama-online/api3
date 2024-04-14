package lt.gama.model.i.base;

import lt.gama.model.type.enums.DBType;

import java.time.LocalDateTime;

public interface IBaseEntity {

    LocalDateTime getCreatedOn();

    void setCreatedOn(LocalDateTime createdOn);

    LocalDateTime getUpdatedOn();

    void setUpdatedOn(LocalDateTime updatedOn);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);

    Boolean getArchive();

    void setArchive(Boolean archive);

    Boolean getHidden();

    void setHidden(Boolean hidden);

    DBType getDb();
}
