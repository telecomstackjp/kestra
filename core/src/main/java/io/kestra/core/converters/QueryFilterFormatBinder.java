package io.kestra.core.converters;

import com.google.common.annotations.VisibleForTesting;
import io.kestra.core.models.QueryFilter;
import io.kestra.core.models.flows.FlowScope;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class QueryFilterFormatBinder implements AnnotatedRequestArgumentBinder<QueryFilterFormat, List<QueryFilter>> {

    private static final Pattern FILTER_PATTERN = Pattern.compile("filters\\[(.*?)\\]\\[(.*?)\\](?:\\[(\\d+)\\])?");

    @VisibleForTesting
    static List<QueryFilter> getQueryFilters(Map<String, List<String>> queryParams) {
        List<QueryFilter> filters = new ArrayList<>();

        queryParams.forEach((key, values) -> {
            if (!key.startsWith("filters[")) return;

            Matcher matcher = FILTER_PATTERN.matcher(key);

            if (matcher.matches()) {
                String fieldStr = matcher.group(1);
                String operationStr = matcher.group(2);
                String nestedKey = matcher.group(3);     // Extract nested key if present

                QueryFilter.Field field = QueryFilter.Field.fromString(fieldStr);
                QueryFilter.Op operation = QueryFilter.Op.fromString(operationStr);

                Object value = nestedKey != null ? Map.of(nestedKey, values.getFirst()) :
                    switch (field) {
                        case SCOPE -> toFlowScopes(values);
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

}