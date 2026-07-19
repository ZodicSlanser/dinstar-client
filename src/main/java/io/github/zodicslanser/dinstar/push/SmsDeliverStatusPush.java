package io.github.zodicslanser.dinstar.push;

import io.github.zodicslanser.dinstar.model.SmsDeliverStatus;

import java.util.List;

/**
 * Inbound {@code sms_deliver_status} push: SMS delivery receipts. Mirrors
 * {@code query_sms_deliver_status}; match to a send via {@code ref_id}.
 *
 * @param sn               the gateway serial
 * @param smsDeliverStatus the delivery receipts (JSON key {@code sms_deliver_status})
 */
public record SmsDeliverStatusPush(String sn, List<SmsDeliverStatus> smsDeliverStatus) {
}
