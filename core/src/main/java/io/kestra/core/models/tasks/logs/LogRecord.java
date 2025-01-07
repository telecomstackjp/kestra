package io.kestra.core.models.tasks.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
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
    Value<String> bodyValue;
    Attributes attributes;
    int totalAttributeCount;

    public Body getBody(){
        throw new UnsupportedOperationException();
    }
}
