package net.eatsense.persistence;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.DashboardConfiguration;
import net.eatsense.domain.DashboardItem;

public class DashBoarditemRepository extends GenericRepository<DashboardItem> {

	public DashBoarditemRepository() {
		super(DashboardItem.class);
		OfyService.register(DashboardConfiguration.class);
	}

	
	/**
	 * Load dashboard configuration with the default name "dashboard" for the supplied Business.
	 * 
	 * @param locationKey Key of the parent {@link Business}
	 * @return {@link DashboardConfiguration} entity, child of the {@link Business}
	 */
	DashboardConfiguration getConfiguration(Key<Business> locationKey) {
		Key<DashboardConfiguration> key = Key.create(locationKey, DashboardConfiguration.class, "dashboard");
		logger.info("Getting DashboardConfiguration, key={}", key);
		return ofy().get(key);
	}
	
	/**
	 * Save the DashBoardConfiguation entity.
	 * 
	 * @param config the {@link DashboardConfiguration} entity to save.
	 * @return
	 */
	DashboardConfiguration saveOrUpdateConfiguration(DashboardConfiguration config) {
		logger.info("Saving DashboardConfiguration, name={}, location={}", config.getName(), config.getLocation());
		ofy().put(config);
		
		return config;
	}
}
