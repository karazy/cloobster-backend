package net.eatsense.configuration;

import net.eatsense.persistence.OfyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

public class ConfigurationRepository extends DAOBase {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	static {
		OfyService.register(Configuration.class);
		OfyService.register(WhiteLabelConfiguration.class);
	}
	
	/**
	 * Load configuration with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public Configuration get(String id) throws NotFoundException {
		Configuration configuration = ofy().get(Configuration.class, id);
		configuration.setRepository(this);
		return configuration;
	}
	
	public Configuration createDefault() {
		Configuration configuration = new Configuration();
		configuration.setRepository(this);
		return configuration;
	}

	public Key<Configuration> save(Configuration configuration) {
		return ofy().put(configuration);		
	}
	
	public Key<WhiteLabelConfiguration> createWhitelabelConfiguration() {
		WhiteLabelConfiguration wlc = new WhiteLabelConfiguration();
		return ofy().put(wlc);
	}
}
