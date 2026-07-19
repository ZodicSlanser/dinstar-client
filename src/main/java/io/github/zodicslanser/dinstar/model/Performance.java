package io.github.zodicslanser.dinstar.model;

/**
 * Device performance section returned by {@code get_status} with {@code ["performance"]}.
 *
 * <p>Every value is a <em>string</em> on the wire (the gateway serialises these numbers as strings),
 * so they are modelled as {@code String}; parse to a number at the call site if you need arithmetic.
 * Units are gateway-defined (percentages for cpu, kilobytes for memory/flash).
 *
 * @param cpuUsed        CPU usage percentage
 * @param flashTotal     total flash
 * @param flashUsed      used flash
 * @param memoryTotal    total memory
 * @param memoryUsed     used memory
 * @param memoryFree     free memory
 * @param memoryCached   cached memory
 * @param memoryBuffers  buffer memory
 */
public record Performance(
        String cpuUsed,
        String flashTotal,
        String flashUsed,
        String memoryTotal,
        String memoryUsed,
        String memoryFree,
        String memoryCached,
        String memoryBuffers
) {
}
