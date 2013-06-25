package net.eatsense.configuration.addon;

import java.util.Map;

import com.google.appengine.api.datastore.Key;

public interface AddonConfigurationService {
	
	public AddonConfiguration get(String addonName);
	
	public AddonConfiguration get(String addonName, Key parent);
	
	public Key put(AddonConfiguration config);
	
	public void create(String addonName, Map<String, String> configMap);
}
