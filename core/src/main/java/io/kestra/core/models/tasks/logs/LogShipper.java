package io.kestra.core.models.tasks.logs;

import io.kestra.core.models.annotations.Plugin;
import reactor.core.publisher.Flux;

@Plugin
public abstract class LogShipper implements io.kestra.core.models.Plugin {

    protected String type;

    public abstract void sendLogs(Flux<LogRecord> logRecord);

}
