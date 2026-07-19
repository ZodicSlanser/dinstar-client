package io.github.zodicslanser.dinstar.model;

import java.util.List;
import java.util.Optional;

/**
 * Response to {@code get_port_info}.
 *
 * @param errorCode the envelope status ({@code 200} = query OK)
 * @param sn        the gateway serial
 * @param info      per-port info; may be {@code null}
 */
public record GetPortInfoResponse(int errorCode, String sn, List<PortInfo> info) {

    /**
     * The info entry for a specific port.
     *
     * @param port the port, 0–31
     * @return the {@link PortInfo}, or empty if not present
     */
    public Optional<PortInfo> infoFor(int port) {
        if (info == null) {
            return Optional.empty();
        }
        return info.stream().filter(i -> i.port() == port).findFirst();
    }
}
