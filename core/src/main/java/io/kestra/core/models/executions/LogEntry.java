package io.kestra.core.models.executions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.kestra.core.models.DeletedInterface;
import io.kestra.core.models.TenantInterface;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.models.triggers.AbstractTrigger;
import io.kestra.core.models.triggers.TriggerContext;
import io.micronaut.core.annotation.Nullable;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.slf4j.event.Level;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@Builder(toBuilder = true)
public class LogEntry implements DeletedInterface, TenantInterface {
    @Hidden
    @Pattern(regexp = "^[a-z0-9][a-z0-9_-]*")
    @JsonView(MessageView.class)
    String tenantId;

    @NotNull
    @JsonView(MessageView.class)
    String namespace;

    @NotNull
    @JsonView(MessageView.class)
    String flowId;

    @Nullable
    @JsonView(MessageView.class)
    String taskId;

    @Nullable
    @JsonView(MessageView.class)
    String executionId;

    @Nullable
    @JsonView(MessageView.class)
    String taskRunId;

    @Nullable
    @JsonInclude
    @JsonView(MessageView.class)
    Integer attemptNumber;

    @Nullable
    @JsonView(MessageView.class)
    String triggerId;

    Instant timestamp;

    Level level;

    @JsonView(MessageView.class)
    String thread;

    @JsonView(MessageView.class)
    String message;

    @NotNull
    @Builder.Default
    boolean deleted = false;

    public static List<Level> findLevelsByMin(Level minLevel) {
        if (minLevel == null) {
            return Arrays.asList(Level.values());
        }

        return Arrays.stream(Level.values())
            .filter(level -> level.toInt() >= minLevel.toInt())
            .toList();
    }

    public static LogEntry of(Execution execution) {
        return LogEntry.builder()
            .tenantId(execution.getTenantId())
            .namespace(execution.getNamespace())
            .flowId(execution.getFlowId())
            .executionId(execution.getId())
            .build();
    }

    public static LogEntry of(TaskRun taskRun) {
        return LogEntry.builder()
            .tenantId(taskRun.getTenantId())
            .namespace(taskRun.getNamespace())
            .flowId(taskRun.getFlowId())
            .taskId(taskRun.getTaskId())
            .executionId(taskRun.getExecutionId())
            .taskRunId(taskRun.getId())
            .attemptNumber(taskRun.attemptNumber())
            .build();
    }

    public static LogEntry of(Flow flow, AbstractTrigger abstractTrigger) {
        return LogEntry.builder()
            .tenantId(flow.getTenantId())
            .namespace(flow.getNamespace())
            .flowId(flow.getId())
            .triggerId(abstractTrigger.getId())
            .build();
    }

    public static LogEntry of(TriggerContext triggerContext, AbstractTrigger abstractTrigger) {
        return LogEntry.builder()
            .tenantId(triggerContext.getTenantId())
            .namespace(triggerContext.getNamespace())
            .flowId(triggerContext.getFlowId())
            .triggerId(abstractTrigger.getId())
            .build();
    }

    public static String toPrettyString(LogEntry logEntry) {
        return logEntry.getTimestamp().toString() + " " + logEntry.getLevel() + " " + logEntry.getMessage();
    }

    public Map<String, String> toMap() {
        return Stream
            .of(
                new AbstractMap.SimpleEntry<>("tenantId", this.tenantId),
                new AbstractMap.SimpleEntry<>("namespace", this.namespace),
                new AbstractMap.SimpleEntry<>("flowId", this.flowId),
                new AbstractMap.SimpleEntry<>("taskId", this.taskId),
                new AbstractMap.SimpleEntry<>("executionId", this.executionId),
                new AbstractMap.SimpleEntry<>("taskRunId", this.taskRunId),
                new AbstractMap.SimpleEntry<>("triggerId", this.triggerId)
            )
            .filter(e -> e.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SneakyThrows
    public String toJson(){
        JsonMapper mapper = JsonMapper.builder()
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .serializationInclusion(JsonInclude.Include.ALWAYS)
            .build();
        return mapper.writerWithView(MessageView.class)
            .writeValueAsString(this);
    }

    public static class MessageView{}

}
