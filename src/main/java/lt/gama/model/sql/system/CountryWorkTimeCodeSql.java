package lt.gama.model.sql.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.salary.WorkTimeCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "country_work_time_code")
public class CountryWorkTimeCodeSql extends BaseEntitySql implements IId<String> {

    /**
     * id - country code
     */
    @Id
    private String id;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<WorkTimeCode> codes;


    @SuppressWarnings("unused")
    protected CountryWorkTimeCodeSql() {}

    public CountryWorkTimeCodeSql(String id) {
        this.id = id;
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

    public List<WorkTimeCode> getCodes() {
        return codes;
    }

    public void setCodes(List<WorkTimeCode> codes) {
        this.codes = codes;
    }

    @Override
    public String toString() {
        return "CountryWorkTimeCodeSql{" +
                "id='" + id + '\'' +
                ", codes=" + codes +
                "} " + super.toString();
    }
}
