package info.shillem.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import info.shillem.dto.BaseField;

public class UrlQuery<E extends Enum<E> & BaseField> extends Query<E> {

    public static class Builder<E extends Enum<E> & BaseField> extends QueryBuilder<E, Builder<E>> {

        private String url;

        public Builder(String url) {
            this.url = Objects.requireNonNull(url, "URL cannot be null");
        }

        public UrlQuery<E> build() {
            return new UrlQuery<>(this);
        }

    }

    private final String url;

    private UrlQuery(Builder<E> builder) {
        super(builder);

        url = builder.url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        Map<String, Object> m = new HashMap<String, Object>();

        m.put("url", url);

        return m.toString();
    }

}
