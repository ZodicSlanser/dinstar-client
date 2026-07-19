package io.github.zodicslanser.dinstar.model;

import java.util.List;
import java.util.Optional;

/**
 * Response to {@code send_ussd}.
 *
 * <p><strong>Two-layer status.</strong> {@link #errorCode()} is the <em>envelope</em>: {@code 202}
 * means the gateway accepted the payload (a hard rejection like {@code 400} would already have thrown
 * {@link io.github.zodicslanser.dinstar.DinstarException} before you got here). The real per-port outcome lives in
 * {@link #result()} — a list of {@link PortResult} whose {@link PortResult#isSent()} is the only
 * success signal. There is deliberately no {@code isSuccess()} on this type: a caller must look at the
 * per-port results and therefore cannot read a {@code 486}/{@code 503} as sent.
 *
 * @param errorCode the envelope status ({@code 202} = accepted)
 * @param sn        the gateway serial
 * @param result    per-port outcomes; may be {@code null} if the gateway returned no array
 */
public record SendUssdResponse(int errorCode, String sn, List<PortResult> result) {

    /** @return {@code true} if the gateway accepted the payload ({@code error_code == 202}) */
    public boolean isAccepted() {
        return errorCode == 202;
    }

    /**
     * The per-port result for a specific port.
     *
     * @param port the port, 0–31
     * @return the {@link PortResult}, or empty if that port is not in {@link #result()}
     */
    public Optional<PortResult> resultFor(int port) {
        if (result == null) {
            return Optional.empty();
        }
        return result.stream().filter(r -> r.port() == port).findFirst();
    }

    /**
     * Whether every port in {@link #result()} sent successfully. With an empty/absent result array
     * this is {@code false} — nothing is proven sent.
     *
     * @return {@code true} only if there is at least one result and all report {@code 200}
     */
    public boolean allSent() {
        return result != null && !result.isEmpty() && result.stream().allMatch(PortResult::isSent);
    }
}
