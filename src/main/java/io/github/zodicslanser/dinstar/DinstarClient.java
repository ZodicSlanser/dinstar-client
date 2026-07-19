package io.github.zodicslanser.dinstar;

import io.github.zodicslanser.dinstar.internal.DinstarHttp;
import io.github.zodicslanser.dinstar.model.AckResponse;
import io.github.zodicslanser.dinstar.model.CallForwardType;
import io.github.zodicslanser.dinstar.model.GetCdrRequest;
import io.github.zodicslanser.dinstar.model.GetCdrResponse;
import io.github.zodicslanser.dinstar.model.GetPortInfoRequest;
import io.github.zodicslanser.dinstar.model.GetPortInfoResponse;
import io.github.zodicslanser.dinstar.model.GetStatusResponse;
import io.github.zodicslanser.dinstar.model.InfoType;
import io.github.zodicslanser.dinstar.model.PortAction;
import io.github.zodicslanser.dinstar.model.QueryIncomingSmsRequest;
import io.github.zodicslanser.dinstar.model.QueryIncomingSmsResponse;
import io.github.zodicslanser.dinstar.model.QuerySmsDeliverStatusRequest;
import io.github.zodicslanser.dinstar.model.QuerySmsDeliverStatusResponse;
import io.github.zodicslanser.dinstar.model.QuerySmsInQueueResponse;
import io.github.zodicslanser.dinstar.model.QuerySmsResultRequest;
import io.github.zodicslanser.dinstar.model.QuerySmsResultResponse;
import io.github.zodicslanser.dinstar.model.SendSmsRequest;
import io.github.zodicslanser.dinstar.model.SendSmsResponse;
import io.github.zodicslanser.dinstar.model.SendUssdRequest;
import io.github.zodicslanser.dinstar.model.SendUssdResponse;
import io.github.zodicslanser.dinstar.model.StkFrameIndexResponse;
import io.github.zodicslanser.dinstar.model.StkGoRequest;
import io.github.zodicslanser.dinstar.model.StkViewResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A thin, typed client for one Dinstar UC2000-VG gateway (HTTP API v202011). The API surface is
 * identical across firmware versions, so this client is firmware-agnostic and does no version
 * negotiation.
 *
 * <h2>One client per gateway</h2>
 * This is a plain, constructable instance — not a Spring singleton. An app running three gateways
 * builds three clients:
 * <pre>{@code
 * DinstarClient gw1 = new DinstarClient(DinstarConfig.builder()
 *         .baseUrl("https://gateway.example.com").username("<username>").password("<password>")
 *         .authScheme(AuthScheme.DIGEST).verifyTls(false).build());
 * try (gw1) {
 *     SendUssdResponse r = gw1.sendUssd(0, "*125#");
 *     r.resultFor(0).ifPresent(p -> {
 *         if (!p.isSent()) log.warn("port 0 not sent: {}", p.sendStatus());
 *     });
 * }
 * }</pre>
 * The client is thread-safe and pools connections; {@link #close()} releases them.
 *
 * <h2>Two-layer status — the rule that matters</h2>
 * Send calls have two independent status layers:
 * <ol>
 *   <li>the <b>envelope</b> {@code error_code} — {@code 202} means the gateway accepted the payload.
 *       A hard-error envelope ({@code 400}/{@code 413}/{@code 500}/{@code 550}/{@code 404}) throws
 *       {@link DinstarException} instead of returning.</li>
 *   <li>the <b>per-port</b> {@code result[]} — the real outcome: {@code 200} sent, {@code 486} busy,
 *       {@code 503} not registered. Exposed as typed {@link io.github.zodicslanser.dinstar.model.PortResult};
 *       there is no boolean that collapses {@code 202} into "sent".</li>
 * </ol>
 * Always inspect the per-port result of a send. Branch on the body, never on HTTP transport status.
 *
 * <h2>Method → endpoint</h2>
 * <table border="1">
 *   <caption>Public methods and the HTTP endpoints they call</caption>
 *   <tr><th>Method</th><th>HTTP</th></tr>
 *   <tr><td>{@link #sendUssd(SendUssdRequest)}</td><td>POST /api/send_ussd</td></tr>
 *   <tr><td>{@link #queryUssdReply(List)}</td><td>GET /api/query_ussd_reply</td></tr>
 *   <tr><td>{@link #sendSms(SendSmsRequest)}</td><td>POST /api/send_sms</td></tr>
 *   <tr><td>{@link #querySmsResult(QuerySmsResultRequest)}</td><td>POST /api/query_sms_result</td></tr>
 *   <tr><td>{@link #querySmsDeliverStatus(QuerySmsDeliverStatusRequest)}</td><td>POST /api/query_sms_deliver_status</td></tr>
 *   <tr><td>{@link #queryIncomingSms(QueryIncomingSmsRequest)}</td><td>GET /api/query_incoming_sms</td></tr>
 *   <tr><td>{@link #querySmsInQueue()}</td><td>GET /api/query_sms_in_queue</td></tr>
 *   <tr><td>{@link #stopSms(int)}</td><td>GET /api/stop_sms</td></tr>
 *   <tr><td>{@link #getPortInfo(GetPortInfoRequest)}</td><td>GET /api/get_port_info</td></tr>
 *   <tr><td>{@link #resetPort(int)}</td><td>GET /api/set_port_info?action=reset</td></tr>
 *   <tr><td>{@link #powerPort(int, boolean)}</td><td>GET /api/set_port_info?action=power</td></tr>
 *   <tr><td>{@link #selectSlot(int, int)}</td><td>GET /api/set_port_info?action=slot</td></tr>
 *   <tr><td>{@link #setCallForward(int, CallForwardType, String)}</td><td>GET /api/set_port_info?action=CallForward</td></tr>
 *   <tr><td>{@link #checkCallForward(int)}</td><td>GET /api/set_port_info?action=CheckCallForward</td></tr>
 *   <tr><td>{@link #getCallForward(int)}</td><td>GET /api/get_port_info?info_type=CallForward</td></tr>
 *   <tr><td>{@link #getCdr(GetCdrRequest)}</td><td>POST /api/get_cdr</td></tr>
 *   <tr><td>{@link #getStatus()}</td><td>POST /api/get_status</td></tr>
 *   <tr><td>{@link #getStkView(int)}</td><td>GET /GetSTKView (not under /api/)</td></tr>
 *   <tr><td>{@link #stkGo(StkGoRequest)}</td><td>POST /STKGo (not under /api/)</td></tr>
 *   <tr><td>{@link #getStkCurrFrameIndex(int)}</td><td>GET /GetSTKCurrFrameIndex (not under /api/)</td></tr>
 * </table>
 *
 * <p>Every method throws {@link DinstarException} on a transport/auth failure or a hard-error
 * envelope {@code error_code}. Per-port send failures (486/503) do <em>not</em> throw — inspect the
 * returned response.
 */
public final class DinstarClient implements Closeable {

    private final DinstarHttp http;

    /**
     * Creates a client for one gateway.
     *
     * @param config the per-gateway configuration
     */
    public DinstarClient(DinstarConfig config) {
        this.http = new DinstarHttp(Objects.requireNonNull(config, "config"));
    }

    // ---------------------------------------------------------------- USSD

    /**
     * Sends (or cancels) a USSD string. See {@link SendUssdRequest} for the three traps: two-layer
     * status, verbatim text (trailing {@code #} required, never auto-added, 60-byte cap), and the
     * port-array broadcast.
     *
     * <p><strong>Check the per-port result.</strong> An {@code error_code:202} only means accepted.
     * Read {@link SendUssdResponse#resultFor(int)} / {@link SendUssdResponse#allSent()} — a
     * {@code 486}/{@code 503} there is a failure.
     *
     * @param request the request
     * @return the response, whose per-port {@code result[]} is the authoritative outcome
     * @throws DinstarException on a transport/auth failure or a hard-error envelope {@code error_code}
     *                          (e.g. {@code 400} when {@code text} is missing)
     */
    public SendUssdResponse sendUssd(SendUssdRequest request) {
        return http.post("/api/send_ussd", request, SendUssdResponse.class);
    }

    /**
     * Convenience single-port USSD send. The {@code text} is passed verbatim — include your own
     * trailing {@code #}; it is never appended for you.
     *
     * @param port the single target port, 0–31
     * @param text the USSD string, verbatim, max 60 bytes
     * @return the response; inspect {@link SendUssdResponse#resultFor(int)} for the real outcome
     * @throws DinstarException as {@link #sendUssd(SendUssdRequest)}
     */
    public SendUssdResponse sendUssd(int port, String text) {
        return sendUssd(SendUssdRequest.send(port, text));
    }

    /**
     * Force-tear-down a stuck USSD session on a port and unlock it.
     *
     * @param port the port to cancel, 0–31
     * @return the response
     * @throws DinstarException as {@link #sendUssd(SendUssdRequest)}
     */
    public SendUssdResponse cancelUssd(int port) {
        return sendUssd(SendUssdRequest.cancel(port));
    }

    /**
     * Reads the latest asynchronous USSD reply for one or more ports.
     *
     * <p><strong>No correlation id.</strong> A reply is just {@code {port, text}} — nothing ties it
     * to a specific {@code send_ussd}. The caller owns per-port serialization (hold a per-port lock
     * across send-then-read). An empty/single-space text means "nothing yet".
     *
     * @param ports the ports to read, each 0–31
     * @return the replies
     * @throws DinstarException on a transport/auth failure or a hard-error envelope {@code error_code}
     */
    public io.github.zodicslanser.dinstar.model.UssdReplyResponse queryUssdReply(List<Integer> ports) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("port", csvInts(ports)));
        return http.get("/api/query_ussd_reply", params, io.github.zodicslanser.dinstar.model.UssdReplyResponse.class);
    }

    /**
     * Reads the latest asynchronous USSD reply for one or more ports (varargs form).
     *
     * @param ports the ports to read, each 0–31
     * @return the replies
     * @throws DinstarException as {@link #queryUssdReply(List)}
     */
    public io.github.zodicslanser.dinstar.model.UssdReplyResponse queryUssdReply(int... ports) {
        List<Integer> list = new ArrayList<>(ports.length);
        for (int p : ports) {
            list.add(p);
        }
        return queryUssdReply(list);
    }

    // ---------------------------------------------------------------- SMS

    /**
     * Sends one or more SMS. Two-layer like USSD: {@code error_code:202} means accepted (returns a
     * {@code task_id}); the delivery outcome is learned later via {@link #querySmsResult}.
     *
     * @param request the request
     * @return the response ({@code task_id}, queue depth)
     * @throws DinstarException on transport/auth failure or a hard-error envelope (e.g. {@code 550}
     *                          no available port, {@code 413} too large)
     */
    public SendSmsResponse sendSms(SendSmsRequest request) {
        return http.post("/api/send_sms", request, SendSmsResponse.class);
    }

    /**
     * Queries SMS sending results (correlate by {@code user_id}).
     *
     * @param request the filter
     * @return the results
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public QuerySmsResultResponse querySmsResult(QuerySmsResultRequest request) {
        return http.post("/api/query_sms_result", request, QuerySmsResultResponse.class);
    }

    /**
     * Queries SMS delivery receipts (correlate by {@code ref_id}).
     *
     * @param request the filter
     * @return the receipts
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public QuerySmsDeliverStatusResponse querySmsDeliverStatus(QuerySmsDeliverStatusRequest request) {
        return http.post("/api/query_sms_deliver_status", request, QuerySmsDeliverStatusResponse.class);
    }

    /**
     * Polls for incoming SMS.
     *
     * <p><strong>Trap:</strong> {@link io.github.zodicslanser.dinstar.model.IncomingSmsFlag#UNREAD} mutates state
     * (marks messages read). For reliable ingestion use
     * {@link QueryIncomingSmsRequest#since(int)} (flag {@code all} + high-water mark) and dedup on
     * {@code (sn, incoming_sms_id)}.
     *
     * @param request the filter
     * @return the messages plus read/unread counts
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public QueryIncomingSmsResponse queryIncomingSms(QueryIncomingSmsRequest request) {
        List<NameValuePair> params = new ArrayList<>();
        if (request.flag() != null) {
            params.add(new BasicNameValuePair("flag", request.flag().wire()));
        }
        if (request.incomingSmsId() != null) {
            params.add(new BasicNameValuePair("incoming_sms_id", String.valueOf(request.incomingSmsId())));
        }
        if (request.port() != null && !request.port().isEmpty()) {
            params.add(new BasicNameValuePair("port", csvInts(request.port())));
        }
        return http.get("/api/query_incoming_sms", params, QueryIncomingSmsResponse.class);
    }

    /**
     * Returns the current SMS send backlog.
     *
     * @return the queue depth
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public QuerySmsInQueueResponse querySmsInQueue() {
        return http.get("/api/query_sms_in_queue", List.of(), QuerySmsInQueueResponse.class);
    }

    /**
     * Stops a queued SMS send task.
     *
     * @param taskId the {@code task_id} from {@link SendSmsResponse#taskId()}
     * @return the acknowledgement
     * @throws DinstarException on transport/auth failure or a hard-error envelope (e.g. {@code 404}
     *                          if the task no longer exists)
     */
    public AckResponse stopSms(int taskId) {
        List<NameValuePair> params = List.of(new BasicNameValuePair("task_id", String.valueOf(taskId)));
        return http.get("/api/stop_sms", params, AckResponse.class);
    }

    // ---------------------------------------------------------------- Ports / SIM

    /**
     * Reads port/SIM telemetry.
     *
     * @param request which ports and which fields
     * @return the per-port info
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public GetPortInfoResponse getPortInfo(GetPortInfoRequest request) {
        List<NameValuePair> params = new ArrayList<>();
        if (request.port() != null && !request.port().isEmpty()) {
            params.add(new BasicNameValuePair("port", csvInts(request.port())));
        }
        params.add(new BasicNameValuePair("info_type", csvInfoTypes(request.infoType())));
        return http.get("/api/get_port_info", params, GetPortInfoResponse.class);
    }

    /**
     * Soft-resets a module. <strong>Watchdog use only</strong> — this violently kills any in-flight
     * USSD/SMS on the port.
     *
     * @param port the port, 0–31
     * @return the acknowledgement
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public AckResponse resetPort(int port) {
        return setPortInfo(port, PortAction.RESET, null, null);
    }

    /**
     * Powers a module on or off (a hard cycle; re-registration takes 10–30s).
     * <strong>Watchdog use only</strong>.
     *
     * @param port the port, 0–31
     * @param on   {@code true} to power on, {@code false} to power off
     * @return the acknowledgement
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public AckResponse powerPort(int port, boolean on) {
        return setPortInfo(port, PortAction.POWER, on ? "on" : "off", null);
    }

    /**
     * Selects the active SIM slot (multi-SIM gateways only).
     *
     * @param port the port, 0–31
     * @param slot the slot, 0–3
     * @return the acknowledgement
     * @throws DinstarException on transport/auth failure or a hard-error envelope (e.g. {@code 400}
     *                          on a non-multi-SIM gateway)
     */
    public AckResponse selectSlot(int port, int slot) {
        return setPortInfo(port, PortAction.SLOT, String.valueOf(slot), null);
    }

    /**
     * Configures call forwarding on a port.
     *
     * @param port   the port, 0–31
     * @param type   the forward condition
     * @param number the target number (may be {@code null} for {@link CallForwardType#CANCEL_ALL})
     * @return the acknowledgement
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public AckResponse setCallForward(int port, CallForwardType type, String number) {
        return setPortInfo(port, PortAction.CALL_FORWARD, type.wire(), number);
    }

    /**
     * Asks the network to report the current call-forward setting. Read it back afterwards with
     * {@link #getCallForward(int)}.
     *
     * @param port the port, 0–31
     * @return the acknowledgement
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public AckResponse checkCallForward(int port) {
        return setPortInfo(port, PortAction.CHECK_CALL_FORWARD, null, null);
    }

    /**
     * Reads the current call-forward configuration for a port (populates
     * {@link io.github.zodicslanser.dinstar.model.PortInfo#callForwarding()}).
     *
     * @param port the port, 0–31
     * @return the per-port info with the {@code CallForwarding} map
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public GetPortInfoResponse getCallForward(int port) {
        List<NameValuePair> params = List.of(
                new BasicNameValuePair("port", String.valueOf(requirePort(port))),
                new BasicNameValuePair("info_type", InfoType.CALL_FORWARD.wire()));
        return http.get("/api/get_port_info", params, GetPortInfoResponse.class);
    }

    private AckResponse setPortInfo(int port, PortAction action, String param, String number) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("port", String.valueOf(requirePort(port))));
        params.add(new BasicNameValuePair("action", action.wire()));
        if (param != null) {
            params.add(new BasicNameValuePair("param", param));
        }
        if (number != null) {
            params.add(new BasicNameValuePair("number", number));
        }
        return http.get("/api/set_port_info", params, AckResponse.class);
    }

    // ---------------------------------------------------------------- CDR / status

    /**
     * Reads call detail records.
     *
     * @param request the filter
     * @return the CDRs
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public GetCdrResponse getCdr(GetCdrRequest request) {
        return http.post("/api/get_cdr", request, GetCdrResponse.class);
    }

    /**
     * Reads device performance (CPU/memory/flash). Sent as the bare array {@code ["performance"]};
     * the response has no {@code error_code} (fetched raw).
     *
     * @return the performance section
     * @throws DinstarException on a transport/auth failure or an unparseable body
     */
    public GetStatusResponse getStatus() {
        return http.postRaw("/api/get_status", List.of("performance"), GetStatusResponse.class);
    }

    // ---------------------------------------------------------------- STK (not under /api/)

    /**
     * Reads the current SIM Toolkit frame. Not under {@code /api/}; response has no
     * {@code error_code} (fetched raw).
     *
     * @param port the port, 0–31
     * @return the current STK frame
     * @throws DinstarException on a transport/auth failure or an unparseable body
     */
    public StkViewResponse getStkView(int port) {
        List<NameValuePair> params = List.of(new BasicNameValuePair("port", String.valueOf(requirePort(port))));
        return http.getRaw("/GetSTKView", params, StkViewResponse.class);
    }

    /**
     * Navigates the SIM Toolkit (select an item, submit input, or ok/cancle/home). Not under
     * {@code /api/}; {@code port} is sent as a bare integer. This endpoint <em>does</em> carry an
     * {@code error_code}.
     *
     * @param request the navigation request
     * @return the acknowledgement
     * @throws DinstarException on transport/auth failure or a hard-error envelope
     */
    public AckResponse stkGo(StkGoRequest request) {
        return http.post("/STKGo", request, AckResponse.class);
    }

    /**
     * Reads the current STK frame id. Not under {@code /api/}; response has no {@code error_code}
     * (fetched raw).
     *
     * @param port the port, 0–31
     * @return the current frame id
     * @throws DinstarException on a transport/auth failure or an unparseable body
     */
    public StkFrameIndexResponse getStkCurrFrameIndex(int port) {
        List<NameValuePair> params = List.of(new BasicNameValuePair("port", String.valueOf(requirePort(port))));
        return http.getRaw("/GetSTKCurrFrameIndex", params, StkFrameIndexResponse.class);
    }

    // ---------------------------------------------------------------- helpers

    private static int requirePort(int port) {
        if (port < 0 || port > 31) {
            throw new IllegalArgumentException("port must be 0..31, was " + port);
        }
        return port;
    }

    private static String csvInts(List<Integer> values) {
        Objects.requireNonNull(values, "ports");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("at least one port is required");
        }
        return values.stream().map(p -> String.valueOf(requirePort(p))).collect(Collectors.joining(","));
    }

    private static String csvInfoTypes(List<InfoType> types) {
        return types.stream().map(InfoType::wire).collect(Collectors.joining(","));
    }

    /**
     * Releases pooled connections. The client is unusable afterwards.
     *
     * @throws IOException if the underlying HTTP client fails to close
     */
    @Override
    public void close() throws IOException {
        http.close();
    }
}
