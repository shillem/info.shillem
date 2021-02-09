package info.shillem.dao;

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
    protected Map<String, Object> toMap() {
        Map<String, Object> properties = super.toMap();

        properties.put("url", url);

        return properties;
    }

}
