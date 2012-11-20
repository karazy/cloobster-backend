package net.eatsense.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Transient;

import net.eatsense.domain.embedded.TranslatedField;

import com.google.common.collect.ImmutableMap;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public abstract class TranslatedEntity<T extends GenericEntity<T>> {
	
	public TranslatedEntity() {
	}
	
	public TranslatedEntity(String lang, Key<T> parent) {
		super();
		this.lang = lang;
		this.parent = parent;
	}

	@Id
	private String lang;
	
	@Parent
	private Key<T> parent;
	
	public Key<T> getParent() {
		return parent;
	}

	public void setParent(Key<T> parent) {
		this.parent = parent;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
	public abstract T applyTranslation(T entity);
	public abstract T setFieldsFromEntity(T entity);
}
