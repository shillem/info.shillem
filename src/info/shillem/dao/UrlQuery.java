package info.shillem.dao;

import java.util.Objects;

import info.shillem.dto.BaseField;

public class UrlQuery<E extends Enum<E> & BaseField> extends Query<E> {

    public static class Builder<E extends Enum<E> & BaseField>
            extends AbstractQueryBuilder<E, Builder<E>, UrlQuery<E>> {

        private String url;

        public Builder(String url) {
            this.url = Objects.requireNonNull(url, "The URL cannot be null");
        }

        @Override
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

}
