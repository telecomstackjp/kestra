package io.kestra.core.models.tasks.logs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.models.executions.LogEntry;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import java.time.Instant;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

public class LogRecordTest {

    @Test
    void should_convert_log_record_to_string() throws JsonProcessingException {
        LogRecord logRecord = LogRecord.builder()
            .timestampEpochNanos(1322907330123456789L)
            .observedTimestampEpochNanos(1322907330123456789L)
            .severity(Severity.ERROR)
            .severityText("ERROR")
            .bodyValue(Value.of(
                LogEntry.builder()
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
                    .build()
                    .toLogMap()
                    .entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, entry -> Value.of(entry.getValue())))
            ))
            .build();
        String log = new ObjectMapper().writeValueAsString(logRecord);
        assertThat(log, is("{\"resource\":null,\"instrumentationScopeInfo\":null,\"timestampEpochNanos\":1322907330123456789,\"observedTimestampEpochNanos\":1322907330123456789,\"spanContext\":null,\"severity\":\"ERROR\",\"severityText\":\"ERROR\",\"attributes\":null,\"totalAttributeCount\":0,\"bodyValue\":{\"value\":[{\"key\":\"taskRunId\",\"value\":{\"value\":\"taskRunId\",\"type\":\"STRING\"}},{\"key\":\"attemptNumber\",\"value\":{\"value\":\"1\",\"type\":\"STRING\"}},{\"key\":\"executionId\",\"value\":{\"value\":\"executionId\",\"type\":\"STRING\"}},{\"key\":\"triggerId\",\"value\":{\"value\":\"triggerId\",\"type\":\"STRING\"}},{\"key\":\"tenantId\",\"value\":{\"value\":\"tenantId\",\"type\":\"STRING\"}},{\"key\":\"namespace\",\"value\":{\"value\":\"namespace\",\"type\":\"STRING\"}},{\"key\":\"thread\",\"value\":{\"value\":\"thread\",\"type\":\"STRING\"}},{\"key\":\"message\",\"value\":{\"value\":\"message\",\"type\":\"STRING\"}},{\"key\":\"flowId\",\"value\":{\"value\":\"flowId\",\"type\":\"STRING\"}},{\"key\":\"taskId\",\"value\":{\"value\":\"taskId\",\"type\":\"STRING\"}}],\"type\":\"KEY_VALUE_LIST\"}}"));
    }
}
