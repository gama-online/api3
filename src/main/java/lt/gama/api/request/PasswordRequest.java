package lt.gama.api.request;

/**
 * Gama
 * Created by valdas on 2015-10-13.
 */
public class PasswordRequest {

    private String password;


    @SuppressWarnings("unused")
    protected PasswordRequest() {}

    public PasswordRequest(String password) {
        this.password = password;
    }

    // generated

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "PasswordRequest{" +
                "password='" + password + '\'' +
                '}';
    }
}
