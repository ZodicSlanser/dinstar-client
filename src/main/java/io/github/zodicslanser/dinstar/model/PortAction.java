package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@code action} of a {@code set_port_info} request. Most callers use the friendly methods on
 * {@code DinstarClient} ({@code resetPort}, {@code powerPort}, {@code selectSlot},
 * {@code setCallForward}, {@code checkCallForward}) rather than this enum directly.
 */
public enum PortAction {
    /** Soft reset of the module. Violently kills any in-flight USSD/SMS on the port. */
    RESET("reset"),
    /** Hard power on/off (with {@code param=on|off}); a full power cycle takes 10–30s to re-register. */
    POWER("power"),
    /** Select the active SIM slot (multi-SIM gateways only), with {@code param=0..3}. */
    SLOT("slot"),
    /** Configure call forwarding, with {@code param=<CallForwardType>} and {@code number}. */
    CALL_FORWARD("CallForward"),
    /** Ask the network to report the current call-forward setting (read back via get_port_info). */
    CHECK_CALL_FORWARD("CheckCallForward");

    private final String wire;

    PortAction(String wire) {
        this.wire = wire;
    }

    /** @return the exact string the gateway expects */
    @JsonValue
    public String wire() {
        return wire;
    }
}
