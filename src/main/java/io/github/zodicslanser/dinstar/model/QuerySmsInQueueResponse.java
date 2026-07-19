package io.github.zodicslanser.dinstar.model;

/**
 * Response to {@code query_sms_in_queue} — the current send backlog.
 *
 * @param errorCode the envelope status ({@code 200} = query OK)
 * @param sn        the gateway serial
 * @param inQueue   number of SMS segments waiting to be sent
 */
public record QuerySmsInQueueResponse(int errorCode, String sn, int inQueue) {
}
