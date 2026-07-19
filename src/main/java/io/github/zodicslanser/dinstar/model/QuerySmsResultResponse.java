package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * Response to {@code query_sms_result}.
 *
 * @param errorCode the envelope status ({@code 200} = query OK)
 * @param sn        the gateway serial
 * @param result    per-message results; may be {@code null}
 */
public record QuerySmsResultResponse(int errorCode, String sn, List<SmsResult> result) {
}
