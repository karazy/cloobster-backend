package net.eatsense.configuration.addon;

import java.util.Map;

import net.eatsense.exceptions.NotFoundException;

import com.google.appengine.api.datastore.Key;

public interface AddonConfigurationService {
	
	/**
	 * Retrieve AddonConfiguration by unique name.
	 * @param name
	 * @return {@link AddonConfiguration} with that name.
	 * @throws NotFoundException if no object was saved with that name 
	 */
	public AddonConfiguration get(String name);
	
	/**
	 * Same as {@link #get(String, Key)} but with a parent
	 * 
	 * @param name
	 * @param parent
	 * @return
	 */
	public AddonConfiguration get(String name, Key parent);
	
	public Iterable<AddonConfiguration> getAll(Key parent, boolean keysOnly);
	
	public Key put(AddonConfiguration config);
	
	public AddonConfiguration create(String name, Map<String, String> configMap);
	public AddonConfiguration create(String name, Key parent, Map<String, String> configMap);
}
