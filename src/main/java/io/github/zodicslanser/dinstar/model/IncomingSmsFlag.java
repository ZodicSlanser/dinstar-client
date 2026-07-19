package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@code flag} of a {@code query_incoming_sms} request — which messages to return.
 *
 * <p><strong>Trap:</strong> {@link #UNREAD} (the gateway default) <em>mutates state</em>: the
 * messages it returns are marked read, so a crash mid-processing loses them. For reliable ingestion
 * prefer polling by a high-water mark ({@code incoming_sms_id}) with {@link #ALL}, and dedup on
 * {@code (sn, incoming_sms_id)}.
 */
public enum IncomingSmsFlag {
    /** Unread messages only. Reading marks them read (state mutation — see class note). */
    UNREAD("unread"),
    /** Already-read messages only. */
    READ("read"),
    /** All messages regardless of read state (does not mutate). */
    ALL("all");

    private final String wire;

    IncomingSmsFlag(String wire) {
        this.wire = wire;
    }

    /** @return the exact string the gateway expects */
    @JsonValue
    public String wire() {
        return wire;
    }
}
