package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;

/**
 * Body of a {@code POST /api/send_sms} request.
 *
 * <p>Like {@code send_ussd}, the response is two-layer: the envelope {@code error_code:202} means
 * accepted and returns a {@code task_id}; the actual delivery is learned later via
 * {@code query_sms_result} (keyed by {@link SmsRecipient#userId()}).
 *
 * <p>{@code null} fields are omitted on the wire, but {@link #requestStatusReport()} is serialised
 * even when {@code false}. Limits enforced by the gateway: at most 128 numbers and ~1500 bytes per
 * request (a {@code 413} otherwise).
 *
 * @param text                the message body; may contain {@code #param#} placeholders filled from
 *                            each recipient's {@link SmsRecipient#textParam()}
 * @param port                ports to send from, 0–31 verbatim; {@code null} to let the gateway pick
 * @param param               the recipients
 * @param encoding            {@link SmsEncoding#UNICODE} (70 chars/segment, default) or
 *                            {@link SmsEncoding#GSM_7BIT} (160)
 * @param requestStatusReport whether to request delivery receipts (kept even when {@code false})
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendSmsRequest(
        String text,
        List<Integer> port,
        List<SmsRecipient> param,
        SmsEncoding encoding,
        Boolean requestStatusReport
) {
    /** Validates recipients and ports. */
    public SendSmsRequest {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(param, "param");
        if (param.isEmpty()) {
            throw new IllegalArgumentException("at least one recipient is required");
        }
        if (port != null) {
            for (Integer p : port) {
                if (p == null || p < 0 || p > 31) {
                    throw new IllegalArgumentException("port must be 0..31, was " + p);
                }
            }
            port = List.copyOf(port);
        }
        param = List.copyOf(param);
    }

    /**
     * A minimal request: one message to a set of recipients, Unicode, with delivery reports, letting
     * the gateway choose the port.
     *
     * @param text  the message body
     * @param param the recipients
     * @return the request
     */
    public static SendSmsRequest of(String text, List<SmsRecipient> param) {
        return new SendSmsRequest(text, null, param, SmsEncoding.UNICODE, true);
    }
}
