package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * One entry of the {@code info[]} array from {@code get_port_info}. Only the fields named in the
 * request's {@code info_type} are populated; the rest are {@code null}.
 *
 * <p>{@link #reg()} and {@link #callstate()} are kept as raw strings for forward-compatibility;
 * use {@link RegisterStatus#fromWire(String)} to branch on registration. {@link #remainCredit()} is
 * a decimal <em>string</em> as the operator returns it (may be empty).
 *
 * @param port              the port, 0–31 verbatim
 * @param type              module type (e.g. {@code GSM}) or {@code null}
 * @param imei              module IMEI or {@code null}
 * @param imsi              SIM IMSI or {@code null}
 * @param iccid             SIM ICCID or {@code null}
 * @param number            configured MSISDN or {@code null}
 * @param reg               registration status string (see {@link RegisterStatus}) or {@code null}
 * @param slot              active SIM slot ({@code 255} = not multi-SIM) or {@code null}
 * @param callstate         current call state or {@code null}
 * @param signal            signal strength 0–31 or {@code null}
 * @param gprs              GPRS attachment state or {@code null}
 * @param remainCredit      remaining prepaid credit as a decimal string or {@code null}
 * @param callForwarding    for {@code info_type=CallForward}: a map of forward type
 *                          (e.g. {@code Unconditional}) to target number; {@code null} otherwise
 */
public record PortInfo(
        int port,
        String type,
        String imei,
        String imsi,
        String iccid,
        String number,
        String reg,
        Integer slot,
        String callstate,
        Integer signal,
        String gprs,
        String remainCredit,
        @JsonProperty("CallForwarding") Map<String, String> callForwarding
) {
}
