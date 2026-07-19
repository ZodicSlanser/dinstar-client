package io.github.zodicslanser.dinstar.model;

/**
 * One received SMS, from {@code query_incoming_sms} or the {@code sms} push.
 *
 * <p>Dedup on {@code (sn, incomingSmsId)} — the id is unique per gateway but may reset on a factory
 * reset or firmware flash, so the bare id is not globally unique across the fleet.
 *
 * @param incomingSmsId the gateway-local message id (monotonic high-water mark for polling)
 * @param port          the receiving port, 0–31 verbatim
 * @param number        sender's number
 * @param smsc          SMS center number
 * @param timestamp     receive time, {@code yyyy-MM-dd HH:mm:ss}
 * @param text          message body
 */
public record IncomingSms(
        int incomingSmsId,
        int port,
        String number,
        String smsc,
        String timestamp,
        String text
) {
}
