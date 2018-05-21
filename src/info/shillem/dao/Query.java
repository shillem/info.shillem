package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public interface Query {

	Set<? extends BaseField> getSchema();

	Locale getLocale();

	boolean getCache();

}