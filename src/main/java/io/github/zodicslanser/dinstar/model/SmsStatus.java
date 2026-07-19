package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Lifecycle status of a sent SMS, as reported by {@code query_sms_result} (and the {@code sms_result}
 * push). Deserialised leniently — an unrecognised value maps to {@link #UNKNOWN} rather than failing.
 */
public enum SmsStatus {
    /** Sending failed. */
    FAILED("FAILED"),
    /** Still being sent. */
    SENDING("SENDING"),
    /** Sent to the network successfully (not yet a delivery receipt). */
    SENT_OK("SENT_OK"),
    /** Delivery receipt received from the destination. */
    DELIVERED("DELIVERED"),
    /** Unrecognised status. */
    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String wire;

    SmsStatus(String wire) {
        this.wire = wire;
    }

    /** @return the exact wire string */
    @JsonValue
    public String wire() {
        return wire;
    }

    /**
     * @param value raw wire value
     * @return the matching constant, or {@link #UNKNOWN}
     */
    @JsonCreator
    public static SmsStatus fromWire(String value) {
        if (value != null) {
            for (SmsStatus s : values()) {
                if (s.wire.equalsIgnoreCase(value)) {
                    return s;
                }
            }
        }
        return UNKNOWN;
    }
}
