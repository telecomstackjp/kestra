package io.kestra.repository.postgres;

import io.kestra.core.models.QueryFilter;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.models.flows.FlowScope;
import io.kestra.jdbc.AbstractJdbcRepository;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.kestra.core.models.QueryFilter.Op.EQUALS;
import static io.kestra.jdbc.repository.AbstractJdbcRepository.field;
import static io.kestra.jdbc.repository.AbstractJdbcTriggerRepository.NAMESPACE_FIELD;

public abstract class PostgresFlowRepositoryService {
    public static Condition findCondition(AbstractJdbcRepository<Flow> jdbcRepository, String query, Map<String, String> labels) {
        List<Condition> conditions = new ArrayList<>();

        if (query != null) {
            conditions.add(jdbcRepository.fullTextCondition(Collections.singletonList("fulltext"), query));
        }

        if (labels != null) {
            labels.forEach((key, value) -> {
                String sql = "value -> 'labels' @> '[{\"key\":\"" + key + "\", \"value\":\"" + value + "\"}]'";
                conditions.add(DSL.condition(sql));
            });
        }

        return conditions.isEmpty() ? DSL.trueCondition() : DSL.and(conditions);
    }

    public static Condition findSourceCodeCondition(AbstractJdbcRepository<Flow> jdbcRepository, String query) {
        return jdbcRepository.fullTextCondition(Collections.singletonList("FULLTEXT_INDEX(source_code)"), query);
    }

    public static Condition findCondition(AbstractJdbcRepository<Flow> jdbcRepository, List<QueryFilter> filters, String systemFlowNamespace) {
        List<Condition> conditions = new ArrayList<>();

        if (filters != null) {
            for (QueryFilter filter : filters) {
                QueryFilter.Field field = filter.field();
                QueryFilter.Op operation = filter.operation();
                Object value = filter.value();

                switch (field) {
                    case QUERY -> {
                        if (value instanceof String query) {
                            if (operation.equals(EQUALS))
                                conditions.add(jdbcRepository.fullTextCondition(Collections.singletonList("fulltext"), query));
                            else
                                conditions.add(DSL.not(jdbcRepository.fullTextCondition(Collections.singletonList("fulltext"), query)));

                        }
                    }
                    case SCOPE -> {
                        if (value instanceof List<?> scopeValues) {
                            if (scopeValues.contains(FlowScope.USER)) {
                                conditions.add(field("namespace").ne(systemFlowNamespace));
                            }
                            if (scopeValues.contains(FlowScope.SYSTEM)) {
                                conditions.add(field("namespace").eq(systemFlowNamespace));
                            }
                        }
                    }
                    case NAMESPACE -> {
                        if (value instanceof String namespace) {
                            switch (operation) {
                                case EQUALS -> conditions.add(NAMESPACE_FIELD.eq(namespace));
                                case NOT_EQUALS -> conditions.add(NAMESPACE_FIELD.ne(namespace));
                                case CONTAINS -> conditions.add(NAMESPACE_FIELD.like("%" + namespace + "%"));
                                case STARTS_WITH -> conditions.add(NAMESPACE_FIELD.like(namespace + "%"));
                                case ENDS_WITH -> conditions.add(NAMESPACE_FIELD.like("%" + namespace));
                                default ->
                                    throw new UnsupportedOperationException("Unsupported operation '%s' for field 'namespace'.".formatted(operation));
                            }
                        }
                    }
                    case LABELS -> {
                        if (value instanceof Map<?, ?> labels) {
                            labels.forEach((key, val) -> {
                                String sql = "value -> 'labels' @> '[{\"key\":\"" + key + "\", \"value\":\"" + val + "\"}]'";
                                if (operation.equals(EQUALS))
                                    conditions.add(DSL.condition(sql));
                                else
                                    conditions.add(DSL.not(DSL.condition(sql)));

                            });
                        }
                    }
                    default -> throw new UnsupportedOperationException("Unsupported field '%s'.".formatted(field));
                }
            }
        }

        return conditions.isEmpty() ? DSL.trueCondition() : DSL.and(conditions);
    }


}
