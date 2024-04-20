package lt.gama.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lt.gama.auth.i.IAuth;
import lt.gama.auth.impl.Auth;
import lt.gama.auth.service.TokenService;
import lt.gama.helpers.StringHelper;
import lt.gama.service.AuthSettingsCacheService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static lt.gama.Constants.*;


@Component
@Order(1)
public class AuthFilter implements Filter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthSettingsCacheService authSettingsCacheService;

    @Autowired
    private Auth auth;


    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        auth.clear();

        if (request instanceof HttpServletRequest httpRequest && !"OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            String token = tokenService.resolveToken(httpRequest);
            if (StringHelper.hasValue(token)) {
                IAuth auth = tokenService.getAuthentication(token);
                if (auth != null) {
                    auth.cloneFrom(auth);
                    auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

                    // prepare for logger and tasks
                    MDC.put(LOG_LABEL_LOGIN, auth.getId());
                    MDC.put(LOG_LABEL_COMPANY, String.valueOf(auth.getCompanyId()));
                    MDC.put(LOG_LABEL_USER_NAME, auth.getName());
                    MDC.put(LOG_LABEL_PERMISSIONS, String.join(",", auth.getPermissions()));
                }
            }
        }
        chain.doFilter(request, response);
        auth.clear();
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
