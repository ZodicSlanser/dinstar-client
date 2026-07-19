package io.github.zodicslanser.dinstar.model;

import java.util.List;

/**
 * Parameters for a {@code GET /api/query_incoming_sms} request. A plain parameter holder — the
 * client renders these as query params.
 *
 * <p>For reliable ingestion, poll with {@link IncomingSmsFlag#ALL} and a rising
 * {@code incomingSmsId} high-water mark rather than {@link IncomingSmsFlag#UNREAD} (which marks
 * messages read as a side effect — a crash then loses them).
 *
 * @param flag          which messages to return (defaults applied by the client if {@code null})
 * @param incomingSmsId return only messages with id greater than this (the high-water mark);
 *                      {@code null} for none
 * @param port          ports to filter by, 0–31 verbatim; {@code null} for all
 */
public record QueryIncomingSmsRequest(IncomingSmsFlag flag, Integer incomingSmsId, List<Integer> port) {

    /**
     * Poll for messages newer than a high-water mark, across all ports, without mutating read state.
     *
     * @param sinceId return messages with id greater than this
     * @return the request
     */
    public static QueryIncomingSmsRequest since(int sinceId) {
        return new QueryIncomingSmsRequest(IncomingSmsFlag.ALL, sinceId, null);
    }
}
