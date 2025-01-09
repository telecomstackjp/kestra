package io.kestra.core.models.tasks.logs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class LogRecord implements LogRecordData {

    Resource resource;
    InstrumentationScopeInfo instrumentationScopeInfo;
    long timestampEpochNanos;
    long observedTimestampEpochNanos;
    SpanContext spanContext;
    Severity severity;
    String severityText;
    Attributes attributes;
    int totalAttributeCount;
    Value<String> bodyValue;

    @JsonIgnore
    public Body getBody(){
        throw new UnsupportedOperationException();
    }

}
