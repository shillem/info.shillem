package info.shillem.dao;

import java.util.AbstractMap.SimpleEntry;
import java.util.Objects;

import info.shillem.dto.BaseField;
import info.shillem.util.StringUtil;

public class IdentifierQuery extends AbstractQuery {

    public static class Builder extends AbstractQueryBuilder<IdentifierQuery.Builder> {

        private SimpleEntry<? extends BaseField, String> identifier;

        public Builder(BaseField field, String value) {
            Objects.requireNonNull(field, "The field identifier is invalid");

            if (StringUtil.isEmpty(value)) {
                throw new IllegalArgumentException(
                        "The value for " + identifier.getValue() + " is invalid");
            }

            this.identifier = new SimpleEntry<>(field, value);
        }

        public IdentifierQuery build() {
            return new IdentifierQuery(this);
        }

    }

    private final SimpleEntry<? extends BaseField, String> identifier;

    private IdentifierQuery(Builder builder) {
        super(builder);

        identifier = builder.identifier;
    }

    public SimpleEntry<? extends BaseField, String> getIdentifier() {
        return identifier;
    }

}
