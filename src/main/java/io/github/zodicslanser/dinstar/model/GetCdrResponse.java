package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * Response to {@code get_cdr}.
 *
 * @param errorCode the envelope status ({@code 200} = query OK)
 * @param sn        the gateway serial
 * @param cdr       the call records; may be {@code null} or empty
 */
public record GetCdrResponse(int errorCode, String sn, List<Cdr> cdr) {
}
