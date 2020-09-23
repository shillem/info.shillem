package info.shillem.dao;

import java.util.HashMap;
import java.util.Map;

import info.shillem.dto.BaseField;

public class UrlQuery<E extends Enum<E> & BaseField> extends Query<E> {

    private final String url;

    UrlQuery(UrlQueryBuilder<E> builder) {
        super(builder.base);

        url = builder.url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        Map<String, Object> properties = new HashMap<>();

        properties.put("url", url);

        return properties.toString();
    }

}
