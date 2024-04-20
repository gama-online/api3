package lt.gama.service.sync.openCart.model;

import lt.gama.service.sync.SyncHttpService;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCLogin extends OCResponse {

    private String success;

    private String token;

    private SyncHttpService.Cookie session;

    public OCLogin() {
    }

    public OCLogin(String token, SyncHttpService.Cookie session) {
        this.token = token;
        this.session = session;
    }

    // generated

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SyncHttpService.Cookie getSession() {
        return session;
    }

    public void setSession(SyncHttpService.Cookie session) {
        this.session = session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OCLogin ocLogin = (OCLogin) o;
        return Objects.equals(success, ocLogin.success) && Objects.equals(token, ocLogin.token) && Objects.equals(session, ocLogin.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), success, token, session);
    }

    @Override
    public String toString() {
        return "OCLogin{" +
                "success='" + success + '\'' +
                ", token='" + token + '\'' +
                ", session='" + session + '\'' +
                "} " + super.toString();
    }
}
