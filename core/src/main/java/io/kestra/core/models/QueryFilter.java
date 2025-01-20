package io.kestra.core.models;

import io.kestra.core.utils.Enums;
import lombok.Builder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public record QueryFilter(
    QueryField field,
    Op operation,
    Object value
) {
    public static final Map<ComponentType, List<FieldOperations>> COMPONENT_TO_FILTERS = Map.of(
        ComponentType.FLOW, List.of(
            FieldOperations.of(QueryField.QUERY, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH, Op.REGEX)),
            FieldOperations.of(QueryField.SCOPE, List.of(Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.NAMESPACE, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH)),
            FieldOperations.of(QueryField.LABELS, List.of(Op.EQUALS, Op.NOT_EQUALS))
        ),
        ComponentType.NAMESPACE, List.of(
            FieldOperations.of(QueryField.EXISTING_ONLY, List.of(Op.EQUALS, Op.NOT_EQUALS))
        ),
        ComponentType.EXECUTION, List.of(
            FieldOperations.of(QueryField.QUERY, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH, Op.REGEX)),
            FieldOperations.of(QueryField.SCOPE, List.of(Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.FLOW_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.IN, Op.NOT_IN)),
            FieldOperations.of(QueryField.START_DATE, List.of(Op.GREATER_THAN, Op.LESS_THAN, Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.END_DATE, List.of(Op.GREATER_THAN, Op.LESS_THAN, Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.DURATION, List.of(Op.GREATER_THAN, Op.LESS_THAN, Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.STATE, List.of(Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.LABELS, List.of(Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.TRIGGER_EXECUTION_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS)),
            FieldOperations.of(QueryField.CHILD_FILTER, List.of(Op.EQUALS, Op.NOT_EQUALS))

        ),
        ComponentType.LOG, List.of(
            FieldOperations.of(QueryField.NAMESPACE, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH)),
            FieldOperations.of(QueryField.START_DATE, List.of(Op.GREATER_THAN, Op.LESS_THAN, Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.END_DATE, List.of(Op.GREATER_THAN, Op.LESS_THAN, Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.FLOW_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.IN, Op.NOT_IN)),
            FieldOperations.of(QueryField.TRIGGER_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.IN, Op.NOT_IN)),
            FieldOperations.of(QueryField.MIN_LEVEL, List.of(Op.EQUALS, Op.NOT_EQUALS))

            ),
        ComponentType.TRIGGER, List.of(
            FieldOperations.of(QueryField.QUERY, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH, Op.REGEX)),
            FieldOperations.of(QueryField.NAMESPACE, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH)),
            FieldOperations.of(QueryField.WORKER_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.IN, Op.NOT_IN)),
            FieldOperations.of(QueryField.FLOW_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.IN, Op.NOT_IN))
            ),
        ComponentType.TASK, List.of(
            FieldOperations.of(QueryField.NAMESPACE, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH)),
            FieldOperations.of(QueryField.QUERY, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH, Op.REGEX)),
            FieldOperations.of(QueryField.END_DATE, List.of(Op.GREATER_THAN, Op.LESS_THAN, Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.FLOW_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.IN, Op.NOT_IN)),
            FieldOperations.of(QueryField.START_DATE, List.of(Op.GREATER_THAN, Op.LESS_THAN, Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.STATE, List.of(Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.LABELS, List.of(Op.EQUALS, Op.NOT_EQUALS)),
            FieldOperations.of(QueryField.TRIGGER_EXECUTION_ID, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS)),
            FieldOperations.of(QueryField.CHILD_FILTER, List.of(Op.EQUALS, Op.NOT_EQUALS))
            ),
        ComponentType.TEMPLATE, List.of(
            FieldOperations.of(QueryField.QUERY, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH, Op.REGEX)),
            FieldOperations.of(QueryField.NAMESPACE, List.of(Op.EQUALS, Op.NOT_EQUALS, Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH))
            )

    );

    public record FieldOperations(QueryField field, List<Op> allowedOperations) {
        public static FieldOperations of(QueryField field, List<Op> allowedOperations) {
            return new FieldOperations(field, allowedOperations);
        }
    }

    public enum Op {
        EQUALS("$eq"),
        NOT_EQUALS("$ne"),
        GREATER_THAN("$gt"),
        LESS_THAN("$lt"),
        IN("$in"),
        NOT_IN("$notIn"),
        STARTS_WITH("$startsWith"),
        ENDS_WITH("$endsWith"),
        CONTAINS("$contains"),
        REGEX("$regex");

        private static final Map<String, Op> BY_VALUE = Arrays.stream(values())
            .collect(Collectors.toMap(Op::value, Function.identity()));

        private final String value;

        Op(String value) {
            this.value = value;
        }

        public static Op fromString(String value) {
            return Enums.fromString(value, BY_VALUE, "operation");
        }

        public String value() {
            return value;
        }
    }

    public enum QueryField {
        QUERY("query"),
        SCOPE("scope"),
        NAMESPACE("namespace"),
        LABELS("labels"),
        FLOW_ID("flowId"),
        START_DATE("startDate"),
        TIME_RANGE("timeRange"),
        END_DATE("endDate"),
        STATE("state"),
        TRIGGER_EXECUTION_ID("triggerExecutionId"),
        TRIGGER_ID("triggerId"),
        CHILD_FILTER("childFilter"),
        WORKER_ID("workerId"),
        EXISTING_ONLY("existingOnly"),
        DURATION("timeRange"),
        MIN_LEVEL("minLevel");

        private static final Map<String, QueryField> BY_VALUE = Arrays.stream(values())
            .collect(Collectors.toMap(QueryField::value, Function.identity()));

        private final String value;

        QueryField(String value) {
            this.value = value;
        }

        public static QueryField fromString(String value) {
            return Enums.fromString(value, BY_VALUE, "field");
        }

        public String value() {
            return value;
        }
    }

    public enum ComponentType {
        FLOW("flow"),
        NAMESPACE("namespace"),
        EXECUTION("execution"),
        LOG("log"),
        TASK("task"),
        TEMPLATE("template"),
        TRIGGER("trigger");

        private static final Map<String, ComponentType> BY_VALUE = Arrays.stream(values())
            .collect(Collectors.toMap(ComponentType::value, Function.identity()));

        private final String value;

        ComponentType(String value) {
            this.value = value;
        }

        public static ComponentType fromString(String value) {
            return Enums.fromString(value, BY_VALUE, "component type");
        }

        public String value() {
            return value;
        }
    }
}
