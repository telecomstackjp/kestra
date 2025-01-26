package io.kestra.jdbc.repository;

import io.kestra.core.models.QueryFilter;
import io.kestra.core.models.dashboards.ColumnDescriptor;
import io.kestra.core.models.dashboards.DataFilter;
import io.kestra.core.models.dashboards.Order;
import io.kestra.core.models.flows.State;
import io.kestra.core.repositories.ExecutionRepositoryInterface;
import io.kestra.core.repositories.ExecutionRepositoryInterface.ChildFilter;
import io.kestra.core.utils.DateUtils;
import io.kestra.core.utils.ListUtils;
import io.kestra.jdbc.services.JdbcFilterService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

public abstract class AbstractJdbcRepository {

    protected Condition defaultFilter() {
        return field("deleted", Boolean.class).eq(false);
    }

    protected Condition defaultFilter(Boolean allowDeleted) {
        return allowDeleted ? DSL.trueCondition() : field("deleted", Boolean.class).eq(false);
    }

    protected Condition defaultFilter(String tenantId) {
        return this.defaultFilter(tenantId, false);
    }

    protected Condition defaultFilter(String tenantId, boolean allowDeleted) {
        var tenant = buildTenantCondition(tenantId);
        return allowDeleted ? tenant : tenant.and(field("deleted", Boolean.class).eq(false));
    }

    protected Condition defaultFilterWithNoACL(String tenantId) {
       return defaultFilterWithNoACL(tenantId, false);
    }

    protected Condition defaultFilterWithNoACL(String tenantId, boolean deleted) {
        var tenant = buildTenantCondition(tenantId);
        return deleted ? tenant : tenant.and(field("deleted", Boolean.class).eq(false));
    }

    protected Condition buildTenantCondition(String tenantId) {
        return tenantId == null ? field("tenant_id").isNull() : field("tenant_id").eq(tenantId);
    }

    public static Field<Object> field(String name) {
        return DSL.field(DSL.quotedName(name));
    }

    public static <T> Field<T> field(String name, Class<T> cls) {
        return DSL.field(DSL.quotedName(name), cls);
    }

    protected List<Field<?>> groupByFields(Duration duration) {
        return groupByFields(duration, null, null);
    }

    protected List<Field<?>> groupByFields(Duration duration, boolean withAs) {
        return groupByFields(duration, null, null, withAs);
    }

    protected Field<Integer> weekFromTimestamp(Field<Timestamp> timestampField) {
        return DSL.week(timestampField);
    }

    protected List<Field<?>> groupByFields(Duration duration, @Nullable String dateField, @Nullable DateUtils.GroupType groupBy) {
        return groupByFields(duration, dateField, groupBy, true);
    }

    protected List<Field<?>> groupByFields(Duration duration, @Nullable String dateField, @Nullable DateUtils.GroupType groupBy, boolean withAs) {
        String field = dateField != null ? dateField : "timestamp";
        Field<Integer> month = withAs ? DSL.month(DSL.timestamp(field(field, Date.class))).as("month") : DSL.month(DSL.timestamp(field(field, Date.class)));
        Field<Integer> year = withAs ? DSL.year(DSL.timestamp(field(field, Date.class))).as("year") : DSL.year(DSL.timestamp(field(field, Date.class)));
        Field<Integer> day = withAs ? DSL.day(DSL.timestamp(field(field, Date.class))).as("day") : DSL.day(DSL.timestamp(field(field, Date.class)));
        Field<Integer> week = withAs ? weekFromTimestamp(DSL.timestamp(field(field, Date.class))).as("week") : weekFromTimestamp(DSL.timestamp(field(field, Date.class)));
        Field<Integer> hour = withAs ? DSL.hour(DSL.timestamp(field(field, Date.class))).as("hour") : DSL.hour(DSL.timestamp(field(field, Date.class)));
        Field<Integer> minute = withAs ? DSL.minute(DSL.timestamp(field(field, Date.class))).as("minute") : DSL.minute(DSL.timestamp(field(field, Date.class)));

        if (groupBy == DateUtils.GroupType.MONTH || duration.toDays() > DateUtils.GroupValue.MONTH.getValue()) {
            return List.of(year, month);
        } else if (groupBy == DateUtils.GroupType.WEEK || duration.toDays() > DateUtils.GroupValue.WEEK.getValue()) {
            return List.of(year, week);
        } else if (groupBy == DateUtils.GroupType.DAY || duration.toDays() > DateUtils.GroupValue.DAY.getValue()) {
            return List.of(year, month, day);
        } else if (groupBy == DateUtils.GroupType.HOUR || duration.toHours() > DateUtils.GroupValue.HOUR.getValue()) {
            return List.of(year, month, day, hour);
        } else {
            return List.of(year, month, day, hour, minute);
        }
    }

    protected <F extends Enum<F>> SelectConditionStep<Record> select(
        DSLContext context,
        JdbcFilterService filterService,
        DataFilter<F, ? extends ColumnDescriptor<F>> descriptors,
        Map<F, String> fieldsMapping,
        Table<Record> table,
        String tenantId) {

        return context
            .select(
                descriptors.getColumns().entrySet().stream()
                    .map(entry -> {
                        ColumnDescriptor<F> col = entry.getValue();
                        String key = entry.getKey();
                        Field<?> field = columnToField(col, fieldsMapping);
                        if (col.getAgg() != null) {
                            field = filterService.buildAggregation(field, col.getAgg());
                        }
                        return field.as(key);
                    })
                    .toList()
            )
            .from(table)
            .where(this.defaultFilter(tenantId));
    }

    /**
     * Applies the filters from the provided descriptors to the given select condition step.
     * Used in the fetchData() method
     *
     * @param selectConditionStep the select condition step to which the filters will be applied
     * @param jdbcFilterService the service used to apply the filters
     * @param descriptors the data filter containing the filter conditions
     * @param fieldsMapping a map of field enums to their corresponding database column names
     * @param <F> the type of the fields enum
     * @return the select condition step with the applied filters
     */
    protected <F extends Enum<F>> SelectConditionStep<Record> where(SelectConditionStep<Record> selectConditionStep, JdbcFilterService jdbcFilterService, DataFilter<F, ? extends ColumnDescriptor<F>> descriptors, Map<F, String> fieldsMapping) {
        return jdbcFilterService.addFilters(selectConditionStep, fieldsMapping, descriptors.getWhere());
    }

    /**
     * Groups the results of the given select condition step based on the provided descriptors and field mappings.
     * Used in the fetchData() method
     *
     * @param selectConditionStep the select condition step to which the grouping will be applied
     * @param descriptors the data filter containing the column descriptors for grouping
     * @param fieldsMapping a map of field enums to their corresponding database column names
     * @param <F> the type of the fields enum
     * @return the select having step with the applied grouping
     */
    protected <F extends Enum<F>> SelectHavingStep<Record> groupBy(SelectConditionStep<Record> selectConditionStep, DataFilter<F, ? extends ColumnDescriptor<F>> descriptors, Map<F, String> fieldsMapping) {
        return selectConditionStep.groupBy(
            descriptors.getColumns().values().stream()
                .filter(col -> col.getAgg() == null)
                .map(col -> field(fieldsMapping.get(col.getField())))
                .toList()
        );
    }

    /**
     * Applies ordering to the given select step based on the provided descriptors.
     * Used in the fetchData() method
     *
     * @param selectHavingStep the select step to which the ordering will be applied
     * @param descriptors the data filter containing the order by information
     * @param <F> the type of the fields enum
     * @return the select step with the applied ordering
     */
    protected <F extends Enum<F>> SelectSeekStepN<Record> orderBy(SelectHavingStep<Record> selectHavingStep, DataFilter<F, ? extends ColumnDescriptor<F>> descriptors) {
        List<SortField<?>> orderFields = new ArrayList<>();
        if (!ListUtils.isEmpty(descriptors.getOrderBy())) {
            orderFields = descriptors.getOrderBy().stream()
                .map(orderBy -> {
                    Field<?> field = field(orderBy.getColumn());
                    return orderBy.getOrder() == Order.ASC ? field.asc() : field.desc();
                })
                .toList();

        }

        return selectHavingStep.orderBy(orderFields);
    }

    /**
     * Fetches the results of the given select step and applies pagination if a pageable object is provided.
     * Used in the fetchData() method
     *
     * @param selectSeekStep the select step to fetch the results from
     * @param pageable the pageable object containing the pagination information
     * @return the list of fetched results
     */
    protected List<Map<String, Object>> fetchSeekStep(SelectSeekStepN<Record> selectSeekStep, @Nullable Pageable pageable) {

        return (pageable != null && pageable.getSize() != -1 ?
            selectSeekStep.limit(pageable.getSize()).offset(pageable.getOffset() - pageable.getSize()) :
            selectSeekStep
        ).fetch()
            .intoMaps();
    }

    protected <F extends Enum<F>> Field<?> columnToField(ColumnDescriptor<?> column, Map<F, String> fieldsMapping) {
        return column.getField() != null ? field(fieldsMapping.get(column.getField())) : null;
    }

    protected <T extends Record> SelectConditionStep<T> filter(
        SelectConditionStep<T> select,
        List<QueryFilter> filters
    ) {
        if (filters == null) return select;

        for (QueryFilter filter : filters) {
            QueryFilter.Field field = filter.field();
            QueryFilter.Op operation = filter.operation();
            Object value = filter.value();

            // Handling for Field.STATE
            if (field.equals(QueryFilter.Field.STATE)) {

                select = select.and(generateStateCondition(value, operation));
                continue;
            }
            // Handle Field.CHILD_FILTER
            if (field.equals(QueryFilter.Field.CHILD_FILTER)) {
                select = handleChildFilter(select, value);
                continue;
            }
            // Convert the field name to lowercase and quote it
            Name columnName = DSL.quotedName(field.name().toLowerCase());

            // Default handling for other fields
            switch (operation) {
                case EQUALS -> select = select.and(DSL.field(columnName).eq(value));
                case NOT_EQUALS -> select = select.and(DSL.field(columnName).ne(value));
                case GREATER_THAN -> select = select.and(DSL.field(columnName).greaterThan(value));
                case LESS_THAN -> select = select.and(DSL.field(columnName).lessThan(value));
                case IN -> {
                    if (value instanceof Collection<?>) {
                        select = select.and(DSL.field(columnName).in((Collection<?>) value));
                    } else {
                        throw new IllegalArgumentException("IN operation requires a collection as value");
                    }
                }
                case NOT_IN -> {
                    if (value instanceof Collection<?>) {
                        select = select.and(DSL.field(columnName).notIn((Collection<?>) value));
                    } else {
                        throw new IllegalArgumentException("NOT_IN operation requires a collection as value");
                    }
                }
                case STARTS_WITH -> select = select.and(DSL.field(columnName).like(value + "%"));
                case ENDS_WITH -> select = select.and(DSL.field(columnName).like("%" + value));
                case CONTAINS -> select = select.and(DSL.field(columnName).like("%" + value + "%"));
                case REGEX -> select = select.and(DSL.field(columnName).likeRegex((String) value));
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            }
        }

        return select;
    }

    // Generate the condition for Field.STATE
    private Condition generateStateCondition(Object value, QueryFilter.Op operation) {
        if (value instanceof List<?> list && list.stream().allMatch(item -> item instanceof State.Type)) {
            // List of State.Type values
            List<String> stateNames = list.stream().map(item -> ((State.Type) item).name()).toList();
            return switch (operation) {
                case IN -> DSL.field(DSL.quotedName("state_current")).in(stateNames);
                case NOT_IN -> DSL.field(DSL.quotedName("state_current")).notIn(stateNames);
                default ->
                    throw new IllegalArgumentException("Unsupported operation for list of State.Type: " + operation);
            };
        } else if (value instanceof State.Type singleState) {
            // Single State.Type value
            return switch (operation) {
                case EQUALS -> DSL.field(DSL.quotedName("state_current")).eq(singleState.name());
                case NOT_EQUALS -> DSL.field(DSL.quotedName("state_current")).ne(singleState.name());
                default ->
                    throw new IllegalArgumentException("Unsupported operation for single State.Type: " + operation);
            };

        } else {
            throw new IllegalArgumentException("Field 'state' requires a State.Type or List<State.Type> value");
        }
    }
    // Handle CHILD_FILTER field logic
    private <T extends Record> SelectConditionStep<T> handleChildFilter(SelectConditionStep<T> select, Object value) {
        if (!(value instanceof ChildFilter childFilter)) {
            throw new IllegalArgumentException("Field 'childFilter' requires a ChildFilter value");
        }

        return switch (childFilter) {
            case CHILD -> select.and(DSL.field(DSL.quotedName("trigger_execution_id")).isNotNull());
            case MAIN -> select.and(DSL.field(DSL.quotedName("trigger_execution_id")).isNull());
        };
    }

}
