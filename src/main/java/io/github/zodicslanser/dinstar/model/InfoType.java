package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A field selector for {@code get_port_info}. Pass one or more; they are joined comma-separated on
 * the wire ({@code info_type=reg,signal,callstate}). Each selected type populates the matching field
 * of {@link PortInfo}.
 */
public enum InfoType {
    /** Module type, e.g. {@code GSM}. */
    TYPE("type"),
    /** Module IMEI. */
    IMEI("imei"),
    /** SIM IMSI. */
    IMSI("imsi"),
    /** SIM ICCID. */
    ICCID("iccid"),
    /** MSISDN configured on the SIM (often empty). */
    NUMBER("number"),
    /** Registration status (see {@link RegisterStatus}). */
    REG("reg"),
    /** Active SIM slot ({@code 255} = not multi-SIM). */
    SLOT("slot"),
    /** Current call state (Idle, Processing, Ringing, Active, …). */
    CALLSTATE("callstate"),
    /** Signal strength, 0–31. */
    SIGNAL("signal"),
    /** GPRS attachment state ({@code attached}/{@code detached}). */
    GPRS("gprs"),
    /** Remaining prepaid credit as reported by the operator (a decimal string, may be empty). */
    REMAIN_CREDIT("remain_credit"),
    /** Remaining monthly credit. */
    REMAIN_MONTHLY_CREDIT("remain_monthly_credit"),
    /** Remaining daily credit. */
    REMAIN_DAILY_CREDIT("remain_daily_credit"),
    /** Remaining daily call time. */
    REMAIN_DAILY_CALLTIME("remain_daily_calltime"),
    /** Remaining hourly call time. */
    REMAIN_HOURLY_CALLTIME("remain_hourly_calltime"),
    /** Remaining daily connections. */
    REMAIN_DAILY_CONNECT("remain_daily_connect"),
    /** Current call-forward configuration (returns a {@code CallForwarding} object per port). */
    CALL_FORWARD("CallForward");

    private final String wire;

    InfoType(String wire) {
        this.wire = wire;
    }

    /** @return the exact string the gateway expects */
    @JsonValue
    public String wire() {
        return wire;
    }
}
