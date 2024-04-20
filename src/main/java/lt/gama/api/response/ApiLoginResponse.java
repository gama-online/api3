package lt.gama.api.response;


public class ApiLoginResponse {

    private String token;

    private String refresh;

    private String name;

    private String companyName;

    private int companyIndex;

    // generated

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getCompanyIndex() {
        return companyIndex;
    }

    public void setCompanyIndex(int companyIndex) {
        this.companyIndex = companyIndex;
    }

    @Override
    public String toString() {
        return "ApiLoginResponse{" +
                "token='" + token + '\'' +
                ", refresh='" + refresh + '\'' +
                ", name='" + name + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyIndex=" + companyIndex +
                '}';
    }
}
