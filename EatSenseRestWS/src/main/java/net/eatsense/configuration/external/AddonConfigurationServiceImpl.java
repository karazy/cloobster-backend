package net.eatsense.configuration.external;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Key;

/**
 * Loads and stores configuration Objects for feature configuration
 * 
 * @author Nils Weiher
 *
 */
public class AddonConfigurationServiceImpl implements AddonConfigurationService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public AddonConfiguration get(String addonName) {
		
		return null;
	}

	@Override
	public AddonConfiguration get(String addonName,
			com.googlecode.objectify.Key<?> parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(AddonConfiguration config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void create(String addonName, Map<String, String> configMap) {
		// TODO Auto-generated method stub
		
	}
	
	
}
