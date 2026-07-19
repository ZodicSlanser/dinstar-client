package io.github.zodicslanser.dinstar.model;

/**
 * Response to {@code send_sms}.
 *
 * <p>{@link #errorCode()} {@code 202} means accepted for sending — not delivered. Use
 * {@code query_sms_result} (keyed by the recipient {@code user_id}) or the {@code sms_result} push
 * for the actual outcome. {@link #taskId()} can be passed to {@code stop_sms} to cancel a queued task.
 *
 * @param errorCode  the envelope status ({@code 202} = accepted)
 * @param sn         the gateway serial
 * @param smsInQueue number of SMS segments now queued on the gateway
 * @param taskId     id of the created send task (for {@code stop_sms})
 */
public record SendSmsResponse(int errorCode, String sn, Integer smsInQueue, Integer taskId) {

    /** @return {@code true} if the gateway accepted the SMS for sending ({@code error_code == 202}) */
    public boolean isAccepted() {
        return errorCode == 202;
    }
}
