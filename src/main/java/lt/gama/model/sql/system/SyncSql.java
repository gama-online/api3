package lt.gama.model.sql.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.sync.SyncSettings;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync")
public class SyncSql extends BaseEntitySql implements IId<Long> {

    /**
     * Company Id
     */
    @Id
    private Long id;

    /**
     * Last sync datetime
     */
    private LocalDateTime date;

    /**
     * Sync parameters from companySetting.sync
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private SyncSettings settings;

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public SyncSettings getSettings() {
        return settings;
    }

    public void setSettings(SyncSettings settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "SyncSql{" +
                "id=" + id +
                ", date=" + date +
                ", settings=" + settings +
                "} " + super.toString();
    }
}
