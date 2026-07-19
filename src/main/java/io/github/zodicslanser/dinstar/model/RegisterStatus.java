package io.github.zodicslanser.dinstar.model;

/**
 * SIM registration status reported in {@link PortInfo#reg()} (and the {@code register} push
 * {@code status} is a different, simpler up/down field — not this).
 *
 * <p>{@link PortInfo#reg()} is kept as a raw {@code String} for forward-compatibility; use
 * {@link #fromWire(String)} when you want to branch on it. Note {@link #PIN_REQUIRE} and
 * {@link #PUK_REQUIRE} are operationally distinct — PUK needs a human to intervene.
 */
public enum RegisterStatus {
    /** Module powered off. */
    POWER_OFF,
    /** No SIM inserted. */
    NO_SIM,
    /** SIM PIN required. */
    PIN_REQUIRE,
    /** SIM PUK required (blocked — needs manual intervention). */
    PUK_REQUIRE,
    /** Not registered on any network. */
    UNREGISTER,
    /** Searching for a network. */
    SEARCHING_NETWORK,
    /** Registered and operational. */
    REGISTER_OK,
    /** Unknown / unrecognised. */
    UNKNOWN;

    /**
     * @param value the raw {@code reg} string from {@code get_port_info}
     * @return the matching constant, or {@link #UNKNOWN}
     */
    public static RegisterStatus fromWire(String value) {
        if (value != null) {
            for (RegisterStatus s : values()) {
                if (s.name().equalsIgnoreCase(value)) {
                    return s;
                }
            }
        }
        return UNKNOWN;
    }
}
