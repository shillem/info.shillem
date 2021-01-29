package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class FilterQueryBuilder<E extends Enum<E> & BaseField> {

    final PageQueryBuilder<E> page;
    final Map<E, Object> filters;

    public FilterQueryBuilder() {
        this.page = new PageQueryBuilder<>();
        this.filters = new LinkedHashMap<>();
    }

    public FilterQueryBuilder<E> addOption(String value) {
        page.addOption(value);

        return this;
    }

    public FilterQueryBuilder<E> addOption(String value, String... values) {
        page.addOption(value, values);

        return this;
    }

    public FilterQuery<E> build() {
        return new FilterQuery<>(this);
    }

    public FilterQueryBuilder<E> fetch(E field) {
        page.fetch(field);

        return this;
    }

    public FilterQueryBuilder<E> fetch(E[] fields) {
        page.fetch(fields);

        return this;
    }

    public FilterQueryBuilder<E> fetch(Set<E> fields) {
        page.fetch(fields);

        return this;
    }

    public FilterQueryBuilder<E> fetchTotal(boolean flag) {
        page.fetchTotal(flag);

        return this;
    }

    public FilterQueryBuilder<E> filter(E field, Object value) {
        filters.put(
                Objects.requireNonNull(field, "Field cannot be null"),
                Objects.requireNonNull(value, "Value cannot be null"));

        return this;
    }

    public int getLimit() {
        return page.getLimit();
    }

    public int getOffset() {
        return page.getOffset();
    }

    public FilterQueryBuilder<E> setCollection(String value) {
        page.setCollection(value);

        return this;
    }

    public FilterQueryBuilder<E> setLimit(int value) {
        page.setLimit(value);

        return this;
    }

    public FilterQueryBuilder<E> setLocale(Locale value) {
        page.setLocale(value);

        return this;
    }

    public FilterQueryBuilder<E> setOffset(int value) {
        page.setOffset(value);

        return this;
    }

    public FilterQueryBuilder<E> sort(E field, OrderOperator order) {
        page.sort(field, order);

        return this;
    }

}
