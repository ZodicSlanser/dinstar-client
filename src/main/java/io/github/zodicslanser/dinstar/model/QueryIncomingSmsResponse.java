package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * Response to {@code query_incoming_sms}.
 *
 * @param errorCode the envelope status ({@code 200} = query OK)
 * @param sn        the gateway serial
 * @param sms       the received messages; may be {@code null} or empty
 * @param read      count of messages in the read state after this call
 * @param unread    count of messages still unread
 */
public record QueryIncomingSmsResponse(
        int errorCode,
        String sn,
        List<IncomingSms> sms,
        Integer read,
        Integer unread
) {
}
