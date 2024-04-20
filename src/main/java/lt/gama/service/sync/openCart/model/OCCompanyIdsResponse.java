package lt.gama.service.sync.openCart.model;

import java.util.List;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2019-03-15.
 */
public class OCCompanyIdsResponse extends OCResponse {

    private List<String> companyIds;

    // generated

    public List<String> getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(List<String> companyIds) {
        this.companyIds = companyIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OCCompanyIdsResponse that = (OCCompanyIdsResponse) o;
        return Objects.equals(companyIds, that.companyIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyIds);
    }

    @Override
    public String toString() {
        return "OCCompanyIdsResponse{" +
                "companyIds=" + companyIds +
                "} " + super.toString();
    }
}
