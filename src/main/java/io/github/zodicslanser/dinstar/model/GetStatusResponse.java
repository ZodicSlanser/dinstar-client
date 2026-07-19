package io.github.zodicslanser.dinstar.model;

/**
 * Response to {@code get_status} with {@code ["performance"]}.
 *
 * <p><strong>Different envelope.</strong> Unlike the query endpoints, this response has no
 * {@code error_code} and no {@code sn} — it is a bare object keyed by the requested section. So it is
 * fetched "raw" and never runs through the envelope success check.
 *
 * @param performance the performance section
 */
public record GetStatusResponse(Performance performance) {
}
