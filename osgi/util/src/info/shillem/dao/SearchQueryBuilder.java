package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dao.SearchQuery.Group;
import info.shillem.dao.SearchQuery.Logical;
import info.shillem.dao.SearchQuery.Piece;
import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class SearchQueryBuilder<E extends Enum<E> & BaseField> {

    final QueryBuilder<E> base;
    final PageQueryBuilder<E> page;
    final Group group;

    public SearchQueryBuilder() {
        this.base = new QueryBuilder<>();
        this.page = new PageQueryBuilder<>();
        this.group = new Group();
    }

    public SearchQueryBuilder<E> add(Logical logicalPiece, Piece newPiece) {
        group.add(logicalPiece, newPiece);

        return this;
    }
    
    public SearchQueryBuilder<E> addOption(Enum<? extends QueryOption> value) {
        base.addOption(value);

        return this;
    }

    public SearchQueryBuilder<E> addOption(
            Enum<? extends QueryOption> value,
            @SuppressWarnings("unchecked") Enum<? extends QueryOption>... values) {
        base.addOption(value, values);
        
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
        base.fetch(field);

        return this;
    }

    public SearchQueryBuilder<E> fetch(E[] fields) {
        base.fetch(fields);

        return this;
    }

    public SearchQueryBuilder<E> fetch(Set<E> fields) {
        base.fetch(fields);

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

    public SearchQueryBuilder<E> setCache(boolean flag) {
        page.setCache(flag);

        return this;
    }

    public SearchQueryBuilder<E> setLimit(int limit) {
        page.setLimit(limit);

        return this;
    }

    public SearchQueryBuilder<E> setLocale(Locale locale) {
        base.setLocale(locale);

        return this;
    }

    public SearchQueryBuilder<E> setOffset(int offset) {
        page.setOffset(offset);

        return this;
    }

    public SearchQueryBuilder<E> sort(E field, OrderOperator order) {
        page.sort(field, order);

        return this;
    }

}
