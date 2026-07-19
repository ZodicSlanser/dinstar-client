package io.github.zodicslanser.dinstar;

/**
 * HTTP authentication scheme the {@link DinstarClient} uses against a gateway.
 *
 * <p>The caller decides — neither scheme is hardcoded. Every vendor example authenticates with
 * {@code curl -k --anyauth -u <user>:<pass>}, so the box accepts whichever the client offers:
 *
 * <ul>
 *   <li>{@link #BASIC} — credentials are sent <em>preemptively</em> ({@code Authorization: Basic ...}
 *       on the first request), so no challenge round-trip is needed.</li>
 *   <li>{@link #DIGEST} — Apache HttpClient 5's built-in {@code DigestScheme} answers the gateway's
 *       {@code WWW-Authenticate: Digest} challenge with a correct incrementing {@code nc}, a random
 *       per-request {@code cnonce}, and an {@code ha2} computed over the real request-URI (so the
 *       {@code send_ussd} vs {@code query_ussd_reply?port=...} query-string difference is handled
 *       for free). This library never hand-rolls Digest.</li>
 * </ul>
 */
public enum AuthScheme {
    /** Preemptive HTTP Basic authentication. */
    BASIC,
    /** Reactive HTTP Digest authentication via HttpClient 5's built-in {@code DigestScheme}. */
    DIGEST
}
