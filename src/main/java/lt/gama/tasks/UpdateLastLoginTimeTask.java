package lt.gama.tasks;

import lt.gama.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2018-03-03.
 */
public class UpdateLastLoginTimeTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected AccountService accountService;


    private final String email;


    public UpdateLastLoginTimeTask(long companyId, String email) {
        super(companyId);
        this.email = email;
    }

    @Override
    public void execute() {
        try {
            accountService.updateLastLogin(email);
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }


    @Override
    public String toString() {
        return "email='" + email + '\'' +
                ' ' + super.toString();
    }
}
