package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * Parameters for a {@code GET /api/get_port_info} request. This is a plain parameter holder — the
 * client renders {@code port} and {@code info_type} as comma-separated query strings.
 *
 * @param port     ports to query, 0–31 verbatim; {@code null} or empty means <em>all</em> ports
 * @param infoType the fields to fetch (joined comma-separated on the wire); must not be empty
 */
public record GetPortInfoRequest(List<Integer> port, List<InfoType> infoType) {
    /** Validates that at least one {@link InfoType} is requested. */
    public GetPortInfoRequest {
        if (infoType == null || infoType.isEmpty()) {
            throw new IllegalArgumentException("at least one info_type is required");
        }
        port = port == null ? null : List.copyOf(port);
        infoType = List.copyOf(infoType);
    }

    /**
     * Fetch the given fields across all ports.
     *
     * @param infoType the fields to fetch
     * @return the request
     */
    public static GetPortInfoRequest allPorts(List<InfoType> infoType) {
        return new GetPortInfoRequest(null, infoType);
    }
}
