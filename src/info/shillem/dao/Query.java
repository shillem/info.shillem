package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public interface Query {

	boolean getCache();

	Locale getLocale();

	Set<? extends BaseField> getSchema();

}