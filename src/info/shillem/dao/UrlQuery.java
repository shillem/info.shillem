package info.shillem.dao;

import info.shillem.dto.BaseField;
import info.shillem.util.StringUtil;

public class UrlQuery<E extends Enum<E> & BaseField> extends Query<E> {

    public static class Builder<E extends Enum<E> & BaseField>
            extends AbstractQueryBuilder<E, Builder<E>, UrlQuery<E>> {

        private String url;

        public Builder(String url) {
            if (StringUtil.isEmpty(url)) {
                throw new IllegalArgumentException("The url is invalid");
            }

            this.url = url;
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
