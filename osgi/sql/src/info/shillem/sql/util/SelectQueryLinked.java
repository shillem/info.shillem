package info.shillem.sql.util;

import java.util.Date;
import java.util.Optional;

abstract class SelectQueryLinked {

    private SelectQuery select;

    protected Optional<Schema.Column> findSchemaColumn(String identifier) {
        return getSelect() != null
                ? getSelect().findSchemaColumn(identifier)
                : Optional.empty();
    }

    protected Optional<Schema.Table> findSchemaTable(String identifier) {
        return getSelect() != null
                ? getSelect().findSchemaTable(identifier)
                : Optional.empty();
    }

    protected final SelectQuery getSelect() {
        return select;
    }

    protected final boolean isSelectNested() {
        return getSelect() != null && getSelect().isNested();
    }

    public void link(SelectQuery select) {
        if (this.select != null) {
            throw new IllegalStateException(
                    getClass().getName().concat(" is already linked to ")
                            .concat(select.getClass().getName()));
        }

        this.select = select;
    }

    protected String outputSchemaColumn(Schema.Column col) {
        Schema.Table tab = col.getTable();

        if (col.isNameAs()) {
            Object value = col.getNameAs().getValue();

            if (value == null) {
                return "NULL";
            }

            if (value instanceof String) {
                String val = ((String) value);

                return col.getNameAs().isAsIs()
                        ? val
                        : "'".concat(val.replaceAll("'", "''")).concat("'");
            }

            if (value instanceof Date) {
                return "'".concat(SelectQuery.SHORT_DATE_FORMAT.format((Date) value)).concat("'");
            }

            return String.valueOf(value);
        }

        return (tab.isAliased() ? tab.getAlias() : tab.getName()).concat(".").concat(col.getName());
    }

}
