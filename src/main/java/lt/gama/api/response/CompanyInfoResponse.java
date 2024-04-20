package lt.gama.api.response;

/**
 * Gama
 * Created by valdas on 15-09-13.
 */
public class CompanyInfoResponse {

    private String companyName;

    @SuppressWarnings("unused")
    protected CompanyInfoResponse() {}

    public CompanyInfoResponse(String companyName) {
        this.companyName = companyName;
    }

    // generated

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "CompanyInfoResponse{" +
                "companyName='" + companyName + '\'' +
                '}';
    }
}
