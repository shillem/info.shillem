package info.shillem.rs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import info.shillem.dao.Query;
import info.shillem.dao.QueryBuilder;
import info.shillem.dto.BaseField;

public class RequestQuery {

    private String url;
    private Map<String, Object> filters;
    private List<String> options;
    private List<String> schema;

    public Map<String, Object> getFilters() {
        return filters != null ? filters : Collections.emptyMap();
    }

    public List<String> getOptions() {
        return options != null ? options : Collections.emptyList();
    }

    public List<String> getSchema() {
        return schema != null ? schema : Collections.emptyList();
    }

    public String getUrl() {
        return url;
    }

    public <E extends Enum<E> & BaseField> Query<E> toQuery(Function<String, E> fielder) {
        QueryBuilder<E> builder = new QueryBuilder<E>();

        getOptions().forEach(builder::addOption);
        getSchema().stream().map(fielder).forEach(builder::fetch);
        
        if (!getFilters().isEmpty()) {
            getFilters().forEach((key, value) -> builder.filter(fielder.apply(key), value));
        } else if (getUrl() != null) {
            builder.setUrl(getUrl());
        }
        
        return builder.build();
    }

}
