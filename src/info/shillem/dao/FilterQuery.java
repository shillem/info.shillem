package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import info.shillem.dto.BaseField;

public class FilterQuery extends Query {

    public static class Builder extends AbstractQueryBuilder<FilterQuery.Builder> {

        private final Map<BaseField, Object> filters = new LinkedHashMap<>();

        public Builder filter(BaseField field, Object value) {
            Objects.requireNonNull(field, "Field cannot be null");
            Objects.requireNonNull(field, "Value for " + field + " cannot be null");

            filters.put(field, value);

            return this;
        }

        public FilterQuery build() {
            if (filters.isEmpty()) {
                throw new IllegalStateException("Filters cannot be empty");
            }

            return new FilterQuery(this);
        }

    }

    private final Map<BaseField, Object> filters;

    private FilterQuery(Builder builder) {
        super(builder);

        filters = builder.filters;
    }

    public Map<BaseField, Object> getFilters() {
        return filters;
    }

    public Map.Entry<BaseField, Object> getFirstFilter() {
        return filters.entrySet().iterator().next();
    }

}
