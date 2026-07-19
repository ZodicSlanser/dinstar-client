package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * One recipient of a {@code send_sms} request (an element of the {@code param[]} array).
 *
 * @param number    destination phone number
 * @param textParam values substituted into the message template's {@code #param#} placeholders,
 *                  in order; {@code null} if the {@code text} has no placeholders
 * @param userId    caller-chosen correlation id, echoed back by {@code query_sms_result}; {@code null}
 *                  if you don't need to correlate
 */
public record SmsRecipient(String number, List<String> textParam, Integer userId) {

    /**
     * A recipient with no template params and no correlation id.
     *
     * @param number destination phone number
     * @return the recipient
     */
    public static SmsRecipient to(String number) {
        return new SmsRecipient(number, null, null);
    }

    /**
     * A recipient with a correlation id.
     *
     * @param number destination phone number
     * @param userId correlation id echoed by {@code query_sms_result}
     * @return the recipient
     */
    public static SmsRecipient to(String number, int userId) {
        return new SmsRecipient(number, null, userId);
    }
}
