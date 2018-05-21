package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public abstract class AbstractQuery {

	protected Set<? extends BaseField> schema;
	protected Locale locale;

	public Set<? extends BaseField> getSchema() {
		return schema;
	}

	public Locale getLocale() {
		return locale;
	}

}
