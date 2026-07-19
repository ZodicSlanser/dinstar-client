package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Call-forward condition, the {@code param} of a {@code set_port_info?action=CallForward} request.
 * These are also the keys of the {@code CallForwarding} object returned by
 * {@code get_port_info?info_type=CallForward}.
 */
public enum CallForwardType {
    /** Forward all calls unconditionally. */
    UNCONDITIONAL("Unconditional"),
    /** Forward when the call is not answered. */
    NO_REPLY("NoReply"),
    /** Forward when the line is busy. */
    BUSY("Busy"),
    /** Forward when the phone is unreachable. */
    NOT_REACHABLE("Not_Reachable"),
    /** Cancel all forwarding rules. */
    CANCEL_ALL("CancelAll");

    private final String wire;

    CallForwardType(String wire) {
        this.wire = wire;
    }

    /** @return the exact string the gateway expects ({@code Not_Reachable}, etc.) */
    @JsonValue
    public String wire() {
        return wire;
    }
}
