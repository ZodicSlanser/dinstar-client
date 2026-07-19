package io.github.zodicslanser.dinstar.model;

/**
 * Response to {@code GetSTKCurrFrameIndex} — the current STK frame id. Not under {@code /api/};
 * carries no {@code error_code}/{@code sn}.
 *
 * @param frameId the current frame id
 */
public record StkFrameIndexResponse(int frameId) {
}
