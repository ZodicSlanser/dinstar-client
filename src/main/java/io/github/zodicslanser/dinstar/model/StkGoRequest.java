package io.github.zodicslanser.dinstar.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Body of a {@code POST /STKGo} request (SIM Toolkit navigation). Note this endpoint is <em>not</em>
 * under {@code /api/}, and {@code port} here is a bare integer, not an array.
 *
 * <p>Supply exactly one of {@code item} (menu selection), {@code param} (text input), or
 * {@code action} (navigation). {@code null} fields are omitted.
 *
 * @param port   the port, 0–31 verbatim
 * @param item   an {@link StkItem#itemId()} to select, or {@code null}
 * @param param  text input to submit, or {@code null}
 * @param action a navigation action (ok / cancle / home), or {@code null}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StkGoRequest(int port, Integer item, String param, StkAction action) {

    /**
     * Select a menu item.
     *
     * @param port   the port
     * @param itemId the item id to select
     * @return the request
     */
    public static StkGoRequest selectItem(int port, int itemId) {
        return new StkGoRequest(port, itemId, null, null);
    }

    /**
     * Submit text input.
     *
     * @param port  the port
     * @param input the text to submit
     * @return the request
     */
    public static StkGoRequest input(int port, String input) {
        return new StkGoRequest(port, null, input, null);
    }

    /**
     * Perform a navigation action.
     *
     * @param port   the port
     * @param action the action (ok / cancle / home)
     * @return the request
     */
    public static StkGoRequest navigate(int port, StkAction action) {
        return new StkGoRequest(port, null, null, action);
    }
}
