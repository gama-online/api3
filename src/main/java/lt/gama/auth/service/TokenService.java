package lt.gama.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import dev.paseto.jpaseto.Claims;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.io.Deserializer;
import dev.paseto.jpaseto.io.Serializer;
import jakarta.servlet.http.HttpServletRequest;
import lt.gama.auth.i.IAuth;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.service.ex.rt.GamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public final class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    private static final String CLAIM_REFRESH = "rfr";
    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_USER_NAME = "unm";
    private static final String CLAIM_EMPLOYEE_ID = "eid";
    private static final String CLAIM_IS_ADMIN = "adm";
    private static final String CLAIM_IS_IMPERSONATED = "imp";
    private static final String CLAIM_COMPANY_ID = "cid";
    private static final String CLAIM_PERMISSIONS = "prm";

    private static final String ISSUER = "gama-online";

    @Value("${gama.token.secret-key}") private String secretKey;
    @Value("${gama.token.refresh-expire}") private int tokenRefreshExpire;
    @Value("${gama.token.access-expire}") private int tokenAccessExpire;
    @Value("${gama.token.reset-expire}") private int tokenResetExpire;

    private final Serializer<Map<String, Object>> serializer;
    private final Deserializer<Map<String, Object>> deserializer;

    private Params _params;

    public TokenService(ObjectMapper jacksonObjectMapper) {
        serializer = value -> {
            try {
                return jacksonObjectMapper.writeValueAsBytes(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
        deserializer = bytes -> {
            try {
                return jacksonObjectMapper.readValue(bytes, new TypeReference<Map<String, Object>>() {});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public Params params() {
        Params localRef = _params;
        if (localRef == null) {
            synchronized (this) {
                localRef = _params;
                if (localRef == null) {
                    try {
                        byte[] encodedKeyBytes = Base64.getDecoder().decode(secretKey);
                        SecretKey secretKey = new SecretKeySpec(encodedKeyBytes, "none");
                        _params = localRef = new Params(tokenAccessExpire, tokenRefreshExpire, tokenResetExpire, secretKey);
                    } catch (Exception e) {
                        throw new GamaException(e.getMessage(), e);
                    }
                }
            }
        }
        return localRef;
    }

    public String createToken(IAuth auth) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = BooleanUtils.isTrue(auth.getImpersonated())
                    ? now.plus(12, ChronoUnit.HOURS)
                    : now.plus(params().accessTokenInSeconds, ChronoUnit.SECONDS);
            if (auth.getExpiresAt() != null && auth.getExpiresAt().isBefore(expiresAt)) {
                expiresAt = auth.getExpiresAt();
            }

            return Pasetos.V1.LOCAL.builder()
                    .setSerializer(serializer)
                    .setIssuer(ISSUER)
                    .setTokenId((auth.getUuid() != null ? auth.getUuid() : UUID.randomUUID()).toString())
                    .setIssuedAt(now)
                    .setExpiration(expiresAt)

                    .claim(CLAIM_USER_ID, auth.getId())
                    .claim(CLAIM_USER_NAME, auth.getName())
                    .claim(CLAIM_EMPLOYEE_ID, auth.getEmployeeId())
                    .claim(CLAIM_IS_ADMIN, auth.getAdmin())
                    .claim(CLAIM_IS_IMPERSONATED, auth.getImpersonated())
                    .claim(CLAIM_COMPANY_ID, auth.getCompanyId())
                    .claim(CLAIM_PERMISSIONS, auth.getPermissions() == null ? null : auth.getPermissions().toArray(new String[0]))

                    .setSharedSecret(params().secretKey)
                    .compact();

        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    public String createRefreshToken(String id, String uuid, long companyId, Instant expiresAt) {
        try {
            Instant now = Instant.now();
            if (expiresAt == null) {
                expiresAt = now.plus(params().refreshTokenInSeconds, ChronoUnit.SECONDS);
            }

            return Pasetos.V1.LOCAL.builder()
                    .setSerializer(serializer)
                    .setIssuer(ISSUER)
                    .setTokenId(uuid)
                    .setIssuedAt(now)
                    .setExpiration(expiresAt)

                    .claim(CLAIM_REFRESH, true)
                    .claim(CLAIM_USER_ID, id)
                    .claim(CLAIM_COMPANY_ID, companyId)

                    .setSharedSecret(params().secretKey)
                    .compact();

        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    public IAuth getAuthentication(String token) {
        if (StringHelper.isEmpty(token)) {
            log.debug("No token");
            return null;
        }
        try {
            Claims claims = Pasetos.parserBuilder()
                    .setDeserializer(deserializer)
                    .setSharedSecret(params().secretKey)
                    .build()
                    .parse(token)
                    .getClaims();

            Instant exp = claims.getExpiration();
            if (exp.isBefore(DateUtils.instant())) {
                throw new GamaException("token expired");
            }
            IAuth auth = new Auth();
            auth.setId(claims.get(CLAIM_USER_ID, String.class));
            auth.setName(claims.get(CLAIM_USER_NAME, String.class));
            auth.setEmployeeId(claims.get(CLAIM_EMPLOYEE_ID, Long.class));
            auth.setCompanyId(claims.get(CLAIM_COMPANY_ID, Long.class));
            auth.setUuid(claims.getTokenId());
            auth.setRefresh(claims.get(CLAIM_REFRESH, Boolean.class));
            auth.setAdmin(claims.get(CLAIM_IS_ADMIN, Boolean.class));
            auth.setImpersonated(claims.get(CLAIM_IS_IMPERSONATED, Boolean.class));
            auth.setExpiresAt(exp);

            @SuppressWarnings("unchecked")
            List<String> permissions = claims.get(CLAIM_PERMISSIONS, List.class);
            if (permissions != null) auth.setPermissions(Set.copyOf(permissions));

            return auth;

        } catch (Exception e) {
            log.debug(e.getMessage());
            return null;
        }
    }

    public IAuth getAuthentication(HttpServletRequest req) {
        String token = resolveToken(req);
        return getAuthentication(token);
    }

    public String resolveToken(HttpServletRequest req) {
        String authorizationHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        return authorizationHeader != null && authorizationHeader.startsWith(AUTHENTICATION_SCHEME + " ") ?
                authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim() : null;
    }

    public record Params(
        int accessTokenInSeconds,
        int refreshTokenInSeconds,
        int resetTokenInSeconds,
        SecretKey secretKey) {}
}
