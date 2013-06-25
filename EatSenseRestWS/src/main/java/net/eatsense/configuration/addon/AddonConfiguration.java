package net.eatsense.configuration.addon;

import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class AddonConfiguration {
	public final static String KIND = "AddonConfiguration";
	private String addonName;
	private Map<String, String> configMap = Maps.newHashMap();
	private Key parent;
	private Key key;
	
	protected AddonConfiguration(String name) {
		this.addonName = name;
	}
	
	public Key getKey() {
		if(Strings.isNullOrEmpty(addonName)) {
			// addonName is id for the datastore.
			// no id, means no key!
			key = null;
			return key;
		}
		if(key == null && parent == null) {
			key = KeyFactory.createKey(KIND, addonName);
			
		}
		if(key == null && parent != null) {
			KeyFactory.createKey(parent, KIND, addonName);
		}
		
		return key;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}
}
