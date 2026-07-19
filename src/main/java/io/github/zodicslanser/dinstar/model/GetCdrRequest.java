package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Body of a {@code POST /api/get_cdr} request. All filters optional; {@code null} ones are omitted.
 *
 * @param port       ports to filter by, 0–31 verbatim
 * @param timeAfter  lower time bound, {@code yyyy-MM-dd HH:mm:ss}
 * @param timeBefore upper time bound, {@code yyyy-MM-dd HH:mm:ss}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetCdrRequest(
        List<Integer> port,
        String timeAfter,
        String timeBefore
) {
    /**
     * All CDRs for the given ports.
     *
     * @param ports ports, 0–31
     * @return the request
     */
    public static GetCdrRequest forPorts(List<Integer> ports) {
        return new GetCdrRequest(ports, null, null);
    }
}
