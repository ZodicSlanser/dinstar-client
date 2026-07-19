package io.github.zodicslanser.dinstar.push;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The payload of a {@code device} push (a single object, not an array).
 *
 * <p>Types inferred from the manual example. Note the JSON keys {@code IP} and {@code MAC} are
 * uppercase.
 *
 * @param portNumber total number of ports on the chassis (e.g. 32)
 * @param ip         device IP (JSON key {@code IP})
 * @param mac        device MAC, dash-separated (JSON key {@code MAC})
 * @param status     {@code "power_on"} or {@code "power_off"}
 */
public record DeviceEvent(
        int portNumber,
        @JsonProperty("IP") String ip,
        @JsonProperty("MAC") String mac,
        String status
) {
}
