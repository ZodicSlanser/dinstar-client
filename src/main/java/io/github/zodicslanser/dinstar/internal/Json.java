package io.github.zodicslanser.dinstar.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * The single, shared, pre-configured Jackson {@link ObjectMapper} for the whole library.
 *
 * <p>Configured so the typed records match the gateway wire format without per-field annotations:
 * <ul>
 *   <li><b>snake_case</b> property naming ({@code smsInQueue} ⇄ {@code sms_in_queue}); odd keys like
 *       {@code IP}/{@code MAC}/{@code CallForwarding} carry an explicit {@code @JsonProperty}.</li>
 *   <li><b>Omit nulls</b> on serialization, so optional request filters drop out — while empty
 *       strings ({@code text:""} for a USSD cancel) and {@code false} booleans
 *       ({@code request_status_report:false}) are still sent.</li>
 *   <li><b>Lenient reads</b>: unknown JSON properties are ignored (firmware may add fields) and
 *       unknown enum values fall back to a default rather than throwing.</li>
 * </ul>
 */
public final class Json {

    /** The shared, thread-safe mapper. */
    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .build();

    private Json() {
    }
}
