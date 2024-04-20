package lt.gama.model.sql.entities.id;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.type.auth.CounterDesc;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CounterId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long companyId;

    private String label;

    private String prefix;


    protected CounterId() {
    }

    public CounterId(long companyId, String label, String prefix) {
        this.companyId = companyId;
        this.label = EntityUtils.normalizeEntityName(label);
        this.prefix = prefix == null ? "" : StringHelper.trim(prefix.toLowerCase());
    }

    public CounterId(long companyId, @NotNull CounterDesc desc) {
        this(companyId, desc.getLabel(), desc.getPrefix());
    }

    // generated

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CounterId counterId = (CounterId) o;
        return companyId == counterId.companyId && Objects.equals(label, counterId.label) && Objects.equals(prefix, counterId.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, label, prefix);
    }

    @Override
    public String toString() {
        return "CounterId{" +
                "companyId=" + companyId +
                ", label='" + label + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
