package io.github.zodicslanser.dinstar;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable per-gateway configuration for a {@link DinstarClient}.
 *
 * <p>The consuming application runs three gateways and builds one client per gateway id, so this is
 * a plain value object, not global Spring state. Everything a caller might tune is here — base URL,
 * http vs https, credentials, auth scheme, connect/read timeouts, and TLS verification — with sane
 * defaults for the UC2000-VG out of the box.
 *
 * <p>Build one with the {@link #builder()} (values below are placeholders — supply your own):
 * <pre>{@code
 * DinstarConfig cfg = DinstarConfig.builder()
 *         .baseUrl("https://gateway.example.com") // your gateway's address; http:// also works
 *         .username("<username>")
 *         .password("<password>")
 *         .authScheme(AuthScheme.DIGEST)
 *         .verifyTls(false)                       // box ships a self-signed cert
 *         .connectTimeout(Duration.ofSeconds(5))
 *         .responseTimeout(Duration.ofSeconds(15))
 *         .build();
 * }</pre>
 *
 * @param baseUrl         gateway base URL, e.g. {@code https://gateway.example.com}. A trailing
 *                        {@code /} is trimmed. Endpoint paths ({@code /api/send_ussd},
 *                        {@code /GetSTKView}, …) are appended verbatim.
 * @param username        auth username (required — no default is baked in).
 * @param password        auth password (required — no default is baked in).
 * @param authScheme      {@link AuthScheme#BASIC} or {@link AuthScheme#DIGEST}; the caller decides.
 * @param connectTimeout  TCP connect timeout.
 * @param responseTimeout socket read timeout (time to first response byte). Keep this short for
 *                        {@code send_ussd} — the USSD <em>reply</em> is asynchronous and must NOT be
 *                        waited on here; poll {@code query_ussd_reply} or use the push webhook.
 * @param verifyTls       whether to verify the server TLS certificate. Defaults to {@code false}
 *                        because the box ships a self-signed cert; when {@code false} the trust
 *                        relaxation is scoped to this client's connection manager only, never the JVM.
 */
public record DinstarConfig(
        String baseUrl,
        String username,
        String password,
        AuthScheme authScheme,
        Duration connectTimeout,
        Duration responseTimeout,
        boolean verifyTls
) {
    /** Validates and normalizes the config (trims a trailing slash off {@code baseUrl}). */
    public DinstarConfig {
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(authScheme, "authScheme");
        Objects.requireNonNull(connectTimeout, "connectTimeout");
        Objects.requireNonNull(responseTimeout, "responseTimeout");
        if (baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        baseUrl = baseUrl.strip();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
    }

    /**
     * A new builder pre-populated with only the safe, non-identifying UC2000-VG defaults:
     * {@link AuthScheme#DIGEST}, 5s connect / 15s response timeouts, and {@code verifyTls=false}
     * (self-signed cert). No address or credentials are defaulted — {@link Builder#baseUrl(String)},
     * {@link Builder#username(String)}, and {@link Builder#password(String)} are all required.
     *
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link DinstarConfig}. */
    public static final class Builder {
        private String baseUrl;
        private String username;
        private String password;
        private AuthScheme authScheme = AuthScheme.DIGEST;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration responseTimeout = Duration.ofSeconds(15);
        private boolean verifyTls = false;

        private Builder() {
        }

        /**
         * @param baseUrl gateway base URL (required), e.g. {@code https://gateway.example.com}
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * @param username auth username (required)
         * @return this builder
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * @param password auth password (required)
         * @return this builder
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * @param authScheme {@link AuthScheme#BASIC} or {@link AuthScheme#DIGEST} (default DIGEST)
         * @return this builder
         */
        public Builder authScheme(AuthScheme authScheme) {
            this.authScheme = authScheme;
            return this;
        }

        /**
         * @param connectTimeout TCP connect timeout (default 5s)
         * @return this builder
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * @param responseTimeout socket read timeout (default 15s)
         * @return this builder
         */
        public Builder responseTimeout(Duration responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        /**
         * @param verifyTls whether to verify the server TLS certificate (default {@code false} —
         *                  the box ships self-signed)
         * @return this builder
         */
        public Builder verifyTls(boolean verifyTls) {
            this.verifyTls = verifyTls;
            return this;
        }

        /**
         * @return the immutable {@link DinstarConfig}
         * @throws NullPointerException     if a required field is null
         * @throws IllegalArgumentException if {@code baseUrl} is blank
         */
        public DinstarConfig build() {
            return new DinstarConfig(baseUrl, username, password, authScheme,
                    connectTimeout, responseTimeout, verifyTls);
        }
    }
}
