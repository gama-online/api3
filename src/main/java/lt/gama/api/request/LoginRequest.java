package lt.gama.api.request;

public class LoginRequest {

    private String name;

    private String password;

    private Integer companyIndex;

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getCompanyIndex() {
        return companyIndex;
    }

    public void setCompanyIndex(Integer companyIndex) {
        this.companyIndex = companyIndex;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", companyIndex=" + companyIndex +
                '}';
    }
}
