package lt.gama.service;

import lt.gama.helpers.RequestTimeoutChecker;
import org.springframework.stereotype.Service;

/**
 * Gama
 * Created by valdas on 15-04-26.
 */
@Service
public class CheckRequestTimeoutService {

    public RequestTimeoutChecker init() {
        return new RequestTimeoutChecker();
    }

    public boolean isTimeout(RequestTimeoutChecker checker) {
        return checker.isTimeout();
    }
}
