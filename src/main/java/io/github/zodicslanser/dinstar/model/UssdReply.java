package io.github.zodicslanser.dinstar.model;

/**
 * One asynchronous USSD reply, from {@code query_ussd_reply} or the {@code ussd} push.
 *
 * <p><strong>No correlation id exists.</strong> A reply carries only the {@code port} and the
 * {@code text} — nothing tying it back to a specific {@code send_ussd}. The caller owns per-port
 * serialization: hold a per-port lock across send-then-read so two sessions on one port cannot cross
 * replies. The library cannot and does not correlate for you.
 *
 * <p>An empty or single-space {@code text} means "nothing yet" and is indistinguishable from "the
 * session produced no reply".
 *
 * @param port the port the reply arrived on, 0–31 verbatim
 * @param text the raw USSD reply text (verbatim; may be empty or a single space)
 */
public record UssdReply(int port, String text) {
}
