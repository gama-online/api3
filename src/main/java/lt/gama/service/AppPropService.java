package lt.gama.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class AppPropService {

    private final boolean development;
    private final boolean production;
    private final boolean test;

    public AppPropService(Environment environment) {
        this.development = environment.matchesProfiles("dev");
        this.production = environment.matchesProfiles("prod");
        this.test = !this.development && !this.production;
    }

    public boolean isDevelopment() {
        return development;
    }

    public boolean isProduction() {
        return production;
    }

    public boolean isTest() {
        return test;
    }
}
