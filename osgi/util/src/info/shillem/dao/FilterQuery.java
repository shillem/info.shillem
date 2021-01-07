package info.shillem.dao;

import java.util.HashMap;
import java.util.Map;

import info.shillem.dto.BaseField;

public class FilterQuery<E extends Enum<E> & BaseField> extends PageQuery<E> {

    private final Map<E, Object> filters;

    FilterQuery(FilterQueryBuilder<E> builder) {
        super(builder.base, builder.page);

        filters = builder.filters;
    }

    public Map<E, Object> getFilters() {
        return filters;
    }

    public Map.Entry<E, Object> getFirstFilter() {
        return filters.entrySet().iterator().next();
    }

    @Override
    public String toString() {
        Map<String, Object> properties = new HashMap<>();

        properties.put("filters", filters);
        properties.put("limit", getLimit());
        properties.put("offset", getOffset());
        properties.put("sorters", getSorters());

        return properties.toString();
    }

}
