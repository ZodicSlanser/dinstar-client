package io.github.zodicslanser.dinstar.push;

/**
 * Inbound {@code device} push: chassis power event. The {@code device} field is a single
 * <strong>object</strong>, not an array.
 *
 * @param sn     the gateway serial
 * @param device the power event
 */
public record DevicePush(String sn, DeviceEvent device) {
}
