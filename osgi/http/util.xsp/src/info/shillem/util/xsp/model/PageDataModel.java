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

		private int offset;
		private List<T> data;

		private Page(int offset, int limit) {
			this.limit = limit;

			this.offset = offset;
			this.data = Collections.emptyList();
		}

		private boolean contains(int index) {
			return index - offset < data.size();
		}

		private T get(int index) {
			return data.get(index - offset);
		}

		public int getLimit() {
			return limit;
		}

		public int getOffset() {
			return offset;
		}

		private boolean mayContain(int index) {
			return index >= offset && index < (offset + limit);
		}

		public void setData(List<T> data) {
			this.data = Objects.requireNonNull(data, "Data cannot be null");
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

	}

	private static final long serialVersionUID = 1L;

	private SConsumer<Page<T>> wrappedData;
	private int pageSize;

	private int index;
	private int limit;
	private Page<T> page;

	public PageDataModel() {
		this(null);
	}

	public PageDataModel(SConsumer<Page<T>> wrappedData) {
		super();

		setWrappedData(wrappedData);

		pageSize = 30;
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
			loadPage(index);
		}

		return limit < 0 ? index : limit;
	}

	public void invalidate() {
		setRowIndex(-1);

		limit = getRowIndex();

		page = null;
	}

	@Override
	public boolean isRowAvailable() {
		if (wrappedData == null || index < 0 || page == null) {
			return false;
		}

		return page.contains(index);
	}

	private void loadPage(int index) {
		if (page == null || !page.mayContain(index)) {
			Page<T> page = new Page<>(index, pageSize);

			wrappedData.accept(page);

			if (page.data.size() < page.getLimit()) {
				limit = page.getOffset() + page.data.size();
			}

			this.page = page;
		}
	}

	public void setPageSize(int size) {
		if (size < 1) {
			throw new IllegalArgumentException();
		}

		page = null;

		pageSize = size;
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
			loadPage(index);
		}

		DataModelListener[] listeners = getDataModelListeners();

		if (listeners == null) {
			return;
		}

		DataModelEvent event = new DataModelEvent(
		        this, index, isRowAvailable() ? getRowData() : null);

		Stream.of(listeners).forEach((listener) -> listener.rowSelected(event));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setWrappedData(Object data) {
		wrappedData = data instanceof Consumer ? (SConsumer<Page<T>>) data : null;

		setRowIndex(-1);

		limit = getRowIndex();

		page = null;
	}

}
