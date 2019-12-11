package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class FilterQueryBuilder<E extends Enum<E> & BaseField> {

    final QueryBuilder<E> base;
    final PageQueryBuilder<E> page;
    final Map<E, Object> filters;

    public FilterQueryBuilder() {
        this.base = new QueryBuilder<>();
        this.page = new PageQueryBuilder<>();
        this.filters = new LinkedHashMap<>();
    }

    public FilterQuery<E> build() {
        if (filters.isEmpty()) {
            throw new IllegalStateException("Filter query cannot be empty");
        }

        return new FilterQuery<>(this);
    }

    public FilterQueryBuilder<E> fetch(E field) {
        base.fetch(field);

        return this;
    }

    public FilterQueryBuilder<E> fetch(E[] fields) {
        base.fetch(fields);

        return this;
    }

    public FilterQueryBuilder<E> fetch(Set<E> fields) {
        base.fetch(fields);

        return this;
    }

    public FilterQueryBuilder<E> fetchDatabaseUrl(boolean flag) {
        base.fetchDatabaseUrl(flag);

        return this;
    }

    public FilterQueryBuilder<E> filter(E field, Object value) {
        filters.put(
                Objects.requireNonNull(field, "Field cannot be null"),
                Objects.requireNonNull(value, "Value cannot be null"));

        return this;
    }

    public FilterQueryBuilder<E> setCache(boolean flag) {
        page.setCache(flag);

        return this;
    }

    public FilterQueryBuilder<E> setLocale(Locale locale) {
        base.setLocale(locale);

        return this;
    }

    public FilterQueryBuilder<E> setMaxCount(int maxCount) {
        page.setMaxCount(maxCount);

        return this;
    }

    public FilterQueryBuilder<E> setOffset(int offset) {
        page.setOffset(offset);

        return this;
    }

    public FilterQueryBuilder<E> sort(E field, OrderOperator order) {
        page.sort(field, order);

        return this;
    }

}
