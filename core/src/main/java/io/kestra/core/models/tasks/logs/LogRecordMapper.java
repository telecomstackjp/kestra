package io.kestra.core.models.tasks.logs;

import io.kestra.core.models.executions.LogEntry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import org.slf4j.event.Level;

public final class LogRecordMapper {

    private LogRecordMapper(){}

    public static LogRecord mapToLogRecord(LogEntry log) {
        return LogRecord.builder()
            .resource(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "Kestra")))
            .timestampEpochNanos(instantInNanos(log.getTimestamp()))
            .observedTimestampEpochNanos(instantInNanos(log.getTimestamp()))
            .severity(convertLogLevelToSeverity(log.getLevel()))
            .severityText(log.getLevel().toString())
            .attributes(Attributes.builder()
                .put("tenantId", log.getTenantId())
                .put("namespace", log.getNamespace())
                .put("flowId", log.getFlowId())
                .put("taskId", log.getTaskId())
                .put("executionId", log.getExecutionId())
                .put("taskRunId", log.getTaskRunId())
                .put("attemptNumber", log.getAttemptNumber())
                .put("triggerId", log.getTriggerId())
                .put("thread", log.getThread())
                .put("message", log.getMessage())
                .build())
            .bodyValue(Value.of(LogEntry.toPrettyString(log)))
            .build();
    }

    public static long instantInNanos(Instant instant) {
        return instant.getEpochSecond() * 1_000_000_000 + instant.getNano();
    }

    public static Severity convertLogLevelToSeverity(Level level) {
        return switch (level) {
            case TRACE -> Severity.TRACE;
            case DEBUG -> Severity.DEBUG;
            case INFO -> Severity.INFO;
            case WARN -> Severity.WARN;
            case ERROR -> Severity.ERROR;
        };
    }

}
