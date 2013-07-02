package net.eatsense.configuration.addon;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Map.Entry;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class AddonConfiguration {
	public final static String KIND = "AddonConfiguration";
	private String addonName;
	private Map<String, String> configMap = Maps.newHashMap();
	private Key parent;
	
	/**
	 * Create AddonConfiguration instance from a datastore entity
	 * 
	 * @param entity
	 */
	protected AddonConfiguration(Entity entity) {
		if(entity == null || !entity.getKind().equals(KIND))
			return;
		addonName = entity.getKey().getName();
		parent = entity.getParent();
		
		setFrom(entity);		
	}
	
	public String getAddonName() {
		return addonName;
	}

	public void setAddonName(String addonName) {
		this.addonName = addonName;
	}

	public Key getParent() {
		return parent;
	}

	public void setParent(Key parent) {
		this.parent = parent;
	}

	private Key key;
	
	protected AddonConfiguration(String name, Map<String, String> configMap) {
		this.addonName = name;
		this.configMap = configMap;
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
	
	public Entity buildEntity() {
		Entity entity;
		if(parent != null)
			entity = new Entity(KIND, addonName, parent);
		else
			entity = new Entity(KIND, addonName);
		
		for (Entry<String, String> entry : configMap.entrySet()) {
			if(entry.getValue() != null) {
				entity.setUnindexedProperty(entry.getKey(), entry.getValue());
			}
		}
		
		return entity;
	}
	
	public void setFrom(Entity entity) {
		checkArgument(entity.getKind().equals(KIND), "entity must be of kind: " + KIND);
		
		for (Entry<String, Object> property : entity.getProperties().entrySet()) {
			configMap.put(property.getKey(), (String) property.getValue());
		}
	}
	
	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}
}
