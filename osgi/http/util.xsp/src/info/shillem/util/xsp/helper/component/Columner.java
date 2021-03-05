package info.shillem.util.xsp.helper.component;

import java.io.Serializable;
import java.util.Objects;

import info.shillem.util.OrderOperator;
import info.shillem.util.xsp.dispatcher.Event;
import info.shillem.util.xsp.model.ReadableDataObject;

public class Columner extends EventProviderController {

    public static class Column implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final Columner columner;

        private Column(String name, Columner columner) {
            this.name = name;
            this.columner = columner;
        }

        public String getName() {
            return name;
        }

        public OrderOperator getOrder() {
            return this != columner.column ? OrderOperator.ASCENDING : columner.order;
        }

        public String getRefreshId() {
            return columner.getRefreshId();
        }

        public void toggle() {
            columner.toggleColumn(this);
        }
    }

    public static enum EventId implements Event.Id {
        TOGGLE_COLUMN;
    }

    private static final long serialVersionUID = 1L;

    private final ReadableDataObject<String, Column> columns;
    private final String id;
    private final String refreshId;

    private Column column;
    private OrderOperator order;

    public Columner(String id, String refreshId) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.refreshId = Objects.requireNonNull(refreshId, "Refresh Id cannot be null");
        this.columns = new ReadableDataObject<>(Column.class, (n, cls) -> new Column(n, this));
    }

    public ReadableDataObject<String, Column> getColumn() {
        return columns;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public Column getSelectedColumn() {
        return column;
    }

    public OrderOperator getSelectedOrder() {
        return order;
    }

    public void setColumn(String name, OrderOperator order) {
        this.column = columns.getValue(Objects.requireNonNull(name, "Name cannot be null"));
        this.order = Objects.requireNonNull(order, "Order cannot be null");
    }

    private void toggleColumn(Column column) {
        if (column != this.column) {
            this.column = column;
        } else {
            order = order == OrderOperator.ASCENDING
                    ? OrderOperator.DESCENDING
                    : OrderOperator.ASCENDING;
        }

        fireEvent(new Event(EventId.TOGGLE_COLUMN)
                .setProperty("column", column)
                .setProperty("id", id)
                .setProperty("order", order));
    }

}
