package info.shillem.domino.util;

public class VwPath {

    public static class Builder {

        private final String alias;
        private final String name;

        private NavType navTypePreference;
        private VwRowAccessPreference rowAccessPreference;
        private VwType type;

        public Builder(String alias, String name) {
            this.alias = alias;
            this.name = name;
            this.navTypePreference = NavType.KEY;
            this.rowAccessPreference = VwRowAccessPreference.DOCUMENT;
            this.type = VwType.BACK_END;
        }

        public VwPath build() {
            return new VwPath(this);
        }

        public Builder setNavigatorTypePreference(NavType value) {
            navTypePreference = value;

            return this;
        }

        public Builder setRowAccessPreference(VwRowAccessPreference value) {
            rowAccessPreference = value;

            return this;
        }

        public Builder setType(VwType value) {
            type = value;

            return this;
        }

    }

    private final String alias;
    private final String name;
    private final NavType navTypePreference;
    private final VwRowAccessPreference rowAccessPreference;
    private final VwType type;

    public VwPath(Builder builder) {
        alias = builder.alias;
        name = builder.name;
        navTypePreference = builder.navTypePreference;
        rowAccessPreference = builder.rowAccessPreference;
        type = builder.type;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    public NavType getNavTypePreference() {
        return navTypePreference;
    }

    public VwRowAccessPreference getRowAccessPreference() {
        return rowAccessPreference;
    }

    public VwType getType() {
        return type;
    }

}