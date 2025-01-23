package io.kestra.webserver.controllers.api;

import io.kestra.core.converters.QueryFilterFormat;
import io.kestra.core.models.QueryFilter;
import io.kestra.core.models.executions.LogEntry;
import io.kestra.core.models.executions.TaskRun;
import io.kestra.core.models.flows.State;
import io.kestra.core.repositories.ExecutionRepositoryInterface;
import io.kestra.core.tenant.TenantService;
import io.kestra.webserver.responses.PagedResults;
import io.kestra.webserver.utils.PageableUtils;
import io.kestra.webserver.utils.RequestUtils;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.Format;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;

import java.time.ZonedDateTime;
import java.util.List;

import static io.kestra.core.utils.DateUtils.validateTimeline;

@Controller("/api/v1/taskruns")
@Requires(property = "kestra.repository.type", value = "elasticsearch")
public class TaskRunController {
    @Inject
    protected ExecutionRepositoryInterface executionRepository;

    @Inject
    private TenantService tenantService;

    @ExecuteOn(TaskExecutors.IO)
    @Get(uri = "/search")
    @Operation(tags = {"Executions"}, summary = "Search for taskruns, only available with the Elasticsearch repository")
    public PagedResults<TaskRun> findTaskRun(
        @Parameter(description = "The current page") @QueryValue(defaultValue = "1") @Min(1) int page,
        @Parameter(description = "The current page size") @QueryValue(defaultValue = "10") @Min(1) int size,
        @Parameter(description = "The sort of current page") @Nullable @QueryValue List<String> sort,
        @Parameter(description = "Filters") @QueryFilterFormat List<QueryFilter> filters
    ) throws HttpStatusException {

        return PagedResults.of(executionRepository.findTaskRun(
            PageableUtils.from(page, size, sort),
            tenantService.resolveTenant(),
            filters
        ));
    }
    @ExecuteOn(TaskExecutors.IO)
    @Get(uri = "/maxTaskRunSetting")
    @Hidden
    public Integer maxTaskRunSetting() {
        return executionRepository.maxTaskRunSetting();
    }
}
