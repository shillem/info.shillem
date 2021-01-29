package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class PageQueryBuilder<E extends Enum<E> & BaseField> {

    final QueryBuilder<E> base;
    final Map<E, OrderOperator> sorters;

    int limit;
    int offset;
    boolean total;

    public PageQueryBuilder() {
        this.base = new QueryBuilder<>();
        this.sorters = new LinkedHashMap<>();
    }

    public PageQueryBuilder<E> addOption(String value) {
        base.addOption(value);

        return this;
    }

    public PageQueryBuilder<E> addOption(String value, String... values) {
        base.addOption(value, values);

        return this;
    }

    public PageQuery<E> build() {
        return new PageQuery<>(this);
    }

    public PageQueryBuilder<E> fetch(E field) {
        base.fetch(field);

        return this;
    }

    public PageQueryBuilder<E> fetch(E[] fields) {
        base.fetch(fields);

        return this;
    }

    public PageQueryBuilder<E> fetch(Set<E> fields) {
        base.fetch(fields);

        return this;
    }

    public void fetchTotal(boolean flag) {
        this.total = flag;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public PageQueryBuilder<E> setCollection(String value) {
        base.setCollection(value);

        return this;
    }

    public PageQueryBuilder<E> setLimit(int limit) {
        this.limit = limit;

        return this;
    }

    public PageQueryBuilder<E> setLocale(Locale value) {
        base.setLocale(value);

        return this;
    }

    public PageQueryBuilder<E> setOffset(int offset) {
        this.offset = offset;

        return this;
    }

    public void sort(E field, OrderOperator order) {
        sorters.put(
                Objects.requireNonNull(field, "Field cannot be null"),
                Objects.requireNonNull(order, "Order cannot be null"));
    }

}
