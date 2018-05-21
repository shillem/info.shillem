package info.shillem.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public abstract class AbstractQueryBuilder<T extends AbstractQueryBuilder<?>>
		implements QueryBuilder<T> {

	private Set<BaseField> schema;
	private Locale locale;
	private boolean cache;

	@Override
	public T add(BaseField... fields) {
		if (schema == null) {
			schema = new HashSet<>();
		}

		for (BaseField field : fields) {
			schema.add(field);
		}

		return autocast();
	}

	@Override
	public T add(Set<? extends BaseField> fields) {
		if (schema == null) {
			schema = new HashSet<>();
		}

		schema.addAll(fields);

		return autocast();
	}

	@SuppressWarnings("unchecked")
	private T autocast() {
		return (T) this;
	}

	@Override
	public boolean getCache() {
		return cache;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public Set<BaseField> getSchema() {
		return schema != null ? schema : Collections.emptySet();
	}

	@Override
	public T setCache(boolean flag) {
		this.cache = flag;

		return autocast();
	}

	@Override
	public T setLocale(Locale locale) {
		this.locale = locale;

		return autocast();
	}

}
