package info.shillem.dao;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;
import info.shillem.util.StringUtil;

public class IdentifierQuery extends AbstractQuery {

	public static class Builder {

		private SimpleEntry<? extends BaseField, String> identifier;
		private boolean cache;

		private Set<BaseField> schema;
		private Locale locale;

		public Builder(BaseField field, String value) {
			this.identifier = new SimpleEntry<>(field, value);
			this.cache = true;
		}

		public Builder setCache(boolean cache) {
			this.cache = cache;

			return this;
		}

		public Builder add(BaseField... fields) {
			if (schema == null) {
				schema = new HashSet<>();
			}

			for (BaseField field : fields) {
				schema.add(field);
			}

			return this;
		}

		public Builder addAll(Set<? extends BaseField> fields) {
			if (schema == null) {
				schema = new HashSet<>();
			}

			schema.addAll(fields);

			return this;
		}

		public Builder setLocale(Locale locale) {
			this.locale = locale;

			return this;
		}

		public IdentifierQuery build() {
			return new IdentifierQuery(this);
		}

	}

	private final SimpleEntry<? extends BaseField, String> identifier;
	private final boolean cache;

	private IdentifierQuery(Builder builder) {
		if (builder.identifier.getKey() == null) {
			throw new NullPointerException("The field identifier is invalid");
		}

		if (StringUtil.isEmpty(builder.identifier.getValue())) {
			throw new IllegalStateException("The value for " + builder.identifier.getValue()
					+ " is invalid");
		}

		identifier = builder.identifier;
		cache = builder.cache;

		schema = builder.schema != null ? builder.schema : Collections.<BaseField> emptySet();
		locale = builder.locale;
	}

	public boolean isCached() {
		return cache;
	}

	public SimpleEntry<? extends BaseField, String> getIdentifier() {
		return identifier;
	}

}
