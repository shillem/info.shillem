package info.shillem.util.xsp.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;

import com.ibm.xsp.component.FacesDataIterator;
import com.ibm.xsp.model.TabularDataModel;

import info.shillem.util.xsp.context.SConsumer;

public class PageDataModel<T> extends TabularDataModel implements Serializable {

    public static class Page<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int limit;
        private final int offset;

        private int calculatedOffset;
        private List<T> data;

        private Page(int limit, int offset) {
            this.limit = limit;
            this.offset = offset;

            this.calculatedOffset = offset;
            this.data = Collections.emptyList();
        }

        private boolean contains(int index) {
            return index - calculatedOffset < data.size();
        }

        private T get(int index) {
            return data.get(index - calculatedOffset);
        }

        public int getLimit() {
            return limit;
        }

        public int getOffset() {
            return offset;
        }

        private boolean mayContain(int index) {
            return index >= calculatedOffset && index < (calculatedOffset + limit);
        }

        public void setCalculatedOffset(int value) {
            calculatedOffset = value;
        }

        public void setData(List<T> value) {
            data = Objects.requireNonNull(value, "Data cannot be null");
        }

    }

    private static final long serialVersionUID = 1L;

    private int index;
    private int last;
    private Page<T> page;
    private int size;
    private SConsumer<Page<T>> wrappedData;

    public PageDataModel() {
        this(null);
    }

    public PageDataModel(SConsumer<Page<T>> wrappedData) {
        super();

        size = 30;

        setWrappedData(wrappedData);
    }

    @Override
    public boolean canHaveMoreRows() {
        return true;
    }

    @Override
    public int getRowCount() {
        FacesDataIterator iterator = getDataControl();

        return iterator.getFirst() + iterator.getRows() + 1;
    }

    @Override
    public Object getRowData() {
        if (wrappedData == null) {
            return null;
        }

        if (!isRowAvailable()) {
            throw new IllegalStateException();
        }

        return page.get(index);
    }

    @Override
    public int getRowIndex() {
        return index;
    }

    @Override
    public Object getWrappedData() {
        return wrappedData;
    }

    @Override
    public int hasMoreRows(int index) {
        if (index == Integer.MAX_VALUE) {
            load(index);
        }

        return last < 0 ? index : last;
    }

    public void invalidate() {
        setRowIndex(-1);

        last = getRowIndex();

        page = null;
    }

    @Override
    public boolean isRowAvailable() {
        if (wrappedData == null || index < 0 || page == null) {
            return false;
        }

        return page.contains(index);
    }

    private void load(int index) {
        if (page != null && page.mayContain(index)) {
            return;
        }

        Page<T> page = new Page<>(size, index);

        wrappedData.accept(page);

        if (page.calculatedOffset < page.offset || page.data.size() < page.limit) {
            last = page.calculatedOffset + page.data.size();
        }

        this.page = page;
    }

    @Override
    public void setRowIndex(int index) {
        if (index < -1) {
            throw new IllegalArgumentException();
        }

        if (wrappedData == null || index == this.index) {
            return;
        }

        this.index = index;

        if (index >= 0) {
            load(index);
        }

        DataModelListener[] listeners = getDataModelListeners();

        if (listeners == null) {
            return;
        }

        DataModelEvent event = new DataModelEvent(
                this,
                index,
                isRowAvailable() ? getRowData() : null);

        Stream.of(listeners).forEach((listener) -> listener.rowSelected(event));
    }

    public void setSize(int value) {
        if (value < 1) {
            throw new IllegalArgumentException();
        }

        page = null;
        size = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setWrappedData(Object data) {
        wrappedData = data instanceof Consumer ? (SConsumer<Page<T>>) data : null;

        invalidate();
    }

}
