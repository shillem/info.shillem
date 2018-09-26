package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public abstract class AbstractQuery implements Query {

	private final Set<? extends BaseField> schema;
	private final Locale locale;
	private final boolean cache;
	
	public AbstractQuery(QueryBuilder<?> builder) {
		schema = builder.getSchema();
		locale = builder.getLocale();
		cache = builder.getCache();
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
	public Set<? extends BaseField> getSchema() {
		return schema;
	}

}
