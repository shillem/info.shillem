package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

public class FilterQuery<E extends Enum<E> & BaseField> extends Query<E> {

    public static class Builder<E extends Enum<E> & BaseField> extends QueryBuilder<E, Builder<E>> {

        private final Map<E, Object> filters = new LinkedHashMap<>();
        private int maxCount;
        private boolean cached;

        public FilterQuery<E> build() {
            return new FilterQuery<>(this);
        }

        public Builder<E> filter(E field, Object value) {
            filters.put(
                    Objects.requireNonNull(field, "Field cannot be null"),
                    Objects.requireNonNull(value, "Value for " + field + " cannot be null"));

            return this;
        }
        
        public int getMaxCount() {
            return maxCount;
        }

        public boolean isCached() {
            return cached;
        }

        public Builder<E> setCache(boolean flag) {
            this.cached = flag;

            return this;
        }

        public Builder<E> setMaxCount(int maxCount) {
            this.maxCount = maxCount;

            return this;
        }

    }

    private final Map<E, Object> filters;
    private final int maxCount;
    private final boolean cached;

    private FilterQuery(Builder<E> builder) {
        super(builder);

        filters = builder.filters;
        maxCount = builder.getMaxCount();
        cached = builder.isCached();
    }

    public Map<E, Object> getFilters() {
        return filters;
    }

    public Map.Entry<E, Object> getFirstFilter() {
        return filters.entrySet().iterator().next();
    }
    
    public int getMaxCount() {
        return maxCount;
    }

    public boolean isCached() {
        return cached;
    }

    public <T> Stream<T> limitStream(Stream<T> stream) {
        if (getMaxCount() > 0) {
            return stream.limit(getMaxCount());
        }

        return stream;
    }
    
    @Override
    public String toString() {
        return filters.toString();
    }

}
