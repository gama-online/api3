package lt.gama.model.sql.system;

import jakarta.persistence.*;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.auth.VATRatesDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "country_vat_rate")
public class CountryVatRateSql extends BaseEntitySql implements IId<String> {

    /**
     * id - country code
     */
    @Id
    private String id;

    /**
     * List of rates by dates
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<VATRatesDate> vats;


    @SuppressWarnings("unused")
    protected CountryVatRateSql() {}

    public CountryVatRateSql(String country) {
        this.id = country;
    }

    /**
     *  Return rates map from rates list for date
     */
    public VATRatesDate getRatesMap(LocalDate date) {
        if (vats == null) return null;
        vats.sort(Comparator.comparing(VATRatesDate::getDate));
        VATRatesDate result = null;
        if (date == null) {
            result = vats.get(getVats().size() - 1);
        } else {
            for (VATRatesDate vatRatesDate : vats) {
                if (vatRatesDate.getDate().isAfter(date)) break;
                result = vatRatesDate;
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        if (vats != null) {
            vats.sort(Comparator.comparing(VATRatesDate::getDate));
        }
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

    public List<VATRatesDate> getVats() {
        return vats;
    }

    public void setVats(List<VATRatesDate> vats) {
        this.vats = vats;
    }

    @Override
    public String toString() {
        return "CountryVatRateSql{" +
                "id='" + id + '\'' +
                ", vats=" + vats +
                "} " + super.toString();
    }
}
