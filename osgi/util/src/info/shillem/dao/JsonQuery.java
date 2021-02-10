package info.shillem.dao;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.JsonHandler;
import info.shillem.util.StreamUtil;
import info.shillem.util.StringUtil;

public class JsonQuery<E extends Enum<E> & BaseField> {

    public enum Parameter {
        FETCH, FILTER, LIMIT, OFFSET, SEARCH;

        private final static Set<Parameter> PAGE_PARAMS =
                new HashSet<>(Arrays.asList(FETCH, LIMIT, OFFSET));
        private final static Set<Parameter> SEARCH_PARAMS =
                new HashSet<>(Arrays.asList(FETCH, LIMIT, OFFSET, SEARCH));
    }

    private final Class<E> cls;
    private final JsonHandler handler;

    public JsonQuery(Class<E> cls, JsonHandler handler) {
        this.cls = Objects.requireNonNull(cls, "Class cannot be null");
        this.handler = Objects.requireNonNull(handler, "Handler cannot be null");
    }

    public Query.Clause deserialize(JsonNode node) {
        Entry<String, JsonNode> entry = node.fields().next();

        switch (entry.getKey()) {
        case "$and": {
            Query.Group group = new Query.Group();

            StreamUtil
                    .stream(entry.getValue().iterator())
                    .forEach((e) -> group.and(deserialize(e)));

            return group;
        }
        case "$or": {
            Query.Group group = new Query.Group();

            StreamUtil
                    .stream(entry.getValue().iterator())
                    .forEach((e) -> group.or(deserialize(e)));

            return group;
        }
        default:
            return deserializeNode(entry);
        }
    }

    private Query.Clause deserializeNode(Entry<String, JsonNode> node) {
        E field = Enum.valueOf(cls, node.getKey());
        Class<?> type = field.getProperties().getType();

        if (!node.getValue().isObject()) {
            return new Query.Value<>(field, deserializeValue(node.getValue(), type));
        }

        Entry<String, JsonNode> entry = node.getValue().fields().next();
        ComparisonOperator op = deserializeOperator(entry.getKey());

        if (op == null) {
            throw new UnsupportedOperationException(
                    entry.getKey() + " is not a supported operator");
        }

        if (entry.getValue().isArray()) {
            Set<Object> values = StreamUtil.stream(entry.getValue().iterator())
                    .map((val) -> deserializeValue(val, type))
                    .collect(Collectors.toSet());

            return new Query.Values<>(field, values, op);
        }

        return new Query.Value<>(field, deserializeValue(entry.getValue(), type), op);
    }

    private ComparisonOperator deserializeOperator(String token) {
        switch (token) {
        case "$eq":
            return ComparisonOperator.EQUAL;
        case "$gt":
            return ComparisonOperator.GREATER;
        case "$gte":
            return ComparisonOperator.GREATER_EQUAL;
        case "$in":
            return ComparisonOperator.IN;
        case "$lk":
            return ComparisonOperator.LIKE;
        case "$lt":
            return ComparisonOperator.LOWER;
        case "$lte":
            return ComparisonOperator.LOWER_EQUAL;
        case "$ne":
            return ComparisonOperator.NOT_EQUAL;
        case "$nin":
            return ComparisonOperator.NOT_IN;
        }

        return null;
    }

    private Object deserializeValue(JsonNode node, Class<?> type) {
        if (type.isEnum()) {
            return StringUtil.enumFromString(type, node.asText());
        }

        if (type == Boolean.class) {
            return node.asBoolean();
        }

        if (Number.class.isAssignableFrom(type)) {
            if (type == Integer.class) {
                return node.asInt();
            }

            if (type == Long.class) {
                return node.asLong();
            }

            if (type == Double.class) {
                return node.asDouble();
            }

            if (type == BigDecimal.class) {
                return type.cast(new BigDecimal(node.asText()));
            }
        }

        if (type == Date.class) {
            return ZonedDateTime.parse(node.asText()).toInstant();
        }

        return node.asText();
    }

    public QueryBuilder<E> populate(
            QueryBuilder<E> builder,
            Set<Parameter> parameters,
            Function<Parameter, String> fn) throws JsonProcessingException {
        for (Parameter param : parameters) {
            String value = fn.apply(param);

            if (value == null) {
                continue;
            }

            switch (param) {
            case FETCH:
                Stream.of(((String) value).split(","))
                        .map((name) -> Enum.valueOf(cls, name))
                        .forEach(builder::fetch);

                break;
            case LIMIT:
                builder.setLimit(Integer.valueOf((String) value));

                break;
            case OFFSET:
                builder.setOffset(Integer.valueOf((String) value));

                break;
            case SEARCH:
                builder.andClause(deserialize(handler.deserialize(value)));

                break;
            default:
                throw new UnsupportedOperationException(param + " population is not implemented");
            }
        }

        return builder;
    }

    public static Set<Parameter> getPageParameters() {
        return Parameter.PAGE_PARAMS;
    }

    public static Set<Parameter> getSearchParameters() {
        return Parameter.SEARCH_PARAMS;
    }

}
