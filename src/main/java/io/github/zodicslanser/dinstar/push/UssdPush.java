package io.github.zodicslanser.dinstar.push;

import io.github.zodicslanser.dinstar.model.UssdReply;

import java.util.List;

/**
 * Inbound {@code ussd} push: asynchronous USSD replies. Each carries only {@code port} and
 * {@code text} — no correlation id (see {@link io.github.zodicslanser.dinstar.model.UssdReply}).
 *
 * @param sn   the gateway serial
 * @param ussd the USSD replies
 */
public record UssdPush(String sn, List<UssdReply> ussd) {
}
