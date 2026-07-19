package io.github.zodicslanser.dinstar.push;

/**
 * The payload of an {@code exception_info} push (a single object, not an array). Requires "Exception
 * Event Handling" to be enabled on the gateway.
 *
 * <p>Types inferred from the manual example.
 *
 * @param port   the faulty port, 0–31 verbatim
 * @param type   the fault type (example {@code "call_fail"})
 * @param action what the gateway did in response (example {@code "reset"})
 */
public record ExceptionInfo(int port, String type, String action) {
}
