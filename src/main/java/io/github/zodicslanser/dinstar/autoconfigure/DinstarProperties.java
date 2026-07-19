package io.github.zodicslanser.dinstar.autoconfigure;

import io.github.zodicslanser.dinstar.AuthScheme;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Binds {@code dinstar.*} properties for the optional single-gateway auto-configuration. This is a
 * convenience only — an app driving multiple gateways should build one {@code DinstarClient} per
 * gateway itself rather than using this.
 *
 * <p>Example {@code application.yml} (values are placeholders — supply your own):
 * <pre>{@code
 * dinstar:
 *   base-url: https://gateway.example.com
 *   username: <username>
 *   password: <password>
 *   auth-scheme: digest      # or basic
 *   verify-tls: false
 *   connect-timeout: 5s
 *   response-timeout: 15s
 * }</pre>
 */
@ConfigurationProperties(prefix = "dinstar")
public class DinstarProperties {

    /** Gateway base URL, e.g. {@code https://gateway.example.com}. Required to activate the auto-config. */
    private String baseUrl;

    /** Auth username (required — no default is baked in). */
    private String username;

    /** Auth password (required — no default is baked in). */
    private String password;

    /** {@code basic} or {@code digest} (default {@code digest}). */
    private AuthScheme authScheme = AuthScheme.DIGEST;

    /** TCP connect timeout (default 5s). */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** Socket read timeout (default 15s). */
    private Duration responseTimeout = Duration.ofSeconds(15);

    /** Whether to verify the server TLS certificate (default {@code false} — box ships self-signed). */
    private boolean verifyTls = false;

    /** @return the gateway base URL */
    public String getBaseUrl() {
        return baseUrl;
    }

    /** @param baseUrl the gateway base URL */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** @return the auth username */
    public String getUsername() {
        return username;
    }

    /** @param username the auth username */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the auth password */
    public String getPassword() {
        return password;
    }

    /** @param password the auth password */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return the auth scheme */
    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    /** @param authScheme the auth scheme */
    public void setAuthScheme(AuthScheme authScheme) {
        this.authScheme = authScheme;
    }

    /** @return the connect timeout */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /** @param connectTimeout the connect timeout */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /** @return the response (read) timeout */
    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    /** @param responseTimeout the response (read) timeout */
    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    /** @return whether TLS certificate verification is enabled */
    public boolean isVerifyTls() {
        return verifyTls;
    }

    /** @param verifyTls whether to verify the server TLS certificate */
    public void setVerifyTls(boolean verifyTls) {
        this.verifyTls = verifyTls;
    }
}
