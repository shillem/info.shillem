package info.shillem.dao;

import java.util.Map;

import info.shillem.dto.BaseField;

public class IdQuery<E extends Enum<E> & BaseField> extends Query<E> {

    private final String id;

    IdQuery(IdQueryBuilder<E> builder) {
        super(builder.base);

        id = builder.id;
    }

    public String getId() {
        return id;
    }
    
    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> properties = super.toMap();
        
        properties.put("id", id);
        
        return properties;
    }

}
