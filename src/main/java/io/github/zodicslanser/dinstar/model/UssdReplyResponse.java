package io.github.zodicslanser.dinstar.model;

import java.util.List;
import java.util.Optional;

/**
 * Response to {@code query_ussd_reply}.
 *
 * <p>Each {@link UssdReply} carries only {@code port} and {@code text} — <strong>no correlation
 * id</strong>. Nothing ties a reply to a specific {@code send_ussd}; the caller owns per-port
 * serialization (see {@link UssdReply}). An empty/single-space text means "nothing yet".
 *
 * @param errorCode the envelope status ({@code 200} = query OK)
 * @param sn        the gateway serial
 * @param reply     the replies, one per queried port; may be {@code null}
 */
public record UssdReplyResponse(int errorCode, String sn, List<UssdReply> reply) {

    /**
     * The reply for a specific port.
     *
     * @param port the port, 0–31
     * @return the {@link UssdReply}, or empty if that port is not present
     */
    public Optional<UssdReply> replyFor(int port) {
        if (reply == null) {
            return Optional.empty();
        }
        return reply.stream().filter(r -> r.port() == port).findFirst();
    }
}
