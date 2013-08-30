package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.ConfigurationProvider;
import net.eatsense.configuration.WhiteLabelConfiguration;
import net.eatsense.configuration.addon.AddonConfiguration;
import net.eatsense.configuration.addon.AddonConfigurationService;
import net.eatsense.domain.Business;
import net.eatsense.exceptions.ValidationException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

/**
 * Handles everything related to cloobster configuration.
 * 
 * @author Frederik Reifschneider
 *
 */
public class ConfigurationController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final AddonConfigurationService addonService;
	private final ConfigurationProvider cfgProvider;
	private final LocationController locCtrl;
	private final Iterable<AddonConfiguration> whitelabelCfg;
	private final Configuration config;
	
	@Inject
	public ConfigurationController(AddonConfigurationService addonService, ConfigurationProvider cfgProvider, LocationController locCtrl) {
		this.addonService = addonService;
		this.cfgProvider = cfgProvider;
		this.locCtrl = locCtrl;
		
		this.config = cfgProvider.get();
		
		//call get, will create a whitelabel configuration if non exists
		cfgProvider.getWhitelabels();
		
		whitelabelCfg = addonService.getAll(this.config.getWhitelabels().getRaw(), false);
	}
	
	/**
	 * Get a map of all configured whitelabels.
	 * @param wlc
	 * @return
	 */
	public List<Map<String,String>> getAllWhitelabels(Key<WhiteLabelConfiguration> wlc) {
		 Iterable<AddonConfiguration> configs = addonService.getAll(wlc.getRaw(), false);
		 List<Map<String, String>> whitelabels = new ArrayList<Map<String,String>>();
		 
		 while(configs.iterator().hasNext()) {
			 whitelabels.add(configs.iterator().next().getConfigMap());
		 }
		 return whitelabels;
	}
	
	/**
	 * Get a speficif whitelabel.
	 * @param name
	 *  Name of whitelabel
	 * @return
	 */
	public Map<String, String> getWhitelabel(Key<WhiteLabelConfiguration> wlc ,String name) {		
		return addonService.get(name, wlc.getRaw()).getConfigMap();
	}
	

	/**
	 * Save configuration.
	 * @param company
	 * @param name
	 * @param configMap
	 * @return
	 */
	public Map<String, String> saveWhitelabel(Key<WhiteLabelConfiguration> wlc,
			String name, JSONObject configMap) {
		
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String>  _map = null;
		try {
			_map = mapper.readValue(configMap.toString(), HashMap.class);
		} catch (JsonParseException e1) {
			throw new ValidationException("Could not parse json configuration. " + e1.toString());
		} catch (JsonMappingException e1) {
			throw new ValidationException("Could not parse json configuration. " + e1.toString());
		} catch (IOException e1) {
			throw new ValidationException("Could not parse json configuration. " + e1.toString());
		}
		
		AddonConfiguration config = addonService.create(name, wlc.getRaw(), _map);
		addonService.put(config);
		
		return config.getConfigMap();
	}
	
	/**
	 * Delete a whitelabel from {@link WhiteLabelConfiguration}
	 * @param wlc
	 *  Key of configuration
	 * @param name
	 *  Name of whitelabel
	 */
	public void deleteWhitelabel(Key<WhiteLabelConfiguration> wlc, String name) {
		addonService.delete(name, wlc.getRaw());
	}
	
	/**
	 * Get the whitelabel configuration for given Spot. 
	 * @param spotCode
	 *  Spot qr code to get configuration for.
	 * @return
	 *  Matching whitelabel configuration or <code>null</code> if none was found
	 */
	public AddonConfiguration getWhitelabelConfigurationBySpot(String spotCode) {
		checkNotNull(spotCode, "spotCode was null");
		
		Business loc = locCtrl.getLocationBySpotCode(spotCode);
		AddonConfiguration tempCfg;
		
		if(loc == null) {
			throw new net.eatsense.exceptions.NotFoundException("Unknown location for spot="+ spotCode);
		}
		
		Map<String, String> config = addonService.get("whitelabel", loc.getCompany().getRaw()).getConfigMap();
		
		if(config == null) {
			throw new net.eatsense.exceptions.NotFoundException("No whitelabel configuration found.");
		}
		
		String whitelabel = config.get("key");
		
		while(whitelabelCfg.iterator().hasNext()) {
			tempCfg = whitelabelCfg.iterator().next();
			//check if configuration contains a key and value for that key, then check if selected whitelabel matches the config map value
			if(tempCfg.getConfigMap().containsKey("key") && tempCfg.getConfigMap().containsValue(whitelabel) && tempCfg.getConfigMap().get("key").equals(whitelabel)) {
				return tempCfg;
			}
		}
		
		logger.warn("No whitelabel configuration found for " + whitelabel);
		
		return null;
	}

}
