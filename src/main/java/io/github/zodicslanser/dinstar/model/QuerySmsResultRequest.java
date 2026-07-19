package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Body of a {@code POST /api/query_sms_result} request. All filters are optional and {@code null}
 * ones are omitted; combine them to narrow the query. At most 32 numbers.
 *
 * @param userId     correlation ids from {@code send_sms} to look up (the usual filter)
 * @param number     destination numbers to filter by (max 32)
 * @param port       ports to filter by, 0–31 verbatim
 * @param timeAfter  lower time bound, {@code yyyy-MM-dd HH:mm:ss}
 * @param timeBefore upper time bound, {@code yyyy-MM-dd HH:mm:ss}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuerySmsResultRequest(
        List<Integer> userId,
        List<String> number,
        List<Integer> port,
        String timeAfter,
        String timeBefore
) {
    /**
     * Query by correlation ids only.
     *
     * @param userIds the {@code user_id}s to look up
     * @return the request
     */
    public static QuerySmsResultRequest byUserId(List<Integer> userIds) {
        return new QuerySmsResultRequest(userIds, null, null, null, null);
    }
}
