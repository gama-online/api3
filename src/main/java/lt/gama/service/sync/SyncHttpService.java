package lt.gama.service.sync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.FunctionWithException;
import lt.gama.helpers.StringHelper;
import lt.gama.service.ex.rt.GamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;


@Service
public class SyncHttpService {

    private static final Logger log = LoggerFactory.getLogger(SyncHttpService.class);

    private final ObjectMapper objectMapper;

    public SyncHttpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String decode(String s) {
        if (StringHelper.isEmpty(s)) return s;
        return s.replace("&quot;", "\"");
    }

    public <T> T getRequestData(HttpMethod method, URI uri, String data, Class<T> clazz) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("User-Agent", "Mozilla/5.0 GamaOnline/1.0")
                    .timeout(Duration.of(60, SECONDS))
                    .method(method.name(), StringHelper.hasValue(data) ? HttpRequest.BodyPublishers.ofString(data) : HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return objectMapper.readValue(response.body(), clazz);
            } else {
                log.error(this.getClass().getSimpleName() + ": url='" + uri + "', payload='" + data + "', responseCode=" + responseCode);
                return null;
            }

        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, Class<T> clazz) {
        return getRequestData(method, url, query, null, null, null, null, null, null, (reader) -> objectMapper.readValue(reader, clazz));
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, ContentType contentType, Object data, Class<T> clazz, Cookie cookie) {
        return getRequestData(method, url, query, contentType, data, null, null, cookie, null, (reader) -> objectMapper.readValue(reader, clazz));
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, ContentType contentType, Object data, Class<T> clazz, Cookie cookie, Map<String, String> httpCookies) {
        return getRequestData(method, url, query, contentType, data, null, null, cookie, httpCookies, (reader) -> objectMapper.readValue(reader, clazz));
    }

    public <T> T getRequestData(HttpMethod method, URI uri, ContentType contentType, Object data, Class<T> clazz, Cookie cookie, Map<String, String> httpCookies) {
        return getRequestData(method, uri, contentType, data, null, null, cookie, httpCookies, (reader) -> objectMapper.readValue(reader, clazz));
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, TypeReference<T> valueTypeRef) {
        return getRequestData(method, url, query, null, null, null, null, null, null, (reader) -> objectMapper.readValue(reader, valueTypeRef));
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, TypeReference<T> valueTypeRef, String authorizationHeader) {
        return getRequestData(method, url, query, null, null, authorizationHeader, null, null, null, (reader) -> objectMapper.readValue(reader, valueTypeRef));
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, ContentType contentType, Object data, TypeReference<T> valueTypeRef, String authorizationHeader, List<Header> headers) {
        return getRequestData(method, url, query, contentType, data, authorizationHeader, headers, null, null, (reader) -> objectMapper.readValue(reader, valueTypeRef));
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, ContentType contentType, Object data, TypeReference<T> valueTypeRef, Cookie cookie) {
        return getRequestData(method, url, query, contentType, data, null, null, cookie, null, (reader) -> objectMapper.readValue(reader, valueTypeRef));
    }

    public <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, ContentType contentType, Object data, TypeReference<T> valueTypeRef, Cookie cookie, Map<String, String> httpCookies) {
        return getRequestData(method, url, query, contentType, data, null, null, cookie, httpCookies, (reader) -> objectMapper.readValue(reader, valueTypeRef));
    }
    
    public record Cookie(String name, String value) {}

    public record Header(String name, String value) {}

    public enum ContentType {
        JSON, FORM
    }

    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    // private methods

    private static String paramsURLEncode(Map<String, String> params) {
        if (CollectionsHelper.isEmpty(params)) return "";
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringHelper.isEmpty(entry.getKey())) continue;
            sj.add(URLEncoder.encode(entry.getKey(), UTF_8) + "="
                    + (StringHelper.hasValue(entry.getValue()) ? URLEncoder.encode(entry.getValue(), UTF_8) : ""));
        }
        String p = sj.toString();
        return p.equals("&") ? "" : p;

    }

    private <T> T getRequestData(HttpMethod method, String url, Map<String, String> query, ContentType contentType, Object data, String authorizationHeader, List<Header> headers, Cookie cookie, Map<String, String> httpCookies, FunctionWithException<Reader, T> mapper) {
        if (CollectionsHelper.hasValue(query)) {
            String params = paramsURLEncode(query);
            if (StringHelper.hasValue(params)) {
                url += "?" + params;
            }
        }
        return getRequestData(method, URI.create(url), contentType, data, authorizationHeader, headers, cookie, httpCookies, mapper);
    }

    public <T> T getRequestData(HttpMethod method, URI uri, ContentType contentType, Object data, String authorizationHeader, List<Header> headers, Cookie cookie, Map<String, String> httpCookies, FunctionWithException<Reader, T> mapper) {
        long timeStart = System.currentTimeMillis();
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .version(HttpClient.Version.HTTP_2)
                    .header("User-Agent", "Mozilla/5.0 GamaOnline/1.0")
                    .timeout(Duration.of(60, SECONDS))
                    .method(method.name(), method == HttpMethod.GET || data == null || contentType == null ? HttpRequest.BodyPublishers.noBody()
                            : switch (contentType) {
                                case FORM -> data instanceof Map<?,?> dataMap
                                        ? HttpRequest.BodyPublishers.ofString(paramsBuilder(dataMap))
                                        : HttpRequest.BodyPublishers.noBody();
                                case JSON -> HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(data));
                            });

            if (contentType == ContentType.FORM) requestBuilder.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            else if (contentType == ContentType.JSON) requestBuilder.header("Content-Type", "application/json; charset=UTF-8");

            if (StringHelper.hasValue(authorizationHeader)) requestBuilder.header("Authorization", authorizationHeader);
            if (CollectionsHelper.hasValue(headers)) headers.forEach(h -> requestBuilder.header(h.name(), h.value()));

            if (cookie != null) requestBuilder.header("Cookie", cookie.name() + "=" + cookie.value());

            var response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());

            int responseCode = response.statusCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                try (Reader reader = new InputStreamReader(response.body(), UTF_8)) {
                    T result = mapper.apply(reader);
                    if (responseCode == HttpURLConnection.HTTP_OK && result != null && httpCookies != null) {
                        var cookieHeaders = response.headers().map().get("set-cookie");
                        if (CollectionsHelper.hasValue(cookieHeaders)) {
                            cookieHeaders.forEach(cookieHeader -> {
                                var cookies = HttpCookie.parse(cookieHeader);
                                cookies.forEach(c -> httpCookies.put(c.getName().toUpperCase(), c.getValue()));
                            });
                        }
                    }
                    return result;
                }
            } else {
                log.error(this.getClass().getSimpleName() + ": url='" + uri + "', payload='" + data + "', responseCode=" + responseCode);
                return null;
            }
        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);

        } finally {
            long time = System.currentTimeMillis() - timeStart;
            log.info(this.getClass().getSimpleName() + ": url='" + uri + "', time=" + time + "ms");
        }
    }

    private String paramsBuilder(Map<?, ?> params) {
        return params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
