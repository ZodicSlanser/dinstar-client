package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@code command} field of a {@code send_ussd} request.
 */
public enum UssdCommand {
    /** Start (or continue) a USSD session. Requires non-empty {@code text}. */
    SEND("send"),
    /**
     * Force-tear-down a stuck USSD session and unlock the port. Sent with empty {@code text}.
     * Note this is spelled {@code cancel} — distinct from {@link StkAction#CANCEL}, which the
     * gateway spells {@code cancle}.
     */
    CANCEL("cancel");

    private final String wire;

    UssdCommand(String wire) {
        this.wire = wire;
    }

    /** @return the exact string the gateway expects */
    @JsonValue
    public String wire() {
        return wire;
    }
}
