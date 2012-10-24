package net.eatsense.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

public class ConfigurationRepository extends DAOBase {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	static {
		ObjectifyService.register(Configuration.class);
	}
	
	/**
	 * Load configuration with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public Configuration get(String id) throws NotFoundException {
		return ofy().get(Configuration.class, id);
	}

	public Key<Configuration> save(Configuration configuration) {
		return ofy().put(configuration);		
	}
}
