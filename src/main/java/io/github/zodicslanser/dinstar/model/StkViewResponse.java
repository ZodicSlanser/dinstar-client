package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * Response to {@code GetSTKView} — the current SIM Toolkit frame. This endpoint is not under
 * {@code /api/} and carries no {@code error_code}/{@code sn}.
 *
 * @param title     the frame title
 * @param item      selectable menu items; {@code null} on a display-only frame
 * @param text      display text; present only on display-only frames ({@code inputType == 0})
 * @param inputType the input mode: {@code 0} display-only, {@code 2} select item, {@code 3} yes/no,
 *                  {@code 4} one digit, … (see the vendor manual for the full list)
 * @param frameId   the current frame id
 */
public record StkViewResponse(
        String title,
        List<StkItem> item,
        String text,
        int inputType,
        int frameId
) {
}
