package io.github.zodicslanser.dinstar.model;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Body of a {@code POST /api/send_ussd} request.
 *
 * <h2>Read before use — three traps</h2>
 * <ol>
 *   <li><b>Two-layer status.</b> The response envelope {@code error_code:202} means only that the
 *       gateway <em>accepted</em> this payload. Whether each port actually sent is in the response's
 *       {@code result[]} ({@link PortResult}). A {@code 486}/{@code 503} there is a failure — never
 *       read {@code 202} as "sent".</li>
 *   <li><b>Verbatim text.</b> {@link #text()} is passed through byte-for-byte. The library never
 *       rewrites, sanitizes, or re-encodes it. A trailing {@code #} is <em>required</em> by most
 *       carriers and is <em>never</em> auto-appended — include it yourself. Leading {@code #} and
 *       embedded {@code *} are legal (e.g. a dealer top-up string like {@code #123*3*...#}). The only
 *       rule is a hard 60-byte limit.</li>
 *   <li><b>Port array = BROADCAST.</b> {@link #port()} is a list; every listed port receives the
 *       <em>same</em> USSD string. Use {@link #send(int, String)} for the normal single-port case.
 *       {@link #broadcast(List, String)} exists so a multi-port send is always an explicit,
 *       eyes-open choice — never fire a financial USSD to every port by accident.</li>
 * </ol>
 *
 * @param port    the target ports, 0–31 verbatim; a list of more than one is a broadcast
 * @param command {@link UssdCommand#SEND} to start/continue a session, {@link UssdCommand#CANCEL}
 *                to tear one down
 * @param text    the USSD string, verbatim, max 60 bytes; empty only for a cancel
 */
public record SendUssdRequest(List<Integer> port, UssdCommand command, String text) {

    /** Maximum USSD text length the gateway accepts, in bytes (UTF-8). */
    public static final int MAX_TEXT_BYTES = 60;

    /** Validates the ports and the 60-byte text limit without altering the text. */
    public SendUssdRequest {
        Objects.requireNonNull(port, "port");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(text, "text");
        if (port.isEmpty()) {
            throw new IllegalArgumentException("at least one port is required");
        }
        for (Integer p : port) {
            if (p == null || p < 0 || p > 31) {
                throw new IllegalArgumentException("port must be 0..31, was " + p);
            }
        }
        int bytes = text.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_TEXT_BYTES) {
            throw new IllegalArgumentException(
                    "USSD text exceeds " + MAX_TEXT_BYTES + " bytes (" + bytes + "); not truncating");
        }
        if (command == UssdCommand.SEND && text.isEmpty()) {
            throw new IllegalArgumentException("send requires non-empty text");
        }
        port = List.copyOf(port);
    }

    /**
     * A single-port send — the normal, safe path.
     *
     * @param port a single port, 0–31
     * @param text the USSD string, verbatim (include your own trailing {@code #}), max 60 bytes
     * @return the request
     */
    public static SendUssdRequest send(int port, String text) {
        return new SendUssdRequest(List.of(port), UssdCommand.SEND, text);
    }

    /**
     * A <strong>broadcast</strong> send: the same USSD string to every listed port. Named
     * explicitly so this is never done by accident — do not use for financial transactions.
     *
     * @param ports the ports to broadcast to, each 0–31
     * @param text  the USSD string, verbatim, max 60 bytes
     * @return the request
     */
    public static SendUssdRequest broadcast(List<Integer> ports, String text) {
        return new SendUssdRequest(ports, UssdCommand.SEND, text);
    }

    /**
     * Force-tear-down a stuck session on a port and unlock it (empty text, {@code command=cancel}).
     *
     * @param port the port to cancel, 0–31
     * @return the request
     */
    public static SendUssdRequest cancel(int port) {
        return new SendUssdRequest(List.of(port), UssdCommand.CANCEL, "");
    }
}
