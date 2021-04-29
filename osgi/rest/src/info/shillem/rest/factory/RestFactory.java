package info.shillem.rest.factory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.http.client.methods.RequestBuilder;

public class RestFactory {

    public static class Builder {

        private final Map<String, String> headers;
        private String baseUrl;

        public Builder() {
            headers = new HashMap<>();
        }

        public Builder addHeader(String name, String value) {
            headers.put(name, value);

            return this;
        }

        public RestFactory build() {
            return new RestFactory(this);
        }

        public Builder setBaseUrl(String value) {
            baseUrl = value;

            return this;
        }

    }

    private final Map<String, String> headers;
    private final String baseUrl;

    private RestFactory(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null");

        baseUrl = builder.baseUrl;
        headers = builder.headers;
    }

    public RequestBuilder builder(String method) {
        RequestBuilder builder;

        switch (Objects.requireNonNull(method, "Method cannot be null")) {
        case "GET":
            builder = RequestBuilder.get();
            break;
        case "POST":
            builder = RequestBuilder.post();
            break;
        default:
            throw new IllegalArgumentException(method.concat(" method is invalid"));
        }

        builder.setUri(baseUrl);

        headers.forEach(builder::addHeader);

        return builder;
    }

    public String getBaseUrl() throws SQLException {
        return baseUrl;
    }

}
