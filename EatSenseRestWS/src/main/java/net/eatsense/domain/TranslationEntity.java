package net.eatsense.domain;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class TranslationEntity<T extends GenericEntity<T>> {
	
	@Id
	private String lang;
	
	@Parent
	private Key<GenericEntity<T>> parent;

	public Key<GenericEntity<T>> getParent() {
		return parent;
	}

	public void setParent(Key<GenericEntity<T>> parent) {
		this.parent = parent;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}
