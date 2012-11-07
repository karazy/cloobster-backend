package net.eatsense.domain.embedded;

import com.google.common.base.Objects;

public class TranslatedField {
	private String key;
	private String value;

	public TranslatedField() {
	}
	
	public TranslatedField(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("key", key).add("value", value).toString();
	}
}
