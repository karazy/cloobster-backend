package net.eatsense.configuration.addon;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Map.Entry;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class AddonConfiguration {
	public final static String KIND = "AddonConfiguration";
	private String name;
	private Map<String, String> configMap = Maps.newHashMap();
	private Key parent;
	private Key key;
	
	/**
	 * Create AddonConfiguration instance from a datastore entity.
	 * Using all
	 * 
	 * @param entity
	 */
	protected AddonConfiguration(Entity entity) {
		if(entity == null || !entity.getKind().equals(KIND))
			return;
		name = entity.getKey().getName();
		parent = entity.getParent();
		
		setValues(entity);		
	}
	
	public String getAddonName() {
		return name;
	}

	/**
	 * NOTE: Recreate Datastore key if name changed!
	 * @param name
	 */
	public void setName(String name) {
		checkArgument(!Strings.isNullOrEmpty(name), "addonName cannot be null or empty");
		if(!Objects.equal(this.name, name)) {
			// Reset Datastore key for the entity because a part of it was changed
			this.key = null;
			this.name = name;
		}
	}

	public Key getParent() {
		return parent;
	}

	/**
	 * NOTE: Recreate Datastore key if parent changed!
	 * @param parent 
	 */
	public void setParent(Key parent) {
		if(!Objects.equal(this.parent, parent)) {
			// Reset Datastore key for the entity because a part of it was changed
			this.key = null;
			this.parent = parent;	
		}
	}

	
	protected AddonConfiguration(String name, Key parent,  Map<String, String> configMap) {
		this.name = name;
		this.configMap = configMap;
		this.parent = parent;
	}
	
	/**
	 * @return 
	 */
	public Key getKey() {
		if(Strings.isNullOrEmpty(name)) {
			// addonName is id for the Datastore.
			// no id, means no key!
			key = null;
			return key;
		}
		// Lazy creation of the Datastore key
		// without parent.
		if(key == null && parent == null) {
			key = KeyFactory.createKey(KIND, name);
			
		}
		// ... with parent.
		if(key == null && parent != null) {
			KeyFactory.createKey(parent, KIND, name);
		}
		
		return key;
	}
	
	/**
	 * @return Datastore {@link Entity} filled with values from the configuration
	 */
	public Entity buildEntity() {
		Entity entity;
		if(parent != null)
			entity = new Entity(KIND, name, parent);
		else
			entity = new Entity(KIND, name);
		
		for (Entry<String, String> entry : configMap.entrySet()) {
			if(entry.getValue() != null) {
				entity.setUnindexedProperty(entry.getKey(), entry.getValue());
			}
		}
		
		return entity;
	}
	
	public void setValues(Entity entity) {
		checkArgument(entity.getKind().equals(KIND), "entity must be of kind: " + KIND);
		
		for (Entry<String, Object> property : entity.getProperties().entrySet()) {
			if(property.getValue() instanceof String) {
				// Only use String values
				configMap.put(property.getKey(), (String)property.getValue());
			}
			
		}
	}
	
	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}
}
