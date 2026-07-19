package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * SMS text encoding, controlling the per-message character limit.
 */
public enum SmsEncoding {
    /** UCS2 / Unicode: 70 characters per segment. Gateway default. */
    UNICODE("unicode"),
    /** GSM 7-bit: 160 characters per segment. */
    GSM_7BIT("gsm-7bit");

    private final String wire;

    SmsEncoding(String wire) {
        this.wire = wire;
    }

    /** @return the exact string the gateway expects (e.g. {@code gsm-7bit}) */
    @JsonValue
    public String wire() {
        return wire;
    }
}
