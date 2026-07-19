package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@code action} field of an {@code STKGo} request (SIM Toolkit navigation).
 */
public enum StkAction {
    /** Confirm / proceed on the current STK frame. */
    OK("ok"),
    /**
     * Cancel the current STK operation.
     *
     * <p><strong>Deliberate misspelling.</strong> The gateway's wire value is {@code cancle}, not
     * {@code cancel}. This is the value the firmware actually expects; do not "correct" it. Contrast
     * {@link UssdCommand#CANCEL}, which is correctly spelled {@code cancel} for a different endpoint.
     */
    CANCEL("cancle"),
    /** Return to the STK root menu. */
    HOME("home");

    private final String wire;

    StkAction(String wire) {
        this.wire = wire;
    }

    /** @return the exact string the gateway expects ({@code cancle} for {@link #CANCEL}) */
    @JsonValue
    public String wire() {
        return wire;
    }
}
