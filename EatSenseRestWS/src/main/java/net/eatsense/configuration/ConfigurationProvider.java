package net.eatsense.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

public class ConfigurationProvider implements Provider<Configuration> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private ConfigurationRepository repository;

	@Inject
	public ConfigurationProvider(ConfigurationRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Configuration get() {
		Configuration config;
		try {
			config = repository.get("default");
		} catch (NotFoundException e) {
			logger.warn("No config found returning empty default");
			config = repository.createDefault();
		}
		return config;
	}
	
	public Key<WhiteLabelConfiguration> getWhitelabels() {
		Key<WhiteLabelConfiguration> wlc = get().getWhitelabels();
		Configuration cfg = get();
		
		if(wlc == null) {
			wlc = repository.createWhitelabelConfiguration();
			cfg.setWhitelabels(wlc);
			cfg.save();
		} 
		
		return wlc;
	}
}
