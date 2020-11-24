package info.shillem.sql.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import info.shillem.dao.PageQuery;
import info.shillem.dao.Query;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;
import info.shillem.sql.factory.SqlFactory;
import info.shillem.util.StringUtil;

public abstract class AbstractSqlDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

	protected final SqlFactory factory;

	protected AbstractSqlDao(SqlFactory factory) {
		this.factory = factory;
	}

	protected String getColumnName(E field) {
		return field.name();
	}

	protected void pullColumn(E field, ResultSet resultSet, T wrapper, Locale locale)
	        throws SQLException {
		Class<? extends Serializable> type = field.getProperties().getType();
		Object value = resultSet.getObject(field.toString());

		wrapper.setValue(field, pullValue(type, value));
	}

	protected void pullRow(ResultSet resultSet, T wrapper, Query<E> query) throws SQLException {
		for (E field : query.getSchema()) {
			pullColumn(field, resultSet, wrapper, query.getLocale());
		}
	}

	private <V> V pullValue(Class<V> type, Object value) {
		if (type.isEnum()) {
			if (value instanceof String) {
				return type.cast(StringUtil.enumFromString(type, (String) value));
			}
		} else if (type == Boolean.class) {
			return type.cast(Boolean.valueOf((String) value));
		} else if (Number.class.isAssignableFrom(type)) {
			if (value == null) {
				return null;
			}

			Number num = (Number) value;

			if (type == Integer.class) {
				return type.cast(num.intValue());
			}

			if (type == Long.class) {
				return type.cast(num.longValue());
			}

			if (type == Double.class) {
				return type.cast(num.doubleValue());
			}

			if (type == BigDecimal.class) {
				return type.cast(new BigDecimal(num.toString()));
			}
		} else if (type == Date.class && value instanceof Timestamp) {
			return type.cast(new Date(((Timestamp) value).getTime()));
		}

		return type.cast(value);
	}

	protected T wrapRow(ResultSet resultSet, Supplier<T> supplier, Query<E> query)
	        throws SQLException {
		T wrapper = supplier.get();

		pullRow(resultSet, wrapper, query);

		return wrapper;
	}

	protected List<T> wrapStatement(String plan, Supplier<T> supplier, PageQuery<E> query) {
		try (
		        Statement statement = factory.getConnection().createStatement(
		                ResultSet.TYPE_SCROLL_INSENSITIVE,
		                ResultSet.CONCUR_READ_ONLY);
		        ResultSet resultSet = statement.executeQuery(plan)) {
			if (query.isUnknownOffset()) {
				resultSet.absolute(-1);

				int lastPageOffset = query.recalculateOffset(resultSet.getRow());

				resultSet.absolute(lastPageOffset);
			} else {
				resultSet.absolute(query.getOffset());
			}

			List<T> wrappers = new ArrayList<>();
			int count = 0;

			while (resultSet.next() && count < query.getLimit()) {
				wrappers.add(wrapRow(resultSet, supplier, query));

				count++;
			}

			return wrappers;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
