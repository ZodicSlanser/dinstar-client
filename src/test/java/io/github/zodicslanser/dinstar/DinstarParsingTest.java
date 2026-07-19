package io.github.zodicslanser.dinstar;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zodicslanser.dinstar.internal.DinstarHttp;
import io.github.zodicslanser.dinstar.internal.Json;
import io.github.zodicslanser.dinstar.model.PortResult;
import io.github.zodicslanser.dinstar.model.PortSendStatus;
import io.github.zodicslanser.dinstar.model.SendUssdResponse;
import io.github.zodicslanser.dinstar.model.UssdReplyResponse;
import io.github.zodicslanser.dinstar.push.DevicePush;
import io.github.zodicslanser.dinstar.push.SmsResultPush;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The library's one runnable check. It pins the correctness contract that the whole rewrite exists
 * for: the response parser must treat an accepted-but-busy send as a FAILURE, not a success, and
 * must surface both status layers honestly.
 *
 * <p>Run with {@code mvn test}. No network — these exercise the pure body parser
 * ({@link DinstarHttp#parseEnvelopeBody}) and the typed records against real wire JSON.
 */
class DinstarParsingTest {

    private static final ObjectMapper M = Json.MAPPER;

    /**
     * THE mandatory check: {@code error_code:202} (gateway accepted the payload) with a per-port
     * {@code 486} (port busy) must NOT read as a success.
     */
    @Test
    void accepted202WithPerPort486IsFailureNotSuccess() {
        String body = """
                {"error_code":202,"sn":"da00-0030-1901-2817","result":[{"port":0,"status":486}]}""";

        SendUssdResponse resp = DinstarHttp.parseEnvelopeBody("/api/send_ussd", body, SendUssdResponse.class);

        // The envelope is "accepted" — but that is NOT success.
        assertTrue(resp.isAccepted(), "202 means the gateway accepted the payload");

        // The authoritative per-port layer says the port did NOT send.
        PortResult port0 = resp.resultFor(0).orElseThrow();
        assertEquals(486, port0.status());
        assertEquals(PortSendStatus.PORT_BUSY, port0.sendStatus());
        assertFalse(port0.isSent(), "486 is port-busy — a failure, never a send");
        assertFalse(resp.allSent(), "no port sent, so allSent must be false");
    }

    /** 503 (not registered) is likewise a per-port failure under an accepted envelope. */
    @Test
    void accepted202WithPerPort503IsNotRegistered() {
        String body = """
                {"error_code":202,"sn":"x","result":[{"port":2,"status":503}]}""";
        SendUssdResponse resp = DinstarHttp.parseEnvelopeBody("/api/send_ussd", body, SendUssdResponse.class);
        PortResult p = resp.resultFor(2).orElseThrow();
        assertEquals(PortSendStatus.NOT_REGISTERED, p.sendStatus());
        assertFalse(p.isSent());
    }

    /** The genuine success path: 202 + per-port 200. */
    @Test
    void accepted202WithPerPort200IsSent() {
        String body = """
                {"error_code":202,"sn":"x","result":[{"port":0,"status":200}]}""";
        SendUssdResponse resp = DinstarHttp.parseEnvelopeBody("/api/send_ussd", body, SendUssdResponse.class);
        assertTrue(resp.resultFor(0).orElseThrow().isSent());
        assertTrue(resp.allSent());
    }

    /** A hard-error envelope (400) is surfaced by throwing — never silently dropped like legacy. */
    @Test
    void hardEnvelopeErrorThrows() {
        String body = """
                {"error_code":400,"sn":"da00-0030-1901-2817"}""";
        DinstarException ex = assertThrows(DinstarException.class,
                () -> DinstarHttp.parseEnvelopeBody("/api/send_ussd", body, SendUssdResponse.class));
        assertEquals(400, ex.errorCode());
        assertEquals("/api/send_ussd", ex.endpoint());
        assertEquals("da00-0030-1901-2817", ex.sn());
    }

    /** A USSD reply carries only {port, text} — there is no correlation id to read. */
    @Test
    void ussdReplyHasNoCorrelationId() throws Exception {
        String body = """
                {"error_code":200,"sn":"x","reply":[{"port":0,"text":"Solde: 12.5 DH"}]}""";
        UssdReplyResponse resp = DinstarHttp.parseEnvelopeBody("/api/query_ussd_reply", body, UssdReplyResponse.class);
        assertEquals("Solde: 12.5 DH", resp.replyFor(0).orElseThrow().text());
        // The only fields on a reply are port and text — assert nothing else exists on the wire.
        var node = M.readTree(body).get("reply").get(0);
        assertEquals(2, node.size(), "a reply must be exactly {port, text}");
        assertTrue(node.has("port"));
        assertTrue(node.has("text"));
    }

    /** The push array/object asymmetry deserializes: sms_result is an array, device is an object. */
    @Test
    void pushPayloadsDeserialize() throws Exception {
        String smsResult = """
                {"sn":"x","sms_result":[{"port":1,"number":"10086","time":"2016-07-12 01:46:02",
                "status":"DELIVERED","count":1,"succ_count":1,"ref_id":215,"imsi":"460004642148063"}]}""";
        SmsResultPush arr = M.readValue(smsResult, SmsResultPush.class);
        assertEquals(1, arr.smsResult().size());
        assertEquals(215, arr.smsResult().get(0).refId());

        String device = """
                {"sn":"x","device":{"port_number":32,"IP":"192.0.2.142","MAC":"AA-BB-CC-DD-EE-FF","status":"power_off"}}""";
        DevicePush obj = M.readValue(device, DevicePush.class);
        assertNotNull(obj.device());
        assertEquals("192.0.2.142", obj.device().ip());
        assertEquals(32, obj.device().portNumber());
        assertEquals("power_off", obj.device().status());
    }

    /** An error envelope legitimately has no result array — the field is null, not an exception. */
    @Test
    void okEnvelopeWithoutResultArrayHasNullResult() {
        String body = """
                {"error_code":200,"sn":"x","in_queue":0}""";
        var resp = DinstarHttp.parseEnvelopeBody("/api/query_sms_in_queue", body,
                io.github.zodicslanser.dinstar.model.QuerySmsInQueueResponse.class);
        assertEquals(0, resp.inQueue());
        // sanity: an accepted send with no array yields a null result (see allSent()==false).
        SendUssdResponse noArray = DinstarHttp.parseEnvelopeBody("/api/send_ussd",
                "{\"error_code\":202,\"sn\":\"x\"}", SendUssdResponse.class);
        assertNull(noArray.result());
        assertFalse(noArray.allSent());
    }
}
