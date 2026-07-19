package io.github.zodicslanser.dinstar.model;

/**
 * One entry of the {@code result[]} from {@code query_sms_result} (and the {@code sms_result} push).
 *
 * @param port      the sending port, 0–31 verbatim
 * @param userId    the correlation id echoed from the {@code send_sms} request (may be {@code null}
 *                  in a push); this is the only real SMS correlation handle
 * @param number    destination number
 * @param time      time of sending, {@code yyyy-MM-dd HH:mm:ss}
 * @param status    lifecycle status (see {@link SmsStatus})
 * @param count     number of segments the message was split into
 * @param succCount number of segments sent successfully
 * @param refId     first reference id (0–255) used to match a later delivery receipt
 * @param imsi      IMSI of the SIM used
 */
public record SmsResult(
        int port,
        Integer userId,
        String number,
        String time,
        SmsStatus status,
        int count,
        int succCount,
        int refId,
        String imsi
) {
}
