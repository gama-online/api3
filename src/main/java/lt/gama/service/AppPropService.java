package lt.gama.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class AppPropService {

    private final boolean development;
    private final boolean production;

    public AppPropService(Environment environment) {
        this.development = environment.matchesProfiles("dev");
        this.production = environment.matchesProfiles("prod");
    }

    public boolean isDevelopment() {
        return development;
    }

    public boolean isProduction() {
        return production;
    }
}
