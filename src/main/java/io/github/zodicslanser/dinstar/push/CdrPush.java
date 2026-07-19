package io.github.zodicslanser.dinstar.push;

import io.github.zodicslanser.dinstar.model.Cdr;

import java.util.List;

/**
 * Inbound {@code cdr} push: call detail records. Only <em>answered</em> calls are pushed; use
 * {@code get_cdr} for all.
 *
 * @param sn  the gateway serial
 * @param cdr the call records
 */
public record CdrPush(String sn, List<Cdr> cdr) {
}
