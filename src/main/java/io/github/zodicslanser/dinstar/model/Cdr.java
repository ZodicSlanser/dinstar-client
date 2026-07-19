package io.github.zodicslanser.dinstar.model;

/**
 * One call detail record, from {@code get_cdr} or the {@code cdr} push (push carries answered calls
 * only; use {@code get_cdr} for all).
 *
 * @param port               the port, 0–31 verbatim
 * @param startDate          call start time, {@code yyyy-MM-dd HH:mm:ss}
 * @param answerDate         call answer time, {@code yyyy-MM-dd HH:mm:ss}
 * @param duration           call duration in seconds
 * @param sourceNumber       source number
 * @param destinationNumber  destination number
 * @param direction          {@code gsm->ip}, {@code ip->gsm}, or {@code callback}
 * @param ip                 source IP of an {@code ip->gsm} call
 * @param codec              codec, e.g. {@code G.711U}
 * @param hangup             which party hung up ({@code called}, {@code calling}, or the gateway)
 * @param gsmCode            GSM-side hangup reason code
 * @param bcch               BCCH used for this call (may be empty)
 */
public record Cdr(
        int port,
        String startDate,
        String answerDate,
        int duration,
        String sourceNumber,
        String destinationNumber,
        String direction,
        String ip,
        String codec,
        String hangup,
        int gsmCode,
        String bcch
) {
}
