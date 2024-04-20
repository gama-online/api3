package lt.gama.impexp.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * gama-online
 * Created by valdas on 2015-12-11.
 */
public class ISO20022Record implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Unique key to identify imported bank operation - will be generated on parse
     */
    private UUID uuid;

    private Boolean imported;

    private Boolean exists;

    private Boolean noDebt;

    /**
     * Finish operation after import
     */
    private Boolean finish;

    private ISO20022Entry entry;

    private ISO20022EntryDetail detail;


    public boolean isImported() {
        return imported != null && imported;
    }

    public boolean isExists() {
        return exists != null && exists;
    }

    public boolean isFinish() {
        return finish != null && finish;
    }

    // generated
    // except getImported(), getExists(), getFinish()

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setImported(Boolean imported) {
        this.imported = imported;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public Boolean getNoDebt() {
        return noDebt;
    }

    public void setNoDebt(Boolean noDebt) {
        this.noDebt = noDebt;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
    }

    public ISO20022Entry getEntry() {
        return entry;
    }

    public void setEntry(ISO20022Entry entry) {
        this.entry = entry;
    }

    public ISO20022EntryDetail getDetail() {
        return detail;
    }

    public void setDetail(ISO20022EntryDetail detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ISO20022Record{" +
                "uuid=" + uuid +
                ", imported=" + imported +
                ", exists=" + exists +
                ", noDebt=" + noDebt +
                ", finish=" + finish +
                ", entry=" + entry +
                ", detail=" + detail +
                '}';
    }
}
