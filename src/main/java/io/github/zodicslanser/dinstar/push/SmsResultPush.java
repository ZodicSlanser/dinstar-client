package io.github.zodicslanser.dinstar.push;

import io.github.zodicslanser.dinstar.model.SmsResult;

import java.util.List;

/**
 * Inbound {@code sms_result} push: SMS sending results. Mirrors {@code query_sms_result}. Note the
 * push example omits {@code user_id}, so {@link SmsResult#userId()} may be {@code null} here.
 *
 * @param sn        the gateway serial
 * @param smsResult the sending results (JSON key {@code sms_result})
 */
public record SmsResultPush(String sn, List<SmsResult> smsResult) {
}
