package info.shillem.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import info.shillem.dao.FilterQueryBuilder;
import info.shillem.dao.Query;
import info.shillem.dao.UrlQueryBuilder;
import info.shillem.dto.BaseField;

public class RequestQuery {

    private String url;
    private Map<String, Object> filters;
    private List<String> options;
    private List<String> schema;

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getFilters() {
        return filters != null ? filters : Collections.emptyMap();
    }

    public List<String> getOptions() {
        return options != null ? options : Collections.emptyList();
    }

    public List<String> getSchema() {
        return schema != null ? schema : Collections.emptyList();
    }

    public <E extends Enum<E> & BaseField> Query<E> toQuery(Function<String, E> fielder) {
        if (getUrl() != null) {
            UrlQueryBuilder<E> builder = new UrlQueryBuilder<E>(getUrl());

            getOptions().forEach(builder::addOption);
            getSchema().stream().map(fielder).forEach(builder::fetch);

            return builder.build();
        }

        if (!getFilters().isEmpty()) {
            FilterQueryBuilder<E> builder = new FilterQueryBuilder<>();

            getFilters().forEach((key, value) -> builder.filter(fielder.apply(key), value));
            getOptions().forEach(builder::addOption);
            getSchema().stream().map(fielder).forEach(builder::fetch);

            return builder.build();
        }

        throw new UnsupportedOperationException();
    }

}
