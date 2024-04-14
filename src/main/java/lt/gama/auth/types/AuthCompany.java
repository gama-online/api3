package lt.gama.auth.types;

import java.util.UUID;

public class AuthCompany {

    private UUID id;

    private String companyName;

    public AuthCompany() {
    }

    public AuthCompany(UUID id, String companyName) {
        this.id = id;
        this.companyName = companyName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
