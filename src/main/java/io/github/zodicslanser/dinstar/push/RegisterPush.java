package io.github.zodicslanser.dinstar.push;

import java.util.List;

/**
 * Inbound {@code register} push: live SIM registration up/down events.
 *
 * @param sn       the gateway serial
 * @param register the registration events
 */
public record RegisterPush(String sn, List<RegisterEvent> register) {
}
