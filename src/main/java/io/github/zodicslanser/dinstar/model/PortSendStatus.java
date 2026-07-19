package io.github.zodicslanser.dinstar.model;

/**
 * The per-port outcome of a {@code send_ussd} / {@code send_sms} request — the <em>second</em> and
 * authoritative layer of the two-layer status model.
 *
 * <p>The envelope {@code error_code:202} only means the gateway <em>accepted</em> the payload. The
 * real result of reaching the tower is this per-port code, carried in {@link PortResult}. Reading a
 * {@code 202} as "sent" is exactly the legacy bug this library exists to prevent — only
 * {@link #SENT} is success.
 *
 * @see PortResult
 */
public enum PortSendStatus {
    /** {@code 200} — sent successfully. The only success value. */
    SENT(200),
    /** {@code 486} — the port is busy (e.g. an SMS is already sending on it). Not sent. */
    PORT_BUSY(486),
    /** {@code 503} — the port is not registered on the network. Not sent. */
    NOT_REGISTERED(503),
    /** Any other / unrecognised status code. Treat as not sent. */
    UNKNOWN(-1);

    private final int code;

    PortSendStatus(int code) {
        this.code = code;
    }

    /** @return the numeric wire status code (e.g. {@code 486}), or {@code -1} for {@link #UNKNOWN} */
    public int code() {
        return code;
    }

    /**
     * Maps a raw per-port status code to its enum.
     *
     * @param code the numeric status from a {@code result[]} entry
     * @return the matching constant, or {@link #UNKNOWN} if unrecognised
     */
    public static PortSendStatus fromCode(int code) {
        for (PortSendStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return UNKNOWN;
    }
}
