package io.kestra.webserver.services;

import io.kestra.core.models.executions.Execution;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.models.flows.State;
import io.kestra.core.queues.QueueFactoryInterface;
import io.kestra.core.queues.QueueInterface;
import io.kestra.core.repositories.FlowRepositoryInterface;
import io.kestra.core.services.ConditionService;
import io.micronaut.http.sse.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Singleton
public class ExecutionStreamingService {
    private final Map<String, Set<FluxSink<Event<Execution>>>> subscribers = new ConcurrentHashMap<>();
    @Named(QueueFactoryInterface.EXECUTION_NAMED)
    private QueueInterface<Execution> executionQueue;
    private final FlowRepositoryInterface flowRepository;
    private volatile boolean isConsumerRunning = false;
    private final Object consumerLock = new Object();
    private final ConditionService conditionService;
    @Inject
    public ExecutionStreamingService(
        QueueInterface<Execution> executionQueue,
        FlowRepositoryInterface flowRepository,
        ConditionService conditionService
    ) {
        this.executionQueue = executionQueue;
        this.flowRepository = flowRepository;
        this.conditionService = conditionService;
        this.startQueueConsumer();
    }

    private void startQueueConsumer() {
        synchronized (consumerLock) {
            if (!isConsumerRunning) {
                isConsumerRunning = true;

                // Single queue consumer
                executionQueue.receive(either -> {
                    if (either.isRight()) {
                        log.error("Unable to deserialize execution: {}", either.getRight().getMessage());
                        return;
                    }

                    Execution execution = either.getLeft();
                    String executionId = execution.getId();

                    // Get all subscribers for this execution
                    Set<FluxSink<Event<Execution>>> executionSubscribers = subscribers.get(executionId);

                    if (executionSubscribers!=null && !executionSubscribers.isEmpty()){
                        executionSubscribers.forEach(sink -> {
                            try {
                                sink.next(Event.of(execution).id("progress"));

                                // Check if execution is complete
                                Flow flow = flowRepository.findByExecutionWithoutAcl(execution);
                                if (isStopFollow(flow, execution)) {
                                    sink.next(Event.of(execution).id("end"));
                                    sink.complete();
                                }
                            } catch (Exception e) {
                                log.error("Error sending execution update", e);
                                sink.error(e);
                            }
                        });
                    }
                });
            }
        }
    }

    public void registerSubscriber(String executionId, FluxSink<Event<Execution>> sink) {

        subscribers.computeIfAbsent(executionId, k -> ConcurrentHashMap.newKeySet())
            .add(sink);
        // Cleanup when subscriber disconnects
        sink.onCancel(() -> {
            Set<FluxSink<Event<Execution>>> executionSubscribers = subscribers.get(executionId);
            if (executionSubscribers != null) {
                executionSubscribers.remove(sink);
                if (executionSubscribers.isEmpty()) {
                    subscribers.remove(executionId);
                }
            }
        });
    }

    private boolean isStopFollow(Flow flow, Execution execution) {
        return conditionService.isTerminatedWithListeners(flow, execution) &&
            execution.getState().getCurrent() != State.Type.PAUSED;
    }
}