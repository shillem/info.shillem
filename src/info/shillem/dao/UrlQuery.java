package info.shillem.dao;

import info.shillem.util.StringUtil;

public class UrlQuery extends AbstractQuery {

    public static class Builder extends AbstractQueryBuilder<UrlQuery.Builder> {

        private String url;

        public Builder(String url) {
            if (StringUtil.isEmpty(url)) {
                throw new IllegalArgumentException("The url is invalid");
            }

            this.url = url;
        }

        public UrlQuery build() {
            return new UrlQuery(this);
        }

    }

    private final String url;

    private UrlQuery(Builder builder) {
        super(builder);

        url = builder.url;
    }

    public String getUrl() {
        return url;
    }

}
