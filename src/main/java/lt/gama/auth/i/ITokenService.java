package lt.gama.auth.i;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.SecretKey;
import java.time.Instant;

public interface ITokenService {

    String createToken(IAuth auth);

    String createRefreshToken(String id, String uuid, long companyId, Instant expiresAt);

    IAuth getAuthentication(String token);

    IAuth getAuthentication(HttpServletRequest req);

    String resolveToken(HttpServletRequest req);

    Params params();

    class Params {
        public final int accessTokenInSeconds;
        public final int refreshTokenInSeconds;
        public final int resetTokenInSeconds;
        public final SecretKey secretKey;

        public Params(int accessTokenInSeconds, int refreshTokenInSeconds, int resetTokenInSeconds, SecretKey secretKey) {
            this.accessTokenInSeconds = accessTokenInSeconds;
            this.refreshTokenInSeconds = refreshTokenInSeconds;
            this.resetTokenInSeconds = resetTokenInSeconds;
            this.secretKey = secretKey;
        }
    }
}
