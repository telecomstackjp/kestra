package io.kestra.core.converters;

import com.google.common.annotations.VisibleForTesting;
import io.kestra.core.models.QueryFilter;
import io.kestra.core.models.flows.FlowScope;
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
            if (!key.startsWith("filters[")) return;

            Matcher matcher = FILTER_PATTERN.matcher(key);

            if (matcher.matches()) {
                String fieldStr = matcher.group(1);
                String operationStr = matcher.group(2);

                QueryFilter.Field field = QueryFilter.Field.fromString(fieldStr);
                QueryFilter.Op operation = QueryFilter.Op.fromString(operationStr);

                Object value = switch (field) {
                    case SCOPE -> toFlowScopes(values); // Convert to list FlowScope enum
                    case LABELS -> toMap(values); // Convert labels to a map
                    default -> (operation == QueryFilter.Op.IN || operation == QueryFilter.Op.NOT_IN) ? values
                        : values.size() == 1 ? values.getFirst() : values;
                };


                filters.add(QueryFilter.builder()
                    .field(field)
                    .operation(operation)
                    .value(value)
                    .build());
            }
        });

        return filters;
    }

    private static List<FlowScope> toFlowScopes(List<String> values) {
        return Arrays.stream(values.getFirst().split(","))
            .map(valueStr -> {
                try {
                    return FlowScope.valueOf(valueStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid FlowScope value: " + valueStr, e);
                }
            })
            .toList();
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