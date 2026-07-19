package io.github.zodicslanser.dinstar.internal;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.zodicslanser.dinstar.AuthScheme;
import io.github.zodicslanser.dinstar.DinstarConfig;
import io.github.zodicslanser.dinstar.DinstarException;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Transport for one gateway: Apache HttpClient 5 wiring plus the two-layer body handling. Internal
 * to the library; consumers use {@code DinstarClient}.
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>One thread-safe {@link CloseableHttpClient} backed by a pooling connection manager — the app
 *       hits three gateways concurrently, and threads may share one client per gateway.</li>
 *   <li>A <strong>fresh {@link HttpClientContext} per request</strong>: Digest {@code nc} state is
 *       per-context, so a fresh one keeps concurrent requests from corrupting each other's auth.</li>
 *   <li>{@link AuthScheme#BASIC} → preemptive Basic (an {@link AuthCache} seeded with a
 *       {@link BasicScheme}); {@link AuthScheme#DIGEST} → reactive, HttpClient's built-in
 *       {@code DigestScheme} answering the {@code 401} challenge (correct {@code nc}/{@code cnonce},
 *       {@code ha2} over the real request-URI).</li>
 *   <li>TLS verification is relaxed (trust-all + no hostname check) only when {@code verifyTls=false}
 *       and only on <em>this</em> client's connection manager — never a JVM-global override.</li>
 *   <li>Branching is on the response <em>body</em>. A hard-error envelope {@code error_code} throws;
 *       HTTP transport status is only consulted to detect a genuine {@code 401} or connection fault.</li>
 * </ul>
 */
public final class DinstarHttp implements Closeable {

    private final DinstarConfig config;
    private final CloseableHttpClient client;
    private final CredentialsProvider credentialsProvider;
    private final HttpHost host;
    private final String baseUrl;

    /**
     * Builds the transport for a single gateway.
     *
     * @param config the per-gateway configuration
     */
    public DinstarHttp(DinstarConfig config) {
        this.config = config;
        this.baseUrl = config.baseUrl();
        this.host = HttpHost.create(URI.create(baseUrl));
        this.credentialsProvider = CredentialsProviderBuilder.create()
                .add(new AuthScope(host), config.username(), config.password().toCharArray())
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(config.connectTimeout().toMillis()))
                .setSocketTimeout(Timeout.ofMilliseconds(config.responseTimeout().toMillis()))
                .build();

        PoolingHttpClientConnectionManagerBuilder cmBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig);

        boolean https = "https".equalsIgnoreCase(host.getSchemeName());
        if (https && !config.verifyTls()) {
            cmBuilder.setTlsSocketStrategy(trustAllTls());
        }
        PoolingHttpClientConnectionManager cm = cmBuilder.build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(config.responseTimeout().toMillis()))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(config.connectTimeout().toMillis()))
                .build();

        this.client = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private static TlsSocketStrategy trustAllTls() {
        try {
            SSLContext ssl = SSLContexts.custom()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();
            // DefaultClientTlsStrategy implements TlsSocketStrategy; trust-all + no hostname check,
            // scoped to this client's connection manager only (never a JVM-global override).
            return new DefaultClientTlsStrategy(ssl, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build trust-all TLS strategy", e);
        }
    }

    private HttpClientContext newContext() {
        HttpClientContext ctx = HttpClientContext.create();
        ctx.setCredentialsProvider(credentialsProvider);
        if (config.authScheme() == AuthScheme.BASIC) {
            // Preemptive Basic: seed a per-request auth cache so the Authorization header is sent
            // on the first request without waiting for a 401 challenge.
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basic = new BasicScheme();
            basic.initPreemptive(new UsernamePasswordCredentials(
                    config.username(), config.password().toCharArray()));
            authCache.put(host, basic);
            ctx.setAuthCache(authCache);
        }
        // DIGEST is reactive: no preemptive cache; HttpClient's built-in DigestScheme answers the
        // gateway's WWW-Authenticate challenge with a correct incrementing nc and random cnonce.
        return ctx;
    }

    // ---- POST / GET, with the envelope error_code check (throws on non-200/202) ----

    /**
     * POST a JSON body and parse the response, enforcing the envelope {@code error_code}.
     *
     * @param path the endpoint path (e.g. {@code /api/send_ussd})
     * @param body the request object to serialise as JSON
     * @param type the response record type
     * @param <T>  response type
     * @return the parsed response
     */
    public <T> T post(String path, Object body, Class<T> type) {
        HttpPost req = new HttpPost(baseUrl + path);
        req.setEntity(new StringEntity(writeJson(body), ContentType.APPLICATION_JSON));
        return parseEnvelope(path, execute(req, path), type);
    }

    /**
     * GET with query params and parse the response, enforcing the envelope {@code error_code}.
     *
     * @param path   the endpoint path
     * @param params query parameters (comma-separated CSV values are pre-joined by the caller)
     * @param type   the response record type
     * @param <T>    response type
     * @return the parsed response
     */
    public <T> T get(String path, List<NameValuePair> params, Class<T> type) {
        return parseEnvelope(path, execute(new HttpGet(buildUri(path, params)), path), type);
    }

    // ---- Raw POST / GET, NO envelope check (get_status and the STK views have no error_code) ----

    /**
     * POST a JSON body and parse the response WITHOUT an envelope check (for {@code get_status},
     * whose body is a bare array and whose response has no {@code error_code}).
     *
     * @param path the endpoint path
     * @param body the request object to serialise
     * @param type the response record type
     * @param <T>  response type
     * @return the parsed response
     */
    public <T> T postRaw(String path, Object body, Class<T> type) {
        HttpPost req = new HttpPost(baseUrl + path);
        req.setEntity(new StringEntity(writeJson(body), ContentType.APPLICATION_JSON));
        return parseRaw(path, execute(req, path), type);
    }

    /**
     * GET and parse the response WITHOUT an envelope check (for the STK views, which are not under
     * {@code /api/} and carry no {@code error_code}).
     *
     * @param path   the endpoint path
     * @param params query parameters
     * @param type   the response record type
     * @param <T>    response type
     * @return the parsed response
     */
    public <T> T getRaw(String path, List<NameValuePair> params, Class<T> type) {
        return parseRaw(path, execute(new HttpGet(buildUri(path, params)), path), type);
    }

    // ---- internals ----

    private RawResponse execute(ClassicHttpRequest request, String endpoint) {
        try {
            return client.execute(request, newContext(), response -> {
                HttpEntity entity = response.getEntity();
                String body = entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);
                return new RawResponse(response.getCode(), body);
            });
        } catch (IOException e) {
            throw DinstarException.transport(endpoint, e);
        }
    }

    private <T> T parseEnvelope(String path, RawResponse raw, Class<T> type) {
        checkTransport(path, raw);
        return parseEnvelopeBody(path, raw.body(), type);
    }

    /**
     * Applies the two-layer envelope rule to a response body and maps it to a typed record.
     *
     * <p>This is the heart of the correctness contract, extracted as a pure, network-free function so
     * it can be unit-tested directly: a non-200/202 envelope {@code error_code} throws
     * {@link DinstarException}; {@code 200}/{@code 202} map to {@code type}, leaving any per-port
     * {@code result[]} (486/503) intact as typed values for the caller to inspect.
     *
     * @param endpoint the endpoint path (for error messages)
     * @param body     the raw JSON response body
     * @param type     the target record type
     * @param <T>      response type
     * @return the parsed response
     * @throws DinstarException if the body is unparseable, lacks {@code error_code}, or carries a
     *                          hard-error envelope code
     */
    public static <T> T parseEnvelopeBody(String endpoint, String body, Class<T> type) {
        JsonNode node;
        try {
            node = Json.MAPPER.readTree(body);
        } catch (Exception e) {
            throw DinstarException.malformed(endpoint, body, e);
        }
        JsonNode ec = node.get("error_code");
        if (ec == null || !ec.isNumber()) {
            throw DinstarException.malformed(endpoint, body, null);
        }
        int code = ec.asInt();
        if (code != 200 && code != 202) {
            String sn = node.hasNonNull("sn") ? node.get("sn").asText() : null;
            throw DinstarException.api(endpoint, code, sn, body);
        }
        try {
            return Json.MAPPER.treeToValue(node, type);
        } catch (Exception e) {
            throw DinstarException.malformed(endpoint, body, e);
        }
    }

    private <T> T parseRaw(String path, RawResponse raw, Class<T> type) {
        checkTransport(path, raw);
        try {
            return Json.MAPPER.readValue(raw.body(), type);
        } catch (Exception e) {
            throw DinstarException.malformed(path, raw.body(), e);
        }
    }

    private void checkTransport(String path, RawResponse raw) {
        if (raw.code() == 401) {
            throw DinstarException.transport(path,
                    new IOException("HTTP 401 - authentication rejected (check credentials and auth scheme)"));
        }
        if (raw.code() / 100 != 2) {
            throw DinstarException.transport(path, new IOException("HTTP " + raw.code()));
        }
    }

    private URI buildUri(String path, List<NameValuePair> params) {
        try {
            URIBuilder b = new URIBuilder(baseUrl + path);
            if (params != null && !params.isEmpty()) {
                b.addParameters(params);
            }
            return b.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad URI for " + path, e);
        }
    }

    private String writeJson(Object body) {
        try {
            return Json.MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot serialise request body for " + body, e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    /** A captured raw HTTP response: status code and body text. */
    private record RawResponse(int code, String body) {
    }
}
