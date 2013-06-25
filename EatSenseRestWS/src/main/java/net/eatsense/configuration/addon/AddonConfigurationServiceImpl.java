package net.eatsense.configuration.addon;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

/**
 * Loads and stores configuration Objects for feature configuration
 * 
 * @author Nils Weiher
 *
 */
public class AddonConfigurationServiceImpl implements AddonConfigurationService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final DatastoreService datastore;
	
	@Inject
	public AddonConfigurationServiceImpl(DatastoreService datastore) {
		this.datastore = datastore;
		
	}
	
	@Override
	public AddonConfiguration get(String addonName) {
		
		return null;
	}

	@Override
	public AddonConfiguration get(String addonName,
			Key parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Key put(AddonConfiguration config) {
		checkNotNull(config, "config cannot be null");
		
		Key key = config.getKey();
		
		
		return null;			
	}

	@Override
	public void create(String addonName, Map<String, String> configMap) {
		
	}
}
