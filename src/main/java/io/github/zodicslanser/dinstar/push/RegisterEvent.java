package io.github.zodicslanser.dinstar.push;

/**
 * One SIM registration change in a {@code register} push.
 *
 * <p>Field types are inferred from the manual's example (no typed table). On a {@code "down"} event
 * {@code iccid}/{@code imsi} arrive as the literal string {@code "<NULL>"} (not JSON {@code null}) —
 * treat that value as absent. {@code slot} is present only on multi-SIM gateways.
 *
 * @param port     the port, 0–31 verbatim
 * @param iccid    SIM ICCID, or the literal {@code "<NULL>"} when down
 * @param imsi     SIM IMSI, or the literal {@code "<NULL>"} when down
 * @param number   mobile number
 * @param status   {@code "up"} or {@code "down"}
 * @param sequence event sequence counter
 * @param slot     multi-SIM slot, or {@code null} on single-SIM gateways
 */
public record RegisterEvent(
        int port,
        String iccid,
        String imsi,
        String number,
        String status,
        int sequence,
        Integer slot
) {
    /** The sentinel the gateway uses for an absent iccid/imsi in a "down" event. */
    public static final String NULL_SENTINEL = "<NULL>";

    /** @return {@code true} if this is an "up" (registered) event */
    public boolean isUp() {
        return "up".equalsIgnoreCase(status);
    }
}
