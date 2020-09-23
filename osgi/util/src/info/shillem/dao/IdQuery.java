package info.shillem.dao;

import java.util.HashMap;
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
    public String toString() {
        Map<String, Object> properties = new HashMap<>();

        properties.put("id", id);

        return properties.toString();
    }

}
