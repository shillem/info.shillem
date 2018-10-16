package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import info.shillem.dto.BaseField;

public class FilterQuery<E extends Enum<E> & BaseField> extends Query<E> {

    public static class Builder<E extends Enum<E> & BaseField>
            extends AbstractQueryBuilder<E, Builder<E>, FilterQuery<E>> {

        private final Map<E, Object> filters = new LinkedHashMap<>();

        public Builder<E> filter(E field, Object value) {
            Objects.requireNonNull(field, "Field cannot be null");
            Objects.requireNonNull(field, "Value for " + field + " cannot be null");

            filters.put(field, value);

            return this;
        }

        @Override
        public FilterQuery<E> build() {
            if (filters.isEmpty()) {
                throw new IllegalStateException("Filters cannot be empty");
            }

            return new FilterQuery<>(this);
        }

    }

    private final Map<E, Object> filters;

    private FilterQuery(Builder<E> builder) {
        super(builder);

        filters = builder.filters;
    }

    public Map<E, Object> getFilters() {
        return filters;
    }

    public Map.Entry<E, Object> getFirstFilter() {
        return filters.entrySet().iterator().next();
    }

}
