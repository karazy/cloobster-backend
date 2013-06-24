package net.eatsense.configuration.external;

import java.util.Map;

public interface AddonConfigurationService {
	
	public AddonConfiguration get(String addonName);
	
	public AddonConfiguration get(String addonName, com.googlecode.objectify.Key<?> parent);
	
	public void put(AddonConfiguration config);
	
	public void create(String addonName, Map<String, String> configMap);
}
