package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * Response to {@code query_sms_deliver_status}.
 *
 * @param errorCode the envelope status ({@code 200} = query OK)
 * @param sn        the gateway serial
 * @param result    per-message delivery receipts; may be {@code null}
 */
public record QuerySmsDeliverStatusResponse(int errorCode, String sn, List<SmsDeliverStatus> result) {
}
