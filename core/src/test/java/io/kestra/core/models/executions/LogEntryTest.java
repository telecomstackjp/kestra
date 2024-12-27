package io.kestra.core.models.executions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class LogEntryTest {

    @Test
    public void should_format_to_json_full_log_entry(){
        LogEntry logEntry = LogEntry.builder()
            .tenantId("tenantId")
            .namespace("namespace")
            .flowId("flowId")
            .taskId("taskId")
            .executionId("executionId")
            .taskRunId("taskRunId")
            .attemptNumber(1)
            .triggerId("triggerId")
            .thread("thread")
            .message("message")
            .build();
        assertThat(logEntry.toJson(), is("{\"tenantId\":\"tenantId\",\"namespace\":\"namespace\",\"flowId\":\"flowId\",\"taskId\":\"taskId\",\"executionId\":\"executionId\",\"taskRunId\":\"taskRunId\",\"attemptNumber\":1,\"triggerId\":\"triggerId\",\"thread\":\"thread\",\"message\":\"message\"}"));
    }

    @Test
    public void should_format_to_json_empty_log_entry(){
        LogEntry logEntry = LogEntry.builder().build();
        assertThat(logEntry.toJson(), is("{\"tenantId\":null,\"namespace\":null,\"flowId\":null,\"taskId\":null,\"executionId\":null,\"taskRunId\":null,\"attemptNumber\":null,\"triggerId\":null,\"thread\":null,\"message\":null}"));
    }



}
