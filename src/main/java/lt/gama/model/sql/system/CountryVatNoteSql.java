package lt.gama.model.sql.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.inventory.VATNote;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "country_vat_note")
public class CountryVatNoteSql extends BaseEntitySql implements IId<String> {

    /**
     * id - country code
     */
    @Id
    private String id;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<VATNote> notes;


    @SuppressWarnings("unused")
    protected CountryVatNoteSql() {}

    public CountryVatNoteSql(String country) {
        this.id = country;
    }

    // generated

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public List<VATNote> getNotes() {
        return notes;
    }

    public void setNotes(List<VATNote> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CountryVatNoteSql{" +
                "id='" + id + '\'' +
                ", notes=" + notes +
                "} " + super.toString();
    }
}
