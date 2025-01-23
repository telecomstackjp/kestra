package io.kestra.repository.h2;

import io.kestra.core.models.QueryFilter;
import io.kestra.core.models.flows.Flow;
import io.kestra.jdbc.AbstractJdbcRepository;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.kestra.core.models.QueryFilter.Op.EQUALS;
import static io.kestra.jdbc.repository.AbstractJdbcRepository.field;

public abstract class H2FlowRepositoryService {
    public static Condition findCondition(AbstractJdbcRepository<Flow> jdbcRepository, String query, Map<String, String> labels) {
        List<Condition> conditions = new ArrayList<>();

        if (query != null) {
            conditions.add(jdbcRepository.fullTextCondition(List.of("fulltext"), query));
        }

        if (labels != null) {
            labels.forEach((key, value) -> {
                Field<String> valueField = DSL.field("JQ_STRING(\"value\", '.labels[]? | select(.key == \"" + key + "\") | .value')", String.class);
                if (value == null) {
                    conditions.add(valueField.isNull());
                } else {
                    conditions.add(valueField.eq(value));
                }
            });
        }

        return conditions.isEmpty() ? DSL.trueCondition() : DSL.and(conditions);
    }

    public static Condition findSourceCodeCondition(AbstractJdbcRepository<Flow> jdbcRepository, String query) {
        return jdbcRepository.fullTextCondition(List.of("source_code"), query);
    }

    public static Condition findCondition(Object value, QueryFilter.Op operation) {
        List<Condition> conditions = new ArrayList<>();

        if (value instanceof Map<?, ?> labels) {
            labels.forEach((key, val) -> {
                String sql = "JQ_STRING(\"value\", '.labels[]? | select(.key == \"" + key + "\") | .value')";
                if (operation.equals(EQUALS))
                    conditions.add(DSL.condition(sql));
                else
                    conditions.add(DSL.not(DSL.condition(sql)));

            });
        }
        return conditions.isEmpty() ? DSL.trueCondition() : DSL.and(conditions);
    }
}
