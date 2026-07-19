# dinstar-client

A thin, typed Java client for the **Dinstar UC2000-VG** GSM gateway **HTTP API v202011**.

- **Target:** model UC2000-VG, package `02240222`, API v202011.
- **Firmware-agnostic.** The Dinstar HTTP API is identical across firmware versions, so this client
  does **no** version negotiation or per-firmware branching.
- **A library, not an app.** No `@SpringBootApplication`, no `main`, no global properties driving it.
  A `DinstarClient` is a plain, constructable, thread-safe instance — build **one per gateway**.
- **Honest types.** Typed request/response records per endpoint, and the two-layer send status is
  modelled so a busy/unregistered port cannot be read as a success.

Java 17 · Maven · dependencies: Apache HttpClient 5 + Jackson (Spring Boot auto-config is optional).

```xml
<dependency>
  <groupId>io.github.zodicslanser</groupId>
  <artifactId>dinstar-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## Construction

One client per gateway, from a `DinstarConfig`. The consuming app runs three gateways → three clients.

### Digest auth (what the vendor uses)

Values below are placeholders — supply your own gateway address and credentials.

```java
DinstarConfig cfg = DinstarConfig.builder()
        .baseUrl("https://gateway.example.com") // your gateway; https or http, trailing slash optional
        .username("<username>")
        .password("<password>")
        .authScheme(AuthScheme.DIGEST)    // built-in HttpClient 5 Digest — correct nc/cnonce
        .verifyTls(false)                 // box ships a self-signed cert
        .connectTimeout(Duration.ofSeconds(5))
        .responseTimeout(Duration.ofSeconds(15))
        .build();

DinstarClient client = new DinstarClient(cfg);
```

### Basic auth

```java
DinstarClient client = new DinstarClient(DinstarConfig.builder()
        .baseUrl("http://gateway.example.com")
        .username("<username>").password("<password>")
        .authScheme(AuthScheme.BASIC)     // preemptive Basic
        .build());
```

`DinstarClient` is `Closeable` — close it (or use try-with-resources) to release pooled connections.

### Full config surface

| Builder method | Default | Meaning |
|---|---|---|
| `baseUrl(String)` | — (required) | Gateway base URL, `http` or `https`. Trailing `/` trimmed. |
| `username(String)` | — (required) | Auth username. No default is baked in. |
| `password(String)` | — (required) | Auth password. No default is baked in. |
| `authScheme(AuthScheme)` | `DIGEST` | `BASIC` or `DIGEST`; the caller decides. |
| `connectTimeout(Duration)` | `5s` | TCP connect timeout. |
| `responseTimeout(Duration)` | `15s` | Socket read timeout. Keep short — never block on a USSD *reply* here. |
| `verifyTls(boolean)` | `false` | Verify the server cert. `false` relaxes trust **for this client only**, never the JVM. |

### Optional: Spring Boot single-gateway convenience

If (and only if) you have Spring Boot on the classpath and set `dinstar.base-url`, an
`@AutoConfiguration` builds one `DinstarClient` bean. This is a convenience for the single-gateway
case — multi-gateway apps should build clients directly.

```yaml
dinstar:
  base-url: https://gateway.example.com   # placeholders — supply your own
  username: <username>
  password: <password>
  auth-scheme: digest      # or basic
  verify-tls: false
  connect-timeout: 5s
  response-timeout: 15s
```

---

## Usage by endpoint group

### USSD

```java
// Single-port send. Text is VERBATIM — include your own trailing '#'.
SendUssdResponse r = client.sendUssd(0, "*555*4*2#");

// TWO-LAYER STATUS: error_code:202 only means "accepted". The real outcome is per-port:
r.resultFor(0).ifPresent(p -> {
    if (!p.isSent()) {                       // 486 busy / 503 not-registered
        log.warn("port 0 not sent: {}", p.sendStatus());
    }
});

// Broadcast (same USSD to every listed port) is explicit — never for financial transactions:
client.sendUssd(SendUssdRequest.broadcast(List.of(0, 1, 2), "*125#"));

// Cancel a stuck session and unlock the port:
client.cancelUssd(0);

// Read the async reply (poll, or use the push webhook). No correlation id — {port, text} only:
UssdReplyResponse reply = client.queryUssdReply(0);
reply.replyFor(0).ifPresent(u -> process(u.text()));  // empty/" " means "nothing yet"
```

### SMS

```java
SendSmsResponse s = client.sendSms(SendSmsRequest.of(
        "Your code is #param#",
        List.of(SmsRecipient.to("0612345678", /*userId*/ 42))));
// 202 = accepted for sending. Correlate the outcome later by user_id:
client.querySmsResult(QuerySmsResultRequest.byUserId(List.of(42)));
client.querySmsDeliverStatus(new QuerySmsDeliverStatusRequest(List.of(0), null, null));

// Reliable ingestion: poll by high-water mark (flag=all), NOT flag=unread (which mutates state):
QueryIncomingSmsResponse in = client.queryIncomingSms(QueryIncomingSmsRequest.since(lastSeenId));

client.querySmsInQueue();     // backlog depth
client.stopSms(taskId);       // cancel a queued task
```

### Ports / SIM

```java
GetPortInfoResponse info = client.getPortInfo(
        GetPortInfoRequest.allPorts(List.of(InfoType.REG, InfoType.SIGNAL, InfoType.CALLSTATE)));

client.resetPort(0);            // soft reset  (watchdog use only — kills in-flight USSD/SMS)
client.powerPort(0, false);     // hard power off/on (10–30s re-register)
client.selectSlot(0, 1);        // multi-SIM only
client.setCallForward(0, CallForwardType.UNCONDITIONAL, "15013828917");
client.checkCallForward(0);     // then read it back:
client.getCallForward(0).infoFor(0).ifPresent(p -> System.out.println(p.callForwarding()));
```

### CDR / device status

```java
GetCdrResponse cdr = client.getCdr(GetCdrRequest.forPorts(List.of(0, 1, 2)));
GetStatusResponse st = client.getStatus();   // cpu/memory/flash (all string values)
```

### STK (SIM Toolkit) — note: **not** under `/api/`

```java
StkViewResponse view = client.getStkView(0);
client.stkGo(StkGoRequest.selectItem(0, 1));
client.stkGo(StkGoRequest.input(0, "1234"));
client.stkGo(StkGoRequest.navigate(0, StkAction.OK));   // StkAction.CANCEL wire value is "cancle"
int frame = client.getStkCurrFrameIndex(0).frameId();
```

### Inbound PUSH webhooks

The gateway can POST events to your server (firmware ≥1102, enabled per gateway). This library ships
the **deserialisation records** in `io.github.zodicslanser.dinstar.push`; **you** own the webhook controller. Every
payload has a top-level `sn` (dispatch a whole fleet on it) and **no** `error_code`.

```java
// In your controller, pick the record for the event type you received:
SmsPush sms                = mapper.readValue(body, SmsPush.class);                 // array
UssdPush ussd              = mapper.readValue(body, UssdPush.class);                // array
SmsResultPush res          = mapper.readValue(body, SmsResultPush.class);           // array
SmsDeliverStatusPush del   = mapper.readValue(body, SmsDeliverStatusPush.class);    // array
RegisterPush reg           = mapper.readValue(body, RegisterPush.class);            // array
CdrPush cdr                = mapper.readValue(body, CdrPush.class);                 // array
DevicePush device          = mapper.readValue(body, DevicePush.class);             // OBJECT
ExceptionInfoPush exc      = mapper.readValue(body, ExceptionInfoPush.class);      // OBJECT
```

---

## Gotchas (read these)

1. **Two-layer status.** `error_code` in the body is the envelope: `202` means the gateway *accepted*
   the payload — **not** that anything sent. The real per-port outcome is in `result[]`: `200` sent,
   `486` port busy, `503` not registered. This client exposes both and gives you **no** `isSuccess()`
   that keys off the envelope, so a `486`/`503` cannot be read as a send. A hard-error envelope
   (`400/413/500/550/404`) throws `DinstarException`. Branch on the body, never on HTTP status.

2. **USSD text is verbatim.** The library never rewrites, sanitizes, or re-encodes it. The trailing
   `#` most carriers require is **never auto-appended** — include it yourself. Leading `#` and
   embedded `*` are legal (`#555*3*…#`). The only limit is **60 bytes** (over-length throws, it is
   never truncated).

3. **`port` is 0–31, verbatim.** Never adjusted by ±1 anywhere. Single-port is the easy path
   (`sendUssd(int, String)`). A **list** of ports is a **BROADCAST** — the same USSD to every listed
   port — so it is a separate, explicitly named call (`SendUssdRequest.broadcast(...)`). Never
   broadcast a financial USSD.

4. **USSD replies have no correlation id.** `query_ussd_reply` returns just `{port, text}`. Nothing
   ties a reply to a specific send. **The caller owns per-port serialization** — hold a per-port lock
   across send-then-read so two sessions on one port cannot cross replies. The library cannot
   correlate for you, and does not pretend to.

5. **TLS / self-signed.** The box ships a self-signed cert; the vendor uses `https` with `curl -k`.
   Default is `verifyTls(false)`, which relaxes trust **for this client's connection manager only** —
   never a JVM-global override. Set `verifyTls(true)` if you install the cert. Plain `http` also works
   on a trusted LAN.

6. **Incoming-SMS polling mutates state.** `flag=unread` marks messages read as it returns them, so a
   crash mid-processing loses them. Prefer `QueryIncomingSmsRequest.since(highWaterMark)` (flag=`all`)
   and dedup on `(sn, incoming_sms_id)`.

7. **`reload_port` does not exist** in v202011. The reset equivalent is `resetPort(port)`
   (`set_port_info?action=reset`).

---

## Method → endpoint

| Method | HTTP |
|---|---|
| `sendUssd(...)` / `sendUssd(int,String)` / `cancelUssd(int)` | `POST /api/send_ussd` |
| `queryUssdReply(...)` | `GET /api/query_ussd_reply` |
| `sendSms(...)` | `POST /api/send_sms` |
| `querySmsResult(...)` | `POST /api/query_sms_result` |
| `querySmsDeliverStatus(...)` | `POST /api/query_sms_deliver_status` |
| `queryIncomingSms(...)` | `GET /api/query_incoming_sms` |
| `querySmsInQueue()` | `GET /api/query_sms_in_queue` |
| `stopSms(int)` | `GET /api/stop_sms` |
| `getPortInfo(...)` | `GET /api/get_port_info` |
| `resetPort(int)` | `GET /api/set_port_info?action=reset` |
| `powerPort(int,boolean)` | `GET /api/set_port_info?action=power` |
| `selectSlot(int,int)` | `GET /api/set_port_info?action=slot` |
| `setCallForward(int,...)` | `GET /api/set_port_info?action=CallForward` |
| `checkCallForward(int)` | `GET /api/set_port_info?action=CheckCallForward` |
| `getCallForward(int)` | `GET /api/get_port_info?info_type=CallForward` |
| `getCdr(...)` | `POST /api/get_cdr` |
| `getStatus()` | `POST /api/get_status` |
| `getStkView(int)` | `GET /GetSTKView` *(not under /api/)* |
| `stkGo(...)` | `POST /STKGo` *(not under /api/)* |
| `getStkCurrFrameIndex(int)` | `GET /GetSTKCurrFrameIndex` *(not under /api/)* |

---

## Build & test

```bash
mvn package        # builds the JAR + sources + javadoc JARs
mvn test           # runs DinstarParsingTest
```

The one runnable check (`DinstarParsingTest`) proves the contract this rewrite exists for: an
`error_code:202` response carrying a per-port `486` parses as a **failure**, not a success — plus the
hard-envelope throw, the no-correlation-id USSD reply, and the push array/object asymmetry.

> No live gateway is required to build or test. Wire correctness is pinned by the parser test and the
> validated field maps (vendor manual + a 34-request Postman collection captured against a real box).
