package info.shillem.dao;

import info.shillem.dto.BaseField;
import info.shillem.util.StringUtil;

public class IdQuery<E extends Enum<E> & BaseField> extends Query<E> {

    public static class Builder<E extends Enum<E> & BaseField>
            extends AbstractQueryBuilder<E, Builder<E>, IdQuery<E>> {

        private String id;

        public Builder(String id) {
            if (StringUtil.isEmpty(id)) {
                throw new IllegalArgumentException("The id is invalid");
            }

            this.id = id;
        }

        @Override
        public IdQuery<E> build() {
            return new IdQuery<>(this);
        }

    }

    private final String id;

    private IdQuery(Builder<E> builder) {
        super(builder);

        id = builder.id;
    }

    public String getId() {
        return id;
    }

}
