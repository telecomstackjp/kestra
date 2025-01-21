package io.kestra.core.converters;

import com.google.common.annotations.VisibleForTesting;
import io.kestra.core.models.QueryFilter;
import io.micronaut.core.convert.ArgumentConversionContext;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class QueryFilterFormatBinder implements AnnotatedRequestArgumentBinder<QueryFilterFormat, List<QueryFilter>> {

    private static final Pattern FILTER_PATTERN = Pattern.compile("filters\\[(.*?)\\]\\[(.*?)\\](?:\\[(\\d+)\\])?");

    @Override
    public Class<QueryFilterFormat> getAnnotationType() {
        return QueryFilterFormat.class;
    }

    @Override
    public BindingResult<List<QueryFilter>> bind(ArgumentConversionContext<List<QueryFilter>> context,
                                                 HttpRequest<?> source) {
        Map<String, List<String>> queryParams = source.getParameters().asMap();
        List<QueryFilter> filters = getQueryFilters(queryParams);

        return () -> Optional.of(filters);
    }

    @VisibleForTesting
    static List<QueryFilter> getQueryFilters(Map<String, List<String>> queryParams) {
        List<QueryFilter> filters = new ArrayList<>();

        queryParams.forEach((key, values) -> {
            if (key.startsWith("filters[")) {
                Matcher matcher = FILTER_PATTERN.matcher(key);

                if (matcher.matches()) {
                    String field = matcher.group(1);
                    String operationStr = matcher.group(2);
                    QueryFilter.Field queryField = QueryFilter.Field.fromString(field);
                    QueryFilter.Op operation = QueryFilter.Op.fromString(operationStr);

                    // Create a QueryFilter for each value
                    // Group all values for $in and $notIn into a single QueryFilter
                    if (operation == QueryFilter.Op.IN || operation == QueryFilter.Op.NOT_IN) {
                        var criteria = QueryFilter.builder()
                            .field(queryField)
                            .operation(operation)
                            .value(values) // Add all values as a list
                            .build();
                        filters.add(criteria);
                    } else {
                        values.forEach(value -> {
                            var criteria = QueryFilter.builder()
                                .field(queryField)
                                .operation(operation)
                                .value(queryField.equals(QueryFilter.Field.LABELS)? toMap(values): value)
                                .build();
                            filters.add(criteria);
                        });
                    }
                }
            }
        });
        return filters;
    }
    public static Map<String, String> toMap(List<String> queryString) {
        return queryString == null ? null : queryString
                .stream()
                .map(s -> {
                    String[] split = s.split("[: ]+");
                    if (split.length < 2 || split[0] == null || split[0].isEmpty()) {
                        throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid queryString parameter");
                    }

                    return new AbstractMap.SimpleEntry<>(
                            split[0],
                            s.substring(s.indexOf(":") + 1).trim()
                    );
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}