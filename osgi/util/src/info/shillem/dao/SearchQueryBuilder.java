package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dao.SearchQuery.Group;
import info.shillem.dao.SearchQuery.Logical;
import info.shillem.dao.SearchQuery.Piece;
import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class SearchQueryBuilder<E extends Enum<E> & BaseField> {

    final PageQueryBuilder<E> page;
    final Group group;

    public SearchQueryBuilder() {
        page = new PageQueryBuilder<>();
        group = new Group();
    }

    public SearchQueryBuilder<E> add(Logical logicalPiece, Piece newPiece) {
        group.add(logicalPiece, newPiece);

        return this;
    }

    public SearchQueryBuilder<E> addOption(String value) {
        page.addOption(value);

        return this;
    }

    public SearchQueryBuilder<E> addOption(String value, String... values) {
        page.addOption(value, values);

        return this;
    }

    public SearchQueryBuilder<E> and(Piece piece) {
        group.add(Logical.AND, piece);

        return this;
    }

    public SearchQuery<E> build() {
        if (group.isEmpty()) {
            throw new IllegalStateException("Search query cannot be empty");
        }

        return new SearchQuery<>(this);
    }

    public SearchQueryBuilder<E> fetch(E field) {
        page.fetch(field);

        return this;
    }

    public SearchQueryBuilder<E> fetch(E[] fields) {
        page.fetch(fields);

        return this;
    }

    public SearchQueryBuilder<E> fetch(Set<E> fields) {
        page.fetch(fields);

        return this;
    }

    public SearchQueryBuilder<E> fetchTotal(boolean flag) {
        page.fetchTotal(flag);

        return this;
    }

    public SearchQueryBuilder<E> or(Piece piece) {
        group.add(Logical.OR, piece);

        return this;
    }

    public SearchQueryBuilder<E> setCollection(String value) {
        page.setCollection(value);

        return this;
    }

    public SearchQueryBuilder<E> setLimit(int value) {
        page.setLimit(value);

        return this;
    }

    public SearchQueryBuilder<E> setLocale(Locale value) {
        page.setLocale(value);

        return this;
    }

    public SearchQueryBuilder<E> setOffset(int value) {
        page.setOffset(value);

        return this;
    }

    public SearchQueryBuilder<E> sort(E field, OrderOperator order) {
        page.sort(field, order);

        return this;
    }

}
