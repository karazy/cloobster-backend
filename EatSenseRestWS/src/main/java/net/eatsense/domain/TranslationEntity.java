package net.eatsense.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Id;
import javax.persistence.Transient;

import net.eatsense.domain.embedded.TranslatedField;

import com.google.common.collect.ImmutableMap;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class TranslationEntity {
	
	public TranslationEntity() {
	}
	
	public TranslationEntity(String lang, Key<?> parent) {
		super();
		this.lang = lang;
		this.parent = parent;
	}

	@Id
	private String lang;
	
	@Parent
	private Key<?> parent;
	
	private List<TranslatedField> fields = new ArrayList<TranslatedField>();
	
	@Transient
	private Map<String,String> fieldsMap;
	
	public Key<?> getParent() {
		return parent;
	}

	public void setParent(Key<?> parent) {
		this.parent = parent;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public List<TranslatedField> getFields() {
		return fields;
	}
	
	public Map<String, String> getFieldsMap() {
		if(fieldsMap == null) {
			fieldsMap = new HashMap<String, String>();
			for (TranslatedField field : fields) {
				fieldsMap.put(field.getKey(), field.getValue());
			}
		}
		return fieldsMap;
	}

	public void setFields(List<TranslatedField> fields) {
		this.fields = fields;
		for (TranslatedField field : fields) {
			fieldsMap.put(field.getKey(), field.getValue());
		}
	}

	public Map<String, String> updateFields(Map<String,String> updatedFields) {
		if(fieldsMap == null) {
			fieldsMap = new HashMap<String, String>();
		}
		fields.clear();
		for (Entry<String, String> entry : updatedFields.entrySet()) {			
			fieldsMap.put(entry.getKey(), entry.getValue());
			fields.add(new TranslatedField(entry.getKey(), entry.getValue()));
		}
		
		return fieldsMap;
	}
}
