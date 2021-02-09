package info.shillem.dao;

import java.util.Map;

import info.shillem.dto.BaseField;

public class FilterQuery<E extends Enum<E> & BaseField> extends PageQuery<E> {

    private final Map<E, Object> filters;

    FilterQuery(FilterQueryBuilder<E> builder) {
        super(builder.page);

        filters = builder.filters;
    }

    public Map<E, Object> getFilters() {
        return filters;
    }

    public Map.Entry<E, Object> getFirstFilter() {
        return filters.entrySet().iterator().next();
    }
    
    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> properties = super.toMap();
        
        properties.put("filters", getFilters());
        
        return properties;
    }

}
