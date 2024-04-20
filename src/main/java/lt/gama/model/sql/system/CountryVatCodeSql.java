package lt.gama.model.sql.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.VATCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "country_vat_code")
public class CountryVatCodeSql extends BaseEntitySql implements IId<String> {

    /**
     * id - country code
     */
    @Id
    private String id;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<VATCode> codes;


    @SuppressWarnings("unused")
    protected CountryVatCodeSql() {}

    public CountryVatCodeSql(String country) {
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

    public List<VATCode> getCodes() {
        return codes;
    }

    public void setCodes(List<VATCode> codes) {
        this.codes = codes;
    }

    @Override
    public String toString() {
        return "CountryVatCodeSql{" +
                "id='" + id + '\'' +
                ", codes=" + codes +
                "} " + super.toString();
    }
}
