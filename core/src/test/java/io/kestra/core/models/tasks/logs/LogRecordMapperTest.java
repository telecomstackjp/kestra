package io.kestra.core.models.tasks.logs;


import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.kestra.core.models.executions.LogEntry;
import io.opentelemetry.api.logs.Severity;
import java.time.Instant;
import java.util.stream.Stream;
import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;

@ExtendWith(SoftAssertionsExtension.class)
public class LogRecordMapperTest {

    @Test
    public void should_map_to_log_entry_to_log_record(BDDSoftAssertions softly){
        LogRecord logRecord = LogRecordMapper.mapToLogRecord(LogEntry.builder()
            .tenantId("tenantId")
            .namespace("namespace")
            .flowId("flowId")
            .taskId("taskId")
            .executionId("executionId")
            .taskRunId("taskRunId")
            .attemptNumber(1)
            .triggerId("triggerId")
            .timestamp(Instant.parse("2011-12-03T10:15:30.123456789Z"))
            .level(Level.INFO)
            .thread("thread")
            .message("message")
            .build());
        softly.then(logRecord.getResource().getAttribute(stringKey("service.name"))).isEqualTo("Kestra");
        softly.then(logRecord.getTimestampEpochNanos()).isEqualTo(1322907330123456789L);
        softly.then(logRecord.getObservedTimestampEpochNanos()).isEqualTo(1322907330123456789L);
        softly.then(logRecord.getSeverity()).isEqualTo(Severity.INFO);
        softly.then(logRecord.getSeverityText()).isEqualTo("INFO");
        softly.then(logRecord.getAttributes().get(stringKey("tenantId"))).isEqualTo("tenantId");
        softly.then(logRecord.getAttributes().get(stringKey("namespace"))).isEqualTo("namespace");
        softly.then(logRecord.getAttributes().get(stringKey("flowId"))).isEqualTo("flowId");
        softly.then(logRecord.getAttributes().get(stringKey("taskId"))).isEqualTo("taskId");
        softly.then(logRecord.getAttributes().get(stringKey("executionId"))).isEqualTo("executionId");
        softly.then(logRecord.getAttributes().get(stringKey("taskRunId"))).isEqualTo("taskRunId");
        softly.then(logRecord.getAttributes().get(longKey("attemptNumber"))).isEqualTo(1L);
        softly.then(logRecord.getAttributes().get(stringKey("triggerId"))).isEqualTo("triggerId");
        softly.then(logRecord.getAttributes().get(stringKey("thread"))).isEqualTo("thread");
        softly.then(logRecord.getAttributes().get(stringKey("message"))).isEqualTo("message");
        softly.then(logRecord.getBodyValue().asString()).isEqualTo("2011-12-03T10:15:30.123456789Z INFO message");
    }

    @Test
    public void should_convert_instant_in_nanos(){
        Instant instant = Instant.parse("2011-12-03T10:15:30.123456789Z");
        assertThat(LogRecordMapper.instantInNanos(instant)).isEqualTo(1322907330123456789L);
    }

    @ParameterizedTest
    @MethodSource("logLevelToSeverityProvider")
    public void should_convert_level_to_severity(Level level, Severity expectedSeverity){
        assertThat(LogRecordMapper.convertLogLevelToSeverity(level)).isEqualTo(expectedSeverity);
    }

    static Stream<Arguments> logLevelToSeverityProvider() {
        return Stream.of(
            Arguments.of(Level.TRACE, Severity.TRACE),
            Arguments.of(Level.DEBUG, Severity.DEBUG),
            Arguments.of(Level.INFO, Severity.INFO),
            Arguments.of(Level.WARN, Severity.WARN),
            Arguments.of(Level.ERROR, Severity.ERROR)
        );
    }

}
