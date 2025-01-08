package io.kestra.core.models.tasks.logs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import org.junit.jupiter.api.Test;

public class LogRecordTest {

    @Test
    void should_convert_log_record_to_string() throws JsonProcessingException {
        LogRecord logRecord = LogRecord.builder()
            .timestampEpochNanos(1322907330123456789L)
            .observedTimestampEpochNanos(1322907330123456789L)
            .severity(Severity.ERROR)
            .severityText("ERROR")
            .bodyValue(Value.of(
                "{\"tenantId\":\"tenantId\",\"namespace\":\"namespace\",\"flowId\":\"flowId1\",\"taskId\":\"taskId\",\"executionId\":\"executionId\",\"taskRunId\":\"taskRunId\",\"attemptNumber\":1,\"triggerId\":\"triggerId\",\"thread\":\"thread\",\"message\":\"message\"}"))
            .build();
        String log = new ObjectMapper().writeValueAsString(logRecord);
        assertThat(log, is("{\"resource\":null,\"instrumentationScopeInfo\":null,\"timestampEpochNanos\":1322907330123456789,\"observedTimestampEpochNanos\":1322907330123456789,\"spanContext\":null,\"severity\":\"ERROR\",\"severityText\":\"ERROR\",\"attributes\":null,\"totalAttributeCount\":0,\"tenantId\":\"tenantId\",\"namespace\":\"namespace\",\"flowId\":\"flowId1\",\"taskId\":\"taskId\",\"executionId\":\"executionId\",\"taskRunId\":\"taskRunId\",\"attemptNumber\":1,\"triggerId\":\"triggerId\",\"thread\":\"thread\",\"message\":\"message\"}"));
    }
}
