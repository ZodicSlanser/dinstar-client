package io.github.zodicslanser.dinstar.model;

/**
 * One delivery receipt, from {@code query_sms_deliver_status} (and the {@code sms_deliver_status}
 * push). Matched back to a send via {@link #refId()}.
 *
 * @param port       the sending port, 0–31 verbatim
 * @param number     destination number
 * @param time       time of sending, {@code yyyy-MM-dd HH:mm:ss}
 * @param refId      reference id matching the {@code ref_id} from {@code query_sms_result}
 * @param statusCode delivery code: {@code 0} received by peer, {@code 32–63} temporary error,
 *                   {@code 64–255} permanent error
 * @param imsi       IMSI of the SIM used
 */
public record SmsDeliverStatus(
        int port,
        String number,
        String time,
        int refId,
        int statusCode,
        String imsi
) {
    /** @return {@code true} when {@link #statusCode()} is {@code 0} (received by the peer) */
    public boolean isDelivered() {
        return statusCode == 0;
    }

    /** @return {@code true} for a permanent failure ({@code statusCode} 64–255) */
    public boolean isPermanentFailure() {
        return statusCode >= 64 && statusCode <= 255;
    }
}
