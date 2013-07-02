package net.eatsense.configuration.addon;

import java.util.Map;

import com.google.appengine.api.datastore.Key;

public interface AddonConfigurationService {
	
	public AddonConfiguration get(String addonName);
	
	public AddonConfiguration get(String addonName, Key parent);
	
	public Iterable<AddonConfiguration> getAll(Key parent, boolean onlyKeys);
	
	public Key put(AddonConfiguration config);
	
	public AddonConfiguration create(String addonName, Map<String, String> configMap);
}
