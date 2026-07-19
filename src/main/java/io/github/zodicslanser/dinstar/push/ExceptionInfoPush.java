package io.github.zodicslanser.dinstar.push;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inbound {@code exception_info} push: a port fault self-report. The {@code exception_info} field is
 * a single <strong>object</strong>, not an array.
 *
 * @param sn            the gateway serial
 * @param exceptionInfo the fault report (JSON key {@code exception_info})
 */
public record ExceptionInfoPush(String sn, @JsonProperty("exception_info") ExceptionInfo exceptionInfo) {
}
