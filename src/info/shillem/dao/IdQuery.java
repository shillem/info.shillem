package info.shillem.dao;

import info.shillem.util.StringUtil;

public class IdQuery extends Query {

    public static class Builder extends AbstractQueryBuilder<IdQuery.Builder> {

        private String id;

        public Builder(String id) {
            if (StringUtil.isEmpty(id)) {
                throw new IllegalArgumentException("The id is invalid");
            }

            this.id = id;
        }

        public IdQuery build() {
            return new IdQuery(this);
        }

    }

    private final String id;

    private IdQuery(Builder builder) {
        super(builder);

        id = builder.id;
    }

    public String getId() {
        return id;
    }

}
