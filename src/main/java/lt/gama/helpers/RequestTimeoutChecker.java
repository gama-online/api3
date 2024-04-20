package lt.gama.helpers;

/**
 * Gama
 * Created by valdas on 15-04-26.
 */
public class RequestTimeoutChecker {

    private final long time;

    public RequestTimeoutChecker() {
        this.time = System.currentTimeMillis();
    }

    public boolean isTimeout() {
        long difference = System.currentTimeMillis() - time;
        return (difference > 9 * 60 * 1000);    // 9 min
    }
}
