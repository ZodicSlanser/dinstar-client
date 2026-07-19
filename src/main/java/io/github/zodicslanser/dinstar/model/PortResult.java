package io.github.zodicslanser.dinstar.model;

/**
 * One entry of the {@code result[]} array returned by {@code send_ussd} / {@code send_sms} — the
 * per-port outcome, which is the <em>authoritative</em> layer of the two-layer status model.
 *
 * <p>This type deliberately has <strong>no</strong> {@code isSuccess()} that keys off the envelope.
 * The only way to learn whether a port sent is {@link #sendStatus()} / {@link #isSent()}, so a caller
 * cannot mistake an accepted-but-busy ({@code 486}) or not-registered ({@code 503}) port for a send.
 *
 * @param port   the port number, 0–31, verbatim (never adjusted by ±1)
 * @param status the raw per-port status code: {@code 200} sent, {@code 486} busy, {@code 503} not
 *               registered
 */
public record PortResult(int port, int status) {

    /**
     * The typed per-port outcome.
     *
     * @return {@link PortSendStatus#SENT} only for {@code 200}; {@link PortSendStatus#PORT_BUSY},
     *         {@link PortSendStatus#NOT_REGISTERED}, or {@link PortSendStatus#UNKNOWN} otherwise
     */
    public PortSendStatus sendStatus() {
        return PortSendStatus.fromCode(status);
    }

    /**
     * Whether this port actually sent. The single source of send truth.
     *
     * @return {@code true} only when {@link #status()} is {@code 200}
     */
    public boolean isSent() {
        return status == 200;
    }
}
