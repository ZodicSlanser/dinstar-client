package io.github.zodicslanser.dinstar.push;

import io.github.zodicslanser.dinstar.model.IncomingSms;

import java.util.List;

/**
 * Inbound {@code sms} push: incoming SMS messages. Mirrors {@code query_incoming_sms} but omits the
 * read/unread counts.
 *
 * @param sn  the gateway serial (dispatch key for a multi-gateway webhook)
 * @param sms the received messages
 */
public record SmsPush(String sn, List<IncomingSms> sms) {
}
