package io.kestra.webserver.utils;

import io.kestra.core.models.QueryFilter;
import lombok.Builder;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
@Builder
public record TimeLineSearchUtils(
    ZonedDateTime startDate,
    ZonedDateTime endDate,
    Duration timeRange
) {
    public static TimeLineSearchUtils extractFrom(List<QueryFilter> filters) {
        ZonedDateTime startDate = null;
        ZonedDateTime endDate = null;
        Duration timeRange = null;

        for (QueryFilter filter : filters) {
            switch (filter.field()) {
                case START_DATE -> startDate = ZonedDateTime.parse(filter.value().toString());
                case END_DATE -> endDate = ZonedDateTime.parse(filter.value().toString());
                case TIME_RANGE -> timeRange = parseDuration(filter.value().toString());
            }
        }

        return new TimeLineSearchUtils(startDate, endDate, timeRange);
    }
    private static Duration parseDuration(String duration) {
        try {
           return Duration.parse(duration);
        } catch (DateTimeParseException e){
            System.out.println("hello");
            throw new IllegalArgumentException("Invalid duration: " + duration);
        }
    }
    public List<QueryFilter> updateFilters(List<QueryFilter> filters, ZonedDateTime resolvedStartDate) {

        return filters.stream()
            .filter(filter -> !isTimeRangeFilter(filter)) // Remove TIME_RANGE filter
            .map(filter -> isStartDateFilter(filter)
                ? createUpdatedStartDateFilter(filter, resolvedStartDate)
                : filter)
            .toList();
    }

    private boolean isStartDateFilter(QueryFilter filter) {
        return filter.field() == QueryFilter.Field.START_DATE;
    }

    private boolean isTimeRangeFilter(QueryFilter filter) {
        return filter.field() == QueryFilter.Field.TIME_RANGE;
    }

    private QueryFilter createUpdatedStartDateFilter(QueryFilter filter, ZonedDateTime resolvedStartDate) {
        return QueryFilter.builder()
            .field(QueryFilter.Field.START_DATE)
            .operation(filter.operation())
            .value(resolvedStartDate.toString())
            .build();
    }

}