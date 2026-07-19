/**
 * Records for the inbound PUSH webhook payloads a Dinstar gateway POSTs to your server (firmware
 * ≥1102, enabled per gateway at Mobile Configuration → Basic Configuration → API).
 *
 * <p>This library provides only the deserialisation targets — <strong>it does not build the webhook
 * controller</strong>; that is the consuming app's job. Point Jackson at the right record for the
 * event type you received.
 *
 * <h2>Two structural rules</h2>
 * <ul>
 *   <li>Push payloads carry a top-level {@code sn} (device serial) but <strong>no</strong>
 *       {@code error_code} — one endpoint can serve a whole fleet by dispatching on {@code sn}.</li>
 *   <li>{@code sms}, {@code sms_result}, {@code sms_deliver_status}, {@code ussd}, {@code register},
 *       {@code cdr} wrap an <strong>array</strong>; {@code device} and {@code exception_info} wrap a
 *       single <strong>object</strong>. Getting that asymmetry wrong is the common mistake.</li>
 * </ul>
 *
 * <p>Field types for {@code register}, {@code device}, and {@code exception_info} are inferred from
 * the manual's example JSON (no typed table exists) — validate against a live capture. In a
 * {@code register} "down" event, {@code iccid}/{@code imsi} arrive as the literal string
 * {@code "<NULL>"}, not JSON {@code null}.
 */
package io.github.zodicslanser.dinstar.push;
