package io.github.zodicslanser.dinstar.model;

/**
 * One selectable item of a SIM Toolkit menu frame ({@code GetSTKView}).
 *
 * @param itemId     the item id to pass back as {@code STKGo?item=...}
 * @param itemString the display label
 */
public record StkItem(int itemId, String itemString) {
}
