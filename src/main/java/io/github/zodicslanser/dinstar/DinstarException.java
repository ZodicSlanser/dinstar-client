package io.github.zodicslanser.dinstar;

/**
 * Thrown when a Dinstar API call fails at the transport layer or the gateway rejects the request
 * body with a hard-error envelope {@code error_code}.
 *
 * <p><strong>What this exception does and does NOT represent.</strong> This library branches on the
 * response <em>body</em>, never on the HTTP transport status. Two things can go wrong:
 * <ol>
 *   <li><b>Transport / auth failure</b> — connection refused, timeout, TLS error, or a {@code 401}
 *       that authentication could not satisfy → {@link #transport(String, Throwable)}.</li>
 *   <li><b>Envelope error_code</b> — the gateway parsed the request but rejected it with a hard
 *       error: {@code 400} illegal request, {@code 404} not found, {@code 413} payload too large,
 *       {@code 500} other, {@code 550} no available port → {@link #api(String, Integer, String, String)}.</li>
 * </ol>
 *
 * <p><strong>This is NOT how per-port send failures surface.</strong> An {@code error_code:202}
 * means the gateway <em>accepted</em> the payload — that call returns a normal response object. The
 * real per-port outcome (200 sent / 486 busy / 503 not-registered) lives in the typed
 * {@code result[]} of that response and is never thrown. See
 * {@link io.github.zodicslanser.dinstar.model.PortResult}.
 */
public class DinstarException extends RuntimeException {

    /** The endpoint path that failed, e.g. {@code /api/send_ussd}. */
    private final String endpoint;

    /** The gateway envelope {@code error_code}, or {@code null} for a transport/parse failure. */
    private final Integer errorCode;

    /** The gateway serial {@code sn} if present in the error body, else {@code null}. */
    private final String sn;

    /** The raw response body if one was received, else {@code null}. */
    private final String rawBody;

    private DinstarException(String message, String endpoint, Integer errorCode,
                            String sn, String rawBody, Throwable cause) {
        super(message, cause);
        this.endpoint = endpoint;
        this.errorCode = errorCode;
        this.sn = sn;
        this.rawBody = rawBody;
    }

    /**
     * A transport-layer failure (no usable response body): connection, timeout, TLS, or
     * unsatisfiable authentication.
     *
     * @param endpoint the endpoint path attempted
     * @param cause    the underlying I/O or protocol exception
     * @return a new {@link DinstarException}
     */
    public static DinstarException transport(String endpoint, Throwable cause) {
        return new DinstarException(
                "Transport failure calling " + endpoint + ": " + cause.getMessage(),
                endpoint, null, null, null, cause);
    }

    /**
     * A hard-error envelope {@code error_code} (anything other than 200/202) returned by the gateway.
     *
     * @param endpoint  the endpoint path
     * @param errorCode the envelope {@code error_code}
     * @param sn        the gateway serial, or {@code null}
     * @param rawBody   the raw response body
     * @return a new {@link DinstarException}
     */
    public static DinstarException api(String endpoint, Integer errorCode, String sn, String rawBody) {
        return new DinstarException(
                "Gateway rejected " + endpoint + " with error_code=" + errorCode
                        + " (" + describe(errorCode) + ")",
                endpoint, errorCode, sn, rawBody, null);
    }

    /**
     * A response body that could not be parsed or was missing a required envelope field.
     *
     * @param endpoint the endpoint path
     * @param rawBody  the raw response body
     * @param cause    the parse failure, or {@code null}
     * @return a new {@link DinstarException}
     */
    public static DinstarException malformed(String endpoint, String rawBody, Throwable cause) {
        return new DinstarException(
                "Malformed response from " + endpoint,
                endpoint, null, null, rawBody, cause);
    }

    /** @return the endpoint path that failed */
    public String endpoint() {
        return endpoint;
    }

    /** @return the envelope {@code error_code}, or {@code null} for a transport/parse failure */
    public Integer errorCode() {
        return errorCode;
    }

    /** @return the gateway serial from the error body, or {@code null} */
    public String sn() {
        return sn;
    }

    /** @return the raw response body if one was received, else {@code null} */
    public String rawBody() {
        return rawBody;
    }

    /**
     * Human-readable meaning of a gateway envelope {@code error_code}.
     *
     * @param code the envelope {@code error_code} (may be {@code null})
     * @return a short description
     */
    public static String describe(Integer code) {
        if (code == null) {
            return "transport failure";
        }
        return switch (code) {
            case 200 -> "query OK";
            case 202 -> "accepted";
            case 400 -> "illegal request";
            case 404 -> "not found";
            case 413 -> "payload too large";
            case 500 -> "server error";
            case 550 -> "no available port";
            default -> "unexpected error";
        };
    }
}
