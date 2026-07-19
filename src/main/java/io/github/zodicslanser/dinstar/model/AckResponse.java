package io.github.zodicslanser.dinstar.model;

/**
 * The plain acknowledgement envelope returned by endpoints that carry no per-item array:
 * {@code set_port_info} (reset / power / slot / call-forward), {@code stop_sms}, and {@code STKGo}.
 *
 * <p>If the gateway had rejected the request, a {@link io.github.zodicslanser.dinstar.DinstarException} would have
 * been thrown before this object was produced, so reaching an {@code AckResponse} already means the
 * envelope was {@code 200}/{@code 202}.
 *
 * @param errorCode the envelope status
 * @param sn        the gateway serial
 */
public record AckResponse(int errorCode, String sn) {
}
