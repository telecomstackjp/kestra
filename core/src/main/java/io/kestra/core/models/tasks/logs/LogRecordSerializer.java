package io.kestra.core.models.tasks.logs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;

public class LogRecordSerializer extends JsonSerializer<LogRecord> {

    @Override
    public void serialize(LogRecord logRecord, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeObjectField("resource", logRecord.resource);
        gen.writeObjectField("instrumentationScopeInfo", logRecord.instrumentationScopeInfo);
        gen.writeNumberField("timestampEpochNanos", logRecord.timestampEpochNanos);
        gen.writeNumberField("observedTimestampEpochNanos", logRecord.observedTimestampEpochNanos);
        gen.writeObjectField("spanContext", logRecord.spanContext);
        gen.writeStringField("severity", logRecord.severity != null ? logRecord.severity.name() : null);
        gen.writeStringField("severityText", logRecord.severityText);
        gen.writeObjectField("attributes", logRecord.attributes);
        gen.writeNumberField("totalAttributeCount", logRecord.totalAttributeCount);

        String bodyValueJson = logRecord.bodyValue.getValue();
        Map<String, Object> bodyMap = new ObjectMapper().readValue(bodyValueJson, Map.class);
        for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
            gen.writeObjectField(entry.getKey(), entry.getValue());
        }

        gen.writeEndObject();
    }
}